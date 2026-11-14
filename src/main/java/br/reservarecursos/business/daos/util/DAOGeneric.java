package br.reservarecursos.business.daos.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.ParameterizedType;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO genérico migrado para JPA Criteria (Hibernate 7).
 * O antigo org.hibernate.criterion.Criterion/Order/Projections foi removido no
 * Hibernate 6; os filtros e ordenações agora são os tipos Filtro/Ordem.
 */
@SuppressWarnings("unchecked")
public class DAOGeneric<T> {

    @Autowired
    private SessionFactory sessionFactory;
    private final Class<T> persistentClass;

    public DAOGeneric() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void setSessionFactory(SessionFactory sf) {
        this.sessionFactory = sf;
    }

    public org.hibernate.Session session() {
        return sessionFactory.getCurrentSession();
    }

    public Class<T> clazz() {
        return this.persistentClass;
    }

    public void delete(T entity) {
        session().remove(session().contains(entity) ? entity : session().merge(entity));
    }

    public void delete(Long id) {
        session().createMutationQuery("delete from " + clazz().getSimpleName() + " where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    public T findById(Long id) {
        return session().find(clazz(), id);
    }

    public List<T> listAll() {
        return findByHQL("from " + clazz().getSimpleName());
    }

    public int size() {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(clazz())));
        return session().createQuery(cq).getSingleResult().intValue();
    }

    public T save(T entity) {
        // Equivale ao antigo saveOrUpdate: para entidade nova usa persist(), que
        // atribui o id gerado ao PRÓPRIO objeto (merge() devolve outra instância e
        // deixaria referências @ManyToOne apontando para um objeto transitório).
        Object id = session().getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
        if (id == null) {
            session().persist(entity);
        } else {
            session().merge(entity);
        }
        return entity;
    }

    public List<T> findByCriteriaReturnList(Filtro... filtros) {
        return findByCriteria(null, filtros);
    }

    public T findByCriteriaReturnUniqueResult(Filtro... filtros) {
        return firstOrNull(findByCriteria(null, filtros));
    }

    public List<T> findByCriteria(Ordem ordem, Filtro... filtros) {
        return executar(ordem == null ? null : java.util.Collections.singletonList(ordem),
                java.util.Arrays.asList(filtros), -1, -1);
    }

    public List<T> findByCriteria(List<Ordem> ordens, List<Filtro> filtros) {
        return executar(ordens, filtros, -1, -1);
    }

    public List<T> findByCriteria(List<Ordem> ordens, List<Filtro> filtros, int maxResult) {
        return executar(ordens, filtros, -1, maxResult);
    }

    public T findByCriteriaReturnUniqueResult(Ordem ordem, Filtro... filtros) {
        return firstOrNull(findByCriteria(ordem, filtros));
    }

    public T findByCriteriaReturnUniqueResult(Ordem ordem, int offSet, int size, Filtro... filtros) {
        return firstOrNull(executar(java.util.Collections.singletonList(ordem),
                java.util.Arrays.asList(filtros), offSet, size));
    }

    public List<T> findByCriteria(Ordem ordem) {
        return findByCriteria(ordem, new Filtro[0]);
    }

    public List<T> findByCriteria(Ordem ordem, int offSet, int size) {
        return executar(java.util.Collections.singletonList(ordem),
                java.util.Collections.<Filtro>emptyList(), offSet, size);
    }

    public List<T> findByHQL(Ordem ordem, int offSet, int size) {
        return session().createQuery("from " + clazz().getSimpleName()
                        + " order by " + ordem.getPropertyName(), clazz())
                .setFirstResult(offSet)
                .setMaxResults(size)
                .getResultList();
    }

    public List<T> findByHQL(String hql) {
        return session().createQuery(hql, clazz()).getResultList();
    }

    public T findByHQLUniqueResult(String hql) {
        return firstOrNull(session().createQuery(hql, clazz()).getResultList());
    }

    public void executeSQL(String query) {
        session().doWork(connection -> {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate(query);
            }
        });
    }

    public List<T> executeSQL_List(String sql) {
        return session().createNativeQuery(sql).getResultList();
    }

    private List<T> executar(List<Ordem> ordens, List<Filtro> filtros, int offSet, int maxResult) {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clazz());
        Root<T> root = cq.from(clazz());
        cq.select(root).distinct(true);

        if (filtros != null && !filtros.isEmpty()) {
            List<Predicate> predicados = new ArrayList<>();
            for (Filtro f : filtros) {
                predicados.add(f.toPredicate(cb, root));
            }
            cq.where(predicados.toArray(new Predicate[0]));
        }
        if (ordens != null && !ordens.isEmpty()) {
            List<jakarta.persistence.criteria.Order> jpaOrders = new ArrayList<>();
            for (Ordem o : ordens) {
                jpaOrders.add(o.toOrder(cb, root));
            }
            cq.orderBy(jpaOrders);
        }

        org.hibernate.query.Query<T> query = session().createQuery(cq);
        if (offSet >= 0) {
            query.setFirstResult(offSet);
        }
        if (maxResult >= 0) {
            query.setMaxResults(maxResult);
        }
        return query.getResultList();
    }

    private T firstOrNull(List<T> lista) {
        return lista.isEmpty() ? null : lista.get(0);
    }
}
