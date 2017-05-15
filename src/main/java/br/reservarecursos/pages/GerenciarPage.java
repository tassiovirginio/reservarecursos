package br.reservarecursos.pages;

import br.reservarecursos.business.ReservaBusiness;
import br.reservarecursos.entities.Reserva;
import br.reservarecursos.pages.base.BasePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.Date;
import java.util.List;

/**
 * Created by tassio on 02/04/16.
 */
public class GerenciarPage extends BasePage{

    @SpringBean
    private ReservaBusiness reservaBusiness;

    private Date dateBusca;


    public GerenciarPage() {
        this(null);
    }

    public GerenciarPage(List<Reserva> reservaList) {

        if(reservaList == null){
            reservaList = reservaBusiness.listaReservasGerenciaveis(usuarioLogado);
        }

        add(new ListView<Reserva>("listaResaervas", reservaList) {
            @Override
            protected void populateItem(ListItem<Reserva> item) {
                final Reserva reserva = item.getModelObject();
                item.add(new Label("data", reserva.getData()));
                item.add(new Label("horario", reserva.getHorario().getDesc()));
                item.add(new Label("periodo", reserva.getPeriodo().getDataInicial() + " - " + reserva.getPeriodo().getDataFinal()));
                item.add(new Label("usuario", reserva.getUsuario().getNome()));
                item.add(new Label("ambiente", reserva.getAmbiente().getNome()));
                item.add(new Label("situacao", reserva.getAutorizado()));
                item.add(new Link("autorizar") {
                    @Override
                    public void onClick() {
                        reserva.setAutorizado(true);
                        reservaBusiness.save(reserva);
                        enviarEmail(reserva.getUsuario().getEmail(),"Reserva autorizada: " + reserva.getAmbiente().getNome(),
                                "Reserva autorizada: " + reserva.getAmbiente().getNome()
                                );
                    }
                });
                item.add(new Link("cancelar") {
                    @Override
                    public void onClick() {
                        reserva.setAutorizado(false);
                        reservaBusiness.save(reserva);
                    }
                });
            }
        });


    }
}
