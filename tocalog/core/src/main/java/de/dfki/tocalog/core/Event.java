package de.dfki.tocalog.core;

import java.util.Optional;

/**
 */
public interface Event<T> {
    default boolean is(Class type) {
        return type.isAssignableFrom(get().getClass());
    }

    String getSource();

    long getTimestamp();

    T get();

    default <S> Optional<S> tryGet(Class<S> clazz) {
        if (!is(clazz)) {
            return Optional.empty();
        }
        return Optional.of((S) get());
    }

    static <T> Builder create(T t) {
        return new Builder(t);
    }

    default Class getType() {
        return get().getClass();
    }

    class Builder<T> {
        private T t;
        private long timestamp;
        private String source;

        public Builder(T t) {
            this.t = t;
        }

        public Event<T> build() {
            this.timestamp = System.currentTimeMillis();
            SimpleEvent<T> event = new SimpleEvent<>(this);
            return event;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }
    }

    final class SimpleEvent<T> implements Event<T> {
        private T t;
        private String source;
        private long timestamp;

        private SimpleEvent(Builder<T> builder) {
            t = builder.t;
            source = builder.source;
            timestamp = builder.timestamp;
        }

        @Override
        public T get() {
            return t;
        }

        @Override
        public String getSource() {
            return source;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

    }
}
