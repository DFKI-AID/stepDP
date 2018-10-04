package de.dfki.tocalog.core;

import de.dfki.tocalog.input.Input;

import java.util.List;

/**
 */
public interface InputComponent  {
    List<Input> process(Event event);
}
