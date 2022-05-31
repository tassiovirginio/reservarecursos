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

import br.reservarecursos.business.daos.EmprestimoDAO;
import br.reservarecursos.business.daos.util.BusinessGeneric;
import br.reservarecursos.entities.Emprestimo;
import br.reservarecursos.entities.Recurso;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.hibernate.criterion.Restrictions.*;

/**
 * Created by tassio on 12/01/15.
 */
@Component
@Transactional
public class EmprestimoBusiness extends BusinessGeneric<EmprestimoDAO, Emprestimo> {

    public Emprestimo emprestimoAtual(Recurso recurso){
        return dao.findByCriteriaReturnUniqueResult(
                le("dataHoraInicio",new DateTime()),
                eq("recurso.id",recurso.getId()),
                isNull("dataHoraFim")
        );
    }

    public List<Emprestimo> emprestimosLista(Recurso recurso, int tamanhoLista){

        List<Criterion> listaCriteria = new ArrayList<Criterion>();
        listaCriteria.add(eq("recurso.id",recurso.getId()));
        listaCriteria.add(isNotNull("dataHoraFim"));

        List<Order> listaOrdem = new ArrayList<Order>();
        listaOrdem.add(Order.desc("dataHoraFim"));

        return dao.findByCriteria(listaOrdem,
                listaCriteria,5);
    }

    public int emprestimosQuantidade(Recurso recurso){
        return dao.findByCriteriaReturnList(eq("recurso.id",recurso.getId())).size();
    }

}
