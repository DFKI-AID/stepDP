package de.dfki.tocalog.input;

import java.util.Optional;

public class TextInput extends AbsInput {
    private String text;
    private Optional<String> source = Optional.empty();

    public TextInput(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setSource(String source) {
        //TODO replace with source object: agent, session, hint~predicate?
        this.source = Optional.of(source);
    }

    public Optional<String> getSource() {
        return source;
    }
}
