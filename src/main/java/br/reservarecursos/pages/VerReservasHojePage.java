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
import br.reservarecursos.entities.Ambiente;
import br.reservarecursos.entities.Horario;
import br.reservarecursos.entities.Reserva;
import br.reservarecursos.pages.base.BasePage;
import br.reservarecursos.pages.componentes.AjaxLinkConfirm;
import br.reservarecursos.pages.componentes.LabelDay;
import br.reservarecursos.pages.modal.ReservarModal;
import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerReservasHojePage extends BasePage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private ReservaBusiness reservaBusiness;

    @SpringBean
    private HorarioBusiness horarioBusiness;


    public VerReservasHojePage(final Ambiente ambiente) {

        add(new Label("nomeRecurso", ambiente.getNome()));

        final int diaSemana = LocalDate.now().getDayOfWeek();

        List<Map<Integer, DTO>> lista = new ArrayList<Map<Integer, DTO>>();

        List<Horario> listaHorario = horarioBusiness.getLista(periodoAtual);

        for (Horario horario : listaHorario) {
            Map<Integer, DTO> linha = new HashMap<Integer, DTO>();
            linha.put(0, new DTO(horario.getDesc(), horario));

            Reserva rn = reservaBusiness.getReservaNormal(ambiente, horario, LocalDate.now());
            if (rn != null) {
                linha.put(diaSemana, new DTO(rn.getObs(), rn));
            } else {
                Reserva r = reservaBusiness.getReservaFixa(ambiente, horario, diaSemana);
                if (r != null) {
                    linha.put(diaSemana, new DTO(r.getObs(), r, true));
                } else {
                    Reserva reservaNull = new Reserva();
                    reservaNull.setAmbiente(ambiente);
//                    reservaNull.setDiaSemana(diaSemana);
                    reservaNull.setHorario(horario);
                    reservaNull.setData(LocalDate.now());
                    linha.put(diaSemana, new DTO("", reservaNull));
                }
            }

            lista.add(linha);
        }


        add(new ListView<Map<Integer, DTO>>("horarioSemanaList", lista) {
            @Override
            protected void populateItem(ListItem<Map<Integer, DTO>> item) {
                final Map<Integer, DTO> hs = item.getModelObject();
                final Reserva reserva = (Reserva) hs.get(diaSemana).getObj();

                item.add(new Label("horario", reserva.getHorario().getHoraInicio().toString("HH:mm") + " - " + reserva.getHorario().getHoraFim().toString("HH:mm")));
                item.add(new LabelDay("hoje", reserva.getObs(), hs.get(diaSemana).getFixo()));

                final ReservarModal modal2 = new ReservarModal("modal", usuarioLogado, ambiente, reserva.getHorario(), reserva.getData(), null, VerReservasHojePage.this);
                item.add(modal2);

                AjaxLink linkReservar = new AjaxLink("linkReservar") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        modal2.show(ajaxRequestTarget);
                    }
                };
                linkReservar.setVisible(hs.get(diaSemana).getHorario().trim().isEmpty()
                        && reserva.getHorario().getHoraInicio().isAfter(new LocalTime()) && logado); //&& adminLogado
                item.add(linkReservar);


                AjaxLinkConfirm linkApagar = new AjaxLinkConfirm("linkApagar", "Certeza que deseja deletar?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        reservaBusiness.delete(reserva);
                        setResponsePage(new VerReservasHojePage(ambiente));
                    }
                };
                linkApagar.setVisible(
                        hs.get(diaSemana).getHorario().trim().isEmpty() == false
                        && (
                                (logado && reserva.getUsuario().equals(usuarioLogado))
                                        ||
                                        adminLogado)
                        && reserva.getHorario().getHoraInicio().isAfter(new LocalTime())
                );
                item.add(linkApagar);

            }
        });

    }

    private class DTO implements Serializable {
        private String horario;
        private Object obj;
        private Boolean fixo;

        public DTO(String horario, Object obj) {
            this(horario,obj,false);
        }

        public DTO(String horario, Object obj, Boolean fixo) {
            this.horario = horario;
            this.obj = obj;
            this.fixo = fixo;
        }

        public String getHorario() {
            if(horario == null) horario = "Teste";
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

        public Boolean getFixo() {
            return fixo;
        }

        public void setFixo(Boolean fixo) {
            this.fixo = fixo;
        }
    }

}
