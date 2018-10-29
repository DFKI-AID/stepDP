package de.dfki.tocalog.output;


import de.dfki.tocalog.core.Mode;

/**
 */
public class TextOutput implements Output {
    private String text;

    public TextOutput(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "TextOutput{" +
                "text='" + text + '\'' +
                '}';
    }

    @Override
    public Mode getMode() {
        return Mode.Vision;
    }
}
