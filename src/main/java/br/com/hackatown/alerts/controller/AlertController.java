package br.com.hackatown.alerts.controller;

import br.com.hackatown.alerts.model.AlertRequest;
import br.com.hackatown.alerts.model.AlertResponse;
import br.com.hackatown.alerts.service.SmsAlertService;
import java.util.Objects;

public class AlertController {
    private final SmsAlertService smsAlertService;

    public AlertController(SmsAlertService smsAlertService) {
        this.smsAlertService = Objects.requireNonNull(smsAlertService);
    }

    public AlertResponse createFloodAlert(AlertRequest request) {
        return smsAlertService.prepareFloodRiskAlert(request);
    }
}
