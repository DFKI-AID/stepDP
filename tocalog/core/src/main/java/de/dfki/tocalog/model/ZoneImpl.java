package de.dfki.tocalog.model;

public class ZoneImpl implements de.dfki.tocalog.model.Zone{

    //fields 
    private java.util.Optional<java.lang.String> zone; 

    //fields for base class composition 
    private java.util.Optional<de.dfki.tocalog.model.Entity> Entity_composite = java.util.Optional.of(de.dfki.tocalog.model.Entity.create());
    

    public ZoneImpl() {
        super();
    
        this.zone = java.util.Optional.empty();
    
    }

    //getter / setter 
    public java.util.Optional<java.lang.String> getZone() {
        return this.zone;
    }
    public ZoneImpl setZone(java.lang.String value) {
        this.zone = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isZonePresent() {
        return this.zone.isPresent();
    }
    




    //getter / setter for base class
    
    public java.util.Optional<java.lang.String> getId() {
        return this.Entity_composite.get().getId();
    }
    public ZoneImpl setId(java.lang.String value) {
        this.Entity_composite.get().setId(value);
        return this;
    }
    public boolean isIdPresent() {
        return this.Entity_composite.get().getId().isPresent();
    }
    
    public java.util.Optional<java.lang.Long> getTimestamp() {
        return this.Entity_composite.get().getTimestamp();
    }
    public ZoneImpl setTimestamp(java.lang.Long value) {
        this.Entity_composite.get().setTimestamp(value);
        return this;
    }
    public boolean isTimestampPresent() {
        return this.Entity_composite.get().getTimestamp().isPresent();
    }
    
    public java.util.Optional<java.lang.String> getSource() {
        return this.Entity_composite.get().getSource();
    }
    public ZoneImpl setSource(java.lang.String value) {
        this.Entity_composite.get().setSource(value);
        return this;
    }
    public boolean isSourcePresent() {
        return this.Entity_composite.get().getSource().isPresent();
    }
    
    public java.util.Optional<java.lang.Double> getConfidence() {
        return this.Entity_composite.get().getConfidence();
    }
    public ZoneImpl setConfidence(java.lang.Double value) {
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
    
        tmp.put("zone", 2);
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
        tmp.put(2, "zone");
        
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
                        java.lang.String tmp_zone;tmp_zone = deserializer.readString();this.zone = java.util.Optional.of(tmp_zone);
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
        
        if(this.zone.isPresent()) {
            serializer.beginWriteField(2, "zone");
            serializer.writeString(zone.get());
            serializer.endWriteField(2, "zone");
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
        sb.append("Zone{ ");
            
            sb.append(this.Entity_composite.get() + " ");
            
            
            zone.ifPresent(x -> sb.append(" zone=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Zone copy() {
        ZoneImpl copy = new ZoneImpl();
        if(zone.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = zone.get();
        	copy.setZone(fieldCopy);
        }
        

        copy.Entity_composite = java.util.Optional.of(this.Entity_composite.get().copy());
        
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZoneImpl o_cast = (ZoneImpl) o;
        return 
            java.util.Objects.equals(zone, o_cast.zone) &
        
            true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
            zone
        );
    }

}

