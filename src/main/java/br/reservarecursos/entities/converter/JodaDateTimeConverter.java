package br.reservarecursos.entities.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.joda.time.DateTime;

@Converter(autoApply = false)
public class JodaDateTimeConverter implements AttributeConverter<DateTime, java.sql.Timestamp> {
    @Override
    public java.sql.Timestamp convertToDatabaseColumn(DateTime attribute) {
        return attribute == null ? null : new java.sql.Timestamp(attribute.getMillis());
    }

    @Override
    public DateTime convertToEntityAttribute(java.sql.Timestamp dbData) {
        return dbData == null ? null : new DateTime(dbData.getTime());
    }
}
