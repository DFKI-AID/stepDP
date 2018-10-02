package de.dfki.tocalog.core;

import de.dfki.tocalog.kb.KnowledgeBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public abstract class HypothesisTree {
    public static HypothesisTree or(HypothesisTree h1, HypothesisTree h2) {
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

    public static abstract class Leaf extends HypothesisTree {
        public void accept(Visitor v) {
            v.visit(this);
        }

        public abstract Collection<Object> findMatches(KnowledgeBase kb);
    }

    public static class Node extends HypothesisTree {
        private final Semantic semantic;
        private List<HypothesisTree> childNodes = new ArrayList<>();

        public Node(Semantic semantic) {
            this.semantic = semantic;
        }

        public void accept(Visitor v) {
            v.visit(this);
        }

        public List<HypothesisTree> getChildNodes() {
            return childNodes;
        }
    }

}
