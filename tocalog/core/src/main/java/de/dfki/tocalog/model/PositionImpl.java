package de.dfki.tocalog.model;

public class PositionImpl implements de.dfki.tocalog.model.Position{

    //fields 
    private java.util.Optional<Vector3> position; 

    //fields for base class composition 
    private java.util.Optional<de.dfki.tocalog.model.Entity> Entity_composite = java.util.Optional.of(de.dfki.tocalog.model.Entity.create());
    

    public PositionImpl() {
        super();
    
        this.position = java.util.Optional.empty();
    
    }

    //getter / setter 
    public java.util.Optional<Vector3> getPosition() {
        return this.position;
    }
    public PositionImpl setPosition(Vector3 value) {
        this.position = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isPositionPresent() {
        return this.position.isPresent();
    }
    




    //getter / setter for base class
    
    public java.util.Optional<java.lang.String> getId() {
        return this.Entity_composite.get().getId();
    }
    public PositionImpl setId(java.lang.String value) {
        this.Entity_composite.get().setId(value);
        return this;
    }
    public boolean isIdPresent() {
        return this.Entity_composite.get().getId().isPresent();
    }
    
    public java.util.Optional<java.lang.Long> getTimestamp() {
        return this.Entity_composite.get().getTimestamp();
    }
    public PositionImpl setTimestamp(java.lang.Long value) {
        this.Entity_composite.get().setTimestamp(value);
        return this;
    }
    public boolean isTimestampPresent() {
        return this.Entity_composite.get().getTimestamp().isPresent();
    }
    
    public java.util.Optional<java.lang.String> getSource() {
        return this.Entity_composite.get().getSource();
    }
    public PositionImpl setSource(java.lang.String value) {
        this.Entity_composite.get().setSource(value);
        return this;
    }
    public boolean isSourcePresent() {
        return this.Entity_composite.get().getSource().isPresent();
    }
    
    public java.util.Optional<java.lang.Double> getConfidence() {
        return this.Entity_composite.get().getConfidence();
    }
    public PositionImpl setConfidence(java.lang.Double value) {
        this.Entity_composite.get().setConfidence(value);
        return this;
    }
    public boolean isConfidencePresent() {
        return this.Entity_composite.get().getConfidence().isPresent();
    }
    



    //serialization + helper
    public static final java.util.Map<String, Integer> FIELD_TO_ID_MAP;
    static {
        java.util.Map<String, Integer> tmp = new java.util.HashMap<String, Integer>();
    
        tmp.put("position", 2);
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
        tmp.put(2, "position");
        
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
                        Vector3 tmp_position;tmp_position = Vector3.factory.create(); tmp_position.deserialize(deserializer);this.position = java.util.Optional.of(tmp_position);
                    } break;
                
                
                case 1: {
                    this.Entity_composite.get().deserialize(deserializer);
                    } break;
                
            }
        }
        //deserializer.endReadObject(this);
    }

    public void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException {
        serializer.beginWriteObject(this);
        
        if(this.position.isPresent()) {
            serializer.beginWriteField(2, "position");
            position.get().serialize(serializer);
            serializer.endWriteField(2, "position");
        }
        
        
        if(this.Entity_composite.isPresent()) {
            serializer.beginWriteField(1, "Entity_composite");
            this.Entity_composite.get().serialize(serializer);
            serializer.endWriteField(1, "Entity_composite");
        }
        serializer.endWriteObject(this);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Position{ ");
            
            sb.append(this.Entity_composite.get() + " ");
            
            
            position.ifPresent(x -> sb.append(" position=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Position copy() {
        PositionImpl copy = new PositionImpl();
        if(position.isPresent()) {
        	Vector3 fieldCopy;
        	fieldCopy = position.get().copy();
        	copy.setPosition(fieldCopy);
        }
        

        copy.Entity_composite = java.util.Optional.of(this.Entity_composite.get().copy());
        
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PositionImpl o_cast = (PositionImpl) o;
        return 
            java.util.Objects.equals(position, o_cast.position) &
        
            true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
            position
        );
    }

}

