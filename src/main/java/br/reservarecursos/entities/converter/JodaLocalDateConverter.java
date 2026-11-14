package br.reservarecursos.entities.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.joda.time.LocalDate;

@Converter(autoApply = false)
public class JodaLocalDateConverter implements AttributeConverter<LocalDate, java.sql.Date> {
    @Override
    public java.sql.Date convertToDatabaseColumn(LocalDate attribute) {
        return attribute == null ? null : java.sql.Date.valueOf(attribute.toString("yyyy-MM-dd"));
    }

    @Override
    public LocalDate convertToEntityAttribute(java.sql.Date dbData) {
        return dbData == null ? null : new LocalDate(dbData.toString());
    }
}
