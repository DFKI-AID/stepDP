package de.dfki.tocalog.output;

import java.util.Optional;

/**
 * Request for a single (unimodal) resource allocation
 */
public class AllocationRequest<T extends Output> {
    private long timeout;


    public class Builder<T> {
        private T output;
        private Optional<Long> timestamp = Optional.empty();
        private Optional<Long> timeout = Optional.empty();
        private Optional<String> service = Optional.empty();

        public Builder(T payload) {
            this.output = payload;
        }
    }
}
