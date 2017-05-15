package br.reservarecursos.pages.componentes;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.model.IModel;

import java.util.Date;

/**
 * Created by tassio on 13/12/15.
 */
public class DateFieldHTML5 extends DateTextField {


    public DateFieldHTML5(String id) {
        super(id,"yyyy-MM-dd");
    }

    private DateFieldHTML5(String id, String datePattern) {
        super(id,datePattern);
    }

    private DateFieldHTML5(String id, IModel<Date> model, String datePattern) {
        super(id,model,"yyyy-MM-dd");
    }

    public DateFieldHTML5(String id, IModel<Date> model) {
        super(id,model,"yyyy-MM-dd");
    }

    protected String getInputType() {
        return "date";
    }
}
