package de.dfki.nlg;

/**
 */
public interface TaskVisitor {
    void visit(SimpleTask task);
    void visit(ComplexTask task);
}
