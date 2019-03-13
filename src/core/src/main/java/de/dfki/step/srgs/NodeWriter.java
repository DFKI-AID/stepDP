package de.dfki.step.srgs;

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

    public NodeWriter write(String s) {
        sb.append(s);
        return this;
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
