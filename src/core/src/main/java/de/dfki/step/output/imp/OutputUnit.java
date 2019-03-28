package de.dfki.step.output.imp;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;

import java.util.Collections;
import java.util.Set;

/**
 */
public class OutputUnit {
    //    private Duration when = Duration.ofMillis(System.currentTimeMillis());
    private final Entity output; //what
    private final Set<Entity> target; //whom
    private final Set<Entity> services; //where
    private long minRank = 0;
    private long maxRank = 0;

    public OutputUnit(Entity output, Set<Entity> target, Set<Entity> services) {
        //TODO schemes to check services
        if (!output.get(Ontology.id).isPresent()) {
            throw new IllegalArgumentException("id is required for output");
        }
        this.output = output;
        this.target = target;
        this.services = services;
    }

    public OutputUnit(Entity output, Set<Entity> target) {
        this(output, target, Collections.EMPTY_SET);
    }

    public OutputUnit(Entity output) {
        this(output, Collections.EMPTY_SET);
    }

    public String getOutputId() {
        return output.get(Ontology.id).get();
    }

    public double getScore() {
        if (maxRank == 0) {
            return 0;
        }
        return ((double) minRank) / maxRank;
    }


    public Entity getOutput() {
        return output;
    }

    public Set<Entity> getTarget() {
        return target;
    }

    public Set<Entity> getServices() {
        return services;
    }

    public OutputUnit setServices(Set<Entity> services) {
        return new OutputUnit(output, target, services);
    }

    public OutputUnit incRank(long min, long max) {
        OutputUnit ou = new OutputUnit(output, target, services);
        ou.minRank = this.minRank + min;
        ou.maxRank = this.maxRank + max;
        return ou;
    }

    @Override
    public String toString() {
        return "OutputUnit{" +
                "output=" + output +
                ", target=" + target +
                ", services=" + services +
                ", score=" + getScore() +
                '}';
    }
}
