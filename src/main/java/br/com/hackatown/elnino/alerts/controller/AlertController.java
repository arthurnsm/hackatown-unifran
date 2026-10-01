package br.com.hackatown.elnino.alerts.controller;

import br.com.hackatown.elnino.alerts.model.AlertRequest;
import br.com.hackatown.elnino.alerts.model.AlertResponse;
import br.com.hackatown.elnino.alerts.service.SmsAlertService;
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
