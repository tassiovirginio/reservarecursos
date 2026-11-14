package br.reservarecursos.ldap;

import br.reservarecursos.business.UsuarioBusiness;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by tassio on 19/03/18.
 */
public class Atualizador {

    @Autowired
    private Boolean ldapAtualizacaoAutomatica;

    @Autowired
    private UsuarioBusiness usuarioBusiness;

    public void atualizarUsuarios(){
        if(ldapAtualizacaoAutomatica) {
            System.out.println(new Date() + " - Atualizando Lista de Usuários LDAP....");
            usuarioBusiness.importarUsuariosLDAP();
        }
    }

}
