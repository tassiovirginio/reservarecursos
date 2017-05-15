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

import br.reservarecursos.business.EmprestimoBusiness;
import br.reservarecursos.business.RecursoBusiness;
import br.reservarecursos.entities.Emprestimo;
import br.reservarecursos.entities.Recurso;
import br.reservarecursos.pages.base.BasePage;
import br.reservarecursos.pages.componentes.AjaxLinkConfirm;
import br.reservarecursos.pages.componentes.LinkModelEmprestar;
import br.reservarecursos.pages.componentes.LinkModelHistorico;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.List;

public class MenuRecursosPage extends BasePage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private RecursoBusiness recursoBusiness;

    @SpringBean
    private EmprestimoBusiness emprestimoBusiness;

    public MenuRecursosPage() {

        List<Recurso> listaRecursos = recursoBusiness.listAtivos(grupoSelecionado);

        add(new ListView<Recurso>("listaRecursos", listaRecursos) {
            @Override
            protected void populateItem(ListItem<Recurso> item) {
                final Recurso recurso = item.getModelObject();
                final Emprestimo emprestimo = emprestimoBusiness.emprestimoAtual(recurso);
                int qtd = emprestimoBusiness.emprestimosQuantidade(recurso);

                item.add(new Label("nome", recurso.getNome()));

                Boolean emprestimoNotNull = emprestimo != null;

                String msg = "";

                if(emprestimoNotNull) {
                    item.add(new Label("nomeUsuario", emprestimo.getUsuario().getNome()).add(new AttributeModifier("style", "color: red;")));
                    Interval interval = new Interval(emprestimo.getDataHoraInicio(), new DateTime());
                    item.add(new Label("tempo",interval.toDuration().getStandardHours()));
                    msg = "O usuário "+ emprestimo.getUsuario().getNome() + " esta Devolvendo o " + emprestimo.getRecurso().getNome() + " nesse momento ?";
                }else{
                    item.add(new Label("nomeUsuario", "livre").add(new AttributeModifier("style", "color: green;")));
                    item.add(new Label("tempo","0"));
                }

                item.add(new Label("qtdEmprestimos",qtd));


                LinkModelEmprestar linkModelEmprestar = new LinkModelEmprestar("lkEmprestar", usuarioLogado, recurso, MenuRecursosPage.this);
                item.add(linkModelEmprestar);

                LinkModelHistorico linkModelHistorico = new LinkModelHistorico("lkHistorico", recurso, MenuRecursosPage.this);
                item.add(linkModelHistorico);

                AjaxLinkConfirm linkDevolver = new AjaxLinkConfirm("linkDevolver", msg) {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        emprestimo.setDataHoraFim(new DateTime());
                        emprestimoBusiness.save(emprestimo);
                        info("Recurso " + emprestimo.getRecurso().getNome() + "/" + emprestimo.getUsuario().getNome()+ " Devolvido");
                        setResponsePage(MenuRecursosPage.class);
                    }
                };
                item.add(linkDevolver.setVisible(emprestimoNotNull));


                if(usuarioLogado != null){
                    linkModelEmprestar.setVisible(usuarioLogado.getAdminEmprestimo() && !emprestimoNotNull);
                    linkDevolver.setVisible(usuarioLogado.getAdminEmprestimo() && emprestimoNotNull);
                }else{
                    linkModelEmprestar.setVisible(false);
                    linkDevolver.setVisible(false);
                }
            }
        });

    }

}
