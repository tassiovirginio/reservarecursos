package br.reservarecursos.pages.componentes;

import br.reservarecursos.entities.Reserva;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;

/**
 * Created by tassio on 25/02/16.
 */
public class LabelDay extends Label {

    private Boolean fixo;

    public LabelDay(String id, String label) {
        this(id, label, false);
    }

    public LabelDay(String id, String label, Boolean fixo) {
        super(id, label);
        this.fixo = fixo;
        if(fixo)add(new AttributeModifier("style", "color: red;"));
    }

    public LabelDay(String id, Reserva reserva) {
        super(id, reserva.getObs());
        this.fixo = reserva.getData() == null;
        if(fixo)add(new AttributeModifier("style", "color: red;"));
    }

}
