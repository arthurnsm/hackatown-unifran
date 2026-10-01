package br.com.hackatown.elnino.alerts.controller;

import br.com.hackatown.elnino.alerts.model.AlertRequest;
import br.com.hackatown.elnino.alerts.model.AlertResponse;
import br.com.hackatown.elnino.alerts.service.TelegramAlertService;
import java.util.Objects;

public class AlertController {
    private final TelegramAlertService telegramAlertService;

    public AlertController(TelegramAlertService telegramAlertService) {
        this.telegramAlertService = Objects.requireNonNull(telegramAlertService);
    }

    public AlertResponse createFloodAlert(AlertRequest request) {
        return telegramAlertService.prepareFloodRiskAlert(request);
    }
}
