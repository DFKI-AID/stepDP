package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.model.Agent;
import de.dfki.tocalog.model.Service;
import de.dfki.tocalog.output.Output;

import java.util.*;

/**
 * Suitability of services to present an output to a user
 */
public class Assignment {
    private final Output output;
    private final Agent agent;
    private Map<String, Score> serviceScore = new HashMap<>();
    private List<Service> services = new ArrayList<>();

    public Assignment(Output output, Agent agent) {
        this.output = output;
        this.agent = agent;
    }

    public void addService(Service service) {
        if (!service.getId().isPresent()) {
            throw new IllegalArgumentException("service object does not have an id. service was " + service);
        }
        this.services.add(service);
        this.serviceScore.put(service.getId().get(), new Score());
    }

    protected void sort() {
        services.sort((s1, s2) -> Score.compare(serviceScore.get(s2), serviceScore.get(s1)));
    }

    public void limit(int n) {
        sort();
        while (services.size() > n) {
            services.remove(n - 1);
        }
    }

    public List<Service> getServices() {
        return services;
    }

    public Output getOutput() {
        return output;
    }
}
