package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.kb.Entity;

import java.util.*;

/**
 */
public abstract class OutputNode {

    protected OutputNode(String id) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
    }

    public enum Semantic {
        redundant,
        complementary,
        alternative,
        concurrent,
        sequential,
        optional,
    }

    protected abstract void accept(Visitor visitor);

    protected final String id;

    public String getId() {
        return id;
    }


    public static Internal.Builder buildNode(Semantic semantic) {
        return new Internal.Builder(semantic);
    }

    public static External.Builder buildNode(Entity output) {
        return new External.Builder(output);
    }

    public interface Visitor {

        void visitLeaf(External leaf);

        default void visitInnerNode(Internal node) {
            for (OutputNode childNode : node.getChildNodes()) {
                childNode.accept(this);
            }
        }
    }

    public static class External extends OutputNode {
        private Entity attachment;

        private External(Builder builder) {
            super(builder.id);
            this.attachment = builder.output;
        }

        @Override
        protected void accept(Visitor visitor) {
            visitor.visitLeaf(this);
        }

        public Entity getAttachment() {
            return attachment;
        }

        public External copy() {
            Builder builder = new Builder(attachment);
            builder.setId(this.id);
            return builder.build();
        }

        public static class Builder {
            private Entity output;
            private String id = null;

            public Builder(Entity output) {
                this.output = output;
            }

            public Builder addService(String service) {
                return this;
            }

            public Builder setOutput(Entity output) {
                this.output = output;
                return this;
            }

            public Builder setId(String id) {
                this.id = id;
                return this;
            }

            public External build() {
                return new External(this);
            }
        }
    }

    public static class Internal extends OutputNode {
        private Semantic semantic;
        private List<OutputNode> childNodes;

        private Internal(Builder builder) {
            super(builder.id);
            this.semantic = builder.semantic;
            this.childNodes = builder.childNodes;
        }

        public Semantic getSemantic() {
            return semantic;
        }

        @Override
        protected void accept(Visitor visitor) {
            visitor.visitInnerNode(this);
        }

        public List<OutputNode> getChildNodes() {
            return childNodes;
        }

        public static class Builder {
            private Semantic semantic;
            private List<OutputNode> childNodes = new ArrayList<>();
            private String id = null;

            public Builder(Semantic semantic) {
                this.semantic = semantic;
            }

            public Builder addNode(OutputNode outputNode) {
                childNodes.add(outputNode);
                return this;
            }

            public Internal build() {
                childNodes = Collections.unmodifiableList(childNodes);
                return new Internal(this);
            }

            public Builder setId(String id) {
                this.id = id;
                return this;
            }

        }
    }


}
