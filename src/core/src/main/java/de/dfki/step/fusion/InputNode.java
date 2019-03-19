package de.dfki.step.fusion;

import de.dfki.step.rengine.Token;

import java.util.function.Predicate;

public class InputNode implements FusionNode {
    private final Predicate<Token> pred;

    public InputNode(Predicate<Token> pred) {
        this.pred = pred;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public boolean matches(Token token) {
        return pred.test(token);
    }
}
