package de.dfki.step.fusion;

public class OptionalNode implements FusionNode {
    private final FusionNode child;

    public OptionalNode(FusionNode child) {
        this.child = child;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public FusionNode getChild() {
        return child;
    }
}
