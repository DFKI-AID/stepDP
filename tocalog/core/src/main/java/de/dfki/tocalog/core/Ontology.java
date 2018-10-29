package de.dfki.tocalog.core;

import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import org.pcollections.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 */
public class Ontology {


    public static Optional<Long> getAge(Ent entity) {
        return entity.get(age);
    }


    public static void main(String[] args) {
//        Map<String, Object> person = new HashMap<>();
//        Map<String, Object> building = new HashMap<>();
//        setAge(building, 123);
//
//        if (findAge(person).orElse(0) > 240) {
//            //
//        }

        Ent person1 = new Ent()
                .set(id, "mechanic1")
                .set(age, 123l)
                .set(position, new Vector3(1, 0, 0));

        Ent person2 = new Ent()
                .set(position, new Vector3(2, 0, 0));

        System.out.println(distance(person1, person2).orElse(-1.0) + "");

        long age = Ontology.age.get(person1).orElse(0l);
        System.out.println(age);

        person1.get(Ontology.age).ifPresent(a -> System.out.println("Age is " + a));

        if (zone.matches(person1, z -> z.equals("nearBike"))) {

        }

        KnowledgeBase kb = new KnowledgeBase();
        KnowledgeMap devices = kb.getKnowledgeMap("Device");
        KnowledgeMap deviceComponents = kb.getKnowledgeMap("DeviceComponent");



        Ent nexus5 = new Ent()
                .set(id, "nexus5")
                .set(battery, 0.5);
        devices.add(nexus5);

        Ent nexus6 = nexus5
                .set(id, "nexus6")
                .set(owner, "mechanic1");
        devices.add(nexus6);


        Ent display = new Ent()
                .set(resolution, new Vector2(1024, 2048))
                .set(type, "lcd")
                .set(device, "nexus5");
        deviceComponents.add(display);


        Collection<Ent> dcOfNexus5 = deviceComponents.query(e -> e.get(device).orElse("").equals("nexus5"));


        Ent mergedNexus = nexus5.merge(nexus6);
        System.out.println(mergedNexus);



        Ent session = new Ent()
                .set(id, "session1")
                .set(agents, HashTreePSet.empty());

        PSet<String> agents = session.get(Ontology.agents).orElse(HashTreePSet.empty());
        agents = agents.plus(person1.get(id).get());
        session = session.set(Ontology.agents, agents);

        session = session.plus(Ontology.agents, x -> x.plusAll(Arrays.asList("der mechaniker", "blubb")));
        session.get(Ontology.agents).get().stream().forEach(a -> System.out.println(a));

    }

    static class Vector3 {
        public final double x, y, z;

        public Vector3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return "Vector3{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }

    static class Vector2 {
        public final double x, y;

        public Vector2(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Vector2{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    public static final Attribute<String> id = new Attribute<>("tocalog/id");
    public static final Attribute<String> type = new Attribute<>("tocalog/type");

    public static final Attribute<String> name = new Attribute<>("tocalog/name"); //human-readable name
    public static final Attribute<Long> age = new Attribute<>("tocalog/age");
    public static final Attribute<String> zone = new Attribute<>("tocalog/zone");
    public static final Attribute<String> gender = new Attribute<>("tocalog/gender");

    //KB meta
    public static final Attribute<Long> timestamp = new Attribute<>("tocalog/kb/timestamp");
    public static final Attribute<String> source = new Attribute<>("tocalog/kb/source");
    public static final Attribute<Double> confidence = new Attribute<>("tocalog/kb/confidence");


    public static final Attribute<Vector3> position = new Attribute<>("tocalog/position");
    public static final Attribute<Double> battery = new Attribute<>("tocalog/battery");
    /**
     * a device is owned by none or one human
     */
    public static final Attribute<String> owned = new Attribute<>("tocalog/owned");
    /**
     * the device of a service or a device component
     */
    public static final Attribute<String> device = new Attribute<>("tocalog/device");
    public static final Attribute<Vector2> resolution = new Attribute<>("tocalog/resolution");
    public static final Attribute<String> partOf = new Attribute<>("tocalog/partOf");
    public static final Attribute<String> owner = new Attribute<>("tocalog/owner");

    public static final Attribute<PSet<String>> agents = new Attribute<>("tocalog/session/agents");

    public static final String Service = "tocalog/Service";

    public static class KS {
        private Map<String, Ent> entities = new HashMap<>();

        public synchronized void add(Ent ent) {
            Optional<String> id = ent.get(Ontology.id);
            if (!id.isPresent()) {
                throw new IllegalArgumentException("need id for putting entity into kb");
            }
            this.entities.put(id.get(), ent);
        }

        public synchronized void add(Ent ent, Attribute... attributes) {
            Optional<String> id = ent.get(Ontology.id);
            if (!id.isPresent()) {
                throw new IllegalArgumentException("need id for putting entity into kb");
            }

            Ent kbEnt;

            if (this.entities.containsKey(id)) {
                kbEnt = this.entities.get(id);
            } else {
                kbEnt = new Ent();
            }


            for (Attribute attr : attributes) {
                Optional optValue = ent.get(attr);
                if (!optValue.isPresent()) {
                    throw new IllegalStateException("value not presented for " + attr);
                }
                kbEnt = kbEnt.set(attr, optValue.get());
            }

            this.entities.put(id.get(), kbEnt);
        }

        public synchronized <T> void update(String id, Attribute<T> attr, T value) {
            if (!this.entities.containsKey(id)) {
                //TODO could also create a new entity
                return;
            }
            Ent ent = this.entities.get(id);
            ent = ent.set(attr, value);
            this.entities.put(id, ent);
        }


        //TODO optional or empty ent?
        public synchronized Optional<Ent> get(String id) {
            return Optional.ofNullable(this.entities.get(id));
        }


    }

    public static class Ent {
        public final PMap<String, AttributeValue> attributes;

        public Ent(PMap<String, AttributeValue> attributes) {
            this.attributes = attributes;
        }

        public Ent() {
            this(HashTreePMap.empty());
        }

        public Ent unset(Attribute attr) {
            PMap<String, AttributeValue> newAttr = attributes.minus(attr.name);
            return new Ent(newAttr);
        }

        public <T> Ent set(Attribute<T> attr, T value) {
            return attr.set(this, value);
        }

        public <T> Optional<T> get(Attribute<T> attr) {
            return attr.get(this);
        }

        public <T> Ent plus(Attribute<T> attr, Function<T, T> fnc) {
            Optional<T> optVal = this.get(attr);
            if (!optVal.isPresent()) {
                //TODO maybe create default?
                return this;
            }
            T value = fnc.apply(optVal.get());
            return this.set(attr, value);
        }

        public <T> Ent merge(Ent other) {
            Ent out = this;
            for (AttributeValue av : other.attributes.values()) {
                Object value = av.attribute.get(other).get();
                out = out.set(av.attribute, value);
            }
            return out;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Ent{");
            for (AttributeValue av : attributes.values()) {
                sb.append(av.name).append("=");
                sb.append(av.value);
                sb.append("  ");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    private static class AttributeValue<T> {
        public final String name;
        public final T value;
        public final Attribute<T> attribute;
//        public double confidence;

        public AttributeValue(String name, T value, Attribute<T> attribute) {
            this.name = name;
            this.value = value;
            this.attribute = attribute;
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

        public Ent set(Ent entity, T value) {
            PMap<String, AttributeValue> attributes = entity.attributes.plus(name, new AttributeValue(name, value, this));
            Ent e = new Ent(attributes);
            return e;
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
            for (Attribute attr : attributes) {
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
            if (!ent.get(battery).isPresent()) {
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
