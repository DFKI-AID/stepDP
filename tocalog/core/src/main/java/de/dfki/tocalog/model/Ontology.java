package de.dfki.tocalog.model;

import java.util.*;
import java.util.function.Predicate;

/**
 */
public class Ontology {

    public static Optional<Integer> findAge(Map<String, Object> data) {
        return Optional.ofNullable(getAge(data));
    }

    public static Integer getAge(Map<String, Object> data) {
        return (Integer) data.get("age");
    }

    public static void setAge(Map<String, Object> data, int age) {
        data.put("age", age);
    }

    public static void main(String[] args) {
//        Map<String, Object> person = new HashMap<>();
//        Map<String, Object> building = new HashMap<>();
//        setAge(building, 123);
//
//        if (findAge(person).orElse(0) > 240) {
//            //
//        }

        Set<Ent> persons = new HashSet<>();
        Ent e = new Ent();
        age.set(e, 342l);
        long age = Ontology.age.get(e).orElse(0l);
        System.out.println(age);

        if (zone.matches(e, z -> z.equals("nearBike"))) {

        }

    }

    public static final Attribute<UUID> id = new Attribute<>("tocalog/v1/id");
    public static final Attribute<Long> age = new Attribute<>("tocalog/v1/age");
    public static final Attribute<String> zone = new Attribute<>("tocalog/v1/zone");

    public static class Ent {
        protected Map<String, AttributeValue> attributes = new HashMap<>();

    }

    private static class AttributeValue<T> {
        public final String name;
        public final T value;
//        public double confidence;

        public AttributeValue(String name, T value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class Attribute<T> {
        public final String name;

        public Attribute(String name) {
            this.name = name;
        }

        public boolean matches(Ent entity, Predicate<T> pred) {
            Optional<T> opt = get(entity);
            if (!opt.isPresent()) {
                return false;
            }
            return pred.test(opt.get());
        }

        public T getOrElse(Ent entity, T dflt) {
            Optional<T> opt = get(entity);
            if (opt.isPresent()) {
                return opt.get();
            }
            return dflt;
        }

        public Optional<T> get(Ent entity) {
            AttributeValue attr = entity.attributes.get(name);
            if (attr == null) {
                return Optional.empty();
            }
            T value = (T) attr.value;
            return Optional.ofNullable(value);
        }

        public void set(Ent entity, T value) {
            entity.attributes.put(name, new AttributeValue(name, value));
        }
    }

    public interface Scheme {
        boolean matches(Ent ent);
    }

    public static class PersonScheme implements Scheme {
        @Override
        public boolean matches(Ent ent) {
            if(age.getOrElse(ent, 0l) < 0) {
                return false;
            }
            return false;
        }
    }


}
