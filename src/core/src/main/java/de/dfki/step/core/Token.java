package de.dfki.step.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A token stores arbitrary data as a map, whereby a value can be any data like a primitive, java object, another map
 * or token.
 */
public class Token {
    private static final Logger log = LoggerFactory.getLogger(Token.class);
    public final long timestamp = System.currentTimeMillis();
    private PMap<String, Object> payload = HashTreePMap.empty();

    public Token(Map<String, Object> payload) {
        this.payload = this.payload.plusAll(payload);
    }

    public Token(Builder builder) {
        this.payload = builder.payload;
    }

    public Token() {
    }

    public Token remove(String key) {
        var p = payload.minus(key);
        Token t = new Token(p);
        return t;
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

    public Optional<Object> get(String... keys) {
        return get(List.of(keys));
    }

    public Optional<Object> get(List<String> keys) {
        if (keys.isEmpty()) {
            return Optional.empty();
        }

        Optional<Object> firstObj = get(keys.get(0));
        if (!firstObj.isPresent()) {
            return Optional.empty();
        }

        Object obj = firstObj.get();

        for (int i = 1; i < keys.size(); i++) {
            if(obj instanceof Token) {
                obj = ((Token) obj).payload;
            }
            if (!(obj instanceof Map)) {
                return Optional.empty();
            }

            obj = ((Map<String, Object>) obj).get(keys.get(i));
            if (obj == null) {
                return Optional.empty();
            }
        }
        return Optional.of(obj);
    }

    public <T> Optional<T> get(Class<T> clazz, List<String> keys) {
        Optional<Object> obj = get(keys);
        if (!obj.isPresent()) {
            return Optional.empty();
        }
        if (!clazz.isAssignableFrom(obj.get().getClass())) {
            return Optional.empty();
        }
        return Optional.of((T) obj.get());
    }

    public <T> Optional<T> get(Class<T> clazz, String... keys) {
        return get(clazz, List.of(keys));
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
        if (payload.get(key) == null) {
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

    /**
     * Creates a token out of an json string. json objects are represented as maps, arrays as list and primitives
     * as java primitives.
     *
     * @param recJson
     * @return
     * @throws IOException
     */
    public static Token fromJson(String recJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(recJson);
        Iterator<String> iter = obj.fieldNames();
        Token.Builder builder = Token.builder();
        while (iter.hasNext()) {
            String fieldName = iter.next();
            Object value = parse(obj.get(fieldName));
            builder.add(fieldName, value);
        }

        Token token = builder.build();
        return token;
    }

    private static Object parse(JsonNode jsonNode) {
        if (jsonNode.isDouble()) {
            return jsonNode.doubleValue();
        } else if (jsonNode.isTextual()) {
            return jsonNode.textValue();
        } else if (jsonNode.isLong()) {
            return jsonNode.longValue();
        } else if (jsonNode.isInt()) {
            return jsonNode.intValue();
        } else if (jsonNode.isBoolean()) {
            return jsonNode.booleanValue();
        } else if (jsonNode.isArray()) {
            List<Object> values = new ArrayList<>();
            var iter = jsonNode.iterator();
            while (iter.hasNext()) {
                values.add(parse(iter.next()));
            }
            return values;
        } else if (jsonNode.isObject()) {
            Map<String, Object> values = new HashMap<>();
            Iterator<String> iter = jsonNode.fieldNames();
            while (iter.hasNext()) {
                String fieldName = iter.next();
                Object value = parse(jsonNode.get(fieldName));
                values.put(fieldName, value);
            }
            return Collections.unmodifiableMap(values);
        }
        throw new IllegalArgumentException("unhandled json node type: " + jsonNode);
    }


    /**
     * Extracts a field from all given tokens if available and merges them into one List.
     * If a field value is a collection, each element will be added individually.
     * <p>
     * e.g.
     * t1.origin = "kinect1"
     * t2.origin = ["hololens", "eye_tracker13"]
     * <p>
     * result.origin = ["kinect1", "hololens", "eye_tracker13"]
     *
     * @param fieldName
     * @param clazz     The type of the field. Use Object.class if not relevant.
     * @param tokens
     * @param <T>
     * @return The field value of all tokens if the field is set correctly
     */
    public static <T> List<T> mergeFields(String fieldName, Class<T> clazz, Collection<Token> tokens) {
        List<T> result = new ArrayList<>();
        for (Token t : tokens) {
            if (t.has(fieldName)) {
                Object obj = t.get(fieldName).get();
                if (Collection.class.isAssignableFrom(obj.getClass())) {
                    Collection<Object> objCol = (Collection<Object>) obj;
                    for (Object innerObj : objCol) {
                        if (!clazz.isAssignableFrom(innerObj.getClass())) {
                            log.debug("can't merge token field: type mismatch. expected={} got={}", clazz, innerObj.getClass());
                            continue;
                        }
                        result.add((T) innerObj);
                    }
                    continue;
                }

                if (clazz.isAssignableFrom(obj.getClass())) {
                    result.add((T) obj);
                    continue;
                }

                log.debug("can't merge token fields: not ");
            }
        }
        return result;
    }

    /**
     * Looks in the given tokens and returns the first field value that matches the given class if available.
     *
     * @param fieldName
     * @param clazz
     * @param tokens
     * @param <T>
     * @return
     */
    public static <T> Optional<T> getAny(String fieldName, Class<T> clazz, Collection<Token> tokens) {
        for (Token t : tokens) {
            if (t.has(fieldName)) {
                return t.get(fieldName, clazz);
            }
        }
        return Optional.empty();
    }


}
