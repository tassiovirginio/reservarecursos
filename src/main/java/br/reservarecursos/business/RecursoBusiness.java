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

import br.reservarecursos.business.daos.RecursoDAO;
import br.reservarecursos.business.daos.util.BusinessGeneric;
import br.reservarecursos.entities.Grupo;
import br.reservarecursos.entities.Recurso;
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
public class RecursoBusiness extends BusinessGeneric<RecursoDAO, Recurso> {

    public List<Recurso> getAll(Grupo grupo) {
        return dao.findByCriteria(Order.asc("nome"),
                eq("grupo", grupo)
        );
    }

    public List<Recurso> listAtivos(Grupo grupo){
        return dao.findByCriteria(Order.asc("nome"),
                eq("ativo",Boolean.TRUE),
                eq("grupo",grupo),
                eq("manutencao",Boolean.FALSE)
        );
    }

    public List<Recurso> listManutencao(Grupo grupo){
        return dao.findByCriteria(Order.asc("nome"),
                eq("ativo",Boolean.TRUE),
                eq("grupo",grupo),
                eq("manutencao",Boolean.TRUE)
        );
    }

    public List<Recurso> listDesativados(Grupo grupo){
        return dao.findByCriteria(Order.asc("nome"),
                eq("ativo",Boolean.FALSE),
                eq("grupo",grupo),
                eq("manutencao",Boolean.FALSE)
        );
    }

}
