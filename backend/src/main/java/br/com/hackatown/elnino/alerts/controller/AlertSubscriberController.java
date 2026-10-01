package br.com.hackatown.elnino.alerts.controller;
import br.com.hackatown.elnino.alerts.model.AlertSubscriber;
import br.com.hackatown.elnino.alerts.service.AlertSubscriberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.hackatown.elnino.view.JsonView;
import java.io.IOException;
import java.util.Map;
public final class AlertSubscriberController implements HttpHandler {
    private final AlertSubscriberRepository repository; private final ObjectMapper mapper; private final JsonView view;
    public AlertSubscriberController(AlertSubscriberRepository repository, ObjectMapper mapper, JsonView view) { this.repository = repository; this.mapper = mapper; this.view = view; }
    public void handle(HttpExchange exchange) throws IOException {
        view.prepareCors(exchange); if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { view.noContent(exchange); return; }
        try { if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) view.render(exchange, 200, repository.findAll());
        else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) view.render(exchange, 201, repository.save(mapper.readValue(exchange.getRequestBody(), AlertSubscriber.class)));
        else view.render(exchange, 405, Map.of("error", "Use GET or POST."));
        } catch (IllegalArgumentException exception) { view.render(exchange, 400, Map.of("error", exception.getMessage())); }
    }
}
