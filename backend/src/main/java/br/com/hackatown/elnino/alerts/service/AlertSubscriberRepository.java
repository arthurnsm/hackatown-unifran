package br.com.hackatown.elnino.alerts.service;
import br.com.hackatown.elnino.alerts.model.AlertSubscriber;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
public final class AlertSubscriberRepository {
    private final ObjectMapper mapper; private final Path file;
    public AlertSubscriberRepository(ObjectMapper mapper) { this.mapper = mapper; this.file = Path.of("data", "alert-subscribers.json"); }
    public synchronized List<AlertSubscriber> findAll() throws IOException {
        return Files.exists(file) ? List.copyOf(mapper.readValue(file.toFile(), new TypeReference<List<AlertSubscriber>>() { })) : List.of();
    }
    public synchronized AlertSubscriber save(AlertSubscriber value) throws IOException {
        if (value == null || value.chatId() == null || value.chatId().isBlank() || value.city() == null || value.city().isBlank() || value.state() == null || value.state().isBlank()) throw new IllegalArgumentException("chatId, city and state are required");
        if (value.latitude() < -90 || value.latitude() > 90 || value.longitude() < -180 || value.longitude() > 180) throw new IllegalArgumentException("invalid latitude or longitude");
        List<AlertSubscriber> values = new ArrayList<>(findAll()); values.removeIf(item -> item.chatId().equals(value.chatId())); values.add(value);
        Files.createDirectories(file.getParent()); mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), values); return value;
    }
}
