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
import br.reservarecursos.pages.componentes.LabelAviso;
import br.reservarecursos.pages.componentes.LabelDay;
import br.reservarecursos.pages.componentes.LinkModel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerReservasSemanaPage extends BasePage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private ReservaBusiness reservaBusiness;

    @SpringBean
    private HorarioBusiness horarioBusiness;

    public VerReservasSemanaPage(Ambiente ambiente) {
        this(ambiente, new LocalDate());
    }

    public VerReservasSemanaPage(final Ambiente ambiente, final LocalDate data) {

        add(new Label("msg", data));

        add(new Label("nomeRecurso", ambiente.getNome()));

        List<Map<Integer, DTO>> lista = new ArrayList<Map<Integer, DTO>>();

        List<Horario> listaHorario = horarioBusiness.getLista(periodoAtual);

        final int diaSemana = data.getDayOfWeek();

        for (Horario horario : listaHorario) {
            Map<Integer, DTO> linha = new HashMap<Integer, DTO>();

            linha.put(0, new DTO(horario.getDesc(), horario));

            for (int i = 1; i <= 7; i++) {

                int numeroDias = 0;

                LocalDate dia = null;

                if (diaSemana > i) {
                    numeroDias = diaSemana - i;
                    dia = data.minusDays(numeroDias);
                } else if (diaSemana == i) {
                    dia = data;
                } else if (diaSemana < i) {
                    numeroDias = i - diaSemana;
                    dia = data.plusDays(numeroDias);
                }

                Reserva rn = reservaBusiness.getReservaNormal(ambiente, horario, dia);
                if (rn != null) {
                    linha.put(dia.getDayOfWeek(), new DTO(rn.getObs().trim(), rn));
                } else {
                    Reserva rf = reservaBusiness.getReservaFixa(ambiente, horario, dia.getDayOfWeek());
                    if (rf != null) {
                        linha.put(dia.getDayOfWeek(), new DTO(rf.getObs().trim(), rf, true));
                    } else {
                        Reserva reservaNull = new Reserva();
                        reservaNull.setData(dia);
                        linha.put(dia.getDayOfWeek(), new DTO("", reservaNull));
                    }
                }

            }
            lista.add(linha);
        }

        add(new ListView<Map<Integer, DTO>>("horarioSemanaList", lista) {
            @Override
            protected void populateItem(ListItem<Map<Integer, DTO>> item) {
                final Map<Integer, DTO> hs = item.getModelObject();
                item.add(new Label("horario", hs.get(0).getHorario()));

                item.add(new LabelDay("segunda", hs.get(1).getHorario(),hs.get(1).getFixa()));
                item.add(new LabelDay("terca", hs.get(2).getHorario(),hs.get(2).getFixa()));
                item.add(new LabelDay("quarta", hs.get(3).getHorario(),hs.get(3).getFixa()));
                item.add(new LabelDay("quinta", hs.get(4).getHorario(),hs.get(4).getFixa()));
                item.add(new LabelDay("sexta", hs.get(5).getHorario(),hs.get(5).getFixa()));
                item.add(new LabelDay("sabado", hs.get(6).getHorario(),hs.get(6).getFixa()));
                item.add(new LabelDay("domingo", hs.get(7).getHorario(),hs.get(7).getFixa()));

                Horario horario = (Horario) hs.get(0).getObj();

                final Reserva reserva1 = (Reserva) hs.get(1).getObj();
                final Reserva reserva2 = (Reserva) hs.get(2).getObj();
                final Reserva reserva3 = (Reserva) hs.get(3).getObj();
                final Reserva reserva4 = (Reserva) hs.get(4).getObj();
                final Reserva reserva5 = (Reserva) hs.get(5).getObj();
                final Reserva reserva6 = (Reserva) hs.get(6).getObj();
                final Reserva reserva7 = (Reserva) hs.get(7).getObj();



                item.add(new LabelAviso("segundaGeren", reserva1));
                item.add(new LabelAviso("tercaGeren", reserva2));
                item.add(new LabelAviso("quartaGeren", reserva3));
                item.add(new LabelAviso("quintaGeren", reserva4));
                item.add(new LabelAviso("sextaGeren", reserva5));
                item.add(new LabelAviso("sabadoGeren", reserva6));
                item.add(new LabelAviso("domingoGeren", reserva7));


                item.add(new LinkModel("linkReservar1", usuarioLogado, ambiente, horario, reserva1.getData(), null, VerReservasSemanaPage.this)
                                .setVisible(hs.get(1).getHorario().trim().isEmpty() && logado && (reserva1.getData().isAfter(LocalDate.now())))
                );
                item.add(new AjaxLinkConfirm("linkApagar1", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva1);
                        setResponsePage(new VerReservasSemanaPage(ambiente, reserva1.getData()));
                    }
                }.setVisible(visivelDelete(reserva1)));


                item.add(new LinkModel("linkReservar2", usuarioLogado, ambiente, horario, reserva2.getData(), null, VerReservasSemanaPage.this)
                                .setVisible(hs.get(2).getHorario().trim().isEmpty() && logado && (reserva2.getData().isAfter(LocalDate.now())))
                );
                item.add(new AjaxLinkConfirm("linkApagar2", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva2);
                        setResponsePage(new VerReservasSemanaPage(ambiente, reserva2.getData()));
                    }
                }.setVisible(visivelDelete(reserva2)));


                item.add(new LinkModel("linkReservar3", usuarioLogado, ambiente, horario, reserva3.getData(), null, VerReservasSemanaPage.this)
                                .setVisible(hs.get(3).getHorario().trim().isEmpty() && logado && (reserva3.getData().isAfter(LocalDate.now())))
                );
                item.add(new AjaxLinkConfirm("linkApagar3", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva3);
                        setResponsePage(new VerReservasSemanaPage(ambiente, reserva3.getData()));
                    }
                }.setVisible(visivelDelete(reserva3)));


                item.add(new LinkModel("linkReservar4", usuarioLogado, ambiente, horario, reserva4.getData(), null, VerReservasSemanaPage.this)
                                .setVisible(hs.get(4).getHorario().trim().isEmpty() && logado && (reserva4.getData().isAfter(LocalDate.now())))
                );
                item.add(new AjaxLinkConfirm("linkApagar4", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva4);
                        setResponsePage(new VerReservasSemanaPage(ambiente, reserva4.getData()));
                    }
                }.setVisible(visivelDelete(reserva4)));


                item.add(new LinkModel("linkReservar5", usuarioLogado, ambiente, horario, reserva5.getData(), null, VerReservasSemanaPage.this)
                                .setVisible(hs.get(5).getHorario().trim().isEmpty() && logado && (reserva5.getData().isAfter(LocalDate.now())))
                );
                item.add(new AjaxLinkConfirm("linkApagar5", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva5);
                        setResponsePage(new VerReservasSemanaPage(ambiente, reserva5.getData()));
                    }
                }.setVisible(visivelDelete(reserva5)));


                item.add(new LinkModel("linkReservar6", usuarioLogado, ambiente, horario, reserva6.getData(), null, VerReservasSemanaPage.this)
                                .setVisible(hs.get(6).getHorario().trim().isEmpty() && logado && (reserva6.getData().isAfter(LocalDate.now())))
                );
                item.add(new AjaxLinkConfirm("linkApagar6", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva6);
                        setResponsePage(new VerReservasSemanaPage(ambiente, reserva6.getData()));
                    }
                }.setVisible(visivelDelete(reserva6)));


                item.add(new LinkModel("linkReservar7", usuarioLogado, ambiente, horario, reserva7.getData(), null, VerReservasSemanaPage.this)
                                .setVisible(hs.get(7).getHorario().trim().isEmpty() && logado && (reserva7.getData().isAfter(LocalDate.now())))
                );
                item.add(new AjaxLinkConfirm("linkApagar7", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva7);
                        setResponsePage(new VerReservasSemanaPage(ambiente, reserva7.getData()));
                    }
                }.setVisible(visivelDelete(reserva7)));


            }
        });

    }

    public Boolean visivelDelete(Reserva reserva) {

        Boolean reservaNaoNull = reserva.getId() != null;

        Boolean dataReservaPosteriorHoje = (reserva.getData() != null && reserva.getData().isAfter(LocalDate.now()));

        Boolean usuarioLogadoIgualReserva = (usuarioLogado != null && usuarioLogado.equals(reserva.getUsuario()));

        Boolean diaSemanaPosterioHoje = (reserva.getDiaSemana() != null && reserva.getDiaSemana() > LocalDate.now().getDayOfWeek());

        Boolean reservaFixa = reservaNaoNull && reserva.getData() == null && reserva.getDiaSemana() != null;

        if(reservaFixa)return false;

        if (reservaNaoNull && reserva.getData() == null) {
            System.out.println(reserva);
        }

        if (reservaNaoNull) {
            if (usuarioLogadoIgualReserva || adminLogado) {
                if (dataReservaPosteriorHoje || diaSemanaPosterioHoje || adminLogado) {
                    return true;
                }
            }
        }

        return false;

    }

    private class DTO implements Serializable {
        private String horario;
        private Object obj;
        private Boolean fixa;
        private Boolean autorizado;

        public DTO(String horario, Object obj) {
            this.horario = horario;
            this.obj = obj;
            this.fixa = false;
            this.autorizado = false;
        }

        public DTO(String horario, Object obj, Boolean fixa) {
            this.horario = horario;
            this.obj = obj;
            this.fixa = fixa;
            this.autorizado = false;
        }

        public DTO(String horario, Object obj, Boolean fixa, Boolean autorizado) {
            this.horario = horario;
            this.obj = obj;
            this.fixa = fixa;
            this.autorizado = autorizado;
        }

        public String getHorario() {
            return horario;
        }

        public void setHorario(String horario) {
            this.horario = horario;
        }

        public Object getObj() {
            return obj;
        }

        public void setObj(Object obj) {
            this.obj = obj;
        }

        public Boolean getFixa() {
            return fixa;
        }

        public void setFixa(Boolean fixa) {
            this.fixa = fixa;
        }

        public Boolean getAutorizado() {
            return autorizado;
        }

        public void setAutorizado(Boolean autorizado) {
            this.autorizado = autorizado;
        }
    }

}
