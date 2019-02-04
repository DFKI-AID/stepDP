package de.dfki.rs;

import java.util.Objects;

/**
 */
public class Token {
    public final String topic;
    public final long timestamp = System.currentTimeMillis();
    public final Object payload;
    //TODO confidence

    public Token(String topic, Object payload) {
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
