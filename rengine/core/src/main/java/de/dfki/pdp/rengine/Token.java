package de.dfki.pdp.rengine;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class Token {
    public final long timestamp = System.currentTimeMillis();
    private PMap<String, Object> payload = HashTreePMap.empty();

    private Token(Map<String, Object> payload) {
        this.payload = this.payload.plusAll(payload);
    }

    public Token(Builder builder) {
        this.payload = builder.payload;
    }

    public Token() {
    }


    public Token add(String key, Object value) {
        var p = payload.plus(key, value);
        Token t = new Token(p);
        return t;
    }

    public Token addAll(Map<String, Object> values) {
        var p = payload;

        for(var entry : values.entrySet()) {
            p = p.plus(entry.getKey(), entry.getValue());
        }

        Token t = new Token(p);
        return t;
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(payload.get(key));
    }


    @Override
    public String toString() {
        return "Token{" +
                ", timestamp=" + timestamp +
                ", payload=" + payload.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining()) +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean payloadEquals(String id, Object object) {
        return Objects.equals(this.payload.get(id), object);
    }

    public static class Builder {
        private PMap<String, Object> payload = HashTreePMap.empty();

        public Builder() {

        }

        public Builder add(String key, Object value) {
            payload = payload.plus(key, value);
            return this;
        }

        public Token build() {
            return new Token(this);
        }
    }

}
