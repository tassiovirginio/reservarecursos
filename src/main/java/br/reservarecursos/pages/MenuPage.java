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

import br.reservarecursos.business.AmbienteBusiness;
import br.reservarecursos.entities.Ambiente;
import br.reservarecursos.pages.base.BasePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class MenuPage extends BasePage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private AmbienteBusiness ambienteBusiness;

    public MenuPage() {

        List<Ambiente> listaAmbientes = ambienteBusiness.getListaAmbientesAtivosNaoGerenciaveis(grupoSelecionado);

        add(new ListView<Ambiente>("listaAmbientes", listaAmbientes) {
            @Override
            protected void populateItem(ListItem<Ambiente> item) {
                final Ambiente ambiente = item.getModelObject();
                item.add(new Label("nome", ambiente.getNome()));
                item.add(new Label("descricao", ambiente.getDescricao()));
                item.add(new Link("linkVer") {
                    @Override
                    public void onClick() {
                        setResponsePage(new VerReservasPage(ambiente));
                    }
                });
                item.add(new Link("linkHoje") {
                    @Override
                    public void onClick() {
                        setResponsePage(new VerReservasHojePage(ambiente));
                    }
                });
                item.add(new Link("linkSemana") {
                    @Override
                    public void onClick() {
                        setResponsePage(new VerReservasSemanaPage(ambiente));
                    }
                });
                item.add(new Link("linkProximaSemana") {
                    @Override
                    public void onClick() {
                        setResponsePage(new VerReservasSemanaPage(ambiente, LocalDate.now().plusWeeks(1)));
                    }
                });
            }
        });


        List<Ambiente> listaRecursosGerenciaveis = ambienteBusiness.getListaAmbientesAtivosGerenciaveis(grupoSelecionado);

        add(new ListView<Ambiente>("listaRecursosGerenciaveis", listaRecursosGerenciaveis) {
            @Override
            protected void populateItem(ListItem<Ambiente> item) {
                final Ambiente ambiente = item.getModelObject();
                item.add(new Label("nome", ambiente.getNome()));
                item.add(new Label("descricao", ambiente.getDescricao()));
                item.add(new Label("gerenciador", ambiente.getGerenciador().getNome()));
                item.add(new Link("linkVer") {
                    @Override
                    public void onClick() {
                        setResponsePage(new VerReservasPage(ambiente));
                    }
                }.setVisible(false));
                item.add(new Link("linkHoje") {
                    @Override
                    public void onClick() {
                        setResponsePage(new VerReservasHojePage(ambiente));
                    }
                });
                item.add(new Link("linkSemana") {
                    @Override
                    public void onClick() {
                        setResponsePage(new VerReservasSemanaPage(ambiente));
                    }
                });
                item.add(new Link("linkProximaSemana") {
                    @Override
                    public void onClick() {
                        setResponsePage(new VerReservasSemanaPage(ambiente, LocalDate.now().plusWeeks(1)));
                    }
                });
            }
        });

    }

}
