package de.dfki.tocalog.model;

public class FocusImpl implements de.dfki.tocalog.model.Focus{

    //fields 
    private java.util.Optional<java.lang.String> agent; 
    private java.util.Optional<java.lang.String> focus; 

    //fields for base class composition 
    private java.util.Optional<de.dfki.tocalog.model.Entity> Entity_composite = java.util.Optional.of(de.dfki.tocalog.model.Entity.create());
    

    public FocusImpl() {
        super();
    
        this.agent = java.util.Optional.empty();
    
        this.focus = java.util.Optional.empty();
    
    }

    //getter / setter 
    public java.util.Optional<java.lang.String> getAgent() {
        return this.agent;
    }
    public FocusImpl setAgent(java.lang.String value) {
        this.agent = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isAgentPresent() {
        return this.agent.isPresent();
    }
    
    public java.util.Optional<java.lang.String> getFocus() {
        return this.focus;
    }
    public FocusImpl setFocus(java.lang.String value) {
        this.focus = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isFocusPresent() {
        return this.focus.isPresent();
    }
    




    //getter / setter for base class
    
    public java.util.Optional<java.lang.String> getId() {
        return this.Entity_composite.get().getId();
    }
    public FocusImpl setId(java.lang.String value) {
        this.Entity_composite.get().setId(value);
        return this;
    }
    public boolean isIdPresent() {
        return this.Entity_composite.get().getId().isPresent();
    }
    
    public java.util.Optional<java.lang.Long> getTimestamp() {
        return this.Entity_composite.get().getTimestamp();
    }
    public FocusImpl setTimestamp(java.lang.Long value) {
        this.Entity_composite.get().setTimestamp(value);
        return this;
    }
    public boolean isTimestampPresent() {
        return this.Entity_composite.get().getTimestamp().isPresent();
    }
    
    public java.util.Optional<java.lang.String> getSource() {
        return this.Entity_composite.get().getSource();
    }
    public FocusImpl setSource(java.lang.String value) {
        this.Entity_composite.get().setSource(value);
        return this;
    }
    public boolean isSourcePresent() {
        return this.Entity_composite.get().getSource().isPresent();
    }
    
    public java.util.Optional<java.lang.Double> getConfidence() {
        return this.Entity_composite.get().getConfidence();
    }
    public FocusImpl setConfidence(java.lang.Double value) {
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
    
        tmp.put("agent", 2);
    
        tmp.put("focus", 3);
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
        tmp.put(2, "agent");
        
        tmp.put(3, "focus");
        
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
                        java.lang.String tmp_agent;tmp_agent = deserializer.readString();this.agent = java.util.Optional.of(tmp_agent);
                    } break;
                
                case 3: {
                        java.lang.String tmp_focus;tmp_focus = deserializer.readString();this.focus = java.util.Optional.of(tmp_focus);
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
        
        if(this.agent.isPresent()) {
            serializer.beginWriteField(2, "agent");
            serializer.writeString(agent.get());
            serializer.endWriteField(2, "agent");
        }
        
        if(this.focus.isPresent()) {
            serializer.beginWriteField(3, "focus");
            serializer.writeString(focus.get());
            serializer.endWriteField(3, "focus");
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
        sb.append("Focus{ ");
            
            sb.append(this.Entity_composite.get() + " ");
            
            
            agent.ifPresent(x -> sb.append(" agent=" +x.toString()));
            
            focus.ifPresent(x -> sb.append(" focus=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Focus copy() {
        FocusImpl copy = new FocusImpl();
        if(agent.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = agent.get();
        	copy.setAgent(fieldCopy);
        }
        if(focus.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = focus.get();
        	copy.setFocus(fieldCopy);
        }
        

        copy.Entity_composite = java.util.Optional.of(this.Entity_composite.get().copy());
        
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FocusImpl o_cast = (FocusImpl) o;
        return 
            java.util.Objects.equals(agent, o_cast.agent) &
        
            java.util.Objects.equals(focus, o_cast.focus) &
        
            true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
            agent,focus
        );
    }

}

