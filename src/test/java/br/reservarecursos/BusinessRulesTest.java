package br.reservarecursos;

import br.reservarecursos.business.PeriodoBusiness;
import br.reservarecursos.business.UsuarioBusiness;
import br.reservarecursos.business.daos.GrupoDAO;
import br.reservarecursos.business.daos.PeriodoDAO;
import br.reservarecursos.business.daos.UsuarioDAO;
import br.reservarecursos.entities.Grupo;
import br.reservarecursos.entities.Periodo;
import br.reservarecursos.entities.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Regras de negócio com DAOs reais (sem Spring, sem servidor). */
class BusinessRulesTest {

    private static SessionFactory sf;
    private static Path dbFile;
    private Session session;
    private Transaction tx;

    @BeforeAll
    static void iniciar() throws Exception {
        dbFile = Files.createTempFile("reservarecursos-business-", ".db");
        sf = TestSupport.build(dbFile.toString());
    }

    @AfterAll
    static void encerrar() throws Exception {
        if (sf != null) sf.close();
        if (dbFile != null) Files.deleteIfExists(dbFile);
    }

    @BeforeEach
    void abrir() {
        session = sf.openSession();
        TestSupport.bind(session);
        tx = session.beginTransaction();
    }

    @AfterEach
    void fechar() {
        if (tx.isActive()) tx.rollback();
        TestSupport.unbind(sf);
        session.close();
    }

    private static void injetar(Object alvo, String campo, Object valor) throws Exception {
        Class<?> c = alvo.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(campo);
                f.setAccessible(true);
                f.set(alvo, valor);
                return;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(campo);
    }

    private GrupoDAO grupoDao() throws Exception {
        GrupoDAO d = new GrupoDAO();
        injetar(d, "sessionFactory", sf);
        return d;
    }

    private PeriodoDAO periodoDao() throws Exception {
        PeriodoDAO d = new PeriodoDAO();
        injetar(d, "sessionFactory", sf);
        return d;
    }

    private UsuarioDAO usuarioDao() throws Exception {
        UsuarioDAO d = new UsuarioDAO();
        injetar(d, "sessionFactory", sf);
        return d;
    }

    private static Grupo novoGrupo() {
        Grupo g = new Grupo();
        g.setNome("Grupo");
        g.setLdap("grupo");
        g.setAtivo(true);
        return g;
    }

    private static Periodo novoPeriodo(Grupo g, LocalDate inicio, LocalDate fim) {
        Periodo p = new Periodo();
        p.setDataInicial(inicio);
        p.setDataFinal(fim);
        p.setObs("periodo");
        p.setGrupo(g);
        return p;
    }

    private static String md5(String s) throws Exception {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(s.getBytes(), 0, s.length());
        return (new BigInteger(1, m.digest()).toString(16)).toString();
    }

    @Test
    void periodoComInterseccaoEhRejeitado() throws Exception {
        GrupoDAO gd = grupoDao();
        PeriodoDAO pd = periodoDao();
        Grupo g = novoGrupo();
        gd.save(g);
        pd.save(novoPeriodo(g, new LocalDate(2026, 1, 1), new LocalDate(2026, 12, 30)));
        session.flush();

        PeriodoBusiness business = new PeriodoBusiness();
        injetar(business, "dao", pd);

        Periodo sobreposto = novoPeriodo(g, new LocalDate(2026, 6, 1), new LocalDate(2026, 7, 1));
        assertThrows(RuntimeException.class, () -> business.save(sobreposto));
    }

    @Test
    void periodoSemInterseccaoEhSalvo() throws Exception {
        GrupoDAO gd = grupoDao();
        PeriodoDAO pd = periodoDao();
        Grupo g = novoGrupo();
        gd.save(g);
        pd.save(novoPeriodo(g, new LocalDate(2026, 1, 1), new LocalDate(2026, 12, 30)));
        session.flush();

        PeriodoBusiness business = new PeriodoBusiness();
        injetar(business, "dao", pd);

        Periodo proximo = novoPeriodo(g, new LocalDate(2027, 1, 1), new LocalDate(2027, 12, 30));
        business.save(proximo);
        assertNotNull(proximo.getId());
        assertEquals(2, pd.size());
    }

    @Test
    void loginValidaSenhaPorMd5() throws Exception {
        UsuarioDAO ud = usuarioDao();
        Usuario u = new Usuario();
        u.setLogin("admin");
        u.setNome("Administrador");
        u.setSenha(md5("admin"));
        ud.save(u);
        session.flush();

        UsuarioBusiness business = new UsuarioBusiness();
        injetar(business, "dao", ud);
        injetar(business, "ldapLigado", Boolean.FALSE);

        assertNotNull(business.verificarLogin("admin", "admin"));
        assertNull(business.verificarLogin("admin", "senha-errada"));
        assertNull(business.verificarLogin("inexistente", "admin"));
    }
}
