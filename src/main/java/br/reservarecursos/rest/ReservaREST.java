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
package br.reservarecursos.rest;

import br.reservarecursos.business.ReservaBusiness;
import br.reservarecursos.entities.Reserva;
import br.reservarecursos.rest.base.RestGeneric;
import org.springframework.stereotype.Component;
import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.annotations.parameters.RequestBody;
import org.wicketstuff.rest.utils.http.HttpMethod;

import java.util.List;

/**
 * Created by tassio on 28/11/15.
 */
@Component
@ResourcePath("rest/reservas")
public class ReservaREST extends RestGeneric<ReservaBusiness, Reserva> {

    @MethodMapping("/")
    public List<Reserva> getAll() {
        return super.getAll();
    }

    @MethodMapping(value = "/{id}", httpMethod = HttpMethod.GET)
    public Reserva get(long id) {
        return business.find(id);
    }

    @MethodMapping(value = "/{id}", httpMethod = HttpMethod.DELETE)
    public void delete(long id) {
        business.delete(id);
    }

    @MethodMapping(value = "/", httpMethod = HttpMethod.POST)
    public Reserva create(@RequestBody Reserva obj) {
        business.save(obj);
        return obj;
    }

    @MethodMapping(value = "/busca", httpMethod = HttpMethod.POST)
    public List<Reserva> busca(@RequestBody Reserva obj) {
        System.out.println(obj.getHorario());
        return super.getAll();
    }
}
