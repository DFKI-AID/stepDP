package de.dfki.tocalog.model;

public class PersonImpl implements de.dfki.tocalog.model.Person{

    //fields 
    private java.util.Optional<java.lang.Long> age; 
    private java.util.Optional<java.lang.String> gender; 

    //fields for base class composition 
    private java.util.Optional<de.dfki.tocalog.model.Agent> Agent_composite = java.util.Optional.of(de.dfki.tocalog.model.Agent.create());
    

    public PersonImpl() {
        super();
    
        this.age = java.util.Optional.empty();
    
        this.gender = java.util.Optional.empty();
    
    }

    //getter / setter 
    public java.util.Optional<java.lang.Long> getAge() {
        return this.age;
    }
    public PersonImpl setAge(java.lang.Long value) {
        this.age = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isAgePresent() {
        return this.age.isPresent();
    }
    
    public java.util.Optional<java.lang.String> getGender() {
        return this.gender;
    }
    public PersonImpl setGender(java.lang.String value) {
        this.gender = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isGenderPresent() {
        return this.gender.isPresent();
    }
    




    //getter / setter for base class
    
    public java.util.Optional<java.lang.String> getName() {
        return this.Agent_composite.get().getName();
    }
    public PersonImpl setName(java.lang.String value) {
        this.Agent_composite.get().setName(value);
        return this;
    }
    public boolean isNamePresent() {
        return this.Agent_composite.get().getName().isPresent();
    }
    
    public java.util.Optional<java.lang.String> getId() {
        return this.Agent_composite.get().getId();
    }
    public PersonImpl setId(java.lang.String value) {
        this.Agent_composite.get().setId(value);
        return this;
    }
    public boolean isIdPresent() {
        return this.Agent_composite.get().getId().isPresent();
    }
    
    public java.util.Optional<java.lang.Long> getTimestamp() {
        return this.Agent_composite.get().getTimestamp();
    }
    public PersonImpl setTimestamp(java.lang.Long value) {
        this.Agent_composite.get().setTimestamp(value);
        return this;
    }
    public boolean isTimestampPresent() {
        return this.Agent_composite.get().getTimestamp().isPresent();
    }
    
    public java.util.Optional<java.lang.String> getSource() {
        return this.Agent_composite.get().getSource();
    }
    public PersonImpl setSource(java.lang.String value) {
        this.Agent_composite.get().setSource(value);
        return this;
    }
    public boolean isSourcePresent() {
        return this.Agent_composite.get().getSource().isPresent();
    }
    



    //serialization + helper
    public static final java.util.Map<String, Integer> FIELD_TO_ID_MAP;
    static {
        java.util.Map<String, Integer> tmp = new java.util.HashMap<String, Integer>();
    
        tmp.put("age", 2);
    
        tmp.put("gender", 3);
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
        tmp.put(2, "age");
        
        tmp.put(3, "gender");
        
        ID_TO_FIELD_MAP = java.util.Collections.unmodifiableMap(tmp);
    }


    public void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException {
        deserializer.beginReadObject(this);
        while(true) {
            int fieldId = deserializer.beginReadField();
            if(fieldId < 0) {
                throw new java.io.IOException("unexpected fieldId: " + fieldId);
            }
            if(fieldId == 0) {
                break;
            }
            switch (fieldId) {
                
                case 2: {
                        java.lang.Long tmp_age;tmp_age = deserializer.readI64();this.age = java.util.Optional.of(tmp_age);
                    } break;
                
                case 3: {
                        java.lang.String tmp_gender;tmp_gender = deserializer.readString();this.gender = java.util.Optional.of(tmp_gender);
                    } break;
                
                
                case 1: {
                    this.Agent_composite.get().deserialize(deserializer);
                    } break;
                
            }
        }
        //deserializer.endReadObject(this);
    }

    public void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException {
        serializer.beginWriteObject(this);
        
        if(this.age.isPresent()) {
            serializer.beginWriteField(2, "age");
            serializer.writeI64(age.get());
            serializer.endWriteField(2, "age");
        }
        
        if(this.gender.isPresent()) {
            serializer.beginWriteField(3, "gender");
            serializer.writeString(gender.get());
            serializer.endWriteField(3, "gender");
        }
        
        
        if(this.Agent_composite.isPresent()) {
            serializer.beginWriteField(1, "Agent_composite");
            this.Agent_composite.get().serialize(serializer);
            serializer.endWriteField(1, "Agent_composite");
        }
        serializer.endWriteObject(this);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Person{ ");
            
            sb.append(this.Agent_composite.get() + " ");
            
            
            age.ifPresent(x -> sb.append(" age=" +x.toString()));
            
            gender.ifPresent(x -> sb.append(" gender=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Person copy(de.dfki.sire.Serializer serializer, de.dfki.sire.Deserializer deserializer) throws java.io.IOException {
        byte[] buf = serializer.serialize(this);
        Person copy = de.dfki.tocalog.model.Person.factory.create();
        deserializer.deserialize(buf, copy);
        return copy;
    }
}

