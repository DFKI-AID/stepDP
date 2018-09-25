package de.dfki.tocalog.core;

import java.util.Optional;

/**
 */
public interface EventProducer {
    Optional<Event> nextEvent();
}
