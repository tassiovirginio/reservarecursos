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

import br.reservarecursos.business.daos.ReservaDAO;
import br.reservarecursos.business.daos.util.BusinessGeneric;
import br.reservarecursos.entities.Ambiente;
import br.reservarecursos.entities.Horario;
import br.reservarecursos.entities.Reserva;
import br.reservarecursos.entities.Usuario;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hibernate.criterion.Restrictions.eq;

/**
 * Created by tassio on 12/01/15.
 */
@Component
@Transactional
public class ReservaBusiness extends BusinessGeneric<ReservaDAO, Reserva> {

    public List<Reserva> listaReservasFixa(Ambiente ambiente, Horario horario) {

        return dao.findByHQL(
                "select r from Reserva r where r.data is null" +
                        " and r.horario.id " + horario.getId() +
                        " and r.ambiente.id = " + ambiente.getId());
    }

    public List<Reserva> listaReservasNormais(LocalDate data) {
        return dao.findByCriteriaReturnList(
                eq("data", data)
        );
    }


    public List<Reserva> listaReservasGerenciaveis(Usuario usuario) {
        return dao.findByHQL(
                "select r from Reserva r where r.ambiente.gerenciavel = true and ambiente.gerenciador.id = " + usuario.getId());
    }

    public Reserva getReservaFixa(Ambiente ambiente, Horario horario, Integer diaSemana) {

        return dao.findByHQLUniqueResult(
                "select r from Reserva r where  r.data is null" +
                        " and r.diaSemana = " + diaSemana +
                        " and r.horario.id = " + horario.getId() +
                        " and r.ambiente.id = " + ambiente.getId());
    }


    public Reserva getReservaNormal(Ambiente ambiente, Horario horario, LocalDate data) {

        return dao.findByCriteriaReturnUniqueResult(
                eq("horario", horario),
                eq("ambiente", ambiente),
                eq("data", data)
        );

//        return dao.findByHQLUniqueResult(
//                "select r from Reserva r where" +
//                        " r.data = '" + data +
//                        " and r.horario.id = " + horario.getId() +
//                        " and r.ambiente.id = " + ambiente.getId());
    }


}
