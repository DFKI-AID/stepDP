package de.dfki.pdp.grammar;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO impl rule ref?
 */
public class Rule implements Node {
    private final String id;
    private Scope scope = Scope.PUBLIC;
    private List<Node> items = new ArrayList<>();

    public Rule(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Rule makePrivate() {
        this.scope = Scope.PRIVATE;
        return this;
    }

    public String getScope() {
        return scope.toString().toLowerCase();
    }

    public Rule add(Node item) {
        this.items.add(item);
        return this;
    }


    public boolean isPublic() {
        return this.scope == Scope.PUBLIC;
    }

    @Override
    public void write(NodeWriter nw) {
        nw.write(String.format("<rule id=\"%s\" scope=\"%s\">", id, getScope()));
        nw.increaseIndent();
        items.forEach(i -> {
            nw.newLine();
            i.write(nw);
        });
        nw.decreaseIndent();
        nw.newLine();
        nw.write("</rule>");
    }

    public enum Scope {
        PUBLIC,
        PRIVATE
    }
}
