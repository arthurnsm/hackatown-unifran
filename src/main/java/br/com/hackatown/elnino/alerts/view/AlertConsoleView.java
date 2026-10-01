package br.com.hackatown.elnino.alerts.view;

import br.com.hackatown.elnino.alerts.model.AlertResponse;

public class AlertConsoleView {
    public void render(AlertResponse response) {
        System.out.printf("Alert queued by Twilio%nRecipient: %s%nContent: %s%nMessage SID: %s%n",
                response.getPhoneNumber(), response.getMessage(), response.getMessageSid());
    }
}
