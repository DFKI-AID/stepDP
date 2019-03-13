package de.dfki.pdp.nlg;

/**
 */
public class MoveTask extends SimpleTask {
    private final String origin;
    private final String destination;

    public MoveTask(String origin, String destination) {
        super("move");
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public void accept(TaskVisitor taskVisitor) {
        taskVisitor.visit(this);
    }

}
