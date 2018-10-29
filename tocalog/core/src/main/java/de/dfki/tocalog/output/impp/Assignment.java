package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.core.Ontology;
import de.dfki.tocalog.output.Output;

import java.util.*;

/**
 * Suitability of services to present an output to a user
 */
public class Assignment {
    private final Output output;
    private final String agent;
    private Map<String, Score> serviceScore = new HashMap<>();
    private List<Ontology.Ent> services = new ArrayList<>();

    public Assignment(Output output, String agent) {
        this.output = output;
        this.agent = agent;
    }

    public void addService(Ontology.Ent service) {
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

    public Optional<Ontology.Ent> getBest() {
        if (services.isEmpty()) {
            return Optional.empty();
        }
        sort();
        return Optional.of(services.get(0));
    }

    public List<Ontology.Ent> getServices() {
        return services;
    }

    public Output getOutput() {
        return output;
    }
}
