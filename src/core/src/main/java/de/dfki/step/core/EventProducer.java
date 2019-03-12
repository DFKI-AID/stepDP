package de.dfki.step.core;

import java.util.Optional;

/**
 */
public interface EventProducer {
    Optional<Event> nextEvent();
}
