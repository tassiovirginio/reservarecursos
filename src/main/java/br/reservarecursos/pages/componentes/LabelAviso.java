package br.reservarecursos.pages.componentes;

import br.reservarecursos.entities.Reserva;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Created by tassio on 25/02/16.
 */
public class LabelAviso extends Label {


    public LabelAviso(String id, Reserva reserva) {
        super(id);

        if(reserva != null && reserva.getAmbiente() != null && reserva.getAmbiente().getGerenciavel()) {
            if (reserva.getAutorizado()) {
                setDefaultModel((IModel)(new Model("Autorizado")));
                add(new AttributeModifier("style", "color: green;"));
            } else {
                setDefaultModel((IModel)(new Model("Aguardando")));
                add(new AttributeModifier("style", "color: red;"));
            }
        }else{
            setDefaultModel((IModel)(new Model("")));
        }
    }

}
