package de.dfki.step.output.imp;

/**
 */
public class Rank {
    private double ranking = 1.0;

    public void multiply(double ranking) {
        this.ranking *= ranking;
    }

    public double getRanking() {
        return ranking;
    }
}
