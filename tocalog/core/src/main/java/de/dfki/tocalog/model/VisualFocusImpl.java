package de.dfki.tocalog.model;

public class VisualFocusImpl implements de.dfki.tocalog.model.VisualFocus{

    //fields
    private java.util.Optional<String> focus;
    private java.util.Optional<String> person;

    //fields for base class composition
    private java.util.Optional<Entity> Entity_composite = java.util.Optional.of(de.dfki.tocalog.model.Entity.create());


    public VisualFocusImpl() {
        super();

        this.focus = java.util.Optional.empty();

        this.person = java.util.Optional.empty();

    }

    //getter / setter
    public java.util.Optional<String> getFocus() {
        return this.focus;
    }
    public VisualFocusImpl setFocus(String value) {
        this.focus = java.util.Optional.ofNullable(value);
        return this;
    }

    public java.util.Optional<String> getPerson() {
        return this.person;
    }
    public VisualFocusImpl setPerson(String value) {
        this.person = java.util.Optional.ofNullable(value);
        return this;
    }





    //getter / setter for base class

    public java.util.Optional<String> getId() {
        return this.Entity_composite.get().getId();
    }
    public VisualFocusImpl setId(String value) {
        this.Entity_composite.get().setId(value);
        return this;
    }

    public java.util.Optional<java.util.List<String>> getYo() {
        return this.Entity_composite.get().getYo();
    }
    public VisualFocusImpl setYo(java.util.List<String> value) {
        this.Entity_composite.get().setYo(value);
        return this;
    }




    //serialization + helper
    public static final java.util.Map<String, Integer> FIELD_TO_ID_MAP;
    static {
        java.util.Map<String, Integer> tmp = new java.util.HashMap<String, Integer>();

        tmp.put("focus", 1);

        tmp.put("person", 2);

        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();

        tmp.put(1, "focus");

        tmp.put(2, "person");

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

                case 1: {
                        String tmp_focus;tmp_focus = deserializer.readString();this.focus = java.util.Optional.of(tmp_focus);
                    } break;

                case 2: {
                        String tmp_person;tmp_person = deserializer.readString();this.person = java.util.Optional.of(tmp_person);
                    } break;


                case 3: {
                    this.Entity_composite.get().deserialize(deserializer);
                    } break;

            }
        }
        //deserializer.endReadObject(this);
    }

    public void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException {
        serializer.beginWriteObject(this);

        if(this.focus.isPresent()) {
            serializer.beginWriteField(1, "focus");
            serializer.writeString(focus.get());
            serializer.endWriteField(1, "focus");
        }

        if(this.person.isPresent()) {
            serializer.beginWriteField(2, "person");
            serializer.writeString(person.get());
            serializer.endWriteField(2, "person");
        }


        if(this.Entity_composite.isPresent()) {
            serializer.beginWriteField(3, "Entity_composite");
            this.Entity_composite.get().serialize(serializer);
            serializer.endWriteField(3, "Entity_composite");
        }
        serializer.endWriteObject(this);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VisualFocus{ ");

            sb.append(this.Entity_composite.get() + " ");


            focus.ifPresent(x -> sb.append(" focus=" +x.toString()));

            person.ifPresent(x -> sb.append(" person=" +x.toString()));

        sb.append("}");
        return sb.toString();
    }



    @Override
    public VisualFocus copy(de.dfki.tractat.idl.Serializer serializer, de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException {
        byte[] buf = serializer.serialize(this);
        VisualFocus copy = de.dfki.tocalog.model.VisualFocus.factory.create();
        deserializer.deserialize(buf, copy);
        return copy;
    }
}

