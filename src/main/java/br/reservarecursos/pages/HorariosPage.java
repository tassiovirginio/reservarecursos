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

package br.reservarecursos.pages;

import br.reservarecursos.business.HorarioBusiness;
import br.reservarecursos.business.PeriodoBusiness;
import br.reservarecursos.entities.Horario;
import br.reservarecursos.entities.Periodo;
import br.reservarecursos.pages.base.BasePage;
import br.reservarecursos.pages.componentes.AjaxLinkConfirm;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.yui.calendar.TimeField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

public class HorariosPage extends BasePage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private HorarioBusiness horarioBusiness;

    @SpringBean
    private PeriodoBusiness periodoBusiness;


    private String horaInicial;

    private String horaFinal;

    public HorariosPage() {
        this(new Horario());
    }

    public HorariosPage(final Horario horario) {

        if(horario.getHoraInicio() != null && horario.getHoraFim() != null) {
            horaInicial = horario.getHoraInicio().toString("hh:mm");
            horaFinal = horario.getHoraFim().toString("hh:mm");
        }

        List<Periodo> periodoList = periodoBusiness.getPeriodos(usuarioLogado.getGrupo());

        Form form = new Form("form");
        form.add(new TextField<String>("horaInicial", new PropertyModel<String>(this, "horaInicial")).setRequired(true));
        form.add(new TextField<String>("horaFinal", new PropertyModel<String>(this, "horaFinal")).setRequired(true));

        DropDownChoice choice = new DropDownChoice("periodo", new PropertyModel(horario, "periodo"), periodoList);
        choice.setRequired(true);
        choice.setChoiceRenderer(new ChoiceRenderer("obs"));
        form.add(choice);

        add(form);

        Button buttonNovo = new Button("buttonNovo") {
            @Override
            public void onSubmit() {
                setResponsePage(new HorariosPage(new Horario()));
            }
        };
        form.add(buttonNovo);

        Button buttonSalvar = new Button("buttonSalvar") {
            @Override
            public void onSubmit() {
                LocalTime ltHoraInicial = LocalTime.parse(horaInicial);
                LocalTime ltHoraFim = LocalTime.parse(horaFinal);

                horario.setHoraInicio(ltHoraInicial);
                horario.setHoraFim(ltHoraFim);

                horarioBusiness.save(horario);
                info("Horario Salvo");
                setResponsePage(new HorariosPage(new Horario()));
            }
        };
        form.add(buttonSalvar);

        List<Horario> listaHorario = horarioBusiness.getListaHorarios(usuarioLogado.getGrupo());

        add(new ListView<Horario>("listaHorario", listaHorario) {
            @Override
            protected void populateItem(ListItem<Horario> item) {
                final Horario horario = item.getModelObject();
                item.add(new Label("descricao", horario.getDesc()));
                item.add(new Label("periodo", new PropertyModel(horario, "periodo.obs")));
                item.add(new Label("grupo", horario.getPeriodo().getGrupo().getNome()));

                Link linkEditar = new Link("linkEditar") {
                    @Override
                    public void onClick() {
                        setResponsePage(new HorariosPage(horario));
                    }
                };
                item.add(linkEditar);


                AjaxLinkConfirm linkApagar = new AjaxLinkConfirm("linkApagar", "Tem Certeza?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        horarioBusiness.delete(horario);
                        info("Horario Deletado");
                    }
                };
                item.add(linkApagar);
            }
        });

    }

}
