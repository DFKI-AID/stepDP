package de.dfki.tocalog.output;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

/**
 */
public class Service  {
    public Optional<String> getId() {
        return Optional.empty();
    }

    public void serialize(WritableByteChannel channel) {

    }

    public Service deserialize(ReadableByteChannel channel) {
        return null;
    }
}
