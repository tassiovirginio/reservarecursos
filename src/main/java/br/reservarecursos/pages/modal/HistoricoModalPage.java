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
import br.reservarecursos.business.UsuarioBusiness;
import br.reservarecursos.entities.Emprestimo;
import br.reservarecursos.entities.Horario;
import br.reservarecursos.entities.Recurso;
import br.reservarecursos.entities.Usuario;
import br.reservarecursos.pages.HorariosPage;
import br.reservarecursos.pages.componentes.AjaxLinkConfirm;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.List;

/**
 * Created by tassio on 21/01/15.
 */
public class HistoricoModalPage extends WebPage {

    @SpringBean
    private EmprestimoBusiness emprestimoBusiness;

    @SpringBean
    private UsuarioBusiness usuarioBusiness;

    private Emprestimo emprestimo;

    public HistoricoModalPage(final Recurso recurso, final ModalWindow window) {

        List<Emprestimo> emprestimos = emprestimoBusiness.emprestimosLista(recurso,5);

        add(new ListView<Emprestimo>("listaEmprestimos", emprestimos) {
            @Override
            protected void populateItem(ListItem<Emprestimo> item) {
                final Emprestimo emprestimo = item.getModelObject();
                item.add(new Label("usuario", emprestimo.getUsuario().getNome()));
                item.add(new Label("dataInicio", emprestimo.getDataHoraInicio().toString("dd/MM/yyyy HH:mm")));
                item.add(new Label("dataFim", emprestimo.getDataHoraFim().toString("dd/MM/yyyy HH:mm")));
                Interval interval = new Interval(emprestimo.getDataHoraInicio(),emprestimo.getDataHoraFim());
                item.add(new Label("tempo",interval.toDuration().getStandardMinutes()));
            }
        });

    }

}
