package de.dfki.step.core;

import java.time.Duration;

/**
 */
public class Clock {
    private long iteration = 0;
    private final double rate;

    /**
     * How often the clock is updated. In Milliseconds
     * @param rate
     */
    public Clock(double rate) {
        this.rate = rate;
    }


    public Clock() {
        this(50);
    }

    public void inc() {
        iteration++;
    }

    public long getIteration() {
        return iteration;
    }

    public void setIteration(long iteration) {
        this.iteration = iteration;
    }

    public long convert(Duration duration) {
        long d = duration.toMillis();
        return (long) (d / rate);
    }

    public double getRate() {
        return rate;
    }
}
