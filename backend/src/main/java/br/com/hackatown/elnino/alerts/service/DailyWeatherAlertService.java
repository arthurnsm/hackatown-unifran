package br.com.hackatown.elnino.alerts.service;

import br.com.hackatown.elnino.alerts.model.AlertRequest;
import br.com.hackatown.elnino.alerts.model.AlertResponse;
import br.com.hackatown.elnino.alerts.model.AlertSubscriber;
import core.hackatown.elnino.client.OpenMeteoClient;
import core.hackatown.elnino.model.ForecastDay;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Checks tomorrow's forecast and sends alerts only when a configured threshold is reached. */
public final class DailyWeatherAlertService {
    private final OpenMeteoClient weatherClient;
    private final TelegramAlertService telegram;
    private final AlertSubscriberRepository subscribers;
    private final double heavyRainMm;
    private final double extremeHeatC;

    public DailyWeatherAlertService(OpenMeteoClient weatherClient, TelegramAlertService telegram, AlertSubscriberRepository subscribers) {
        this.weatherClient = weatherClient;
        this.telegram = telegram;
        this.subscribers = subscribers;
        this.heavyRainMm = 30; this.extremeHeatC = 35;
    }

    public List<AlertResponse> checkTomorrow() throws IOException, InterruptedException {
        List<AlertResponse> sent = new ArrayList<>();
        for (AlertSubscriber subscriber : subscribers.findAll()) {
            ForecastDay forecast = weatherClient.fetchTomorrowForecast(subscriber.toLocation()); String place = subscriber.city() + ", " + subscriber.state();
            if (forecast.precipitationSum() >= heavyRainMm) sent.add(telegram.send(new AlertRequest(subscriber.chatId(), place, "Hoje terá chuvas fortes (" + forecast.precipitationSum() + " mm).")));
            if (forecast.meanTemperature() >= extremeHeatC) sent.add(telegram.send(new AlertRequest(subscriber.chatId(), place, "A temperatura média prevista é de " + forecast.meanTemperature() + " °C.")));
        }
        return List.copyOf(sent);
    }
}
