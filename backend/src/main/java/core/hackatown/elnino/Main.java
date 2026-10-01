package core.hackatown.elnino;

import core.hackatown.elnino.client.OpenMeteoClient;
import core.hackatown.elnino.controller.ImpactController;
import core.hackatown.elnino.service.ImpactCalculator;
import core.hackatown.elnino.service.ImpactService;
import core.hackatown.elnino.view.JsonView;
import br.com.hackatown.elnino.alerts.service.DailyWeatherAlertScheduler;
import br.com.hackatown.elnino.alerts.service.DailyWeatherAlertService;
import br.com.hackatown.elnino.alerts.service.TelegramAlertService;
import br.com.hackatown.elnino.alerts.service.TelegramBotClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        ObjectMapper mapper = new ObjectMapper();
        ImpactService service = new ImpactService(
                new OpenMeteoClient(mapper),
                new ImpactCalculator()
        );
        DailyWeatherAlertService dailyAlerts = new DailyWeatherAlertService(
                new OpenMeteoClient(mapper),
                new TelegramAlertService(TelegramBotClient.fromEnvironment()));
        new DailyWeatherAlertScheduler(dailyAlerts).start();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/impacts", new ImpactController(service, new JsonView(mapper)));
        server.createContext("/health", exchange -> {
            byte[] body = "{\"status\":\"ok\"}".getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("API El Niño rodando em http://localhost:" + port);
        System.out.println("Temperatura: /api/impacts?metric=temperature");
        System.out.println("Chuva:       /api/impacts?metric=rainfall");
        System.out.println("Alertas meteorológicos: verificação agendada a cada 10 segundos para teste.");
    }
}
