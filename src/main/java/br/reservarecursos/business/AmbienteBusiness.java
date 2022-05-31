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

import br.reservarecursos.business.daos.AmbienteDAO;
import br.reservarecursos.business.daos.util.BusinessGeneric;
import br.reservarecursos.entities.Ambiente;
import br.reservarecursos.entities.Grupo;
import br.reservarecursos.entities.Usuario;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hibernate.criterion.Restrictions.eq;

/**
 * Created by tassio on 12/01/15.
 */
@Component
@Transactional
public class AmbienteBusiness extends BusinessGeneric<AmbienteDAO, Ambiente> {

    @Override
    public List<Ambiente> listAll() {
        new Exception("use outr método");
        return null;
    }

    public List<Ambiente> getAll(Grupo grupo) {
        return dao.findByCriteria(Order.asc("descricao"),
                eq("grupo", grupo)
        );
    }

    public List<Ambiente> getListaAmbientesAtivos(Grupo grupo) {
        return dao.findByCriteria(Order.asc("descricao"),
                eq("grupo", grupo),
                eq("ativo", Boolean.TRUE)
        );
    }

    public List<Ambiente> getListaAmbientesAtivosGerenciaveis(Grupo grupo) {
        return dao.findByCriteria(Order.asc("descricao"),
                eq("ativo", Boolean.TRUE),
                eq("grupo", grupo),
                eq("gerenciavel", Boolean.TRUE));
    }

    public List<Ambiente> getListaAmbientesAtivosGerenciaveis(Usuario usuario, Grupo grupo) {
        return dao.findByCriteria(Order.asc("descricao"),
                eq("grupo", grupo),
                eq("ativo", Boolean.TRUE),
                eq("gerenciador.id", usuario.getId()), eq("gerenciavel", Boolean.TRUE));
    }

    public List<Ambiente> getListaAmbientesAtivosNaoGerenciaveis(Grupo grupo) {
        return dao.findByCriteria(Order.asc("descricao"),
                eq("grupo", grupo),
                eq("ativo", Boolean.TRUE),
                eq("gerenciavel", Boolean.FALSE));
    }

}
