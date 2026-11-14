package br.reservarecursos.pages;

import br.reservarecursos.business.ReservaBusiness;
import br.reservarecursos.entities.Reserva;
import br.reservarecursos.pages.base.BasePage;
import br.reservarecursos.pages.componentes.DateFieldHTML5;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tassio on 02/04/16.
 */
public class RelatoriosPage extends BasePage{

    @SpringBean
    private ReservaBusiness reservaBusiness;

    private Date dateBusca;


    public RelatoriosPage() {
        this(null);
    }

    public RelatoriosPage(List<Reserva> reservaList) {

        Form form = new Form("form"){
            @Override
            protected void onSubmit() {
                List<Reserva> reservaList = reservaBusiness.listaReservasNormais(new LocalDate(dateBusca));
                setResponsePage(new RelatoriosPage(reservaList));
            }
        };
        form.add(new DateFieldHTML5("dataBusca", new PropertyModel<Date>(this, "dateBusca")).setRequired(true));
        add(form);

        if(reservaList == null){
            reservaList = reservaBusiness.listaReservasNormais(new LocalDate());
        }

        List<Reserva> reservasGrupo = new ArrayList<>();
        for(Reserva r:reservaList){
            if(r.getPeriodo().getGrupo().equals(usuarioLogado.getGrupo())){
                reservasGrupo.add(r);
            }
        }


        add(new ListView<Reserva>("listaResaervas", reservasGrupo) {
            @Override
            protected void populateItem(ListItem<Reserva> item) {
                final Reserva reserva = item.getModelObject();
                item.add(new Label("id", reserva.getId()));
                item.add(new Label("data", reserva.getData()));
                item.add(new Label("horario", reserva.getHorario().getDesc()));
                item.add(new Label("periodo", reserva.getPeriodo().getDataInicial() + " - " + reserva.getPeriodo().getDataFinal()));
                item.add(new Label("usuario", reserva.getUsuario().getNome()));
                item.add(new Label("ambiente", reserva.getAmbiente().getNome()));
            }
        });


    }
}
