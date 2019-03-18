package de.dfki.step.srgs;

/**
 *
 */
public class Tag implements Node{
    private final String content;

    public Tag(String content) {
        this.content = content;
    }

    /**
     * Creates a semantic tag for defining the intent
     * "intent": YOUR_INTENT
     * @param intent
     * @return
     */
    public static Tag intent(String intent) {
        return assign("intent", intent);
    }

    public static Tag assign(String name, String value) {
        return new Tag(String.format("out.%s = \"%s\"", name, value));
    }

    public static Tag rawAssign(String name, String value) {
        return new Tag(String.format("out.%s = %s", name, value));
    }

    public static Tag raw(String content) {
        return new Tag(content);
    }

    @Override
    public void write(NodeWriter nw) {
        nw.write(String.format("<tag>%s</tag>", content));
    }
}
