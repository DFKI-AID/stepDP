package de.dfki.tocalog.model;

public class EntityImpl implements de.dfki.tocalog.model.Entity{

    //fields 
    private java.util.Optional<java.lang.String> id; 
    private java.util.Optional<java.lang.Long> timestamp; 
    private java.util.Optional<java.lang.String> source; 

    //fields for base class composition 

    public EntityImpl() {
        super();
    
        this.id = java.util.Optional.empty();
    
        this.timestamp = java.util.Optional.empty();
    
        this.source = java.util.Optional.empty();
    
    }

    //getter / setter 
    public java.util.Optional<java.lang.String> getId() {
        return this.id;
    }
    public EntityImpl setId(java.lang.String value) {
        this.id = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isIdPresent() {
        return this.id.isPresent();
    }
    
    public java.util.Optional<java.lang.Long> getTimestamp() {
        return this.timestamp;
    }
    public EntityImpl setTimestamp(java.lang.Long value) {
        this.timestamp = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isTimestampPresent() {
        return this.timestamp.isPresent();
    }
    
    public java.util.Optional<java.lang.String> getSource() {
        return this.source;
    }
    public EntityImpl setSource(java.lang.String value) {
        this.source = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isSourcePresent() {
        return this.source.isPresent();
    }
    




    //getter / setter for base class
    



    //serialization + helper
    public static final java.util.Map<String, Integer> FIELD_TO_ID_MAP;
    static {
        java.util.Map<String, Integer> tmp = new java.util.HashMap<String, Integer>();
    
        tmp.put("id", 1);
    
        tmp.put("timestamp", 2);
    
        tmp.put("source", 3);
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
        tmp.put(1, "id");
        
        tmp.put(2, "timestamp");
        
        tmp.put(3, "source");
        
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
                        java.lang.String tmp_id;tmp_id = deserializer.readString();this.id = java.util.Optional.of(tmp_id);
                    } break;
                
                case 2: {
                        java.lang.Long tmp_timestamp;tmp_timestamp = deserializer.readI64();this.timestamp = java.util.Optional.of(tmp_timestamp);
                    } break;
                
                case 3: {
                        java.lang.String tmp_source;tmp_source = deserializer.readString();this.source = java.util.Optional.of(tmp_source);
                    } break;
                
                
            }
        }
        //deserializer.endReadObject(this);
    }

    public void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException {
        serializer.beginWriteObject(this);
        
        if(this.id.isPresent()) {
            serializer.beginWriteField(1, "id");
            serializer.writeString(id.get());
            serializer.endWriteField(1, "id");
        }
        
        if(this.timestamp.isPresent()) {
            serializer.beginWriteField(2, "timestamp");
            serializer.writeI64(timestamp.get());
            serializer.endWriteField(2, "timestamp");
        }
        
        if(this.source.isPresent()) {
            serializer.beginWriteField(3, "source");
            serializer.writeString(source.get());
            serializer.endWriteField(3, "source");
        }
        
        
        serializer.endWriteObject(this);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Entity{ ");
            
            
            id.ifPresent(x -> sb.append(" id=" +x.toString()));
            
            timestamp.ifPresent(x -> sb.append(" timestamp=" +x.toString()));
            
            source.ifPresent(x -> sb.append(" source=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Entity copy() {
        Entity copy = de.dfki.tocalog.model.Entity.factory.create();
        if(id.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = id.get();
        	copy.setId(fieldCopy);
        }
        if(timestamp.isPresent()) {
        	java.lang.Long fieldCopy;
        	fieldCopy = timestamp.get();
        	copy.setTimestamp(fieldCopy);
        }
        if(source.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = source.get();
        	copy.setSource(fieldCopy);
        }
        
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityImpl o_cast = (EntityImpl) o;
        return 
            java.util.Objects.equals(id, o_cast.id) &
        
            java.util.Objects.equals(timestamp, o_cast.timestamp) &
        
            java.util.Objects.equals(source, o_cast.source) &
        
            true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
            id,timestamp,source
        );
    }

}

