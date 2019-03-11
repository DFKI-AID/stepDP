package de.dfki.pdp.nlg;

/**
 */
public class PrintVisitor implements TaskVisitor {
    @Override
    public void visit(SimpleTask task) {
        System.out.println(task.getType());
    }

    @Override
    public void visit(ComplexTask task) {

    }
}
