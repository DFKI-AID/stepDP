package de.dfki.grammar;

/**
 */
public class NodeWriter {
    private StringBuilder sb = new StringBuilder();
    private int indentation = 0;

    public void increaseIndent() {
        indentation++;
    }

    public void decreaseIndent() {
        indentation--;
    }

    public void write(String s) {
        sb.append(s);
    }

    public void newLine() {
        sb.append("\n");
        for(int i=0; i< indentation; i++) {
            sb.append("\t");
        }
    }

    public String getOutput() {
        return sb.toString();
    }
}
