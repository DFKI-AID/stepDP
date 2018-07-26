package de.dfki.tocalog.output;


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
}
