package br.com.hackatown.elnino.alerts.service;

import br.com.hackatown.elnino.alerts.config.EnvironmentConfig;
import br.com.hackatown.elnino.alerts.model.AlertRequest;
import br.com.hackatown.elnino.alerts.model.AlertResponse;
import core.hackatown.elnino.client.OpenMeteoClient;
import core.hackatown.elnino.model.ForecastDay;
import core.hackatown.elnino.model.Location;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Checks tomorrow's forecast and sends alerts only when a configured threshold is reached. */
public final class DailyWeatherAlertService {
    private final OpenMeteoClient weatherClient;
    private final TelegramAlertService telegram;
    private final String chatId;
    private final Location location;
    private final double heavyRainMm;
    private final double extremeHeatC;

    public DailyWeatherAlertService(OpenMeteoClient weatherClient, TelegramAlertService telegram) {
        this.weatherClient = weatherClient;
        this.telegram = telegram;
        this.chatId = EnvironmentConfig.required("TELEGRAM_CHAT_ID");
        this.location = new Location("configured", EnvironmentConfig.valueOrDefault("ALERT_CITY", "Salvador"),
                EnvironmentConfig.valueOrDefault("ALERT_STATE", "BA"),
                EnvironmentConfig.doubleValueOrDefault("ALERT_LATITUDE", -12.9777),
                EnvironmentConfig.doubleValueOrDefault("ALERT_LONGITUDE", -38.5016));
        this.heavyRainMm = EnvironmentConfig.doubleValueOrDefault("HEAVY_RAIN_MM", 30);
        this.extremeHeatC = EnvironmentConfig.doubleValueOrDefault("EXTREME_HEAT_C", 20);
    }

    public List<AlertResponse> checkTomorrow() throws IOException, InterruptedException {
        ForecastDay forecast = weatherClient.fetchTomorrowForecast(location);
        String place = location.city() + ", " + location.state();
        List<AlertResponse> sent = new ArrayList<>();
        if (forecast.precipitationSum() >= heavyRainMm) {
            sent.add(telegram.send(new AlertRequest(chatId, place, "Hoje terá chuvas fortes (" + forecast.precipitationSum() + " mm).")));
        }
        if (forecast.maximumTemperature() >= extremeHeatC) {
            sent.add(telegram.send(new AlertRequest(chatId, place, "Hoje terá calor extremo (" + forecast.maximumTemperature() + " °C).")));
        }
        return List.copyOf(sent);
    }
}
