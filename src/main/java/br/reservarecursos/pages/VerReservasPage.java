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
import br.reservarecursos.business.ReservaBusiness;
import br.reservarecursos.entities.Horario;
import br.reservarecursos.entities.Ambiente;
import br.reservarecursos.entities.Reserva;
import br.reservarecursos.pages.base.BasePage;
import br.reservarecursos.pages.componentes.AjaxLinkConfirm;
import br.reservarecursos.pages.componentes.LabelDay;
import br.reservarecursos.pages.componentes.LinkModel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerReservasPage extends BasePage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private ReservaBusiness reservaBusiness;

    @SpringBean
    private HorarioBusiness horarioBusiness;

    public VerReservasPage(final Ambiente ambiente) {

        add(new Label("nomeRecurso", ambiente.getNome()));

        List<Map<Integer, Object>> lista = new ArrayList<Map<Integer, Object>>();

        List<Horario> listaHorario = horarioBusiness.getLista(periodoAtual);

        for (Horario horario : listaHorario) {
            Map<Integer, Object> linha = new HashMap<Integer, Object>();
            linha.put(0, horario);
            for (int i = 1; i <= 7; i++) {
                Reserva reservaFixa = reservaBusiness.getReservaFixa(ambiente, horario, i);
                if (reservaFixa != null) {
                    linha.put(i, reservaFixa);
                } else {
                    Reserva reservaFixaNull = new Reserva();
                    reservaFixaNull.setObs("");
                    linha.put(i, reservaFixaNull);
                }
            }
            lista.add(linha);
        }


        add(new ListView<Map<Integer, Object>>("horarioSemanaList", lista) {
            @Override
            protected void populateItem(ListItem<Map<Integer, Object>> item) {
                final Map<Integer, Object> hs = item.getModelObject();

                Horario horario = (Horario) hs.get(0);

                final Reserva reserva1 = (Reserva) hs.get(1);
                final Reserva reserva2 = (Reserva) hs.get(2);
                final Reserva reserva3 = (Reserva) hs.get(3);
                final Reserva reserva4 = (Reserva) hs.get(4);
                final Reserva reserva5 = (Reserva) hs.get(5);
                final Reserva reserva6 = (Reserva) hs.get(6);
                final Reserva reserva7 = (Reserva) hs.get(7);

                item.add(new Label("horario", horario.getDesc()));

                item.add(new LabelDay("segunda", reserva1));
                item.add(new LinkModel("linkReservar1", usuarioLogado, ambiente, horario, null, 1, VerReservasPage.this).setVisible(adminLogado && reserva1.getId() == null));
                item.add(new AjaxLinkConfirm("linkApagar1", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva1);
                        setResponsePage(new VerReservasPage(ambiente));
                    }
                }.setVisible(adminLogado && reserva1.getId() != null));

                item.add(new LabelDay("terca", reserva2));
                item.add(new LinkModel("linkReservar2", usuarioLogado, ambiente, horario, null, 2, VerReservasPage.this).setVisible(adminLogado && reserva2.getId() == null));
                item.add(new AjaxLinkConfirm("linkApagar2", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva2);
                        setResponsePage(new VerReservasPage(ambiente));
                    }
                }.setVisible(adminLogado && reserva2.getId() != null));

                item.add(new LabelDay("quarta", reserva3));
                item.add(new LinkModel("linkReservar3", usuarioLogado, ambiente, horario, null, 3, VerReservasPage.this).setVisible(adminLogado && reserva3.getId() == null));
                item.add(new AjaxLinkConfirm("linkApagar3", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva3);
                        setResponsePage(new VerReservasPage(ambiente));
                    }
                }.setVisible(adminLogado && reserva3.getId() != null));

                item.add(new LabelDay("quinta", reserva4));
                item.add(new LinkModel("linkReservar4", usuarioLogado, ambiente, horario, null, 4, VerReservasPage.this).setVisible(adminLogado && reserva4.getId() == null));
                item.add(new AjaxLinkConfirm("linkApagar4", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva4);
                        setResponsePage(new VerReservasPage(ambiente));
                    }
                }.setVisible(adminLogado && reserva4.getId() != null));


                item.add(new LabelDay("sexta", reserva5));
                item.add(new LinkModel("linkReservar5", usuarioLogado, ambiente, horario, null, 5, VerReservasPage.this).setVisible(adminLogado && reserva5.getId() == null));
                item.add(new AjaxLinkConfirm("linkApagar5", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva5);
                        setResponsePage(new VerReservasPage(ambiente));
                    }
                }.setVisible(adminLogado && reserva5.getId() != null));

                item.add(new LabelDay("sabado", reserva6));
                item.add(new LinkModel("linkReservar6", usuarioLogado, ambiente, horario, null, 6, VerReservasPage.this).setVisible(adminLogado && reserva6.getId() == null));
                item.add(new AjaxLinkConfirm("linkApagar6", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva6);
                        setResponsePage(new VerReservasPage(ambiente));
                    }
                }.setVisible(adminLogado && reserva6.getId() != null));

                item.add(new LabelDay("domingo", reserva7));
                item.add(new LinkModel("linkReservar7", usuarioLogado, ambiente, horario, null, 7, VerReservasPage.this).setVisible(adminLogado && reserva7.getId() == null));
                item.add(new AjaxLinkConfirm("linkApagar7", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva7);
                        setResponsePage(new VerReservasPage(ambiente));
                    }
                }.setVisible(adminLogado && reserva7.getId() != null));
            }
        });

    }

}
