package de.dfki.tocalog.input;

import java.util.Optional;

public class GestureInput extends AbsInput {
    private String gestureType;
    private Optional<String> source = Optional.empty();

    public GestureInput(String gestureType) {
        this.gestureType = gestureType;
    }

    public String getGestureType() {
        return gestureType;
    }

    public void setSource(String source) {
        this.source = Optional.of(source);
    }

    public Optional<String> getSource() {
        return source;
    }
}
