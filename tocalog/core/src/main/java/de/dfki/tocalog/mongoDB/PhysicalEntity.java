package de.dfki.tocalog.mongoDB;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

@Entity("PhysicalEntity")
public class PhysicalEntity {

    @Id
    private String id;
    private List<Attribute> attributes = new ArrayList<>();

    public PhysicalEntity() {};

    public PhysicalEntity(String id, List<Attribute> attributes) {
        this.id = id;
        this.attributes = attributes;
    }

    public String getId() {
        return this.id;
    }

    public List<Attribute> getAttributes(){
        return this.attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }



    @Override
    public String toString() {
        return "PhysicalEntity{" +
                "id='" + id + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
