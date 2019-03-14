package de.dfki.step.srgs;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class Item implements Node {
    private final String content;
    private int weight = 1;
    private int minRepitions = 1;
    private int maxRepitions = 1;
    private List<Tag> tags = new ArrayList<>();

    public Item(String content) {
        this.content = content;
    }

    public int getWeight() {
        return weight;
    }

    public Item setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public Item makeOptional() {
        minRepitions = 0;
        return this;
    }

    public int getMinRepitions() {
        return minRepitions;
    }

    public Item setMinRepitions(int minRepitions) {
        this.minRepitions = minRepitions;
        return this;
    }

    public int getMaxRepitions() {
        return maxRepitions;
    }

    public Item setMaxRepitions(int maxRepitions) {
        this.maxRepitions = maxRepitions;
        return this;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public Item addTag(Tag tag) {
        this.tags.add(tag);
        return this;
    }

    public String getContent() {
        return content;
    }

    @Override
    public void write(NodeWriter nw) {
        nw.write(String.format("<item repeat=\"%d-%d\" weight=\"%d\">", minRepitions, maxRepitions, weight));
        nw.increaseIndent();
        nw.newLine();
        nw.write(content);
        for(Tag tag : tags) {
            tag.write(nw);
        }
        nw.decreaseIndent();
        nw.newLine();
        nw.write("</item>");
    }
}
