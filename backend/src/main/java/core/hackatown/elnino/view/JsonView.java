package core.hackatown.elnino.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/** View do MVC: converte os modelos de resposta em JSON HTTP. */
public final class JsonView {
    private final ObjectMapper mapper;

    public JsonView(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void prepareCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    public void noContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    public void render(HttpExchange exchange, int status, Object model) throws IOException {
        byte[] json = mapper.writeValueAsBytes(model);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, json.length);
        try (var output = exchange.getResponseBody()) {
            output.write(json);
        }
    }
}
