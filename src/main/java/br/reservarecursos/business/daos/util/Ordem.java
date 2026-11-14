package br.reservarecursos.business.daos.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;

import java.io.Serializable;

/**
 * Substituto do antigo org.hibernate.criterion.Order (removido no Hibernate 6).
 * Mantém o nome da propriedade porque DAOGeneric.findByHQL monta HQL com ele.
 */
public final class Ordem implements Serializable {

    private final String propriedade;
    private final boolean ascendente;

    private Ordem(String propriedade, boolean ascendente) {
        this.propriedade = propriedade;
        this.ascendente = ascendente;
    }

    public static Ordem asc(String propriedade) {
        return new Ordem(propriedade, true);
    }

    public static Ordem desc(String propriedade) {
        return new Ordem(propriedade, false);
    }

    public String getPropertyName() {
        return propriedade;
    }

    public jakarta.persistence.criteria.Order toOrder(CriteriaBuilder cb, Root<?> root) {
        return ascendente
                ? cb.asc(Filtros.path(root, propriedade))
                : cb.desc(Filtros.path(root, propriedade));
    }
}
