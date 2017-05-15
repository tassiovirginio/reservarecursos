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

import br.reservarecursos.business.UsuarioBusiness;
import br.reservarecursos.entities.Usuario;
import br.reservarecursos.pages.base.BasePage;
import br.reservarecursos.pages.componentes.AjaxLinkConfirm;
import com.googlecode.wicket.kendo.ui.form.TextField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class UsuariosPage extends BasePage {
    private static final long serialVersionUID = 1L;

    @SpringBean(name = "ldapLigado")
    private Boolean ldapLigado;

    @SpringBean
    private UsuarioBusiness usuarioBusiness;

    private String senha;

    public UsuariosPage() {
        this(new Usuario());
    }

    public UsuariosPage(final Usuario usuario) {


        Label labelLdap = null;
        if(ldapLigado) {
            labelLdap = new Label("labelLdap","LDAP: Ligado");
        }else{
            labelLdap = new Label("labelLdap","LDAP: Desligado");
        }
        add(labelLdap);

        Link linkImportar = new Link("linkImportar") {
            @Override
            public void onClick() {
                usuarioBusiness.importarUsuariosLDAP();
                error("Usuários Importados");
                setResponsePage(new UsuariosPage());
            }
        };
        add(linkImportar.setVisible(ldapLigado));


        Form form = new Form("formUsuario");
        form.add(new TextField<String>("nome", new PropertyModel<String>(usuario, "nome")).setRequired(true));
        form.add(new TextField<String>("login", new PropertyModel<String>(usuario, "login")).setRequired(true));
        form.add(new PasswordTextField("senha", new PropertyModel<String>(this, "senha")).setRequired(false));
        form.add(new EmailTextField("email", new PropertyModel<String>(usuario, "email")));
        form.add(new CheckBox("admin", new PropertyModel<Boolean>(usuario, "admin")));
        form.add(new CheckBox("adminEmprestimo", new PropertyModel<Boolean>(usuario, "adminEmprestimo")));
        form.add(new CheckBox("adminGeral", new PropertyModel<Boolean>(usuario, "adminGeral")));


        Button buttonNovo = new Button("buttonNovo") {
            @Override
            public void onSubmit() {
                setResponsePage(new UsuariosPage(new Usuario()));
            }
        };
        form.add(buttonNovo);

        Button buttonSalvar = new Button("buttonSalvar") {
            @Override
            public void onSubmit() {
                if (senha != null) {
                    usuario.setSenha(MD5(senha));
                }
                usuarioBusiness.save(usuario);
                info("Usuário Salvo");
                setResponsePage(new UsuariosPage(new Usuario()));
            }
        };
        form.add(buttonSalvar);


        add(form);

        List<Usuario> listaUsuarios = usuarioBusiness.listAll();

        add(new ListView<Usuario>("listaUsuarios", listaUsuarios) {
            @Override
            protected void populateItem(ListItem<Usuario> item) {
                final Usuario usuario1 = item.getModelObject();
                item.add(new Label("nome", usuario1.getNome()));
                item.add(new Label("admin", usuario1.getAdmin()));
                item.add(new Label("adminEmprestimo", usuario1.getAdminEmprestimo()));
                item.add(new Label("adminGeral", usuario1.getAdminGeral()));
                if(usuario1.getGrupo() != null){
                    item.add(new Label("grupo", usuario1.getGrupo().getNome()));
                }else{
                    item.add(new Label("grupo", ""));
                }
                item.add(new Label("login", usuario1.getLogin()));
                item.add(new Label("extensionAttribute10", usuario1.getExtensionAttribute10()));
                item.add(new Label("extensionAttribute1", usuario1.getExtensionAttribute1()));
                item.add(new Label("extensionAttribute2", usuario1.getExtensionAttribute2()));

                Link linkEditar = new Link("linkEditar") {
                    @Override
                    public void onClick() {
                        setResponsePage(new UsuariosPage(usuario1));
                    }
                };
                item.add(linkEditar);

                AjaxLinkConfirm linkApagar = new AjaxLinkConfirm("linkApagar", "Tem Certeza?") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        usuarioBusiness.delete(usuario1);
                        info("Usuário Deletado");
                    }
                };
                item.add(linkApagar);
            }
        });

    }

    private String MD5(String senha) {
        String senhaMD5 = "";
        try {
            MessageDigest m2 = MessageDigest.getInstance("MD5");
            m2.update(senha.getBytes(), 0, senha.length());
            senhaMD5 = (new BigInteger(1, m2.digest()).toString(16)).toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return senhaMD5;
    }


}
