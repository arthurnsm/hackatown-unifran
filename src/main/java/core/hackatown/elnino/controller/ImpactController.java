package br.com.hackatown.elnino.controller;

import br.com.hackatown.elnino.model.Metric;
import br.com.hackatown.elnino.service.ImpactService;
import br.com.hackatown.elnino.view.JsonView;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/** Controller do MVC: valida a requisição e coordena Model/Service e View. */
public final class ImpactController implements HttpHandler {
    private final ImpactService service;
    private final JsonView view;

    public ImpactController(ImpactService service, JsonView view) {
        this.service = service;
        this.view = view;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        view.prepareCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            view.noContent(exchange);
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            view.render(exchange, 405, Map.of("error", "Use o método GET."));
            return;
        }

        try {
            String selected = queryParameters(exchange).get("metric");
            view.render(exchange, 200, service.getImpacts(Metric.from(selected)));
        } catch (IllegalArgumentException exception) {
            view.render(exchange, 400, Map.of("error", exception.getMessage()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            view.render(exchange, 503, Map.of("error", "Consulta climática interrompida."));
        } catch (Exception exception) {
            System.err.println("Falha ao consultar dados climáticos: " + exception.getMessage());
            view.render(exchange, 502, Map.of("error", "Não foi possível consultar a Open-Meteo agora."));
        }
    }

    private Map<String, String> queryParameters(HttpExchange exchange) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isBlank()) return Map.of();
        return Arrays.stream(query.split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(
                        pair -> decode(pair[0]),
                        pair -> pair.length == 2 ? decode(pair[1]) : "",
                        (first, ignored) -> first
                ));
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
