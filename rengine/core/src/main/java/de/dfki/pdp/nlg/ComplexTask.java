package de.dfki.pdp.nlg;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ComplexTask implements Task {
    private final List<Task> subtasks = new ArrayList<>();

    public ComplexTask(Task... subtasks) {
        this(List.of(subtasks));
    }

    public ComplexTask(List<Task> subtasks) {
        this.subtasks.addAll(subtasks);
    }

    @Override
    public void accept(TaskVisitor taskVisitor) {
        taskVisitor.visit(this);
        subtasks.forEach(t -> t.accept(taskVisitor));
    }
}
