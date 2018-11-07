package de.dfki.tocalog.core;

import de.dfki.tocalog.input.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 */
public class Inputs {
    private static final Logger log = LoggerFactory.getLogger(Inputs.class);
    private Map<String, Set<Object>> consumers = new HashMap<>();
    private List<Input> inputs = new ArrayList<>();

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public void removeOld(long timeout) {
        long deadline = System.currentTimeMillis() - timeout;
        inputs.removeIf(i -> i.getTimestamp() < deadline);
    }

    public void remove(Collection<String> inputIds) {
        inputs.removeIf(i -> inputIds.contains(i.getId()));
        consumers.entrySet().removeIf(e -> inputIds.contains(e.getKey()));
    }

    public static final Inputs EMPTY = new Inputs();

    public void add(Collection<Input> tmpInputs) {
        inputs.addAll(tmpInputs);
    }

    public boolean isConsumed(Input input) {
        return consumers.get(input.getId()) != null;
    }

    public Set<Object> getConsumers(Input input) {
        if (!consumers.containsKey(input.getId())) {
            return Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(consumers.get(input.getId()));
    }

    public void consume(Input input, Object consumer) {
        if (!consumers.containsKey(input.getId())) {
            consumers.put(input.getId(), new HashSet<>());
        }

        if(consumers.get(input.getId()) == null) {
            consumers.put(input.getId(), new HashSet<>());
        }
        consumers.get(input.getId()).add(consumer);
    }

    public boolean wasConsumedBy(Input input, Object consumer) {
        return getConsumers(input).contains(consumer);
    }

    public List<Input> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    @Override
    public String toString() {
        return "Inputs{" +
                "consumers=" + consumers +
                ", inputs=" + inputs +
                '}';
    }
}
