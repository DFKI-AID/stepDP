package de.dfki.tocalog.model;

public class SessionImpl implements de.dfki.tocalog.model.Session{

    //fields 
    private java.util.Optional<java.util.List<java.lang.String>> agents; 

    //fields for base class composition 
    private java.util.Optional<de.dfki.tocalog.model.Entity> Entity_composite = java.util.Optional.of(de.dfki.tocalog.model.Entity.create());
    

    public SessionImpl() {
        super();
    
        this.agents = java.util.Optional.empty();
    
    }

    //getter / setter 
    public java.util.Optional<java.util.List<java.lang.String>> getAgents() {
        return this.agents;
    }
    public SessionImpl setAgents(java.util.List<java.lang.String> value) {
        this.agents = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isAgentsPresent() {
        return this.agents.isPresent();
    }
    




    //getter / setter for base class
    
    public java.util.Optional<java.lang.String> getId() {
        return this.Entity_composite.get().getId();
    }
    public SessionImpl setId(java.lang.String value) {
        this.Entity_composite.get().setId(value);
        return this;
    }
    public boolean isIdPresent() {
        return this.Entity_composite.get().getId().isPresent();
    }
    
    public java.util.Optional<java.lang.Long> getTimestamp() {
        return this.Entity_composite.get().getTimestamp();
    }
    public SessionImpl setTimestamp(java.lang.Long value) {
        this.Entity_composite.get().setTimestamp(value);
        return this;
    }
    public boolean isTimestampPresent() {
        return this.Entity_composite.get().getTimestamp().isPresent();
    }
    
    public java.util.Optional<java.lang.String> getSource() {
        return this.Entity_composite.get().getSource();
    }
    public SessionImpl setSource(java.lang.String value) {
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
    
        tmp.put("agents", 2);
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
        tmp.put(2, "agents");
        
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
                        java.util.ArrayList<java.lang.String> tmp_agents;int n = deserializer.beginReadList();
                        tmp_agents = new java.util.ArrayList<>();
                        for(int i=0; i<n; i++) {
                            java.lang.String tmp;
                            tmp = deserializer.readString();
                            tmp_agents.add(tmp);
                        }
                        deserializer.endReadList();this.agents = java.util.Optional.of(tmp_agents);
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
        
        if(this.agents.isPresent()) {
            serializer.beginWriteField(2, "agents");
            serializer.beginWriteList(agents.get().size());
            for(java.lang.String tmp : agents.get()) { serializer.writeString(tmp); }
            serializer.endWriteList();;
            serializer.endWriteField(2, "agents");
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
        sb.append("Session{ ");
            
            sb.append(this.Entity_composite.get() + " ");
            
            
            agents.ifPresent(x -> sb.append(" agents=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Session copy() {
        SessionImpl copy = new SessionImpl();
        if(agents.isPresent()) {
        	java.util.ArrayList<java.lang.String> fieldCopy;
        	fieldCopy = new java.util.ArrayList<>();
            for(java.lang.String tmp : agents.get()) {
                java.lang.String tmpCopy;
                tmpCopy = tmp;
                fieldCopy.add(tmpCopy);
            }
        	copy.setAgents(fieldCopy);
        }
        

        copy.Entity_composite = java.util.Optional.of(this.Entity_composite.get().copy());
        
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionImpl o_cast = (SessionImpl) o;
        return 
            java.util.Objects.equals(agents, o_cast.agents) &
        
            true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
            agents
        );
    }

}

