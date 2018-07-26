package de.dfki.tocalog.output;

import java.util.Optional;

/**
 */
public class OutputRequest<T extends Output> {
    private OutputRequest(Builder<T> builder) {

    }

    public static class Builder<T extends Output> {
        private T output;
        private Optional<String> target; //TODO replace with target class? person, session, any, service
        private Optional<Long> timestamp = Optional.empty();
        private Optional<Long> timeout = Optional.empty();

        public Builder(T output) {
            this.output = output;
        }

        public OutputRequest<T> build() {
            return new OutputRequest<>(this);
        }
    }
}
