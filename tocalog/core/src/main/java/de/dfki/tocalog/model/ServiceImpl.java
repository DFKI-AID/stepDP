package de.dfki.tocalog.model;

public class ServiceImpl implements de.dfki.tocalog.model.Service{

    //fields 
    private java.util.Optional<java.lang.String> uri; 
    private java.util.Optional<java.lang.String> type; 
    private java.util.Optional<java.lang.String> device; 

    //fields for base class composition 
    private java.util.Optional<de.dfki.tocalog.model.Entity> Entity_composite = java.util.Optional.of(de.dfki.tocalog.model.Entity.create());
    

    public ServiceImpl() {
        super();
    
        this.uri = java.util.Optional.empty();
    
        this.type = java.util.Optional.empty();
    
        this.device = java.util.Optional.empty();
    
    }

    //getter / setter 
    public java.util.Optional<java.lang.String> getUri() {
        return this.uri;
    }
    public ServiceImpl setUri(java.lang.String value) {
        this.uri = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isUriPresent() {
        return this.uri.isPresent();
    }
    
    public java.util.Optional<java.lang.String> getType() {
        return this.type;
    }
    public ServiceImpl setType(java.lang.String value) {
        this.type = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isTypePresent() {
        return this.type.isPresent();
    }
    
    public java.util.Optional<java.lang.String> getDevice() {
        return this.device;
    }
    public ServiceImpl setDevice(java.lang.String value) {
        this.device = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isDevicePresent() {
        return this.device.isPresent();
    }
    




    //getter / setter for base class
    
    public java.util.Optional<java.lang.String> getId() {
        return this.Entity_composite.get().getId();
    }
    public ServiceImpl setId(java.lang.String value) {
        this.Entity_composite.get().setId(value);
        return this;
    }
    public boolean isIdPresent() {
        return this.Entity_composite.get().getId().isPresent();
    }
    
    public java.util.Optional<java.lang.Long> getTimestamp() {
        return this.Entity_composite.get().getTimestamp();
    }
    public ServiceImpl setTimestamp(java.lang.Long value) {
        this.Entity_composite.get().setTimestamp(value);
        return this;
    }
    public boolean isTimestampPresent() {
        return this.Entity_composite.get().getTimestamp().isPresent();
    }
    
    public java.util.Optional<java.lang.String> getSource() {
        return this.Entity_composite.get().getSource();
    }
    public ServiceImpl setSource(java.lang.String value) {
        this.Entity_composite.get().setSource(value);
        return this;
    }
    public boolean isSourcePresent() {
        return this.Entity_composite.get().getSource().isPresent();
    }
    
    public java.util.Optional<java.lang.Double> getConfidence() {
        return this.Entity_composite.get().getConfidence();
    }
    public ServiceImpl setConfidence(java.lang.Double value) {
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
    
        tmp.put("uri", 2);
    
        tmp.put("type", 3);
    
        tmp.put("device", 4);
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
        tmp.put(2, "uri");
        
        tmp.put(3, "type");
        
        tmp.put(4, "device");
        
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
                        java.lang.String tmp_uri;tmp_uri = deserializer.readString();this.uri = java.util.Optional.of(tmp_uri);
                    } break;
                
                case 3: {
                        java.lang.String tmp_type;tmp_type = deserializer.readString();this.type = java.util.Optional.of(tmp_type);
                    } break;
                
                case 4: {
                        java.lang.String tmp_device;tmp_device = deserializer.readString();this.device = java.util.Optional.of(tmp_device);
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
        
        if(this.uri.isPresent()) {
            serializer.beginWriteField(2, "uri");
            serializer.writeString(uri.get());
            serializer.endWriteField(2, "uri");
        }
        
        if(this.type.isPresent()) {
            serializer.beginWriteField(3, "type");
            serializer.writeString(type.get());
            serializer.endWriteField(3, "type");
        }
        
        if(this.device.isPresent()) {
            serializer.beginWriteField(4, "device");
            serializer.writeString(device.get());
            serializer.endWriteField(4, "device");
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
        sb.append("Service{ ");
            
            sb.append(this.Entity_composite.get() + " ");
            
            
            uri.ifPresent(x -> sb.append(" uri=" +x.toString()));
            
            type.ifPresent(x -> sb.append(" type=" +x.toString()));
            
            device.ifPresent(x -> sb.append(" device=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Service copy() {
        ServiceImpl copy = new ServiceImpl();
        if(uri.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = uri.get();
        	copy.setUri(fieldCopy);
        }
        if(type.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = type.get();
        	copy.setType(fieldCopy);
        }
        if(device.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = device.get();
        	copy.setDevice(fieldCopy);
        }
        

        copy.Entity_composite = java.util.Optional.of(this.Entity_composite.get().copy());
        
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceImpl o_cast = (ServiceImpl) o;
        return 
            java.util.Objects.equals(uri, o_cast.uri) &
        
            java.util.Objects.equals(type, o_cast.type) &
        
            java.util.Objects.equals(device, o_cast.device) &
        
            true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
            uri,type,device
        );
    }

}

