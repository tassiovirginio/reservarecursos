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

package br.reservarecursos.pages.componentes;

import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxLink;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;

/**
 * Created by tassio on 03/12/15.
 */
public abstract class AjaxLinkConfirm extends AjaxLink {

    private String msg;

    public AjaxLinkConfirm(String id, String msg) {
        super(id);
        this.msg = msg;
    }

    protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
    {
        super.updateAjaxAttributes( attributes );
        AjaxCallListener ajaxCallListener = new AjaxCallListener();
        ajaxCallListener.onPrecondition( "return confirm('" + msg + "');" );
        attributes.getAjaxCallListeners().add( ajaxCallListener );
    }


}
