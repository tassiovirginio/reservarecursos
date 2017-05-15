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

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.wicketstuff.rest.contenthandling.IObjectSerialDeserial;

/**
 * Created by tassio on 29/11/15.
 */
public class GsonJodaObjectSerialDeserial implements IObjectSerialDeserial<String> {

    private final Gson gson;

    public GsonJodaObjectSerialDeserial() {
        this.gson = Converters.registerAll(new GsonBuilder()).create();
    }

    public String serializeObject(Object target, String mimeType) {
        return this.gson.toJson(target);
    }

    public <E> E deserializeObject(String source, Class<E> targetClass, String mimeType) {
        return this.gson.fromJson(source, targetClass);
    }

}
