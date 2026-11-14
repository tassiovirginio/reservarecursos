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

import br.reservarecursos.business.daos.HorarioDAO;
import br.reservarecursos.business.daos.util.BusinessGeneric;
import br.reservarecursos.entities.Grupo;
import br.reservarecursos.entities.Horario;
import br.reservarecursos.entities.Periodo;
import br.reservarecursos.business.daos.util.Filtro;
import br.reservarecursos.business.daos.util.Ordem;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static br.reservarecursos.business.daos.util.Filtros.*;

/**
 * Created by tassio on 12/01/15.
 */
@Component
@Transactional
public class HorarioBusiness extends BusinessGeneric<HorarioDAO, Horario> {

    public List<Horario> getLista(Periodo periodo) {
        return dao.findByCriteria(Ordem.asc("horaInicio"), eq("periodo", periodo));
    }

    public List<Horario> getListaHorarios(Grupo grupo){
        if(grupo != null) {


            List<Ordem> listaOrder = new ArrayList<>();
            listaOrder.add(Ordem.asc("horaInicio"));

            List<Filtro> listCriteria = new ArrayList<>();
            listCriteria.add(eq("periodo.grupo", grupo));

            return dao.findByCriteria(listaOrder, listCriteria);

        }else{
            return new ArrayList<>();
        }
    }

}
