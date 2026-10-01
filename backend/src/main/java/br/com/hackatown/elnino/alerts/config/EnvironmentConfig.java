package br.com.hackatown.elnino.alerts.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Loads local development values from .env while giving priority to system environment variables. */
public final class EnvironmentConfig {
    private static final Map<String, String> DOT_ENV_VALUES = loadDotEnv();

    private EnvironmentConfig() {
    }

    public static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = DOT_ENV_VALUES.get(name);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configuration value " + name + " is required.");
        }
        return value;
    }

    private static Map<String, String> loadDotEnv() {
        Path dotEnv = Path.of(".env");
        if (!Files.isRegularFile(dotEnv)) {
            return Map.of();
        }

        try {
            Map<String, String> values = new HashMap<>();
            for (String line : Files.readAllLines(dotEnv)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] entry = trimmed.split("=", 2);
                if (entry.length == 2) {
                    values.put(entry[0].trim(), entry[1].trim());
                }
            }
            return Map.copyOf(values);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read the local .env file.", exception);
        }
    }
}
