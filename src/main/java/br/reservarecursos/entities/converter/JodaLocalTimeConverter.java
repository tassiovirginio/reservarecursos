package br.reservarecursos.entities.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.joda.time.LocalTime;

@Converter(autoApply = false)
public class JodaLocalTimeConverter implements AttributeConverter<LocalTime, java.sql.Time> {
    @Override
    public java.sql.Time convertToDatabaseColumn(LocalTime attribute) {
        return attribute == null ? null : java.sql.Time.valueOf(attribute.toString("HH:mm:ss"));
    }

    @Override
    public LocalTime convertToEntityAttribute(java.sql.Time dbData) {
        return dbData == null ? null : new LocalTime(dbData.toString());
    }
}
