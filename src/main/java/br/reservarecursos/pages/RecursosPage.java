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
import br.reservarecursos.business.RecursoBusiness;
import br.reservarecursos.business.UsuarioBusiness;
import br.reservarecursos.entities.Ambiente;
import br.reservarecursos.entities.Recurso;
import br.reservarecursos.entities.Usuario;
import br.reservarecursos.pages.base.BasePage;
import br.reservarecursos.pages.componentes.AjaxLinkConfirm;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

public class RecursosPage extends BasePage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private RecursoBusiness recursoBusiness;

    @SpringBean
    private UsuarioBusiness usuarioBusiness;

    public RecursosPage() {
        this(new Recurso());
    }

    public RecursosPage(final Recurso recurso) {


        List<Usuario> listaUsuario = usuarioBusiness.listAll();

        Form form = new Form("form");
        form.add(new TextField<String>("nome", new PropertyModel<String>(recurso, "nome")).setRequired(true));
        form.add(new CheckBox("ativo", new PropertyModel<Boolean>(recurso, "ativo")));
        form.add(new CheckBox("manutencao", new PropertyModel<Boolean>(recurso, "manutencao")));

//        form.add(new DropDownChoice<Usuario>("grupo",new PropertyModel(recurso, "administrador"),usuarioList,new ChoiceRenderer<>( "login", "id" )));

        add(form);

        Button buttonNovo = new Button("buttonNovo") {
            @Override
            public void onSubmit() {
                setResponsePage(new RecursosPage(new Recurso()));
            }
        };
        form.add(buttonNovo);

        Button buttonSalvar = new Button("buttonSalvar") {
            @Override
            public void onSubmit() {
                recurso.setGrupo(usuarioLogado.getGrupo());
                recursoBusiness.save(recurso);
                info("Recurso Salvo");
                setResponsePage(new RecursosPage(new Recurso()));
            }
        };
        form.add(buttonSalvar);

        List<Recurso> listaRecurso = recursoBusiness.getAll(usuarioLogado.getGrupo());

        add(new ListView<Recurso>("listaRecurso", listaRecurso) {
            @Override
            protected void populateItem(ListItem<Recurso> item) {
                final Recurso recurso = item.getModelObject();
                item.add(new Label("nome", recurso.getNome()));
                item.add(new Label("ativo", recurso.getAtivo()));
                item.add(new Label("manutencao", recurso.getManutencao()));
                if(recurso.getGrupo() != null){
                    item.add(new Label("grupo", recurso.getGrupo().getNome()));
                }else{
                    item.add(new Label("grupo", ""));
                }

                Link linkEditar = new Link("linkEditar") {
                    @Override
                    public void onClick() {
                        setResponsePage(new RecursosPage(recurso));
                    }
                };
                item.add(linkEditar);


                AjaxLinkConfirm linkApagar = new AjaxLinkConfirm("linkApagar", "Tem Certeza?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        recursoBusiness.delete(recurso);
                        info("Recurso Deletado");
                    }
                };
                item.add(linkApagar);
            }
        });

    }

}
