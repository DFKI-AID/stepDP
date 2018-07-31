package de.dfki.tocalog.model;

public class EntityImpl implements de.dfki.tocalog.model.Entity{

    //fields
    private java.util.Optional<String> id;
    private java.util.Optional<java.util.List<String>> yo;

    //fields for base class composition

    public EntityImpl() {
        super();

        this.id = java.util.Optional.empty();

        this.yo = java.util.Optional.empty();

    }

    //getter / setter
    public java.util.Optional<String> getId() {
        return this.id;
    }
    public EntityImpl setId(String value) {
        this.id = java.util.Optional.ofNullable(value);
        return this;
    }

    public java.util.Optional<java.util.List<String>> getYo() {
        return this.yo;
    }
    public EntityImpl setYo(java.util.List<String> value) {
        this.yo = java.util.Optional.ofNullable(value);
        return this;
    }





    //getter / setter for base class




    //serialization + helper
    public static final java.util.Map<String, Integer> FIELD_TO_ID_MAP;
    static {
        java.util.Map<String, Integer> tmp = new java.util.HashMap<String, Integer>();

        tmp.put("id", 1);

        tmp.put("yo", 2);

        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();

        tmp.put(1, "id");

        tmp.put(2, "yo");

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
                        String tmp_id;tmp_id = deserializer.readString();this.id = java.util.Optional.of(tmp_id);
                    } break;

                case 2: {
                        java.util.ArrayList<String> tmp_yo;int n = deserializer.beginReadList();
                        tmp_yo = new java.util.ArrayList<>();
                        for(int i=0; i<n; i++) {
                            String tmp;
                            tmp = deserializer.readString();
                            tmp_yo.add(tmp);
                        }
                        deserializer.endReadList();this.yo = java.util.Optional.of(tmp_yo);
                    } break;


            }
        }
        //deserializer.endReadObject(this);
    }

    public void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException {
        serializer.beginWriteObject(this);

        if(this.id.isPresent()) {
            serializer.beginWriteField(1, "id");
            serializer.writeString(id.get());
            serializer.endWriteField(1, "id");
        }

        if(this.yo.isPresent()) {
            serializer.beginWriteField(2, "yo");
            serializer.beginWriteList(yo.get().size());
            for(String tmp : yo.get()) { serializer.writeString(tmp); }
            serializer.endWriteList();;
            serializer.endWriteField(2, "yo");
        }


        serializer.endWriteObject(this);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Entity{ ");


            id.ifPresent(x -> sb.append(" id=" +x.toString()));

            yo.ifPresent(x -> sb.append(" yo=" +x.toString()));

        sb.append("}");
        return sb.toString();
    }



    @Override
    public Entity copy(de.dfki.tractat.idl.Serializer serializer, de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException {
        byte[] buf = serializer.serialize(this);
        Entity copy = de.dfki.tocalog.model.Entity.factory.create();
        deserializer.deserialize(buf, copy);
        return copy;
    }
}

