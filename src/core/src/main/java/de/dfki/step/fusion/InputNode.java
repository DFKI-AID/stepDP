package de.dfki.step.fusion;

import de.dfki.step.core.Schema;
import de.dfki.step.core.Token;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * This node should be matched against unimodal input.
 */
public class InputNode implements FusionNode {
    private final String id;
    private final Predicate<Token> pred;

    public InputNode(String id, Predicate<Token> pred) {
        this.id = id;
        this.pred = pred;
    }

    public InputNode(Predicate<Token> pred) {
        this(UUID.randomUUID().toString(), pred);
    }


    public InputNode(Schema schema) {
        this(t -> schema.matches(t));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public boolean matches(Token token) {
        return pred.test(token);
    }
}
