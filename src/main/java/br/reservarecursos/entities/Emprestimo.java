package br.reservarecursos.entities;

import javax.persistence.*;
import java.io.Serializable;

import org.joda.time.*;

/**
 * Created by tassio on 29/11/16.
 */
@Entity
public class Emprestimo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Usuario operador;

    @ManyToOne
    private Recurso recurso;

    private DateTime dataHoraInicio;

    private DateTime dataHoraFim;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Recurso getRecurso() {
        return recurso;
    }

    public void setRecurso(Recurso recurso) {
        this.recurso = recurso;
    }

    public DateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(DateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public DateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(DateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public Usuario getOperador() {
        return operador;
    }

    public void setOperador(Usuario operador) {
        this.operador = operador;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Emprestimo that = (Emprestimo) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
