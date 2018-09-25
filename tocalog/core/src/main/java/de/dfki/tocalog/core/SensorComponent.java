package de.dfki.tocalog.core;

import java.util.Optional;

/**
 */
public interface SensorComponent {
    Optional<SensorInfo> process(Event event);
}
