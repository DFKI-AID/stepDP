package de.dfki.pdp.nlg;

/**
 */
public abstract class SimpleTask implements Task {

    /**
     * type of the task e.g. 'move'
     */
    private final String type;

    public SimpleTask(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
