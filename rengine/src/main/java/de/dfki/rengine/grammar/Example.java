package de.dfki.rengine.grammar;

/**
 */
public class Example implements Node {
    private final String example;

    public Example(String example) {
        this.example = example;
    }

    @Override
    public void write(NodeWriter nw) {
        nw.write(String.format("<example>%s</example>", example));
        nw.newLine();
    }
}
