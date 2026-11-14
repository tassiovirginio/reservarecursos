package br.reservarecursos.business.daos.util;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.io.Serializable;

/**
 * Fábricas equivalentes ao antigo org.hibernate.criterion.Restrictions.
 * Navegação por ponto ("grupo.id", "periodo.grupo") é resolvida como path
 * JPA (join implícito para associações ManyToOne).
 */
public final class Filtros implements Serializable {

    private Filtros() {
    }

    public static Path<?> path(Root<?> root, String propriedade) {
        Path<?> p = root;
        for (String parte : propriedade.split("\\.")) {
            p = p.get(parte);
        }
        return p;
    }

    public static Filtro eq(String propriedade, Object valor) {
        // cb.equal(path, null) gera "coluna = ?" com parâmetro nulo no Hibernate,
        // que nunca casa (e, dentro de not(), vira "coluna <> ?"). O antigo
        // Restrictions.eq(..., null) do Hibernate 4 gerava "is null". Preservamos
        // essa semântica: é usado em PeriodoBusiness.save com id de entidade nova.
        if (valor == null) {
            return (cb, root) -> cb.isNull(path(root, propriedade));
        }
        return (cb, root) -> cb.equal(path(root, propriedade), valor);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Filtro le(String propriedade, Object valor) {
        return (cb, root) -> cb.lessThanOrEqualTo((Path<Comparable>) (Path) path(root, propriedade), (Comparable) valor);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Filtro ge(String propriedade, Object valor) {
        return (cb, root) -> cb.greaterThanOrEqualTo((Path<Comparable>) (Path) path(root, propriedade), (Comparable) valor);
    }

    public static Filtro isNull(String propriedade) {
        return (cb, root) -> cb.isNull(path(root, propriedade));
    }

    public static Filtro isNotNull(String propriedade) {
        return (cb, root) -> cb.isNotNull(path(root, propriedade));
    }

    public static Filtro and(Filtro... filtros) {
        return (cb, root) -> {
            Predicate[] predicados = new Predicate[filtros.length];
            for (int i = 0; i < filtros.length; i++) {
                predicados[i] = filtros[i].toPredicate(cb, root);
            }
            return cb.and(predicados);
        };
    }

    public static Filtro not(Filtro filtro) {
        return (cb, root) -> cb.not(filtro.toPredicate(cb, root));
    }
}
