package de.dfki.step.core;

import de.dfki.step.input.Input;

import java.util.List;

/**
 */
public interface InputComponent  {
    List<Input> process(Event event);
}
