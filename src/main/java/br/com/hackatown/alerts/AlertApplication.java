package br.com.hackatown.alerts;

import br.com.hackatown.alerts.controller.AlertController;
import br.com.hackatown.alerts.model.AlertRequest;
import br.com.hackatown.alerts.model.AlertResponse;
import br.com.hackatown.alerts.service.SmsAlertService;
import br.com.hackatown.alerts.service.TwilioSmsClient;
import br.com.hackatown.alerts.view.AlertConsoleView;

public class AlertApplication {
    public static void main(String[] args) {
        AlertController controller = new AlertController(
                new SmsAlertService(TwilioSmsClient.fromEnvironment()));
        AlertResponse response = controller.createFloodAlert(
                new AlertRequest("+5511999999999", "Franca, SP", "high risk"));
        new AlertConsoleView().render(response);
    }
}
