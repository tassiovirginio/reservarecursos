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
package br.reservarecursos.pages.modal;

import br.reservarecursos.business.EmprestimoBusiness;
import br.reservarecursos.business.ReservaBusiness;
import br.reservarecursos.business.UsuarioBusiness;
import br.reservarecursos.entities.*;
import br.reservarecursos.pages.base.BasePage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.List;

/**
 * Created by tassio on 21/01/15.
 */
public class EmprestarModalPage extends WebPage {

    @SpringBean
    private EmprestimoBusiness emprestimoBusiness;

    @SpringBean
    private UsuarioBusiness usuarioBusiness;

    private Emprestimo emprestimo;

    private String senha;

    public EmprestarModalPage(final Usuario usuarioLogado, final Recurso recurso, final ModalWindow window, final BasePage page) {

        List<Usuario> usuarioList = usuarioBusiness.buscarUsuariosPorGrupo(usuarioLogado.getGrupo());

        DateTime now = new DateTime();

        emprestimo = new Emprestimo();
        emprestimo.setRecurso(recurso);
        emprestimo.setOperador(usuarioLogado);
        emprestimo.setDataHoraInicio(now);

        Form form = new Form("form");

        ChoiceRenderer choiceRenderer = new ChoiceRenderer("nome", "id");
        form.add(new DropDownChoice<Usuario>("usuario",new PropertyModel<Usuario>(emprestimo,"usuario")
                ,usuarioList,choiceRenderer).setRequired(true));
        form.add(new PasswordTextField("senha",new PropertyModel<String>(this,"senha")));
        form.add(new Label("nome", recurso.getNome()));
        form.add(new Label("data", now.toString("dd-MM-yyyy HH:mm")));


        final AjaxSubmitLink ajaxSubmitLink = new AjaxSubmitLink("save") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {

                Usuario usuarioValidar = usuarioBusiness.verificarLogin(emprestimo.getUsuario().getLogin(),senha);

                if(usuarioValidar != null){
                    emprestimoBusiness.save(emprestimo);
                }else{
                    page.info("senha incorreta !");
                }

                window.close(target);
            }
        };
        form.add(ajaxSubmitLink);
        add(form);
    }

}
