package br.com.hackatown.elnino.alerts;

import br.com.hackatown.elnino.alerts.controller.AlertController;
import br.com.hackatown.elnino.alerts.model.AlertRequest;
import br.com.hackatown.elnino.alerts.model.AlertResponse;
import br.com.hackatown.elnino.alerts.service.SmsAlertService;
import br.com.hackatown.elnino.alerts.service.TwilioSmsClient;
import br.com.hackatown.elnino.alerts.view.AlertConsoleView;

public class AlertApplication {
    public static void main(String[] args) {
        AlertController controller = new AlertController(
                new SmsAlertService(TwilioSmsClient.fromEnvironment()));
        AlertResponse response = controller.createFloodAlert(
                new AlertRequest("+5511999999999", "Franca, SP", "high risk"));
        new AlertConsoleView().render(response);
    }
}
