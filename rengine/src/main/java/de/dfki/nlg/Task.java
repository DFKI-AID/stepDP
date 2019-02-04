package de.dfki.nlg;

/**
 */
public interface Task {
    void accept(TaskVisitor taskVisitor);
}
