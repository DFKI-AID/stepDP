package de.dfki.tocalog.model;

public class PersonImpl implements de.dfki.tocalog.model.Person{

    //fields
    private java.util.Optional<String> name;
    private java.util.Optional<java.util.List<String>> things;

    //fields for base class composition
    private java.util.Optional<Entity> Entity_composite = java.util.Optional.of(de.dfki.tocalog.model.Entity.create());


    public PersonImpl() {
        super();

        this.name = java.util.Optional.empty();

        this.things = java.util.Optional.empty();

    }

    //getter / setter
    public java.util.Optional<String> getName() {
        return this.name;
    }
    public PersonImpl setName(String value) {
        this.name = java.util.Optional.ofNullable(value);
        return this;
    }

    public java.util.Optional<java.util.List<String>> getThings() {
        return this.things;
    }
    public PersonImpl setThings(java.util.List<String> value) {
        this.things = java.util.Optional.ofNullable(value);
        return this;
    }





    //getter / setter for base class

    public java.util.Optional<String> getId() {
        return this.Entity_composite.get().getId();
    }
    public PersonImpl setId(String value) {
        this.Entity_composite.get().setId(value);
        return this;
    }

    public java.util.Optional<java.util.List<String>> getYo() {
        return this.Entity_composite.get().getYo();
    }
    public PersonImpl setYo(java.util.List<String> value) {
        this.Entity_composite.get().setYo(value);
        return this;
    }




    //serialization + helper
    public static final java.util.Map<String, Integer> FIELD_TO_ID_MAP;
    static {
        java.util.Map<String, Integer> tmp = new java.util.HashMap<String, Integer>();

        tmp.put("name", 1);

        tmp.put("things", 2);

        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();

        tmp.put(1, "name");

        tmp.put(2, "things");

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
                        String tmp_name;tmp_name = deserializer.readString();this.name = java.util.Optional.of(tmp_name);
                    } break;

                case 2: {
                        java.util.ArrayList<String> tmp_things;int n = deserializer.beginReadList();
                        tmp_things = new java.util.ArrayList<>();
                        for(int i=0; i<n; i++) {
                            String tmp;
                            tmp = deserializer.readString();
                            tmp_things.add(tmp);
                        }
                        deserializer.endReadList();this.things = java.util.Optional.of(tmp_things);
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

        if(this.name.isPresent()) {
            serializer.beginWriteField(1, "name");
            serializer.writeString(name.get());
            serializer.endWriteField(1, "name");
        }

        if(this.things.isPresent()) {
            serializer.beginWriteField(2, "things");
            serializer.beginWriteList(things.get().size());
            for(String tmp : things.get()) { serializer.writeString(tmp); }
            serializer.endWriteList();;
            serializer.endWriteField(2, "things");
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
        sb.append("Person{ ");

            sb.append(this.Entity_composite.get() + " ");


            name.ifPresent(x -> sb.append(" name=" +x.toString()));

            things.ifPresent(x -> sb.append(" things=" +x.toString()));

        sb.append("}");
        return sb.toString();
    }



    @Override
    public Person copy(de.dfki.tractat.idl.Serializer serializer, de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException {
        byte[] buf = serializer.serialize(this);
        Person copy = de.dfki.tocalog.model.Person.factory.create();
        deserializer.deserialize(buf, copy);
        return copy;
    }
}

