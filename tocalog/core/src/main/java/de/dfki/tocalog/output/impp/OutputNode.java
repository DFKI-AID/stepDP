package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.output.Output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static InnerNode.Builder buildNode(Semantic semantic) {
        return new InnerNode.Builder(semantic);
    }

    public static Leaf.Builder buildNode(Output output) {
        return new Leaf.Builder(output);
    }

    public interface Visitor {

        void visitLeaf(Leaf leaf);

        default void visitInnerNode(InnerNode node) {
            for (OutputNode childNode : node.getChildNodes()) {
                childNode.accept(this);
            }
        }
    }

    static class Leaf extends OutputNode {
        private Output output;
        private List<String> services;

        private Leaf(Builder builder) {
            this.output = builder.output;
            this.services = builder.services;
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

        public Leaf copy() {
            Builder builder = new Builder(output);
            for(String service : services) {
                builder.addService(service);
            }
            return builder.build();
        }

        public static class Builder {
            private Output output;
            private List<String> services = new ArrayList<>();

            public Builder(Output output) {
                this.output = output;
            }

            public Builder addService(String service) {
                this.services.add(service);
                return this;
            }

            public Leaf build() {
                return new Leaf(this);
            }
        }
    }

    static class InnerNode extends OutputNode {
        private Semantic semantic;
        private List<OutputNode> childNodes = new ArrayList<>();

        private InnerNode(Builder builder) {
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

            public Builder(Semantic semantic) {
                this.semantic = semantic;
            }

            public Builder addNode(OutputNode outputNode) {
                childNodes.add(outputNode);
                return this;
            }

            public InnerNode build() {
                childNodes = Collections.unmodifiableList(childNodes);
                return new InnerNode(this);
            }
        }
    }


}
