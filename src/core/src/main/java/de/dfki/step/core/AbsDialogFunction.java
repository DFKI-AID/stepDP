package de.dfki.step.core;

import de.dfki.step.input.Input;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public abstract class AbsDialogFunction implements DialogFunction {
    private final Object origin;
    private final Collection<Input> consumedInputs;

    public AbsDialogFunction(Object origin, Inputs inputs, Collection<String> consumedInputs) {
        this(origin, inputs.getInputs().stream().filter(i -> consumedInputs.contains(i.getId())).collect(Collectors.toSet()));
    }

    public AbsDialogFunction(Object origin, Collection<Input> consumedInputs) {
        this.origin = origin;
        this.consumedInputs = consumedInputs;
    }


    public AbsDialogFunction(Object origin, Input... consumedInputs) {
        this.origin = origin;
        this.consumedInputs = Collections.unmodifiableList(List.of(consumedInputs));
    }

    @Override
    public Collection<Input> consumedInputs() {
        return consumedInputs;
    }

    @Override
    public Object getOrigin() {
        return origin;
    }
}
