package br.com.hackatown.elnino.alerts.view;

import br.com.hackatown.elnino.alerts.model.AlertResponse;

public class AlertConsoleView {
    public void render(AlertResponse response) {
        System.out.printf("Alert sent through Telegram%nChat ID: %s%nContent: %s%nMessage ID: %s%n",
                response.getChatId(), response.getMessage(), response.getMessageId());
    }
}
