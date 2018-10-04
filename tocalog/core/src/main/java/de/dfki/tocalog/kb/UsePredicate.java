package de.dfki.tocalog.kb;

import de.dfki.tocalog.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
public class UsePredicate extends PredicateHelper {
    private static Logger log = LoggerFactory.getLogger(UsePredicate.class);
    public static final String USES_PRED = "agent.uses";

    private UsePredicate(KnowledgeBase kb) {
        super(kb);
    }

    public KnowledgeBase.Key<Device> getKey() {
        return kb.getKey(Device.class);
    }


    public Set<DeviceComponent> getComponents(Device device) {
        if (!device.getId().isPresent()) {
            log.warn("can't handle device without an id");
            return Collections.EMPTY_SET;
        }
        EKnowledgeMap<DeviceComponent> km = kb.getKnowledgeMap(DeviceComponent.class);
        try {
            return km.lock().getStream()
                    .map(e -> e.getValue())
                    .filter(e -> e.getDevice().isPresent())
                    .filter(e -> e.getDevice().get().equals(device.getId()))
                    .collect(Collectors.toSet());
        } finally {
            km.unlock();
        }
    }

    /**
     * @param device
     * @return The ids of agent that the currently use the given device
     */
    public Set<Agent> usedByAgents(Device device) {
        Set<String> subjectIds = usedBy(device);
        EKnowledgeMap<Agent> agentKm = kb.getKnowledgeMap(Agent.class);
        Set<Agent> agents = agentKm.get(subjectIds);
        return agents;
    }

    public Set<String> usedBy(Device device) {
        if (!device.getId().isPresent()) {
            log.warn("can't handle device without an id");
            return Collections.EMPTY_SET;
        }
        Set<String> subjectIds = inverseRelation(USES_PRED, device.getId().get());
        return subjectIds;
    }

    public Set<Device> uses(Agent agent) {
        if (!agent.getId().isPresent()) {
            log.warn("can't handle agent without an id");
            return Collections.EMPTY_SET;
        }
        Set<String> deviceIds = relation(agent.getId().get(), USES_PRED);
        EKnowledgeMap<Device> deviceKm = kb.getKnowledgeMap(Device.class);
        Set<Device> devices = deviceKm.get(deviceIds);
        return devices;
    }



    public UsePredicate create(KnowledgeBase kb) {
        return new UsePredicate(kb);
    }
}
