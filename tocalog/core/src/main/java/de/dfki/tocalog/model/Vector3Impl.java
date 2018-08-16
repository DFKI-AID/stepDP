package de.dfki.tocalog.model;

public class Vector3Impl implements de.dfki.tocalog.model.Vector3{

    //fields 
    private java.util.Optional<java.lang.Double> x; 
    private java.util.Optional<java.lang.Double> y; 
    private java.util.Optional<java.lang.Double> z; 

    //fields for base class composition 

    public Vector3Impl() {
        super();
    
        this.x = java.util.Optional.empty();
    
        this.y = java.util.Optional.empty();
    
        this.z = java.util.Optional.empty();
    
    }

    //getter / setter 
    public java.util.Optional<java.lang.Double> getX() {
        return this.x;
    }
    public Vector3Impl setX(java.lang.Double value) {
        this.x = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isXPresent() {
        return this.x.isPresent();
    }
    
    public java.util.Optional<java.lang.Double> getY() {
        return this.y;
    }
    public Vector3Impl setY(java.lang.Double value) {
        this.y = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isYPresent() {
        return this.y.isPresent();
    }
    
    public java.util.Optional<java.lang.Double> getZ() {
        return this.z;
    }
    public Vector3Impl setZ(java.lang.Double value) {
        this.z = java.util.Optional.ofNullable(value);
        return this;
    }
    public boolean isZPresent() {
        return this.z.isPresent();
    }
    




    //getter / setter for base class
    



    //serialization + helper
    public static final java.util.Map<String, Integer> FIELD_TO_ID_MAP;
    static {
        java.util.Map<String, Integer> tmp = new java.util.HashMap<String, Integer>();
    
        tmp.put("x", 1);
    
        tmp.put("y", 2);
    
        tmp.put("z", 3);
    
        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();
        
        tmp.put(1, "x");
        
        tmp.put(2, "y");
        
        tmp.put(3, "z");
        
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
                        java.lang.Double tmp_x;tmp_x = deserializer.readDouble();this.x = java.util.Optional.of(tmp_x);
                    } break;
                
                case 2: {
                        java.lang.Double tmp_y;tmp_y = deserializer.readDouble();this.y = java.util.Optional.of(tmp_y);
                    } break;
                
                case 3: {
                        java.lang.Double tmp_z;tmp_z = deserializer.readDouble();this.z = java.util.Optional.of(tmp_z);
                    } break;
                
                
            }
        }
        //deserializer.endReadObject(this);
    }

    public void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException {
        serializer.beginWriteObject(this);
        
        if(this.x.isPresent()) {
            serializer.beginWriteField(1, "x");
            serializer.writeDouble(x.get());
            serializer.endWriteField(1, "x");
        }
        
        if(this.y.isPresent()) {
            serializer.beginWriteField(2, "y");
            serializer.writeDouble(y.get());
            serializer.endWriteField(2, "y");
        }
        
        if(this.z.isPresent()) {
            serializer.beginWriteField(3, "z");
            serializer.writeDouble(z.get());
            serializer.endWriteField(3, "z");
        }
        
        
        serializer.endWriteObject(this);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vector3{ ");
            
            
            x.ifPresent(x -> sb.append(" x=" +x.toString()));
            
            y.ifPresent(x -> sb.append(" y=" +x.toString()));
            
            z.ifPresent(x -> sb.append(" z=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }



    @Override
    public Vector3 copy() {
        Vector3 copy = de.dfki.tocalog.model.Vector3.factory.create();
        if(x.isPresent()) {
        	java.lang.Double fieldCopy;
        	fieldCopy = x.get();
        	copy.setX(fieldCopy);
        }
        if(y.isPresent()) {
        	java.lang.Double fieldCopy;
        	fieldCopy = y.get();
        	copy.setY(fieldCopy);
        }
        if(z.isPresent()) {
        	java.lang.Double fieldCopy;
        	fieldCopy = z.get();
        	copy.setZ(fieldCopy);
        }
        
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3Impl o_cast = (Vector3Impl) o;
        return 
            java.util.Objects.equals(x, o_cast.x) &
        
            java.util.Objects.equals(y, o_cast.y) &
        
            java.util.Objects.equals(z, o_cast.z) &
        
            true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
            x,y,z
        );
    }

}

