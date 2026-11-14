package br.reservarecursos.pages.modal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.markup.html.panel.Panel;

import java.io.Serializable;

/**
 * Adaptador para o ModalDialog do Wicket 11.
 *
 * O ModalWindow (usado até o Wicket 8) foi removido no Wicket 9. Esta classe
 * mantém a API antiga usada pelas telas (show/close/setPageCreator/
 * setWindowClosedCallback e os setters de layout, agora sem efeito) para
 * preservar o fluxo original sem reescrever cada chamada.
 *
 * Diferença relevante: o conteúdo do ModalDialog precisa ser um Panel cujo id
 * é ModalDialog.CONTENT_ID (não mais um Page dentro de um iframe).
 */
public abstract class ModalBase extends ModalDialog {

    public static final String CSS_CLASS_GRAY = "modal-gray";

    public interface PageCreator extends Serializable {
        Panel createContent();
    }

    public interface WindowClosedCallback extends Serializable {
        void onClose(AjaxRequestTarget target);
    }

    private PageCreator pageCreator;
    private WindowClosedCallback windowClosedCallback;

    public ModalBase(String id) {
        super(id);
    }

    public ModalBase setResizable(boolean value) {
        return this;
    }

    public ModalBase setAutoSize(boolean value) {
        return this;
    }

    public ModalBase setMinimalWidth(int value) {
        return this;
    }

    public ModalBase setMinimalHeight(int value) {
        return this;
    }

    public ModalBase setCssClassName(String value) {
        return this;
    }

    public void setPageCreator(PageCreator creator) {
        this.pageCreator = creator;
    }

    public void setWindowClosedCallback(WindowClosedCallback callback) {
        this.windowClosedCallback = callback;
    }

    public void show(AjaxRequestTarget target) {
        if (pageCreator != null) {
            setContent(pageCreator.createContent());
        }
        open(target);
    }

    @Override
    public ModalDialog close(AjaxRequestTarget target) {
        ModalDialog dialog = super.close(target);
        if (windowClosedCallback != null) {
            windowClosedCallback.onClose(target);
        }
        return dialog;
    }
}
