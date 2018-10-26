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
        return (Integer) data.get("Age");
    }

    public static void setAge(Map<String, Object> data, int age) {
        data.put("Age", age);
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
        Ent person1 = new Ent();
        person1.set(age, 123l);
        person1.set(position, new Vector3(1,0,0));

        Ent person2 = new Ent();
        person2.set(position, new Vector3(2,0,0));

        System.out.println(distance(person1, person2).orElse(-1.0) + "");

        long age = Ontology.age.get(person1).orElse(0l);
        System.out.println(age);

        person1.get(Ontology.age).ifPresent(a -> System.out.println("Age is " + a));

        if (zone.matches(person1, z -> z.equals("nearBike"))) {

        }

        Ent nexus5 = new Ent()
                .set(id, "nexus5")
                .set(battery, 0.5);


    }

    static class Vector3 {
        public final double x, y, z;

        public Vector3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static final Attribute<String> id = new Attribute<>("tocalog/id");
    public static final Attribute<Long> age = new Attribute<>("tocalog/age");
    public static final Attribute<String> zone = new Attribute<>("tocalog/zone");
    public static final Attribute<String> gender = new Attribute<>("tocalog/gender");
    public static final Attribute<Vector3> position = new Attribute<>("tocalog/position");
    public static final Attribute<Double> battery = new Attribute<>("tocalog/dp/battery");

    public static class Ent {
        protected Map<String, AttributeValue> attributes = new HashMap<>();

        public <T> Ent set(Attribute<T> attr, T value) {
            attr.set(this, value);
            return this;
        }

        public <T> Optional<T> get(Attribute<T> attr) {
            return attr.get(this);
        }
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

    public static abstract class AbsScheme implements Scheme {
        private final List<Attribute> attributes;
        private final Map<String, Predicate<Attribute>> constraints = new HashMap();

        protected AbsScheme(List<Attribute> attributes) {
            this.attributes = attributes;
        }

        @Override
        public boolean matches(Ent ent) {
            for(Attribute attr : attributes) {
                Optional optValue = ent.get(attr);
                if (!optValue.isPresent()) {
                    return false;
                }
            }
            return true;
        }
    }

    public class DeviceScheme implements Scheme {

        @Override
        public boolean matches(Ent ent) {
            if(!ent.get(battery).isPresent()) {
                return true;
            }
            return false;
        }
    }

//    public static class PersonScheme implements Scheme {
//        @Override
//        public boolean matches(Ent ent) {
//            if (Age.getOrElse(ent, 0l) < 0) {
//                return false;
//            }
//            return false;
//        }
//    }


    public static Optional<Double> distance(Ent ent1, Ent ent2) {
        Optional<Vector3> v1 = ent1.get(position);
        Optional<Vector3> v2 = ent2.get(position);
        if (!v1.isPresent() || !v2.isPresent()) {
            return Optional.empty();
        }
        double distance = v1.get().x * v2.get().x;
        distance += v1.get().y * v2.get().y;
        distance += v1.get().z * v2.get().z;
        distance = Math.sqrt(distance);
        return Optional.of(distance);
    }

}
