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

import br.reservarecursos.business.PeriodoBusiness;
import br.reservarecursos.entities.Periodo;
import br.reservarecursos.pages.base.BasePage;
import br.reservarecursos.pages.componentes.AjaxLinkConfirm;
import br.reservarecursos.pages.componentes.DateFieldHTML5;
import com.googlecode.wicket.kendo.ui.form.TextArea;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

public class PeriodosPage extends BasePage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private PeriodoBusiness periodoBusiness;

    private Date dataInicial;

    private Date dataFinal;

    public PeriodosPage() {
        this(new Periodo());
    }


    public PeriodosPage(final Periodo periodo) {

        if (periodo.getId() != null) {
            dataInicial = periodo.getDataInicial().toDate();
            dataFinal = periodo.getDataFinal().toDate();
        }

        Form form = new Form("form");
        form.add(new DateFieldHTML5("dataInicial", new PropertyModel<Date>(this, "dataInicial")).setRequired(true));
        form.add(new DateFieldHTML5("dataFinal", new PropertyModel<Date>(this, "dataFinal")).setRequired(true));
        form.add(new TextArea<String>("obs", new PropertyModel<String>(periodo, "obs")).setRequired(true));
        add(form);

        Button buttonNovo = new Button("buttonNovo") {
            @Override
            public void onSubmit() {
                setResponsePage(new PeriodosPage(new Periodo()));
            }
        };
        form.add(buttonNovo);

        Button buttonSalvar = new Button("buttonSalvar") {
            @Override
            public void onSubmit() {
                if(grupoSelecionado != null) {
                    periodo.setDataInicial(new LocalDate(dataInicial));
                    periodo.setDataFinal(new LocalDate(dataFinal));
                    periodo.setGrupo(usuarioLogado.getGrupo());
                    try {
                        periodoBusiness.save(periodo);
                    } catch (Exception e) {
                        e.getMessage();
                        error(e.getMessage());
                    }
                    info("Périodo Salvo");
                }else{
                    info("Você Não tem grupo, não pode cadastrar um periodo");
                }
                setResponsePage(new PeriodosPage(new Periodo()));
            }
        };
        form.add(buttonSalvar);

        List<Periodo> listaPeriodos = periodoBusiness.getPeriodos(usuarioLogado.getGrupo());

        add(new ListView<Periodo>("listaPeriodos", listaPeriodos) {
            @Override
            protected void populateItem(ListItem<Periodo> item) {
                final Periodo periodo = item.getModelObject();
                item.add(new Label("dataInicial", periodo.getDataInicial().toString("dd/MM/yyyy")));
                item.add(new Label("dataFinal", periodo.getDataFinal().toString("dd/MM/yyyy")));
                item.add(new Label("obs", periodo.getObs()));

                if(periodo.getGrupo() != null) {
                    item.add(new Label("grupo", periodo.getGrupo().getNome()));
                }else{
                    item.add(new Label("grupo", ""));
                }

                Link linkEditar = new Link("linkEditar") {
                    @Override
                    public void onClick() {
                        setResponsePage(new PeriodosPage(periodo));
                    }
                };
                item.add(linkEditar);


                AjaxLinkConfirm linkApagar = new AjaxLinkConfirm("linkApagar", "Tem Certeza?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        periodoBusiness.delete(periodo);
                        info("Periodo Deletado");
                    }
                };
                item.add(linkApagar);
            }
        });

    }

}
