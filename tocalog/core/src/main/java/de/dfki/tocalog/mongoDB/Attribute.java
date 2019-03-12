package de.dfki.tocalog.mongoDB;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


public class Attribute {


    private String name;
    private String value;

    public Attribute() {}

    public Attribute(String id, String name, String value) {
        this.name = name;
        this.value = value;

    }

    public Attribute(String id, String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
