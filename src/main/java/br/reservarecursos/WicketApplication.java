/**
 * Copyright 2015 - Tássio Guerreiro Antunes Virgínio
 *
 * Este arquivo é parte do programa  Reserva de Recursos
 *
 * O Reserva de Recursos é um software livre; você pode redistribui-lo e/ou modifica-lo
 * dentro dos termos da Licença Pública Geral GNU como publicada pela
 * Fundação do Software Livre (FSF); na versão 2 da Licença.
 *
 * Este programa é distribuido na esperança que possa ser util, mas SEM
 * NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO a qualquer
 * MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU
 * para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU, sob o
 * título "licensa_uso.htm", junto com este programa, se não, escreva para a
 * Fundação do Software Livre(FSF) Inc., 51 Franklin St, Fifth Floor,
 */

package br.reservarecursos;

import br.reservarecursos.business.*;
import br.reservarecursos.entities.*;
import br.reservarecursos.ldap.PersonRepo;
import br.reservarecursos.pages.*;
import br.reservarecursos.rest.base.PackageScannerSpring;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.response.filter.AjaxServerAndClientTimeFilter;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class WicketApplication extends WebApplication {

    @Autowired
    private PersonRepo personRepo;

	@Autowired
	private PackageScannerSpring packageScannerSpring;

    static Logger log = Logger.getLogger(WicketApplication.class.getName());
    @Autowired
    private Boolean dbTeste;
    @Autowired
    private Boolean restLigar;
    @Autowired
    private Boolean ldapLigado;
    @Autowired
    private UsuarioBusiness usuarioBusiness;
    @Autowired
    private PeriodoBusiness periodoBusiness;
    @Autowired
    private AmbienteBusiness ambienteBusiness;
    @Autowired
    private ReservaBusiness reservaBusiness;
    @Autowired
    private HorarioBusiness horarioBusiness;
    @Autowired
    private RecursoBusiness recursoBusiness;

    @Override
    public Class<MenuPage> getHomePage() {
        return MenuPage.class;
    }

    @Override
    public void init() {
        log.info("\n" +
                "********************************************************************\n"+
                "***                      Carregando o Sistema                    ***\n"+
                "********************************************************************");

        getResourceSettings().setResourcePollFrequency(Duration.ONE_MINUTE);

        getApplicationSettings().setUploadProgressUpdatesEnabled(true);

        getRequestCycleSettings().addResponseFilter(new AjaxServerAndClientTimeFilter());

        getComponentInstantiationListeners().add(new SpringComponentInjector(this));

        getRequestCycleSettings().setResponseRequestEncoding("UTF-8");
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

        getDebugSettings().setAjaxDebugModeEnabled(false);

        // don't throw exceptions for missing translations
        getResourceSettings().setThrowExceptionOnMissingResource(false);

        // enable ajax debug etc.
        getDebugSettings().setDevelopmentUtilitiesEnabled(false);

        // make markup friendly as in deployment-mode
        getMarkupSettings().setStripWicketTags(false);


        mountPage("/menu_ambientes/", MenuPage.class);
        mountPage("/menu_recursos/", MenuRecursosPage.class);
        mountPage("/ambientes/", AmbientesPage.class);
        mountPage("/recursos/", RecursosPage.class);
        mountPage("/horarios/", HorariosPage.class);
        mountPage("/periodos/", PeriodosPage.class);
        mountPage("/usuarios/", UsuariosPage.class);
        mountPage("/reservashoje/", VerReservasHojePage.class);
        mountPage("/reservasfixas/", VerReservasPage.class);
        mountPage("/reservassemana/", VerReservasSemanaPage.class);
        mountPage("/relatorios/", RelatoriosPage.class);
        mountPage("/gerenciar/", GerenciarPage.class);
        mountPage("/grupos/", GrupoPage.class);


        if(restLigar)packageScannerSpring.scanPackage(this, "br.edu.ifto.reservarecursos.rest");

        if(dbTeste)dbTeste();

        criarAdmin();

    }

    private void criarAdmin(){
        log.info("\n" +
        "********************************************************************\n"+
        "***  Criando o usuário Administrador - login:admin senha:admin   ***\n"+
        "********************************************************************");
        Usuario usuarioAdmin = usuarioBusiness.getUsuario("admin");
        if(usuarioAdmin == null) {
            Usuario admin = new Usuario();
            admin.setEmail("admin@admin.org.br");
            admin.setLogin("admin");
            admin.setNome("Administrador");
            admin.setSenha("21232f297a57a5a743894a0e4a801fc3");
            admin.setAdmin(true);
            admin.setAdminGeral(true);
            admin.setAdminEmprestimo(false);
            usuarioBusiness.save(admin);
        }

        Usuario usuarioAdminEmprestimo = usuarioBusiness.getUsuario("adminEmprestimo");
        if(usuarioAdminEmprestimo == null) {
            Usuario admin = new Usuario();
            admin.setEmail("adminEmprestimo@admin.org.br");
            admin.setLogin("admin2");
            admin.setNome("Admin. Emprestimos");
            admin.setSenha("21232f297a57a5a743894a0e4a801fc3");
            admin.setAdmin(false);
            admin.setAdminEmprestimo(true);
            usuarioBusiness.save(admin);
        }
    }

    private void dbTeste() {

        log.info("\n" +
                "********************************************************************\n"+
                "***  Iniciando o Banco de Dados de Teste                         ***\n"+
                "********************************************************************");

        Usuario u1 = new Usuario();
        u1.setEmail("teste@teste.org.br");
        u1.setLogin("teste");
        u1.setNome("Teste Teste");
        u1.setSenha("698dc19d489c4e4db73e28a713eab07b");
        u1.setAdmin(true);
        u1.setAdminEmprestimo(false);
        usuarioBusiness.save(u1);

        Usuario u2 = new Usuario();
        u2.setEmail("teste2@teste.org.br");
        u2.setLogin("teste2");
        u2.setNome("Teste2 Teste");
        u2.setSenha("38851536d87701d2191990e24a7f8d4e");
        u2.setAdmin(false);
        u2.setAdminEmprestimo(false);
        usuarioBusiness.save(u2);

        Periodo periodo = new Periodo();
        periodo.setDataInicial(new LocalDate().withMonthOfYear(1).withDayOfMonth(1));
        periodo.setDataFinal(new LocalDate().withMonthOfYear(12).withDayOfMonth(30));
        periodo.setObs("Periodo de " + periodo.getDataInicial().getYear() + "/X");
        periodoBusiness.save(periodo);

        Ambiente r1 = new Ambiente();
        r1.setNome("Laboratório de Informática 01");
        r1.setDescricao("30 Computadores - Acesso a Internet");
        r1.setAtivo(true);
        r1.setGerenciavel(false);
        ambienteBusiness.save(r1);

        Ambiente r2 = new Ambiente();
        r2.setNome("Laboratório de Informática 02");
        r2.setDescricao("30 Computadores - Acesso a Internet");
        r2.setAtivo(true);
        r2.setGerenciavel(false);
        ambienteBusiness.save(r2);

        Ambiente r3 = new Ambiente();
        r3.setNome("Laboratório 3");
        r3.setDescricao("Teste teste teste");
        r3.setAtivo(true);
        r3.setGerenciavel(false);
        ambienteBusiness.save(r3);

        Ambiente r4 = new Ambiente();
        r4.setNome("Auditório");
        r4.setDescricao("Teste teste teste");
        r4.setAtivo(true);
        r4.setGerenciavel(true);
        r4.setGerenciador(u1);
        ambienteBusiness.save(r4);

        Horario horario1 = new Horario();
        horario1.setHoraInicio(new LocalTime(7,30));
        horario1.setHoraFim(new LocalTime(8,30));
        horario1.setPeriodo(periodo);
        horarioBusiness.save(horario1);

        Horario horario2 = new Horario();
        horario2.setHoraInicio(new LocalTime(8,30));
        horario2.setHoraFim(new LocalTime(9,30));
        horario2.setPeriodo(periodo);
        horarioBusiness.save(horario2);

        Horario horario3 = new Horario();
        horario3.setHoraInicio(new LocalTime(10,00));
        horario3.setHoraFim(new LocalTime(11,00));
        horario3.setPeriodo(periodo);
        horarioBusiness.save(horario3);

        horarioBusiness.save(new Horario(new LocalTime(11,00), new LocalTime(12,00), periodo));

        horarioBusiness.save(new Horario(new LocalTime(13,30), new LocalTime(14,30), periodo));
        horarioBusiness.save(new Horario(new LocalTime(14,30), new LocalTime(15,30), periodo));
        horarioBusiness.save(new Horario(new LocalTime(16,00), new LocalTime(17,00), periodo));
        horarioBusiness.save(new Horario(new LocalTime(17,00), new LocalTime(18,00), periodo));

        horarioBusiness.save(new Horario(new LocalTime(18,00), new LocalTime(19,00), periodo));
        horarioBusiness.save(new Horario(new LocalTime(19,00), new LocalTime(21,00), periodo));
        horarioBusiness.save(new Horario(new LocalTime(21,00), new LocalTime(23,00), periodo));

        Reserva reserva1 = new Reserva();
        reserva1.setObs("Aula 1 Teste");
        reserva1.setData(LocalDate.now());
        reserva1.setHorario(horario2);
        reserva1.setPeriodo(periodo);
        reserva1.setAmbiente(r1);
        reserva1.setUsuario(u1);
        reserva1.setAutorizado(false);
        reservaBusiness.save(reserva1);

        //FIXO(Sem Data)
        Reserva reservaFixo1 = new Reserva();
        reservaFixo1.setObs("Aula Fixa 1");
        reservaFixo1.setHorario(horario1);
        reservaFixo1.setPeriodo(periodo);
        reservaFixo1.setAmbiente(r2);
        reservaFixo1.setUsuario(u2);
        reservaFixo1.setDiaSemana(1);
        reservaFixo1.setAutorizado(false);
        reservaBusiness.save(reservaFixo1);

        Reserva reservaFixo2 = new Reserva();
        reservaFixo2.setObs("Aula Fixa 2");
        reservaFixo2.setHorario(horario1);
        reservaFixo2.setPeriodo(periodo);
        reservaFixo2.setAmbiente(r2);
        reservaFixo2.setUsuario(u2);
        reservaFixo2.setDiaSemana(2);
        reservaFixo2.setAutorizado(false);
        reservaBusiness.save(reservaFixo2);

        Reserva reservaFixo3 = new Reserva();
        reservaFixo3.setObs("Aula Fixa 3");
        reservaFixo3.setHorario(horario1);
        reservaFixo3.setPeriodo(periodo);
        reservaFixo3.setAmbiente(r2);
        reservaFixo3.setUsuario(u2);
        reservaFixo3.setDiaSemana(3);
        reservaFixo3.setAutorizado(false);
        reservaBusiness.save(reservaFixo3);

        Recurso recurso01 = new Recurso();
        recurso01.setNome("DataShow 01");
        recurso01.setAtivo(true);
        recurso01.setManutencao(false);
        recursoBusiness.save(recurso01);

        Recurso recurso02 = new Recurso();
        recurso02.setNome("DataShow 02");
        recurso02.setAtivo(true);
        recurso02.setManutencao(false);
        recursoBusiness.save(recurso02);

        Recurso recurso03 = new Recurso();
        recurso03.setNome("DataShow 03");
        recurso03.setAtivo(true);
        recurso03.setManutencao(false);
        recursoBusiness.save(recurso03);


    }

}
