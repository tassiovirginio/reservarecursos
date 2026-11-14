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

import br.reservarecursos.business.ReservaBusiness;
import br.reservarecursos.entities.Horario;
import br.reservarecursos.entities.Ambiente;
import br.reservarecursos.entities.Reserva;
import br.reservarecursos.entities.Usuario;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.LocalDate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/**
 * Created by tassio on 21/01/15.
 */
public class ReservarModalPage extends WebPage {

    @SpringBean
    private ReservaBusiness reservaBusiness;

    @SpringBean
    private MailSender mailSender;

    private Reserva reserva;

    public ReservarModalPage(final Usuario usuario, final Ambiente ambiente, final Horario horario, final LocalDate data, final Integer diaSemana, final ModalWindow window) {

        reserva = new Reserva();
        reserva.setAmbiente(ambiente);
        reserva.setHorario(horario);
        reserva.setPeriodo(horario.getPeriodo());
        reserva.setData(data);
        reserva.setDiaSemana(diaSemana);
        reserva.setUsuario(usuario);

        Form form = new Form("form");

        form.add(new Label("ambiente", ambiente.getNome()));
        form.add(new Label("horario", horario.getDesc()));

        if(data == null) {
            form.add(new Label("data", "Horário Fixo"));
        }else{
            form.add(new Label("data", data.toString("dd/MM/yyyy")));
        }

        form.add(new Label("diaSemanaLabel", "").setVisible(diaSemana != null));
        form.add(new Label("diaSemana", diaSemana));

        form.add(new TextArea<String>("objetivo", new PropertyModel<String>(reserva, "obs")));


        final AjaxSubmitLink ajaxSubmitLink = new AjaxSubmitLink("save") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {

                //validação
                if(data != null && data.getDayOfYear() == new LocalDate().getDayOfYear() && reserva.getUrgencia() == false){
                    error("É obrigatório marcar o checkbox.");
                }else {
                    if(reserva.getObs() != null) {
                        reservaBusiness.save(reserva);
                        if (reserva.getAmbiente().getGerenciavel()) {
                            if(reserva.getAmbiente().getGerenciador() != null && reserva.getAmbiente().getGerenciador().getEmail() != null && !reserva.getAmbiente().getGerenciador().getEmail().isEmpty()){
                                enviarEmail(reserva.getAmbiente().getGerenciador().getEmail(),
                                        "Uma Reserva de Ambiente Gerenciavel foi realizada",
                                        "reserva: " + reserva.getObs() + " - " + reserva.getData());
                            }
                        }
                    }
                }
                window.close(target);
            }
        };

        form.add(ajaxSubmitLink);

        final AjaxCheckBox checkBox = new AjaxCheckBox("urgencia", new PropertyModel<Boolean>(reserva, "urgencia")) {
            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                ajaxSubmitLink.setEnabled(this.getModelObject());
                ajaxRequestTarget.add(ajaxSubmitLink);
            }
        };

        checkBox.setRequired(true);
        WebMarkupContainer webMarkupContainer = new WebMarkupContainer("msgCheckBox");
        webMarkupContainer.add(checkBox);
        form.add(webMarkupContainer);


        if(data == null){
            webMarkupContainer.setVisible(false);
            ajaxSubmitLink.setEnabled(true);
        }else {
            if (data.getDayOfYear() == new LocalDate().getDayOfYear()) {
                webMarkupContainer.setVisible(true);
                ajaxSubmitLink.setEnabled(false);
            } else {
                webMarkupContainer.setVisible(false);
                ajaxSubmitLink.setEnabled(true);
            }
        }

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
                    e.printStackTrace();
                }
            }
        };
        emailThread.start();
    }

}
