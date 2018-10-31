package a;

import de.dfki.tocalog.core.Event;
import de.dfki.tocalog.core.EventProducer;

import java.util.Optional;

/**
 */
public class TimeEventProducer implements EventProducer {
    private final long updateInterval;
    private long lastUpdate = System.currentTimeMillis();

    public TimeEventProducer(long updateInterval) {
        this.updateInterval = updateInterval;
    }


    @Override
    public Optional<Event> nextEvent() {
        long now = System.currentTimeMillis();
        if (now - lastUpdate > updateInterval) {
            lastUpdate = now;
            return Optional.of(Event.create(new Object())
                    .setSource(this.getClass().getSimpleName())
                    .build()
            );
        }

        return Optional.empty();
    }
}
