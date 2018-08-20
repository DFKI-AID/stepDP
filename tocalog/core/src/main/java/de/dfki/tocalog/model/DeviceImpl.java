package de.dfki.tocalog.model;

public class DeviceImpl implements de.dfki.tocalog.model.Device{

    //fields 
    private java.util.Optional<java.lang.String> name; 

    //fields for base class composition 
    private java.util.Optional<de.dfki.tocalog.model.Entity> Entity_composite = java.util.Optional.of(de.dfki.tocalog.model.Entity.create());
    

    public DeviceImpl() {
        super();
    
        this.name = java.util.Optional.empty();
    
    }

    //getter / setter 
    public java.util.Optional<java.lang.String> getName() {
        return this.name;
    }
    public DeviceImpl setName(java.lang.String value) {
        this.name = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isNamePresent() {
        return this.name.isPresent();
    }
    




    //getter / setter for base class
    
    public java.util.Optional<java.lang.String> getId() {
        return this.Entity_composite.get().getId();
    }
    public DeviceImpl setId(java.lang.String value) {
        this.Entity_composite.get().setId(value);
        return this;
    }
    public boolean isIdPresent() {
        return this.Entity_composite.get().getId().isPresent();
    }
    
    public java.util.Optional<java.lang.Long> getTimestamp() {
        return this.Entity_composite.get().getTimestamp();
    }
    public DeviceImpl setTimestamp(java.lang.Long value) {
        this.Entity_composite.get().setTimestamp(value);
        return this;
    }
    public boolean isTimestampPresent() {
        return this.Entity_composite.get().getTimestamp().isPresent();
    }
    
    public java.util.Optional<java.lang.String> getSource() {
        return this.Entity_composite.get().getSource();
    }
    public DeviceImpl setSource(java.lang.String value) {
        this.Entity_composite.get().setSource(value);
        return this;
    }
    public boolean isSourcePresent() {
        return this.Entity_composite.get().getSource().isPresent();
    }
    



    //serialization + helper
    public static final java.util.Map<String, Integer> FIELD_TO_ID_MAP;
    static {
        java.util.Map<String, Integer> tmp = new java.util.HashMap<String, Integer>();
    
        tmp.put("name", 2);
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
        tmp.put(2, "name");
        
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
                        java.lang.String tmp_name;tmp_name = deserializer.readString();this.name = java.util.Optional.of(tmp_name);
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
        
        if(this.name.isPresent()) {
            serializer.beginWriteField(2, "name");
            serializer.writeString(name.get());
            serializer.endWriteField(2, "name");
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
        sb.append("Device{ ");
            
            sb.append(this.Entity_composite.get() + " ");
            
            
            name.ifPresent(x -> sb.append(" name=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Device copy() {
        DeviceImpl copy = new DeviceImpl();
        if(name.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = name.get();
        	copy.setName(fieldCopy);
        }
        

        copy.Entity_composite = java.util.Optional.of(this.Entity_composite.get().copy());
        
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceImpl o_cast = (DeviceImpl) o;
        return 
            java.util.Objects.equals(name, o_cast.name) &
        
            true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
            name
        );
    }

}

