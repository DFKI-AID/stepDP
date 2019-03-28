package de.dfki.step.output.imp;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;

import java.util.*;

/**
 * Suitability of services to present an output to a user
 */
public class Assignment {
    private final Entity output;
    private final String agent;
    private Map<String, Score> serviceScore = new HashMap<>();
    private List<Entity> services = new ArrayList<>();

    public Assignment(Entity output, String agent) {
        this.output = output;
        this.agent = agent;
    }

    public void addService(Entity service) {
        if (!service.get(Ontology.id).isPresent()) {
            throw new IllegalArgumentException("service needs an id");
        }
        this.services.add(service);
        this.serviceScore.put(service.get(Ontology.id).get(), new Score());
    }

    protected void sort() {
        services.sort((s1, s2) -> Score.compare(serviceScore.get(s1), serviceScore.get(s2)));
    }

    public void limit(int n) {
        sort();
        while (services.size() > n) {
            services.remove(n - 1);
        }
    }

    public Optional<Entity> getBest() {
        if (services.isEmpty()) {
            return Optional.empty();
        }
        sort();
        return Optional.of(services.get(0));
    }

    public List<Entity> getServices() {
        return services;
    }

    public Entity getOutput() {
        return output;
    }
}
