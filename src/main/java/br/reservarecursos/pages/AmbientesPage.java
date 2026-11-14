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
import br.reservarecursos.business.UsuarioBusiness;
import br.reservarecursos.entities.Ambiente;
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

public class AmbientesPage extends BasePage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private AmbienteBusiness ambienteBusiness;

    @SpringBean
    private UsuarioBusiness usuarioBusiness;

    public AmbientesPage() {
        this(new Ambiente());
    }

    public AmbientesPage(final Ambiente ambiente) {

        List<Usuario> listaUsuario = usuarioBusiness.buscarUsuariosPorGrupo(usuarioLogado.getGrupo());

        Form form = new Form("form");
        form.add(new TextField<String>("nome", new PropertyModel<String>(ambiente, "nome")).setRequired(true));
        form.add(new TextField<String>("dscricao", new PropertyModel<String>(ambiente, "descricao")).setRequired(true));
        form.add(new CheckBox("ativo", new PropertyModel<Boolean>(ambiente, "ativo")));
        form.add(new CheckBox("gerenciavel", new PropertyModel<Boolean>(ambiente, "gerenciavel")));

        ChoiceRenderer choiceRenderer = new ChoiceRenderer("nome", "id");
        form.add(new DropDownChoice<Usuario>("gerenciador",new PropertyModel<Usuario>(ambiente,"gerenciador")
                ,listaUsuario,choiceRenderer).setRequired(false));

        add(form);

        Button buttonNovo = new Button("buttonNovo") {
            @Override
            public void onSubmit() {
                setResponsePage(new AmbientesPage(new Ambiente()));
            }
        };
        form.add(buttonNovo);

        Button buttonSalvar = new Button("buttonSalvar") {
            @Override
            public void onSubmit() {
                ambiente.setGrupo(usuarioLogado.getGrupo());
                ambienteBusiness.save(ambiente);
                info("Ambiente Salvo");
                setResponsePage(new AmbientesPage(new Ambiente()));
            }
        };
        form.add(buttonSalvar);

        List<Ambiente> listaAmbiente = ambienteBusiness.getAll(usuarioLogado.getGrupo());

        add(new ListView<Ambiente>("listaAmbiente", listaAmbiente) {
            @Override
            protected void populateItem(ListItem<Ambiente> item) {
                final Ambiente ambiente = item.getModelObject();
                item.add(new Label("nome", ambiente.getNome()));
                item.add(new Label("descricao", ambiente.getDescricao()));
                item.add(new Label("ativo", ambiente.getAtivo()));
                item.add(new Label("gerenciavel", ambiente.getGerenciavel()));

                if(ambiente.getGerenciador() != null) {
                    item.add(new Label("gerenciador", ambiente.getGerenciador().getNome()));
                }else{
                    item.add(new Label("gerenciador", ""));
                }

                if(ambiente.getGrupo() != null){
                    item.add(new Label("grupo", ambiente.getGrupo().getNome()));
                }else{
                    item.add(new Label("grupo", ""));
                }

                Link linkEditar = new Link("linkEditar") {
                    @Override
                    public void onClick() {
                        setResponsePage(new AmbientesPage(ambiente));
                    }
                };
                item.add(linkEditar);


                AjaxLinkConfirm linkApagar = new AjaxLinkConfirm("linkApagar", "Tem Certeza?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        ambienteBusiness.delete(ambiente);
                        info("Ambiente Deletado");
                        setResponsePage(new AmbientesPage(ambiente));
                    }
                };
                item.add(linkApagar);
            }
        });

    }

}
