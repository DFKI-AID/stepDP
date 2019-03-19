package de.dfki.step.fusion;

/**
 * The pattern we are looking for in our input
 */
public interface FusionNode {
    void accept(Visitor visitor);

    interface Visitor {
        void visit(InputNode input);
        void visit(ParallelNode input);

    }
}
