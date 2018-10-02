package de.dfki.tocalog.input;

import java.util.UUID;

/**
 */
public abstract class AbsInput implements Input {
    private final String id = this.getClass().getSimpleName() + "-" + UUID.randomUUID().toString().substring(0, 10);
    private final long timestamp = System.currentTimeMillis();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
