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

package br.reservarecursos.business.daos.util;

import br.reservarecursos.entities.Emprestimo;
import br.reservarecursos.entities.Recurso;
import org.exolab.castor.types.DateTime;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

import static org.hibernate.criterion.Restrictions.*;

/**
 * Created by tassio on 29/11/15.
 */
@Transactional
public class BusinessGeneric<T extends DAOGeneric, Y extends Serializable> {

    @Autowired
    protected T dao;

    public int size() {
        return dao.size();
    }

    public void save(Y u) {
        dao.save(u);
    }

    public void delete(Y u) {
        dao.delete(u);
    }

    public void delete(Long id) {
        dao.delete(id);
    }

    public List<Y> find(Long first, Long count) {
        return dao.findByHQL(Order.desc("id"), first.intValue(), count.intValue());
    }

    public Y find(Long id) {
        return (Y) dao.findById(id);
    }

    public List<Y> listAll() {
        return dao.listAll();
    }

}
