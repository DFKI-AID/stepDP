package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.output.Output;

import java.util.*;

/**
 */
public abstract class OutputNode {
    public enum Semantic {
        redundant,
        complementary,
        alternative,
        concurrent,
        sequential,
        optional,
    }

    protected abstract void accept(Visitor visitor);

    protected Optional<String> id;

    public Optional<String> getId() {
        return id;
    }



    public static Internal.Builder buildNode(Semantic semantic) {
        return new Internal.Builder(semantic);
    }

    public static External.Builder buildNode(Output output) {
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
        private Output output;
        private List<String> services;

        private External(Builder builder) {
            this.output = builder.output;
            this.services = builder.services;
            this.id = builder.id;
        }

        @Override
        protected void accept(Visitor visitor) {
            visitor.visitLeaf(this);
        }

        public Output getOutput() {
            return output;
        }

        public List<String> getServices() {
            return services;
        }

        public External copy() {
            Builder builder = new Builder(output);
            for(String service : services) {
                builder.addService(service);
            }
            return builder.build();
        }

        public static class Builder {
            private Output output;
            private List<String> services = new ArrayList<>();
            private Optional<String> id = Optional.empty();

            public Builder(Output output) {
                this.output = output;
            }

            public Builder addService(String service) {
                this.services.add(service);
                return this;
            }

            public Builder setId(String id) {
                this.id = Optional.of(id);
                return this;
            }

            public External build() {
                return new External(this);
            }
        }
    }

    public static class Internal extends OutputNode {
        private Semantic semantic;
        private List<OutputNode> childNodes = new ArrayList<>();

        private Internal(Builder builder) {
            this.semantic = builder.semantic;
            this.childNodes = builder.childNodes;
            this.id = builder.id;
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
            private Optional<String> id = Optional.empty();

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
                this.id = Optional.of(id);
                return this;
            }

        }
    }


}