package br.com.hackatown.elnino.alerts.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import br.com.hackatown.elnino.alerts.model.AlertResponse;

/** Runs the forecast check once a day. */
public final class DailyWeatherAlertScheduler {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final DailyWeatherAlertService alertService;

    public DailyWeatherAlertScheduler(DailyWeatherAlertService alertService) {
        this.alertService = alertService;
    }

    public void start() {
        executor.scheduleAtFixedRate(this::checkSafely, 0, 1, TimeUnit.DAYS);
    }

    private void checkSafely() {
        try {
            var sentAlerts = alertService.checkTomorrow();
            System.out.println("Weather alert check completed. Alerts sent: " + sentAlerts.size());
        } catch (Exception exception) {
            System.err.println("Daily weather check failed: " + exception.getMessage());
        }
    }
}
