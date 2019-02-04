package de.dfki.rengine;

import java.time.Duration;

/**
 */
public class Clock {
    private int iteration = 0;
    private final double rate;

    public Clock(double rate) {
        this.rate = rate;
    }

    public void inc() {
        iteration++;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
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
