package de.dfki.tocalog.model;

public class TripleImpl implements de.dfki.tocalog.model.Triple{

    //fields 
    private java.util.Optional<java.lang.String> subject; 
    private java.util.Optional<java.lang.String> predicate; 
    private java.util.Optional<java.lang.String> object; 

    //fields for base class composition 
    private java.util.Optional<de.dfki.tocalog.model.Entity> Entity_composite = java.util.Optional.of(de.dfki.tocalog.model.Entity.create());
    

    public TripleImpl() {
        super();
    
        this.subject = java.util.Optional.empty();
    
        this.predicate = java.util.Optional.empty();
    
        this.object = java.util.Optional.empty();
    
    }

    //getter / setter 
    public java.util.Optional<java.lang.String> getSubject() {
        return this.subject;
    }
    public TripleImpl setSubject(java.lang.String value) {
        this.subject = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isSubjectPresent() {
        return this.subject.isPresent();
    }
    
    public java.util.Optional<java.lang.String> getPredicate() {
        return this.predicate;
    }
    public TripleImpl setPredicate(java.lang.String value) {
        this.predicate = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isPredicatePresent() {
        return this.predicate.isPresent();
    }
    
    public java.util.Optional<java.lang.String> getObject() {
        return this.object;
    }
    public TripleImpl setObject(java.lang.String value) {
        this.object = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isObjectPresent() {
        return this.object.isPresent();
    }
    




    //getter / setter for base class
    
    public java.util.Optional<java.lang.String> getId() {
        return this.Entity_composite.get().getId();
    }
    public TripleImpl setId(java.lang.String value) {
        this.Entity_composite.get().setId(value);
        return this;
    }
    public boolean isIdPresent() {
        return this.Entity_composite.get().getId().isPresent();
    }
    
    public java.util.Optional<java.lang.Long> getTimestamp() {
        return this.Entity_composite.get().getTimestamp();
    }
    public TripleImpl setTimestamp(java.lang.Long value) {
        this.Entity_composite.get().setTimestamp(value);
        return this;
    }
    public boolean isTimestampPresent() {
        return this.Entity_composite.get().getTimestamp().isPresent();
    }
    
    public java.util.Optional<java.lang.String> getSource() {
        return this.Entity_composite.get().getSource();
    }
    public TripleImpl setSource(java.lang.String value) {
        this.Entity_composite.get().setSource(value);
        return this;
    }
    public boolean isSourcePresent() {
        return this.Entity_composite.get().getSource().isPresent();
    }
    
    public java.util.Optional<java.lang.Double> getConfidence() {
        return this.Entity_composite.get().getConfidence();
    }
    public TripleImpl setConfidence(java.lang.Double value) {
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
    
        tmp.put("subject", 2);
    
        tmp.put("predicate", 3);
    
        tmp.put("object", 4);
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
        tmp.put(2, "subject");
        
        tmp.put(3, "predicate");
        
        tmp.put(4, "object");
        
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
                        java.lang.String tmp_subject;tmp_subject = deserializer.readString();this.subject = java.util.Optional.of(tmp_subject);
                    } break;
                
                case 3: {
                        java.lang.String tmp_predicate;tmp_predicate = deserializer.readString();this.predicate = java.util.Optional.of(tmp_predicate);
                    } break;
                
                case 4: {
                        java.lang.String tmp_object;tmp_object = deserializer.readString();this.object = java.util.Optional.of(tmp_object);
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
        
        if(this.subject.isPresent()) {
            serializer.beginWriteField(2, "subject");
            serializer.writeString(subject.get());
            serializer.endWriteField(2, "subject");
        }
        
        if(this.predicate.isPresent()) {
            serializer.beginWriteField(3, "predicate");
            serializer.writeString(predicate.get());
            serializer.endWriteField(3, "predicate");
        }
        
        if(this.object.isPresent()) {
            serializer.beginWriteField(4, "object");
            serializer.writeString(object.get());
            serializer.endWriteField(4, "object");
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
        sb.append("Triple{ ");
            
            sb.append(this.Entity_composite.get() + " ");
            
            
            subject.ifPresent(x -> sb.append(" subject=" +x.toString()));
            
            predicate.ifPresent(x -> sb.append(" predicate=" +x.toString()));
            
            object.ifPresent(x -> sb.append(" object=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Triple copy() {
        TripleImpl copy = new TripleImpl();
        if(subject.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = subject.get();
        	copy.setSubject(fieldCopy);
        }
        if(predicate.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = predicate.get();
        	copy.setPredicate(fieldCopy);
        }
        if(object.isPresent()) {
        	java.lang.String fieldCopy;
        	fieldCopy = object.get();
        	copy.setObject(fieldCopy);
        }
        

        copy.Entity_composite = java.util.Optional.of(this.Entity_composite.get().copy());
        
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripleImpl o_cast = (TripleImpl) o;
        return 
            java.util.Objects.equals(subject, o_cast.subject) &
        
            java.util.Objects.equals(predicate, o_cast.predicate) &
        
            java.util.Objects.equals(object, o_cast.object) &
        
            true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
            subject,predicate,object
        );
    }

}

