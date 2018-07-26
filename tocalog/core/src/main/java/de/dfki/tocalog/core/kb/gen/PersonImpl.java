package de.dfki.tocalog.core.kb.gen;

public class PersonImpl implements de.dfki.tocalog.core.kb.gen.Person{

    //fields
    private java.util.Optional<String> name;
    private java.util.Optional<java.util.List<String>> things;
    private java.util.Optional<String> friend;
    private java.util.Optional<java.util.Map<String,Double>> meta;
    private java.util.Optional<Person> wuhu;
    private java.util.Optional<java.util.List<Person>> foo;
    private java.util.Optional<String> id;


    public PersonImpl() {

        this.name = java.util.Optional.empty();

        this.things = java.util.Optional.empty();

        this.friend = java.util.Optional.empty();

        this.meta = java.util.Optional.empty();

        this.wuhu = java.util.Optional.empty();

        this.foo = java.util.Optional.empty();

        this.id = java.util.Optional.empty();

    }

    //getter / setter
    public java.util.Optional<String> getName() {
        return this.name;
    }
    public Person setName(String value) {
        this.name = java.util.Optional.ofNullable(value);
        return this;
    }

    public java.util.Optional<java.util.List<String>> getThings() {
        return this.things;
    }
    public Person setThings(java.util.List<String> value) {
        this.things = java.util.Optional.ofNullable(value);
        return this;
    }

    public java.util.Optional<String> getFriend() {
        return this.friend;
    }
    public Person setFriend(String value) {
        this.friend = java.util.Optional.ofNullable(value);
        return this;
    }

    public java.util.Optional<java.util.Map<String,Double>> getMeta() {
        return this.meta;
    }
    public Person setMeta(java.util.Map<String,Double> value) {
        this.meta = java.util.Optional.ofNullable(value);
        return this;
    }

    public java.util.Optional<Person> getWuhu() {
        return this.wuhu;
    }
    public Person setWuhu(de.dfki.tocalog.core.kb.gen.Person value) {
        this.wuhu = java.util.Optional.ofNullable(value);
        return this;
    }

    public java.util.Optional<java.util.List<Person>> getFoo() {
        return this.foo;
    }
    public Person setFoo(java.util.List<Person> value) {
        this.foo = java.util.Optional.ofNullable(value);
        return this;
    }

    public java.util.Optional<String> getId() {
        return this.id;
    }
    public Person setId(String value) {
        this.id = java.util.Optional.ofNullable(value);
        return this;
    }





    //serialization + helper
    public static final java.util.Map<String, Integer> FIELD_TO_ID_MAP;
    static {
        java.util.Map<String, Integer> tmp = new java.util.HashMap<String, Integer>();

        tmp.put("name", 1);

        tmp.put("things", 2);

        tmp.put("friend", 3);

        tmp.put("meta", 4);

        tmp.put("wuhu", 5);

        tmp.put("foo", 6);

        tmp.put("id", 7);

        FIELD_TO_ID_MAP = java.util.Collections.unmodifiableMap(tmp);
    }

    public static final java.util.Map<Integer, String> ID_TO_FIELD_MAP;
    static {
        java.util.Map<Integer, String> tmp = new java.util.HashMap<Integer, String>();

        tmp.put(1, "name");

        tmp.put(2, "things");

        tmp.put(3, "friend");

        tmp.put(4, "meta");

        tmp.put(5, "wuhu");

        tmp.put(6, "foo");

        tmp.put(7, "id");

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
                        String tmp_name;tmp_name = deserializer.readString();this.name = java.util.Optional.of(tmp_name);
                    }
                    break;

                case 2:
                    {
                        java.util.ArrayList<String> tmp_things;int n = deserializer.beginReadList();tmp_things = new java.util.ArrayList<>();for(int i=0; i<n; i++) {String tmp;tmp = deserializer.readString();tmp_things.add(tmp);}deserializer.endReadList();this.things = java.util.Optional.of(tmp_things);
                    }
                    break;

                case 3:
                    {
                        String tmp_friend;tmp_friend = deserializer.readString();this.friend = java.util.Optional.of(tmp_friend);
                    }
                    break;

                case 4:
                    {
                        java.util.HashMap<String,Double> tmp_meta;int n = deserializer.beginReadMap();tmp_meta = new java.util.HashMap<>();for(int i=0; i<n; i++) {String tmpKey;tmpKey = deserializer.readString();Double tmpValue;tmpValue = deserializer.readDouble();tmp_meta.put(tmpKey, tmpValue);}deserializer.endReadMap();this.meta = java.util.Optional.of(tmp_meta);
                    }
                    break;

                case 5:
                    {
                        de.dfki.tocalog.core.kb.gen.Person tmp_wuhu;tmp_wuhu = de.dfki.tocalog.core.kb.gen.Person.factory.create(); tmp_wuhu.deserialize(deserializer);this.wuhu = java.util.Optional.of(tmp_wuhu);
                    }
                    break;

                case 6:
                    {
                        java.util.ArrayList<Person> tmp_foo;int n = deserializer.beginReadList();tmp_foo = new java.util.ArrayList<>();for(int i=0; i<n; i++) {
                        de.dfki.tocalog.core.kb.gen.Person tmp;tmp = de.dfki.tocalog.core.kb.gen.Person.factory.create(); tmp.deserialize(deserializer);tmp_foo.add(tmp);}deserializer.endReadList();this.foo = java.util.Optional.of(tmp_foo);
                    }
                    break;

                case 7:
                    {
                        String tmp_id;tmp_id = deserializer.readString();this.id = java.util.Optional.of(tmp_id);
                    }
                    break;

            }

        }
        deserializer.endReadObject(this);
    }

    public void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException {
        serializer.beginWriteObject("");

        if(this.name.isPresent()) {
            serializer.beginWriteField(1, "name");
            serializer.writeString(name.get());
            serializer.endWriteField(1, "name");
        }

        if(this.things.isPresent()) {
            serializer.beginWriteField(2, "things");
            serializer.beginWriteList(things.get().size());for(String tmp : things.get()) { serializer.writeString(tmp); }serializer.endWriteList();;
            serializer.endWriteField(2, "things");
        }

        if(this.friend.isPresent()) {
            serializer.beginWriteField(3, "friend");
            serializer.writeString(friend.get());
            serializer.endWriteField(3, "friend");
        }

        if(this.meta.isPresent()) {
            serializer.beginWriteField(4, "meta");
            serializer.beginWriteMap(meta.get().size());for(java.util.Map.Entry<String,Double> tmp : meta.get().entrySet()) { serializer.writeString(tmp.getKey()); serializer.writeDouble(tmp.getValue()); }serializer.endWriteMap();;
            serializer.endWriteField(4, "meta");
        }

        if(this.wuhu.isPresent()) {
            serializer.beginWriteField(5, "wuhu");
            wuhu.get().serialize(serializer);
            serializer.endWriteField(5, "wuhu");
        }

        if(this.foo.isPresent()) {
            serializer.beginWriteField(6, "foo");
            serializer.beginWriteList(foo.get().size());for(de.dfki.tocalog.core.kb.gen.Person tmp : foo.get()) { tmp.serialize(serializer); }serializer.endWriteList();;
            serializer.endWriteField(6, "foo");
        }
        
        if(this.id.isPresent()) {
            serializer.beginWriteField(7, "id");
            serializer.writeString(id.get());
            serializer.endWriteField(7, "id");
        }
        
        serializer.endWriteObject("");
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Person{");
            
            name.ifPresent(x -> sb.append(" name=" +x.toString()));
            
            things.ifPresent(x -> sb.append(" things=" +x.toString()));
            
            friend.ifPresent(x -> sb.append(" friend=" +x.toString()));
            
            meta.ifPresent(x -> sb.append(" meta=" +x.toString()));
            
            wuhu.ifPresent(x -> sb.append(" wuhu=" +x.toString()));
            
            foo.ifPresent(x -> sb.append(" foo=" +x.toString()));
            
            id.ifPresent(x -> sb.append(" id=" +x.toString()));
            
        sb.append("}");
        return sb.toString();
    }


    @Override
    public Person copy(de.dfki.tractat.idl.Serializer serializer, de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException {
        byte[] buf = serializer.serialize(this);
        Person copy = Person.factory.create();
        deserializer.deserialize(buf, copy);
        return copy;
    }
}

