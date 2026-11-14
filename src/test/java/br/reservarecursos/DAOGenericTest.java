package br.reservarecursos;

import br.reservarecursos.business.daos.util.Filtros;
import br.reservarecursos.business.daos.util.Ordem;
import br.reservarecursos.entities.Ambiente;
import br.reservarecursos.entities.Grupo;
import br.reservarecursos.entities.Horario;
import br.reservarecursos.entities.Periodo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testes de integração da camada de acesso migrada para JPA Criteria. */
class DAOGenericTest {

    private static SessionFactory sf;
    private static Path dbFile;
    private Session session;
    private Transaction tx;

    @BeforeAll
    static void iniciar() throws Exception {
        dbFile = Files.createTempFile("reservarecursos-dao-", ".db");
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
        tx = session.beginTransaction();
    }

    @AfterEach
    void fechar() {
        if (tx.isActive()) tx.rollback();
        session.close();
    }

    private Grupo grupo(String nome) {
        Grupo g = new Grupo();
        g.setNome(nome);
        g.setLdap(nome);
        g.setAtivo(true);
        new TestSupport.GrupoDao(session).save(g);
        return g;
    }

    private Ambiente ambiente(String nome, Grupo g, boolean ativo) {
        Ambiente a = new Ambiente();
        a.setNome(nome);
        a.setDescricao("descrição");
        a.setAtivo(ativo);
        a.setGerenciavel(false);
        a.setGrupo(g);
        new TestSupport.AmbienteDao(session).save(a);
        return a;
    }

    @Test
    void crudContagemHqlEDelete() {
        Grupo g = grupo("Grupo A");
        TestSupport.AmbienteDao dao = new TestSupport.AmbienteDao(session);
        Ambiente a = ambiente("Lab 01", g, true);

        assertNotNull(g.getId(), "persist() deve atribuir o id ao próprio objeto");
        assertNotNull(a.getId());
        assertEquals(1, dao.size());
        assertEquals("Lab 01", dao.findById(a.getId()).getNome());
        assertEquals(1, dao.listAll().size());
        assertEquals(1, dao.findByHQL("from Ambiente order by nome").size());
        assertEquals("Lab 01", dao.findByHQLUniqueResult("from Ambiente").getNome());

        session.flush();
        dao.delete(a.getId());
        // delete por HQL em massa não remove a entidade do cache de 1º nível.
        session.clear();
        assertEquals(0, dao.size());
        assertNull(dao.findById(a.getId()));
    }

    @Test
    void filtrosOrdemEPathAninhado() {
        Grupo g = grupo("Grupo B");
        ambiente("Beta", g, true);
        ambiente("Alpha", g, true);
        ambiente("Zeta", g, false);
        session.flush();

        TestSupport.AmbienteDao dao = new TestSupport.AmbienteDao(session);

        List<Ambiente> ativos = dao.findByCriteria(Ordem.asc("nome"), Filtros.eq("ativo", Boolean.TRUE));
        assertEquals(List.of("Alpha", "Beta"), ativos.stream().map(Ambiente::getNome).toList());

        List<Ambiente> desc = dao.findByCriteria(Ordem.desc("nome"));
        assertEquals("Zeta", desc.get(0).getNome());

        // "grupo.id" exercita a navegação por ponto / join implícito usada nos Business.
        assertEquals(3, dao.findByCriteriaReturnList(Filtros.eq("grupo.id", g.getId())).size());
        assertEquals("Beta",
                dao.findByCriteriaReturnUniqueResult(Filtros.eq("nome", "Beta")).getNome());

        assertEquals(1, dao.findByCriteriaReturnList(
                Filtros.and(Filtros.eq("ativo", true), Filtros.not(Filtros.eq("nome", "Alpha")))).size());
        assertEquals(3, dao.findByCriteriaReturnList(Filtros.isNotNull("descricao")).size());
        assertEquals(0, dao.findByCriteriaReturnList(Filtros.isNull("descricao")).size());

        assertEquals(2, dao.findByCriteria(java.util.List.of(Ordem.asc("nome")), java.util.List.of(), 2).size());
        assertEquals(2, dao.findByCriteria(Ordem.asc("nome"), 0, 2).size());
        assertEquals(2, dao.findByHQL(Ordem.desc("nome"), 0, 2).size());
    }

    @Test
    void convertersJodaFazemRoundTrip() {
        Grupo g = grupo("Grupo C");
        TestSupport.PeriodoDao pd = new TestSupport.PeriodoDao(session);
        Periodo p = new Periodo();
        p.setDataInicial(new LocalDate(2026, 1, 1));
        p.setDataFinal(new LocalDate(2026, 12, 30));
        p.setObs("2026");
        p.setGrupo(g);
        pd.save(p);

        TestSupport.HorarioDao hd = new TestSupport.HorarioDao(session);
        Horario h = new Horario();
        h.setHoraInicio(new LocalTime(7, 30));
        h.setHoraFim(new LocalTime(8, 30));
        h.setPeriodo(p);
        hd.save(h);

        session.flush();
        session.clear(); // força leitura real do SQLite

        assertEquals(new LocalDate(2026, 1, 1), pd.findById(p.getId()).getDataInicial());
        assertEquals(new LocalDate(2026, 12, 30), pd.findById(p.getId()).getDataFinal());
        assertEquals(new LocalTime(7, 30), hd.findById(h.getId()).getHoraInicio());
        assertEquals(new LocalTime(8, 30), hd.findById(h.getId()).getHoraFim());
    }
}
