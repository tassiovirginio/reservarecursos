/**
 * Copyright 2015 - Tássio Guerreiro Antunes Virgínio
 *
 * Este arquivo é parte do programa Reserva de Recursos
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
package br.reservarecursos.pages.base;

import br.reservarecursos.business.AmbienteBusiness;
import br.reservarecursos.business.GrupoBusiness;
import br.reservarecursos.business.PeriodoBusiness;
import br.reservarecursos.business.UsuarioBusiness;
import br.reservarecursos.entities.Ambiente;
import br.reservarecursos.entities.Grupo;
import br.reservarecursos.entities.Periodo;
import br.reservarecursos.entities.Usuario;
import br.reservarecursos.pages.*;
import com.googlecode.wicket.kendo.ui.form.TextField;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.ComponentPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by tassio on 13/01/15.
 */
public class BasePage extends WebPage {

    private static final long serialVersionUID = 1L;
    protected Boolean logado;
    protected Usuario usuarioLogado;
    protected Boolean adminLogado = false;
    protected Boolean adminLogadoEmprestimo = false;
    protected Periodo periodoAtual;

    protected Boolean adminGeral = false;

    @SpringBean
    private GrupoBusiness grupoBusiness;

    @SpringBean
    private MailSender mailSender;

    @SpringBean
    private UsuarioBusiness usuarioBusiness;

    @SpringBean
    private PeriodoBusiness periodoBusiness;

    @SpringBean
    private AmbienteBusiness ambienteBusiness;

    private Usuario usuarioForm;

    protected Grupo grupoSelecionado;

    private Label labelPeriodo;

    public BasePage() {

        add(new FeedbackPanel("feedback"));

        List<Grupo> grupoList = grupoBusiness.getGruposAtivos();

        grupoSelecionado = (Grupo)getSession().getAttribute("grupoSelecionado");

        periodoAtual = periodoBusiness.getPeriodoAtual(grupoSelecionado);

        final DropDownChoice<Grupo> ddcGrupos = new DropDownChoice<Grupo>("grupoSelecionado",new PropertyModel(this, "grupoSelecionado"),grupoList,new ChoiceRenderer<>( "nome", "id" )){

            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }

            @Override
            protected void onSelectionChanged(Grupo grupoSelecionado) {
                getSession().setAttribute("grupoSelecionado",grupoSelecionado);
                BasePage.this.get("grupo").setDefaultModelObject(grupoSelecionado.getNome());
                periodoAtual = periodoBusiness.getPeriodoAtual(grupoSelecionado);
                if(periodoAtual == null) {
                    labelPeriodo.setDefaultModelObject("Sem Periodo Cadastrado");
                }else{
                    labelPeriodo.setDefaultModelObject(periodoAtual.getObs());
                }
                super.onSelectionChanged(grupoSelecionado);

                System.out.println(grupoSelecionado);
                System.out.println(periodoAtual);
                System.out.println(this);

                setResponsePage(new MenuPage());
            }

        };
        add(ddcGrupos);


        if(periodoAtual == null) {
            labelPeriodo = new Label("periodo", "Sem Periodo Cadastrado");
        }else{
            labelPeriodo = new Label("periodo", periodoAtual.getObs());
        }

        add(labelPeriodo);

        if(grupoSelecionado != null){
            add(new Label("grupo", grupoSelecionado.getNome()));
        }else{
            add(new Label("grupo", "Selecione um Campus"));
        }

        usuarioLogado = (Usuario) getSession().getAttribute("usuario");

        List<Ambiente> listaAmbientesUsuarioLogadoGerenciados = new ArrayList<Ambiente>();

        if (usuarioLogado != null) {
            if(usuarioLogado.getGrupo() != null) {

                String firstName = new StringTokenizer(usuarioLogado.getNome()).nextToken();

                add(new Label("userName", firstName + " (" + usuarioLogado.getGrupo().getNome() + ")"));
            }else{
                String firstName = new StringTokenizer(usuarioLogado.getNome()).nextToken();

                add(new Label("userName", firstName + "()"));
            }
            logado = true;
            adminLogado = usuarioLogado.getAdmin();
            adminGeral = usuarioLogado.getAdminGeral();
            adminLogadoEmprestimo = usuarioLogado.getAdminEmprestimo();
            createFormLogin(false);
            listaAmbientesUsuarioLogadoGerenciados = ambienteBusiness.getListaAmbientesAtivosGerenciaveis(usuarioLogado,usuarioLogado.getGrupo());
        } else {
            add(new Label("userName", ""));
            logado = false;
            createFormLogin(true);
        }

        Boolean usuarioComGrupo = (usuarioLogado != null && usuarioLogado.getGrupo() != null);


        add(new Link("lkMenu") {
            @Override
            public void onClick() {
                setResponsePage(new MenuPage());
            }
        });

        add(new Link("lkMenuRecusos") {
            @Override
            public void onClick() {
                setResponsePage(new MenuRecursosPage());
            }
        });

        add(new Link("lkAmbientes") {
            @Override
            public void onClick() {
                setResponsePage(new AmbientesPage());
            }
        }.setVisible(adminLogado && usuarioComGrupo));

        add(new Link("lkRecursos") {
            @Override
            public void onClick() {
                setResponsePage(new RecursosPage());
            }
        }.setVisible(adminLogado && usuarioComGrupo));

        add(new Link("lkHorarios") {
            @Override
            public void onClick() {
                setResponsePage(new HorariosPage());
            }
        }.setVisible(adminLogado && usuarioComGrupo));

        add(new Link("lkPeriodo") {
            @Override
            public void onClick() {
                setResponsePage(new PeriodosPage());
            }
        }.setVisible(adminLogado && usuarioComGrupo));

        add(new Link("lkRelatorios") {
            @Override
            public void onClick() {
                setResponsePage(new RelatoriosPage());
            }
        }.setVisible(adminLogado && usuarioComGrupo));

        add(new Link("lkGerenciar") {
            @Override
            public void onClick() {
                setResponsePage(new GerenciarPage());
            }
        }.setVisible(logado && listaAmbientesUsuarioLogadoGerenciados.size() > 0));

        add(new Link("lkGrupos") {
            @Override
            public void onClick() {
                setResponsePage(new GrupoPage());
            }
        }.setVisible(adminGeral));

        add(new Link("lkUsuarios") {
            @Override
            public void onClick() {
                setResponsePage(new UsuariosPage());
            }
        }.setVisible(adminGeral));

        add(new Link("lkSair") {
            @Override
            public void onClick() {
                getSession().clear();
                getSession().invalidateNow();
                setResponsePage(new MenuPage());
            }
        }.setVisible(usuarioLogado != null));

    }

    private void createFormLogin(Boolean visible) {

        usuarioForm = new Usuario();


        Form form = new Form("formLogin") {
            @Override
            protected void onSubmit() {
                Usuario usuarioVerificado = usuarioBusiness.verificarLogin(usuarioForm);

                if (usuarioVerificado != null) {
                    getSession().setAttribute("usuario", usuarioVerificado);
                    getSession().setAttribute("grupoSelecionado",usuarioVerificado.getGrupo());
                    setResponsePage(new MenuPage());
                } else {
                    error("Login Inválido");
                }
            }
        };

        form.add(new TextField<String>
                        ("login", new PropertyModel<String>(usuarioForm, "login"))
                        .setRequired(true)
        );

        form.add(new PasswordTextField
                        ("senha", new PropertyModel<String>(usuarioForm, "senha"))
                        .setRequired(true)
        );

        form.setVisible(visible);

        add(form);
    }

    /**
     * Método responsavel por enviar os emails no sistema.
     *
     * @param to      destinatario
     * @param subject assunto
     * @param msg     mensagem do email
     */
    protected void enviarEmail(final String to, final String subject, final String msg) {
        Thread emailThread = new Thread() {
            @Override
            public void run() {
                try {
                    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
                    simpleMailMessage.setTo(to);
                    simpleMailMessage.setSubject(subject);
                    simpleMailMessage.setText(msg);
                    mailSender.send(simpleMailMessage);
                } catch (Exception e) {
//                    e.printStackTrace();
                    System.out.println("Email não pode ser enviado: " + subject + " - " + msg);
                }
            }
        };
        emailThread.start();
    }

}
