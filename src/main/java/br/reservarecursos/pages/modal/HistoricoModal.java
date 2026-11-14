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
package br.reservarecursos.pages.modal;

import br.reservarecursos.entities.Recurso;
import br.reservarecursos.entities.Usuario;
import br.reservarecursos.pages.MenuRecursosPage;
import br.reservarecursos.pages.base.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

/**
 * Created by tassio on 03/12/15.
 */
public class HistoricoModal extends ModalWindow {

    public HistoricoModal(String id, final Recurso recurso, final BasePage page) {
        super(id);

        setResizable(true);
        setAutoSize(true);
        setMinimalWidth(500);
        setMinimalHeight(310);
        setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        setPageCreator(new PageCreator() {
            @Override
            public Page createPage() {
                return new HistoricoModalPage(recurso, HistoricoModal.this);
            }
        });
        setWindowClosedCallback(new WindowClosedCallback() {
            @Override
            public void onClose(AjaxRequestTarget target) {
                setResponsePage(new MenuRecursosPage());
            }
        });

    }
}
