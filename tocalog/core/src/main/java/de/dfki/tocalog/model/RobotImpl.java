package de.dfki.tocalog.model;

public class RobotImpl implements de.dfki.tocalog.model.Robot{

    //fields 

    //fields for base class composition 
    private java.util.Optional<de.dfki.tocalog.model.Agent> Agent_composite = java.util.Optional.of(de.dfki.tocalog.model.Agent.create());
    

    public RobotImpl() {
        super();
    
    }

    //getter / setter 




    //getter / setter for base class
    
    public java.util.Optional<java.lang.String> getName() {
        return this.Agent_composite.get().getName();
    }
    public RobotImpl setName(java.lang.String value) {
        this.Agent_composite.get().setName(value);
        return this;
    }
    public boolean isNamePresent() {
        return this.Agent_composite.get().getName().isPresent();
    }
    
    public java.util.Optional<java.lang.String> getId() {
        return this.Agent_composite.get().getId();
    }
    public RobotImpl setId(java.lang.String value) {
        this.Agent_composite.get().setId(value);
        return this;
    }
    public boolean isIdPresent() {
        return this.Agent_composite.get().getId().isPresent();
    }
    
    public java.util.Optional<java.lang.Long> getTimestamp() {
        return this.Agent_composite.get().getTimestamp();
    }
    public RobotImpl setTimestamp(java.lang.Long value) {
        this.Agent_composite.get().setTimestamp(value);
        return this;
    }
    public boolean isTimestampPresent() {
        return this.Agent_composite.get().getTimestamp().isPresent();
    }
    
    public java.util.Optional<java.lang.String> getSource() {
        return this.Agent_composite.get().getSource();
    }
    public RobotImpl setSource(java.lang.String value) {
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
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
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
                
                
                case 1: {
                    this.Agent_composite.get().deserialize(deserializer);
                    } break;
                
            }
        }
        //deserializer.endReadObject(this);
    }

    public void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException {
        serializer.beginWriteObject(this);
        
        
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
        sb.append("Robot{ ");
            
            sb.append(this.Agent_composite.get() + " ");
            
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Robot copy() {
        Robot copy = de.dfki.tocalog.model.Robot.factory.create();
        
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RobotImpl o_cast = (RobotImpl) o;
        return 
            true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
            
        );
    }

}

