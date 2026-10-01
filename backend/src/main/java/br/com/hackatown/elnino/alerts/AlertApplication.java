package br.com.hackatown.elnino.alerts;

import br.com.hackatown.elnino.alerts.config.EnvironmentConfig;
import br.com.hackatown.elnino.alerts.controller.AlertController;
import br.com.hackatown.elnino.alerts.model.AlertRequest;
import br.com.hackatown.elnino.alerts.model.AlertResponse;
import br.com.hackatown.elnino.alerts.service.TelegramAlertService;
import br.com.hackatown.elnino.alerts.service.TelegramBotClient;
import br.com.hackatown.elnino.alerts.view.AlertConsoleView;

public class AlertApplication {
    public static void main(String[] args) {
        AlertController controller = new AlertController(
                new TelegramAlertService(TelegramBotClient.fromEnvironment()));
        AlertResponse response = controller.createFloodAlert(
                new AlertRequest(
                        EnvironmentConfig.required("TELEGRAM_CHAT_ID"),
                        "Franca, SP",
                        "high risk"));
        new AlertConsoleView().render(response);
    }
}
