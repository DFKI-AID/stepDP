package de.dfki.rengine;

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
    public static final String TYPE_KEY = "TOPIC";
    public final long timestamp = System.currentTimeMillis();
    private PMap<String, Object> payload = HashTreePMap.empty();

    private Token(String topic, Map<String, Object> payload) {
//        if (payload.containsKey(TYPE_KEY)) {
//            throw new IllegalArgumentException("Using the key %s is disallowed".format(TYPE_KEY));
//        }

        this.payload = this.payload.plusAll(payload);
        this.payload = this.payload.plus(TYPE_KEY, topic);
    }

    public Token(Builder builder) {
        this.payload = builder.payload;
    }

    public Token(String type) {
        payload = payload.plus(TYPE_KEY, type);
    }

    public boolean topicIs(String topic) {
        return Objects.equals(topic, this.payload.get(TYPE_KEY));
    }

    public Token add(String key, Object value) {
        if (Objects.equals(key, TYPE_KEY)) {
            throw new IllegalArgumentException("Using the key %s is disallowed".format(TYPE_KEY));
        }

        var p = payload.plus(key, value);
        Token t = new Token((String) p.get(TYPE_KEY), p);
        return t;
    }

    public Token addAll(Map<String, Object> values) {
        var p = payload;

        for(var entry : values.entrySet()) {
            if (Objects.equals(entry.getKey(), TYPE_KEY)) {
                throw new IllegalArgumentException("Using the key %s is disallowed".format(TYPE_KEY));
            }
            p = p.plus(entry.getKey(), entry.getValue());
        }

        Token t = new Token((String) p.get(TYPE_KEY), p);
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

    public static Builder builder(String topic) {
        return new Builder(topic);
    }

    public boolean payloadEquals(String id, Object object) {
        return Objects.equals(this.payload.get(id), object);
    }

    public static class Builder {
        private PMap<String, Object> payload = HashTreePMap.empty();

        public Builder(String topic) {
            payload = payload.plus(TYPE_KEY, topic);
        }

        public Builder add(String key, Object value) {
            if (Objects.equals(key, TYPE_KEY)) {
                throw new IllegalArgumentException("Using the key %s is disallowed".format(TYPE_KEY));
            }
            payload = payload.plus(key, value);
            return this;
        }

        public Token build() {
            return new Token(this);
        }
    }

}
