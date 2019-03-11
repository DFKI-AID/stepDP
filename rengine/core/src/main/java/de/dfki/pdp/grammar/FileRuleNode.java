package de.dfki.pdp.grammar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * TODO move into Rule and create real parser
 */
public class FileRuleNode extends Rule {
    private final String content;

    private FileRuleNode(String id, String file) throws IOException {
        super(id);
        var path = Paths.get(FileRuleNode.class.getResource(file).getPath());
        this.content = new String(Files.readAllBytes(path));
    }

    @Override
    public void write(NodeWriter nw) {
        nw.write(content);
        nw.newLine();
    }


    @Override
    public void setScope(Scope scope) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rule makePrivate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getScope() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rule add(Node item) {
        throw new UnsupportedOperationException();
    }

    public static Rule create(String file) throws IOException {
        var list = List.of(file.split("/"));
        var fileName = list.get(list.size() - 1);
        fileName = fileName.split(".xml")[0];
        return new FileRuleNode(fileName, file);
    }
}
