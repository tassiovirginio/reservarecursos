package br.reservarecursos.business.daos.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.io.Serializable;

/**
 * Substituto do antigo org.hibernate.criterion.Criterion (removido no Hibernate 6).
 * Um filtro é uma fábrica de Predicate avaliada com o CriteriaBuilder da sessão.
 */
@FunctionalInterface
public interface Filtro extends Serializable {
    Predicate toPredicate(CriteriaBuilder cb, Root<?> root);
}
