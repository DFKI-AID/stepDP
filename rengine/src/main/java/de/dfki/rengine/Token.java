package de.dfki.rengine;

import java.util.Objects;

/**
 */
public class Token<T> {
    public final String topic;
    public final long timestamp = System.currentTimeMillis();
    public final T payload;
    //TODO confidence

    public Token(String topic, T payload) {
        this.topic = topic;
        this.payload = payload;
    }

    public boolean topicIs(String topic) {
        return Objects.equals(topic, this.topic);
    }


    @Override
    public String toString() {
        return "Token{" +
                "topic='" + topic + '\'' +
                ", timestamp=" + timestamp +
                ", payload=" + payload +
                '}';
    }
}
