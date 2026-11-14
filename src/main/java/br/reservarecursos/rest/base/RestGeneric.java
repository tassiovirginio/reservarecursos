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
package br.reservarecursos.rest.base;

import br.reservarecursos.business.daos.util.BusinessGeneric;
import br.reservarecursos.entities.Usuario;
import org.apache.wicket.Session;
import org.apache.wicket.request.resource.IResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.wicketstuff.rest.contenthandling.json.webserialdeserial.JsonWebSerialDeserial;
import org.wicketstuff.rest.resource.AbstractRestResource;
import org.wicketstuff.rest.resource.MethodMappingInfo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tassio on 28/11/15.
 */
public class RestGeneric<T extends BusinessGeneric, W extends Serializable> extends AbstractRestResource<JsonWebSerialDeserial> {

    @Autowired
    protected T business;

    protected RestGeneric() {
        super(new JsonWebSerialDeserial(new GsonJodaObjectSerialDeserial()));
    }

    public List<W> getAll() {
        return business.listAll();
    }


    @Override
    protected void onBeforeMethodInvoked(MethodMappingInfo mappedMethod, IResource.Attributes attributes) {
        Usuario usuarioLogado = (Usuario) Session.get().getAttribute("usuario");

        if ((usuarioLogado == null)) {
            getCurrentWebResponse().sendError(401, "Acesso Negado");
        }

    }

}
