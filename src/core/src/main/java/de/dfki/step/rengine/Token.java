package de.dfki.step.rengine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.io.IOException;
import java.util.*;
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
        if (value == null) {
            throw new IllegalArgumentException("null is not allowed as value in a token");
        }
        var p = payload.plus(key, value);
        Token t = new Token(p);
        return t;
    }

    public Token addAll(Map<String, Object> values) {
        var p = payload;

        for (var entry : values.entrySet()) {
            p = p.plus(entry.getKey(), entry.getValue());
        }

        Token t = new Token(p);
        return t;
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(payload.get(key));
    }


    public <T> Optional<T> get(String key, Class<T> clazz) {
        if (!has(key)) {
            return Optional.empty();
        }
        Object obj = payload.get(key);
        if (!clazz.isAssignableFrom(obj.getClass())) {
            return Optional.empty();
        }

        return Optional.of((T) obj);
    }

    public boolean has(String key) {
        return payload.get(key) != null;
    }

    public <T> boolean has(String key, Class<T> clazz) {
        if(payload.get(key) == null) {
            return false;
        }
        return clazz.isAssignableFrom(payload.get(key).getClass());
    }

    public static Token Empty = new Token();


    @Override
    public String toString() {
        return "Token{" +
                ", timestamp=" + timestamp +
                ", payload={ " + payload.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue() + " ")
                .collect(Collectors.joining()) +
                "}}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean payloadEquals(String id, Object object) {
        return Objects.equals(this.payload.get(id), object);
    }

    public boolean payloadEqualsOneOf(String id, Object... objects) {
        if (!this.payload.containsKey(id)) {
            return false;
        }
        for (Object obj : objects) {
            if (Objects.equals(obj, payload.get(id))) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> getPayload() {
        return payload;
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

    public static Token fromJson(String recJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(recJson);
        Iterator<String> iter = obj.fieldNames();
        Token.Builder builder = Token.builder();
        while(iter.hasNext()) {
            String fieldName = iter.next();
            Object value = parse(obj.get(fieldName));
            builder.add(fieldName, value);
        }

        Token token = builder.build();
        return token;
    }

    private static Object parse(JsonNode jsonNode) {
        if(jsonNode.isDouble()) {
            return jsonNode.doubleValue();
        } else if(jsonNode.isTextual()) {
            return jsonNode.textValue();
        } else if(jsonNode.isLong()) {
            return jsonNode.longValue();
        } else if(jsonNode.isInt()) {
            return jsonNode.intValue();
        } else if(jsonNode.isBoolean()) {
            return jsonNode.booleanValue();
        } else if(jsonNode.isArray()) {
            List<Object> values = new ArrayList<>();
            var iter = jsonNode.iterator();
            while(iter.hasNext()) {
                values.add(parse(iter.next()));
            }
            return values;
        } else if (jsonNode.isObject()) {
            Map<String, Object> values = new HashMap<>();
            Iterator<String> iter = jsonNode.fieldNames();
            while(iter.hasNext()) {
                String fieldName = iter.next();
                Object value = parse(jsonNode.get(fieldName));
                values.put(fieldName, value);
            }
            return Collections.unmodifiableMap(values);
        }
        throw new IllegalArgumentException("unhandled json node type: " +jsonNode);
    }

}
