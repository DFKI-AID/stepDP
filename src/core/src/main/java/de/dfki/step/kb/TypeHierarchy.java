package de.dfki.step.kb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

/**
 * TODO: performance: cache
 */
public class TypeHierarchy {
    private static final Logger log = LoggerFactory.getLogger(TypeHierarchy.class);
    private final Map<Type, Node> nodes;

    protected TypeHierarchy(Builder builder) {
        this.nodes = new HashMap<>(builder.nodes);
    }

    /**
     * @return String that represents the class hierarchy in the mermaid format
     */
    public String toMermaid() {
        StringBuilder sb = new StringBuilder();
        sb.append("graph TD;\n");
        for (Node node : nodes.values()) {
            for (Node parentNode : node.parents) {
                sb.append(String.format("    %s-->%s;\n", node.type.name, parentNode.type.name));
            }
        }
        return sb.toString();
    }

    /**
     * @param childType
     * @param superType
     * @return true iff the childType inherits / extends / .. the superType
     */
    public boolean inheritsFrom(Type childType, Type superType) {
        Node childNode = nodes.get(childType);
        if (childNode == null) {
            log.warn("Could not check 'inheritance'. Hierarchy does not contain {}", childNode.type);
            return false;
        }

        HashSet<Node> openList = new HashSet<>();
        openList.addAll(childNode.parents);
        while (!openList.isEmpty()) {
            HashSet<Node> newOpenList = new HashSet<>();

            for (Node node : openList) {
                if (node.type.equals(superType)) {
                    return true;
                }
                newOpenList.addAll(node.parents);
            }
            openList = newOpenList;
        }
        return false;
    }


    /**
     * @param entity
     * @param type
     * @return true iff the entity has the given type (in the hierarchy)
     */
    public boolean isA(Entity entity, Type type) {
        Optional<String> childType = entity.get(Ontology.type);
        if (!childType.isPresent()) {
            log.warn("can't check 'is-a' relation: no type found for {}", entity);
            return false;
        }

        return inheritsFrom(new Type(childType.get()), type);
    }

    /**
     * @param type
     * @return All sub classes of the given type in this hierarchy
     */
    public Set<Type> getSubClasses(Type type) {
        Node node = this.nodes.get(type);
        if (node == null) {
            log.warn("Could not get sub classes. Hierarchy does not contain {}", type);
            return Collections.EMPTY_SET;
        }
        Set<Type> subClasses = getClasses(node, n -> n.children);
        return subClasses;
    }

    /**
     *
     * @param type
     * @return All super classes of the given type
     */
    public Set<Type> getSuperClasses(Type type) {
        Node node = this.nodes.get(type);
        if (node == null) {
            log.warn("Could not get super classes. Hierarchy does not contain {}", type);
            return Collections.EMPTY_SET;
        }
        Set<Type> subClasses = getClasses(node, n -> n.parents);
        return subClasses;
    }

    protected Set<Type> getClasses(Node node, Function<Node, Collection<Node>> nextNodeFnc) {
        HashSet<Type> subClasses = new HashSet<>();
        HashSet<Node> open = new HashSet<>();
        open.addAll(nextNodeFnc.apply(node));
        while (!open.isEmpty()) {
            HashSet<Node> nextOpen = new HashSet<>();
            for (Node n : open) {
                subClasses.add(n.type);
                nextOpen.addAll(nextNodeFnc.apply(n));
            }
            open = nextOpen;
        }
        return subClasses;
    }

    /**
     * @throws IllegalStateException if multiple nodes have the same type.
     */
    protected void assertDuplicateTypes() {
        HashSet<Type> types = new HashSet<>();
        for (Node node : nodes.values()) {
            if (types.contains(node.type)) {
                throw new IllegalStateException("duplicate found for type: " + node.type);
            }
            types.add(node.type);
        }
    }

    /**
     * @throws IllegalStateException if a cycle was discovered
     */
    protected void assertTree() {
        for (Node node : nodes.values()) {
            if (inheritsFrom(node.type, node.type)) {
                throw new IllegalStateException("cycle detected for " + node.type);
            }
        }
    }

    protected void assertState() {
        assertTree();
        assertDuplicateTypes();
    }

    private static class Node {
        public final Type type;
        public final List<Node> parents = new ArrayList<>();
        public final List<Node> children = new ArrayList<>();

        private Node(Type type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(type, node.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type);
        }
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {
        private Map<Type, Node> nodes = new HashMap<>();

        public Builder add(Type type) {
            if (!nodes.containsKey(type)) {
                nodes.put(type, new Node(type));
            }
            return this;
        }

        public Builder add(Type childType, Type parentType) {
            add(childType);
            Node childNode = nodes.get(childType);
            add(parentType);
            Node parentNode = nodes.get(parentType);
            nodes.get(childType).parents.add(parentNode);
            nodes.get(parentType).children.add(childNode);
            return this;
        }

        public TypeHierarchy build() {
            TypeHierarchy it = new TypeHierarchy(this);
            it.assertState();
            return it;
        }
    }


}
