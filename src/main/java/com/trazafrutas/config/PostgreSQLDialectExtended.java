package com.trazafrutas.config;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.spi.TypeConfiguration;

public class PostgreSQLDialectExtended extends PostgreSQLDialect {

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);

        TypeConfiguration typeConfiguration = functionContributions.getTypeConfiguration();

        // Registrar función DATE_TRUNC
        functionContributions.getFunctionRegistry().registerPattern(
                "DATE_TRUNC",
                "date_trunc(?1, ?2)",
                typeConfiguration.getBasicTypeRegistry().resolve(StandardBasicTypes.TIMESTAMP)
        );

        // Registrar función para restar meses
        functionContributions.getFunctionRegistry().registerPattern(
                "SUBTRACT_MONTHS",
                "?1 - interval '?2 month'",
                typeConfiguration.getBasicTypeRegistry().resolve(StandardBasicTypes.TIMESTAMP)
        );

        // Registrar función YEAR
        functionContributions.getFunctionRegistry().registerPattern(
                "YEAR",
                "extract(year from ?1)",
                typeConfiguration.getBasicTypeRegistry().resolve(StandardBasicTypes.INTEGER)
        );

        // Registrar función MONTH
        functionContributions.getFunctionRegistry().registerPattern(
                "MONTH",
                "extract(month from ?1)",
                typeConfiguration.getBasicTypeRegistry().resolve(StandardBasicTypes.INTEGER)
        );

        // Registrar función CURRENT_DATE
        functionContributions.getFunctionRegistry().registerPattern(
                "CURRENT_DATE",
                "CURRENT_DATE",
                typeConfiguration.getBasicTypeRegistry().resolve(StandardBasicTypes.DATE)
        );
    }

    // Reemplazar el remapeo incorrecto de tipos ENUM a VarcharTypeDescriptor
    @Override
    public String getTableTypeString() {
        return "WITH (OIDS=FALSE)";
    }
}