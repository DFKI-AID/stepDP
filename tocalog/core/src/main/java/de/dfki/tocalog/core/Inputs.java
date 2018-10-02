package de.dfki.tocalog.core;

import de.dfki.tocalog.input.Input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public class Inputs {
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
    }

    public static final Inputs EMPTY = new Inputs();

    public void add(Collection<Input> tmpInputs) {
        inputs.addAll(tmpInputs);
    }
}
