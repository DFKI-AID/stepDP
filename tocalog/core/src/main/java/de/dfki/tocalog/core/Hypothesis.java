package de.dfki.tocalog.core;

import de.dfki.tocalog.kb.KnowledgeBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public abstract class Hypothesis {
    public static Hypothesis or(Hypothesis h1, Hypothesis h2) {
        Node node = new Node(Semantic.OR);
        node.getChildNodes().add(h1);
        node.getChildNodes().add(h2);
        return node;
    }

    enum Semantic {
        OR,
        AND
    }

    public abstract void accept(Visitor v);


    public interface Visitor {
        void visit(Leaf leaf);
        void visit(Node node);
    }

    public static abstract class Leaf extends Hypothesis {
        public void accept(Visitor v) {
            v.visit(this);
        }

        public abstract Collection<Object> findMatches(KnowledgeBase kb);
    }

    public static class Node extends Hypothesis {
        private final Semantic semantic;
        private List<Hypothesis> childNodes = new ArrayList<>();

        public Node(Semantic semantic) {
            this.semantic = semantic;
        }

        public void accept(Visitor v) {
            v.visit(this);
        }

        public List<Hypothesis> getChildNodes() {
            return childNodes;
        }
    }

    public static class
}
