package br.com.hackatown.elnino.model;

import java.util.Locale;

public enum Metric {
    TEMPERATURE("°C", "Anomalia de temperatura média"),
    RAINFALL("%", "Variação da precipitação acumulada");

    private final String unit;
    private final String description;

    Metric(String unit, String description) {
        this.unit = unit;
        this.description = description;
    }

    public String unit() {
        return unit;
    }

    public String description() {
        return description;
    }

    public static Metric from(String value) {
        if (value == null || value.isBlank()) return TEMPERATURE;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Métrica inválida. Use 'temperature' ou 'rainfall'.");
        }
    }
}
