package de.dfki.step.core;

import java.time.Duration;

public class ClockComponent implements Component {
    private final Clock clock;

    public ClockComponent(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void init(ComponentManager cm) {

    }

    @Override
    public void deinit() {

    }

    @Override
    public void beforeUpdate() {
        clock.inc();
    }

    @Override
    public void update() {
    }

    @Override
    public Object createSnapshot() {
        return clock.getIteration();
    }

    @Override
    public void loadSnapshot(Object snapshot) {
        this.clock.setIteration((Long) snapshot);
    }

    public Clock getClock() {
        return clock;
    }

    public long getIteration() {
        return clock.getIteration();
    }

    public long convert(Duration duration) {
        return clock.convert(duration);
    }
}
