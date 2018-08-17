package de.dfki.tocalog.dialog;

import de.dfki.tocalog.input.Input;

import java.util.List;
import java.util.Optional;

public interface IntentProducer {
    void add(Input input);

    Optional<Intent> getIntent();
}
