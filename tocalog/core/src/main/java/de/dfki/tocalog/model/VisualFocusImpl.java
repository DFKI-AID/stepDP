package de.dfki.tocalog.model;

public class VisualFocusImpl implements VisualFocus {

    //fields
    private java.util.Optional<String> id;
    private java.util.Optional<String> focus;


    public VisualFocusImpl() {

        this.id = java.util.Optional.empty();

        this.focus = java.util.Optional.empty();

    }

    //getter / setter
    public java.util.Optional<String> getId() {
        return this.id;
    }
    public VisualFocus setId(String value) {
        this.id = java.util.Optional.ofNullable(value);
        return this;
    }

    public java.util.Optional<String> getFocus() {
        return this.focus;
    }
    public VisualFocus setFocus(String value) {
        this.focus = java.util.Optional.ofNullable(value);
        return this;
    }





    //serialization + helper
    public static final java.util.Map<String, Integer> FIELD_TO_ID_MAP;
    static {
        java.util.Map<String, Integer> tmp = new java.util.HashMap<String, Integer>();

        tmp.put("id", 1);

        tmp.put("focus", 2);

        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();

        tmp.put(1, "id");

        tmp.put(2, "focus");

        ID_TO_FIELD_MAP = java.util.Collections.unmodifiableMap(tmp);
    }


    public void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException {
        deserializer.beginReadObject(this, FIELD_TO_ID_MAP);
        while(true) {
            int fieldId = deserializer.beginReadField();
            if(fieldId < 0) {
                break;
            }
            switch (fieldId) {

                case 1:
                    {
                        String tmp_id;tmp_id = deserializer.readString();this.id = java.util.Optional.of(tmp_id);
                    }
                    break;

                case 2:
                    {
                        String tmp_focus;tmp_focus = deserializer.readString();this.focus = java.util.Optional.of(tmp_focus);
                    }
                    break;
                
            }

        }
        deserializer.endReadObject(this);
    }

    public void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException {
        serializer.beginWriteObject("");
        
        if(this.id.isPresent()) {
            serializer.beginWriteField(1, "id");
            serializer.writeString(id.get());
            serializer.endWriteField(1, "id");
        }
        
        if(this.focus.isPresent()) {
            serializer.beginWriteField(2, "focus");
            serializer.writeString(focus.get());
            serializer.endWriteField(2, "focus");
        }
        
        serializer.endWriteObject("");
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VisualFocus{");
            
            id.ifPresent(x -> sb.append(" id=" +x.toString()));
            
            focus.ifPresent(x -> sb.append(" focus=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }


    @Override
    public VisualFocus copy(de.dfki.tractat.idl.Serializer serializer, de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException {
        byte[] buf = serializer.serialize(this);
        VisualFocus copy = VisualFocus.factory.create();
        deserializer.deserialize(buf, copy);
        return copy;
    }
}

