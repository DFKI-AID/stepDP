package de.dfki.dialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class Intent {
    private final String intent;
    private final Map<String, Object> payload;

    public Intent(String intent, Map<String, Object> payload) {
        this.intent = intent;
        this.payload = new HashMap<>();
        this.payload.putAll(payload);
    }

    public String getIntent() {
        return intent;
    }

    public Optional<Object> getPayload(String key) {
        return Optional.ofNullable(payload.get(key));
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public boolean is(String intent) {
        return Objects.equals(this.intent, intent);
    }

    public String toString() {
        return "Intent{" + intent + "}" + payload.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining());
    }
}
