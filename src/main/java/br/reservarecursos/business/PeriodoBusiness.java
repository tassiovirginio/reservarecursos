/**
 * Copyright 2015 - Tássio Guerreiro Antunes Virgínio
 * <p>
 * Este arquivo é parte do programa Reserva de Recursos
 * <p>
 * O Reserva de Recursos é um software livre; você pode redistribui-lo e/ou modifica-lo
 * dentro dos termos da Licença Pública Geral GNU como publicada pela
 * Fundação do Software Livre (FSF); na versão 2 da Licença.
 * <p>
 * Este programa é distribuido na esperança que possa ser util, mas SEM
 * NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO a qualquer
 * MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU
 * para maiores detalhes.
 * <p>
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU, sob o
 * título "licensa_uso.htm", junto com este programa, se não, escreva para a
 * Fundação do Software Livre(FSF) Inc., 51 Franklin St, Fifth Floor,
 */

package br.reservarecursos.business;

import br.reservarecursos.business.daos.PeriodoDAO;
import br.reservarecursos.business.daos.util.BusinessGeneric;
import br.reservarecursos.entities.Grupo;
import br.reservarecursos.entities.Periodo;
import org.hibernate.criterion.Order;
import org.joda.time.LocalDate;
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
public class PeriodoBusiness extends BusinessGeneric<PeriodoDAO, Periodo> {

    public Periodo getPeriodoAtual(Grupo grupo) {
        if(grupo != null) {
            return dao.findByCriteriaReturnUniqueResult(
                    and(
                            le("dataInicial", LocalDate.now()), ge("dataFinal", LocalDate.now())
                    ), eq("grupo.id", grupo.getId())
            );
        }else{
            return null;
        }
    }

    public List<Periodo> getPeriodos(Grupo grupo) {
        if(grupo != null) {
            return dao.findByCriteria(Order.asc("obs"),
                    eq("grupo.id", grupo.getId())
            );
        }else{
            return null;
        }
    }

    public void save(Periodo u) {

        try {
            List<Periodo> lista1 = dao.findByCriteriaReturnList(and(not(eq("id",u.getId())),le("dataInicial", u.getDataInicial()), ge("dataFinal", u.getDataInicial())));
            List<Periodo> lista2 = dao.findByCriteriaReturnList(and(not(eq("id",u.getId())),le("dataInicial", u.getDataFinal()), ge("dataFinal", u.getDataFinal())));

            if (lista1.size() == 0 || lista2.size() == 0) {
                super.save(u);
            } else {
                throw new RuntimeException("Não pode salvar Periodo com intersecção com outros periodos.");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
