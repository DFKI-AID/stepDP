package de.dfki.tocalog.kb;

import de.dfki.tocalog.util.Vector2;
import de.dfki.tocalog.util.Vector3;
import org.pcollections.*;

import java.util.*;
import java.util.function.Predicate;

/**
 */
public class Ontology {


    public static Optional<Long> getAge(Entity entity) {
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

        Entity person1 = new Entity()
                .set(id, "mechanic1")
                .set(age, 123l)
                .set(position, new Vector3(1, 0, 0));

        Entity person2 = new Entity()
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


        Collection<Entity> fans = devices.query(d -> d.get(type).orElse("").equals("Fan"));


        Entity nexus5 = new Entity()
                .set(id, "nexus5")
                .set(battery, 0.5);
        devices.add(nexus5);


        Entity nexus6 = nexus5
                .set(id, "nexus6")
                .set(owner, Person.refTo("mechanic1"));
        devices.add(nexus6);


        Entity display = new Entity()
                .set(resolution, new Vector2(1024, 2048))
                .set(type, "lcd")
                .set(device, Device.refTo("nexus5"));
        deviceComponents.add(display);


        Collection<Entity> dcOfNexus5 = deviceComponents.query(e -> e.get(device).orElse(Reference.None).matchesId("nexus5"));


        Entity mergedNexus = nexus5.merge(nexus6);
        System.out.println(mergedNexus);


        Entity session = new Entity()
                .set(id, "session1")
                .set(agents, HashTreePSet.empty());

        PSet<String> agents = session.get(Ontology.agents).orElse(HashTreePSet.empty());
        agents = agents.plus(person1.get(id).get());
        session = session.set(Ontology.agents, agents);

        session = session.plus(Ontology.agents, x -> x.plusAll(Arrays.asList("der mechaniker", "blubb")));
        session.get(Ontology.agents).get().stream().forEach(a -> System.out.println(a));

    }

    public static final Attribute<String> id = new Attribute<>("tocalog/id");
    /**
     * Type of the entity. Can be seen as the class name in a hierarchy
     */
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
    public static final Attribute<String> serviceType = new Attribute<>("tocalog/serviceType");
    /**
     * a device is owned by none or one human
     */
    public static final Attribute<String> owned = new Attribute<>("tocalog/owned");
    /**
     * the device of a service or a device component
     */
    public static final Attribute<Reference> device = new Attribute<>("tocalog/device");
    public static final Attribute<Vector2> resolution = new Attribute<>("tocalog/resolution");
    public static final Attribute<String> partOf = new Attribute<>("tocalog/partOf");
    public static final Attribute<Reference> owner = new Attribute<>("tocalog/owner");

    public static final Attribute<PSet<String>> agents = new Attribute<>("tocalog/session/agents");


    public static final Type Service = new Type("tocalog/Service");
    public static final Type Person = new Type("tocalog/Person");
    public static final Type Device = new Type("tocalog/Device");

    /**
     * e.g. the speaker of a SpeechInput
     * e.g. the actor of a gesture
     */
    public static final Attribute<String> initiator = new Attribute<>("tocalog/initiator");


//    public static boolean isA(Entity entity, String superClass) {
//
//    }

    public interface Scheme {
        boolean validate(Entity entity);
    }

    public static abstract class AbsScheme implements Scheme {
        private final List<Attribute> attributes;
        private final Map<String, Predicate<Attribute>> constraints = new HashMap();

        protected AbsScheme(List<Attribute> attributes) {
            this.attributes = attributes;
        }

        @Override
        public boolean validate(Entity entity) {
            for (Attribute attr : attributes) {
                Optional optValue = entity.get(attr);
                if (!optValue.isPresent()) {
                    return false;
                }
            }
            return true;
        }
    }

    public class DeviceScheme implements Scheme {
        @Override
        public boolean validate(Entity entity) {
            if (!entity.get(battery).isPresent()) {
                return true;
            }
            return false;
        }
    }

//    public static class PersonScheme implements Scheme {
//        @Override
//        public boolean validate(Ent ent) {
//            if (Age.getOrElse(ent, 0l) < 0) {
//                return false;
//            }
//            return false;
//        }
//    }


    public static Optional<Double> distance(Entity entity1, Entity entity2) {
        Optional<Vector3> v1 = entity1.get(position);
        Optional<Vector3> v2 = entity2.get(position);
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
