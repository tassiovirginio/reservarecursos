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

import br.reservarecursos.business.GrupoBusiness;
import br.reservarecursos.business.RecursoBusiness;
import br.reservarecursos.business.UsuarioBusiness;
import br.reservarecursos.entities.Grupo;
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

import java.util.ArrayList;
import java.util.List;

public class GrupoPage extends BasePage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private GrupoBusiness grupoBusiness;

    @SpringBean
    private UsuarioBusiness usuarioBusiness;

    public GrupoPage() {
        this(new Grupo());
    }

    public GrupoPage(final Grupo grupo) {

        Form form = new Form("form");
        form.add(new TextField<String>("nome", new PropertyModel<String>(grupo, "nome")).setRequired(true));
        form.add(new TextField<String>("ldap", new PropertyModel<String>(grupo, "ldap")));
        form.add(new CheckBox("ativo", new PropertyModel<Boolean>(grupo, "ativo")));


        List<Usuario> usuarioList = usuarioBusiness.buscarUsuariosPorGrupo(grupo);

        final DropDownChoice<Usuario> ddcUsuario = new DropDownChoice<Usuario>("adminGrupo",new PropertyModel(grupo, "administrador"),usuarioList,new ChoiceRenderer<>( "login", "id" ));
        form.add(ddcUsuario);

        add(form);

        Button buttonNovo = new Button("buttonNovo") {
            @Override
            public void onSubmit() {
                setResponsePage(new GrupoPage(new Grupo()));
            }
        };
        form.add(buttonNovo);

        Button buttonSalvar = new Button("buttonSalvar") {
            @Override
            public void onSubmit() {
                grupoBusiness.save(grupo);
                info("Grupo Salvo");
                setResponsePage(new GrupoPage(new Grupo()));
            }
        };
        form.add(buttonSalvar);

        List<Grupo> grupoList = grupoBusiness.listAll();

        add(new ListView<Grupo>("listaRecurso", grupoList) {
            @Override
            protected void populateItem(ListItem<Grupo> item) {
                final Grupo grupo = item.getModelObject();
                item.add(new Label("nome", grupo.getNome()));
                item.add(new Label("ldap", grupo.getLdap()));
                item.add(new Label("ativo", grupo.getAtivo()));
                if(grupo.getAdministrador() != null) {
                    item.add(new Label("admin", grupo.getAdministrador().getNome()));
                }else{
                    item.add(new Label("admin", ""));
                }

                Link linkEditar = new Link("linkEditar") {
                    @Override
                    public void onClick() {
                        setResponsePage(new GrupoPage(grupo));
                    }
                };
                item.add(linkEditar);


                AjaxLinkConfirm linkApagar = new AjaxLinkConfirm("linkApagar", "Tem Certeza?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        grupoBusiness.delete(grupo);
                        info("Grupo Deletado");
                    }
                };
                item.add(linkApagar);
            }
        });

    }

}
