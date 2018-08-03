package de.dfki.tocalog.model;

public class ServiceImpl implements de.dfki.tocalog.model.Service{

    //fields 
    private java.util.Optional<java.lang.String> uri; 
    private java.util.Optional<java.lang.String> type; 
    private java.util.Optional<java.util.List<java.lang.String>> components; 

    //fields for base class composition 
    private java.util.Optional<de.dfki.tocalog.model.Entity> Entity_composite = java.util.Optional.of(de.dfki.tocalog.model.Entity.create());
    

    public ServiceImpl() {
        super();
    
        this.uri = java.util.Optional.empty();
    
        this.type = java.util.Optional.empty();
    
        this.components = java.util.Optional.empty();
    
    }

    //getter / setter 
    public java.util.Optional<java.lang.String> getUri() {
        return this.uri;
    }
    public ServiceImpl setUri(java.lang.String value) {
        this.uri = java.util.Optional.ofNullable(value);
        return this;
    }
    
    public java.util.Optional<java.lang.String> getType() {
        return this.type;
    }
    public ServiceImpl setType(java.lang.String value) {
        this.type = java.util.Optional.ofNullable(value);
        return this;
    }
    
    public java.util.Optional<java.util.List<java.lang.String>> getComponents() {
        return this.components;
    }
    public ServiceImpl setComponents(java.util.List<java.lang.String> value) {
        this.components = java.util.Optional.ofNullable(value);
        return this;
    }
    




    //getter / setter for base class
    
    public java.util.Optional<java.lang.String> getId() {
        return this.Entity_composite.get().getId();
    }
    public ServiceImpl setId(java.lang.String value) {
        this.Entity_composite.get().setId(value);
        return this;
    }
    
    public java.util.Optional<java.lang.Long> getTimestamp() {
        return this.Entity_composite.get().getTimestamp();
    }
    public ServiceImpl setTimestamp(java.lang.Long value) {
        this.Entity_composite.get().setTimestamp(value);
        return this;
    }
    
    public java.util.Optional<java.lang.String> getSource() {
        return this.Entity_composite.get().getSource();
    }
    public ServiceImpl setSource(java.lang.String value) {
        this.Entity_composite.get().setSource(value);
        return this;
    }
    



    //serialization + helper
    public static final java.util.Map<String, Integer> FIELD_TO_ID_MAP;
    static {
        java.util.Map<String, Integer> tmp = new java.util.HashMap<String, Integer>();
    
        tmp.put("uri", 2);
    
        tmp.put("type", 3);
    
        tmp.put("components", 4);
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
        tmp.put(2, "uri");
        
        tmp.put(3, "type");
        
        tmp.put(4, "components");
        
        ID_TO_FIELD_MAP = java.util.Collections.unmodifiableMap(tmp);
    }


    public void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException {
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
                        java.util.ArrayList<java.lang.String> tmp_components;int n = deserializer.beginReadList();
                        tmp_components = new java.util.ArrayList<>();
                        for(int i=0; i<n; i++) {
                            java.lang.String tmp;
                            tmp = deserializer.readString();
                            tmp_components.add(tmp);
                        }
                        deserializer.endReadList();this.components = java.util.Optional.of(tmp_components);
                    } break;
                
                
                case 1: {
                    this.Entity_composite.get().deserialize(deserializer);
                    } break;
                
            }
        }
        //deserializer.endReadObject(this);
    }

    public void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException {
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
        
        if(this.components.isPresent()) {
            serializer.beginWriteField(4, "components");
            serializer.beginWriteList(components.get().size());
            for(java.lang.String tmp : components.get()) { serializer.writeString(tmp); }
            serializer.endWriteList();;
            serializer.endWriteField(4, "components");
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
            
            components.ifPresent(x -> sb.append(" components=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Service copy(de.dfki.tractat.idl.Serializer serializer, de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException {
        byte[] buf = serializer.serialize(this);
        Service copy = de.dfki.tocalog.model.Service.factory.create();
        deserializer.deserialize(buf, copy);
        return copy;
    }
}

