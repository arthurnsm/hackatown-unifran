package br.com.hackatown.elnino.alerts.service;

import br.com.hackatown.elnino.alerts.model.AlertRequest;
import br.com.hackatown.elnino.alerts.model.AlertResponse;

public class SmsAlertService {
    private final TwilioSmsClient twilioSmsClient;

    public SmsAlertService(TwilioSmsClient twilioSmsClient) {
        this.twilioSmsClient = twilioSmsClient;
    }

    public AlertResponse prepareFloodRiskAlert(AlertRequest request) {
        validate(request);

        String message = String.format(
                "%s ALERT: flood risk in %s. Avoid flooded areas and seek a safe location.",
                request.getRiskLevel().toUpperCase(), request.getLocation());

        String phoneNumber = validatePhoneNumber(request.getPhoneNumber());
        String messageSid = twilioSmsClient.send(phoneNumber, message);
        return new AlertResponse(phoneNumber, message, messageSid);
    }

    private void validate(AlertRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Alert request is required");
        }
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            throw new IllegalArgumentException("Location is required");
        }
        if (request.getRiskLevel() == null || request.getRiskLevel().isBlank()) {
            throw new IllegalArgumentException("Risk level is required");
        }
    }

    private String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("^\\+55\\d{10,11}$")) {
            throw new IllegalArgumentException("Phone number must use the format +5511999999999");
        }
        return phoneNumber;
    }
}
