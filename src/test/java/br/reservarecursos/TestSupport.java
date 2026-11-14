package br.reservarecursos;

import br.reservarecursos.business.daos.util.DAOGeneric;
import br.reservarecursos.entities.Ambiente;
import br.reservarecursos.entities.Emprestimo;
import br.reservarecursos.entities.Grupo;
import br.reservarecursos.entities.Horario;
import br.reservarecursos.entities.Periodo;
import br.reservarecursos.entities.Recurso;
import br.reservarecursos.entities.Reserva;
import br.reservarecursos.entities.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.context.internal.ThreadLocalSessionContext;

/**
 * Infraestrutura dos testes de integração: SessionFactory Hibernate real sobre
 * um arquivo SQLite temporário, sem Spring e sem servidor. Exercita de verdade
 * o JPA Criteria, o dialeto SQLite e os AttributeConverters de Joda-Time.
 */
public final class TestSupport {

    private TestSupport() {
    }

    public static SessionFactory build(String dbFile) {
        Configuration cfg = new Configuration();
        cfg.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC");
        cfg.setProperty("hibernate.connection.url", "jdbc:sqlite:" + dbFile);
        cfg.setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        cfg.setProperty("hibernate.hbm2ddl.auto", "create");
        cfg.setProperty("hibernate.show_sql", "false");
        cfg.setProperty("hibernate.connection.autocommit", "false");
        // Permite que os DAOs reais usem getCurrentSession() nos testes.
        cfg.setProperty("hibernate.current_session_context_class", "thread");
        for (Class<?> entity : new Class<?>[]{
                Ambiente.class, Emprestimo.class, Grupo.class, Horario.class,
                Periodo.class, Recurso.class, Reserva.class, Usuario.class}) {
            cfg.addAnnotatedClass(entity);
        }
        return cfg.buildSessionFactory();
    }

    public static void bind(Session session) {
        ThreadLocalSessionContext.bind(session);
    }

    public static void unbind(SessionFactory sf) {
        ThreadLocalSessionContext.unbind(sf);
    }

    /**
     * DAOGeneric.session() usa getCurrentSession(); nos testes injetamos uma
     * Session única. DAOGeneric infere o tipo pelo getGenericSuperclass, então
     * cada DAO precisa ser uma classe concreta parametrizada.
     */
    public abstract static class Dao<T> extends DAOGeneric<T> {
        private final Session session;

        protected Dao(Session session) {
            this.session = session;
        }

        @Override
        public Session session() {
            return session;
        }
    }

    public static final class GrupoDao extends Dao<Grupo> {
        public GrupoDao(Session s) { super(s); }
    }

    public static final class AmbienteDao extends Dao<Ambiente> {
        public AmbienteDao(Session s) { super(s); }
    }

    public static final class PeriodoDao extends Dao<Periodo> {
        public PeriodoDao(Session s) { super(s); }
    }

    public static final class HorarioDao extends Dao<Horario> {
        public HorarioDao(Session s) { super(s); }
    }

    public static final class UsuarioDao extends Dao<Usuario> {
        public UsuarioDao(Session s) { super(s); }
    }
}
