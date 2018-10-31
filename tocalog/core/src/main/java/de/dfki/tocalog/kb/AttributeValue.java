package de.dfki.tocalog.kb;

/**
 */
public class AttributeValue<T> {
    public final String name;
    public final T value;
    public final Attribute<T> attribute;
//        public double confidence;

    public AttributeValue(String name, T value, Attribute<T> attribute) {
        this.name = name;
        this.value = value;
        this.attribute = attribute;
    }


}
