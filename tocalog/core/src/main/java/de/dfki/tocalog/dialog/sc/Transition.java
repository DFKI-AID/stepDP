package de.dfki.tocalog.dialog.sc;

/**
 */
public class Transition {
    private final String cond;
    private final String source, target;

    public Transition(String source, String cond, String target) {
        this.cond = cond;
        this.source = source;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getCond() {
        return cond;
    }

}
