package br.reservarecursos.rest.dtos;

import org.joda.time.LocalDate;

import java.io.Serializable;

/**
 * Created by tassio on 29/11/15.
 */
public class BuscaReservas implements Serializable {

    private Integer horario;

    private LocalDate data;

    public Integer getHorario() {
        return horario;
    }

    public void setHorario(Integer horario) {
        this.horario = horario;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

}
