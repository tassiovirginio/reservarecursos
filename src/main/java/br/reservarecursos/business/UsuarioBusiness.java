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

package br.reservarecursos.business;

import br.reservarecursos.business.daos.UsuarioDAO;
import br.reservarecursos.business.daos.util.BusinessGeneric;
import br.reservarecursos.entities.Grupo;
import br.reservarecursos.entities.Usuario;
import br.reservarecursos.ldap.PersonRepo;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hibernate.criterion.Restrictions.eq;

/**
 * Created by tassio on 12/01/15.
 */
@Component
@Transactional
public class UsuarioBusiness extends BusinessGeneric<UsuarioDAO, Usuario> {

    @Autowired
    private Boolean ldapLigado;

    private PersonRepo personRepo;

    @Autowired
    private GrupoBusiness grupoBusiness;

    @Autowired
    private ApplicationContext applicationContext;

    public Usuario verificarLogin(Usuario usuario) {
        Usuario usuarioRetorno = null;

        if (ldapLigado) {
            personRepo = (PersonRepo)applicationContext.getBean("personRepo");
            Boolean logado = personRepo.login(usuario.getLogin(), usuario.getSenha());
            if (logado) {
                usuarioRetorno = getUsuario(usuario.getLogin());
            }
        }

        if(usuarioRetorno == null) {
            String senhaMD5 = "";
            try {
                String s = usuario.getSenha();
                MessageDigest m2 = MessageDigest.getInstance("MD5");
                m2.update(s.getBytes(), 0, s.length());
                senhaMD5 = (new BigInteger(1, m2.digest()).toString(16)).toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            usuarioRetorno = dao.findByCriteriaReturnUniqueResult(eq("login", usuario.getLogin()), eq("senha", senhaMD5));
        }

        return usuarioRetorno;
    }

    public Usuario verificarLogin(String login, String senha) {
        Usuario usuarioRetorno = null;

        if (ldapLigado) {
            personRepo = (PersonRepo)applicationContext.getBean("personRepo");
            Boolean logado = personRepo.login(login, senha);
            if (logado) {
                usuarioRetorno = getUsuario(login);
            }
        }

        if(usuarioRetorno == null) {
            String senhaMD5 = "";
            try {
                String s = senha;
                MessageDigest m2 = MessageDigest.getInstance("MD5");
                m2.update(s.getBytes(), 0, s.length());
                senhaMD5 = (new BigInteger(1, m2.digest()).toString(16)).toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            usuarioRetorno = dao.findByCriteriaReturnUniqueResult(eq("login", login), eq("senha", senhaMD5));
        }

        return usuarioRetorno;
    }

    public Usuario getUsuario(String login) {
        return dao.findByCriteriaReturnUniqueResult(eq("login", login));
    }

    public void importarUsuariosLDAP() {
        if (ldapLigado) {

            Set<String> listaGrupos = new HashSet<String>();

            List<Usuario> listaUsuarios = personRepo.getAllPersonNamesToUsurios();
            for (Usuario usuario : listaUsuarios) {

                if(usuario.getExtensionAttribute1() != null) {
                    listaGrupos.add(usuario.getExtensionAttribute1());
                    Grupo grupo = grupoBusiness.getGrupo(usuario.getExtensionAttribute1());
                    if(grupo == null){
                        grupo = new Grupo();
                        grupo.setNome(usuario.getExtensionAttribute1());
                        grupo.setLdap(usuario.getExtensionAttribute1());
                        grupoBusiness.save(grupo);
                    }


                }
            }

            for (Usuario usuario : listaUsuarios) {

                if(usuario.getExtensionAttribute1() != null) {
                    Usuario usuarioExiste = getUsuario(usuario.getLogin());

                    if (usuarioExiste != null) {
                        usuarioExiste.setAtivo(usuario.getAtivo());
                        usuarioExiste.setGrupo(grupoBusiness.getGrupo(usuario.getExtensionAttribute1()));
                        save(usuarioExiste);
                    } else {
                        usuario.setGrupo(grupoBusiness.getGrupo(usuario.getExtensionAttribute1()));
                        save(usuario);
                    }
                }
            }

        }
    }

    public List<Usuario> listOrdemAlfabetica(){
        return dao.findByCriteria(Order.asc("nome"));
    }


    public List<Usuario> buscarUsuariosPorGrupo(Grupo grupo){
        return dao.findByCriteria(Order.asc("nome"),
                eq("grupo.id", grupo.getId()));
    }

}
