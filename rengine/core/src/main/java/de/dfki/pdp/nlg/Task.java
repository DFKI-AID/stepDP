package de.dfki.pdp.nlg;

/**
 */
public interface Task {
    void accept(TaskVisitor taskVisitor);
}
