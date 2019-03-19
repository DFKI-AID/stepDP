package de.dfki.step.fusion;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.time.Duration;

public class ParallelNode implements FusionNode{
    private PSequence<FusionNode> children = TreePVector.empty();
    private Duration interval = Duration.ofMillis(2000L);


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public ParallelNode add(FusionNode node) {
        children = children.plus(node);
        return this;
    }

    public PSequence<FusionNode> getChildren() {
        return children;
    }

    public Duration getInterval() {
        return interval;
    }

    /**
     * Sets the maximal interval of time between inputs.
     * e.g. the user has to speak and do gesture in an interval of 2 seconds
     * @param distance
     */
    public void setInterval(Duration distance) {
        this.interval = distance;
    }
}
