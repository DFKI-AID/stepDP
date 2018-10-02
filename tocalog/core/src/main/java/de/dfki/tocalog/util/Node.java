package de.dfki.tocalog.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 */
public abstract class Node<T> {

    protected abstract void accept(Visitor visitor);

    public static <T> Internal buildInternal(T content) {
        return new Internal(content);
    }

    public static <T> External buildExternal(T content) {
        return new External(content);
    }

    public interface Visitor<ET, IT> {

        void visitLeaf(External<ET> leaf);

        default void visitInnerNode(Internal<IT> node) {
            for (Node childNode : node.getChildNodes()) {
                childNode.accept(this);
            }
        }
    }

    public static class External<T> extends Node<T> {
        private final T content;

        private External(T content) {
            this.content = content;
        }

        @Override
        protected void accept(Visitor visitor) {
            visitor.visitLeaf(this);
        }

        public T getContent() {
            return content;
        }
    }

    public static class Internal<T> extends Node<T> {
        private final T content;
        private List<Node<T>> childNodes = new ArrayList<>();

        private Internal(T content) {
            this.content = content;
        }

        public T getContent() {
            return content;
        }

        @Override
        protected void accept(Visitor visitor) {
            visitor.visitInnerNode(this);
        }

        public List<Node<T>> getChildNodes() {
            return childNodes;
        }
    }


}
