package de.dfki.step.kb;

import de.dfki.step.core.Mode;
import de.dfki.step.util.Vector2;
import de.dfki.step.util.Vector3;
import org.pcollections.*;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;




/**
 * TODO static / singleton class is ugly. however, accessing the attributes would be annoying otherwise...
 * TODO rename tocalog => toc for higher readability
 */
public class Ontology {


    public static final TypeHierarchy it;


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
                .set(type, "lcd");
//                .set(device, Device.refTo("nexus5"));
        deviceComponents.add(display);


//        Collection<Entity> dcOfNexus5 = deviceComponents.query(e -> e.get(device).orElse(Reference.None).matchesId("nexus5"));


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
     * TODO: use Type class
     */
    public static final Attribute<String> type = new Attribute<>("tocalog/type");
    public static final Attribute<Type> type2 = new Attribute<>("tocalog/type2");


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
    public static final Attribute<Reference> owner = new Attribute<>("tocalog/owner");

    public static final Attribute<PSet<String>> agents = new Attribute<>("tocalog/session/agents");

    public static final Attribute<String> subject = new Attribute<>("tocalog/subject");
    public static final Attribute<String> predicate = new Attribute<>("tocalog/predicate");
    public static final Attribute<String> object = new Attribute<>("tocalog/object");


    public static final Attribute<String> color = new Attribute<>("tocalog/attributes/color");
    public static final Attribute<String> size = new Attribute<>("tocalog/attributes/size");
    public static final Attribute<Double> brightness = new Attribute<>("tocalog/attributes/brightness");
    public static final Attribute<String> location = new Attribute<>("tocalog/attributes/location");

    public static final Type DiscourseFocus = new Type("tocalog/kinect/DiscourseFocus");
    public static final Attribute<String> discourseTarget = new Attribute<>("tocalog/kinect/discourseTarget");
    public static final Attribute<Double> discourseConfidence = new Attribute<>("tocalog/kinect/discourseConfidence");

    public static final Type VisualFocus = new Type("tocalog/kinect/VisualFocus");
    public static final Attribute<String> visualSource = new Attribute<>("tocalog/kinect/visualSource");
    public static final Attribute<String> visualTarget = new Attribute<>("tocalog/kinect/visualTarget");
    public static final Attribute<Double> visualConfidence = new Attribute<>("tocalog/kinect/visualConfidence");

    public static final Type Person = new Type("tocalog/Person");
    public static final Type Robot = new Type("tocalog/Robot");
    public static final Type Entity = new Type("tocalog/Entity");
    public static final Type PhysicalEntity = new Type("tocalog/PhysicalEntity");
    public static final Type Agent = new Type("tocalog/Agent");
    public static final Type Session = new Type("tocalog/Session");

    public static final Type Service = new Type("tocalog/Service");
    public static final Type Device = new Type("tocalog/Device");
    public static final Type DeviceComponent = new Type("tocalog/DeviceComponent");
    public static final Type Monitor = new Type("tocalog/Monitor");
    public static final Type Loudspeaker = new Type("tocalog/Loudspeaker");
    public static final Type Battery = new Type("tocalog/Battery");
    public static final Type Headphones = new Type("tocalog/Headphones");

    public static final Type Number = new Type("tocalog/Number");

    public static final Type Zone = new Type("tocalog/Zone"); //necessary?

    public static final Type Output = new Type("tocalog/Output");
    public static final Type SpeechOutput = new Type("tocalog/SpeechOutput");



    /**
     * e.g. mode through which an output can be perceived
     */
    public static final Attribute<Mode> mode = new Attribute<>("tocalog/mode");
    /**
     * The concrete service type e.g. `a3s-player`
     */
    public static final Attribute<String> service = new Attribute<>("tocalog/service");
    /**
     * Specifying a resource. e.g. `http://a3s:50000/`
     */
    public static final Attribute<URI> uri = new Attribute<>("tocalog/uri");
    public static final Attribute<String> utterance = new Attribute<>("tocalog/utterance");
    public static final Attribute<String> modality = new Attribute<>("tocalog/modality");
    public static final Attribute<Duration> duration = new Attribute<>("tocalog/duration");

    /**
     * e.g. the speaker of a SpeechInput
     * e.g. the actor of a gesture
     */
    public static final Attribute<String> initiator = new Attribute<>("tocalog/initiator");

    public static final Attribute<String> file = new Attribute<>("tocalog/file");

//    public static boolean isA(Entity entity, String superClass) {
//
//    }

    public interface Scheme {
        /**
         * @param entity
         * @return
         * @throws IllegalArgumentException iff the entity is not valid
         */
        void validate(Entity entity);

        default boolean matches(Entity entity) {
            try {
                validate(entity);
                return true;
            } catch(IllegalArgumentException ex) {
                return false;
            }
        }
    }

    public static class AbsScheme implements Scheme {
        private final Map<Attribute, Predicate<Object>> optConstraints;
        private final Map<Attribute, Predicate<Object>> reqConstraints;

        private AbsScheme(Builder builder) {
            this.optConstraints = new HashMap<>(builder.optConstraints);
            this.reqConstraints = new HashMap<>(builder.reqConstraints);
        }

        @Override
        public void validate(Entity entity) {
            validate(entity, reqConstraints, true);
            validate(entity, optConstraints, false);
        }

        protected void validate(Entity entity, Map<Attribute, Predicate<Object>> preds, boolean required) {
            for (var entry : preds.entrySet()) {
                Optional av = entry.getKey().get(entity);
                if (!av.isPresent()) {
                    if(required) {
                        throw new IllegalArgumentException("missing attribute: " + entry.getKey() + " in " + entity);
                    }
                    continue;
                }

                try {
                    if (!entry.getValue().test(av.get())) {
                        throw new IllegalArgumentException();
                    }
                } catch (Exception ex) {
                    throw new IllegalArgumentException("predicate check failed for" + entry.getKey() + " in " + entity, ex);
                }
            }
        }

        public Builder extend() {
            return new Builder(this);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Map<Attribute, Predicate<Object>> optConstraints = new HashMap();
            private Map<Attribute, Predicate<Object>> reqConstraints = new HashMap();

            protected Builder() {
            }

            protected Builder(AbsScheme scheme) {
                this.optConstraints = new HashMap<>(scheme.optConstraints);
                this.reqConstraints = new HashMap<>(scheme.reqConstraints);
            }


            public <T> Builder equal(Attribute<T> attr, T other) {
                return this.matches(attr, x -> Objects.equals(x, other), true);
            }

            public <T> Builder present(Attribute<T> attr) {
                return this.matches(attr, x -> true, true);
            }

            public <T> Builder matches(Attribute<T> attr, Predicate<T> pred) {
                return this.matches(attr, pred, true);
            }

            public <T> Builder matches(Attribute<T> attr, Predicate<T> pred, boolean required) {
                if (required) {
                    this.reqConstraints.put(attr, (Predicate<Object>) pred);
                } else {
                    this.optConstraints.put(attr, (Predicate<Object>) pred);
                }
                return this;
            }

            public AbsScheme build() {
                return new AbsScheme(this);
            }

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


    static {
        it = TypeHierarchy.build()
                .add(Ontology.PhysicalEntity, Ontology.Entity)
                .add(Ontology.Agent, Ontology.PhysicalEntity)
                .add(Ontology.Person, Ontology.Agent)
                .add(Ontology.Robot, Ontology.Agent)
                .add(Ontology.Device, Ontology.PhysicalEntity)
                .add(Ontology.Service, Ontology.Entity)
                .add(Ontology.DeviceComponent, Ontology.PhysicalEntity)
                .add(Ontology.Monitor, Ontology.DeviceComponent)
                .add(Ontology.Loudspeaker, Ontology.DeviceComponent)
                .add(Ontology.Headphones, Ontology.Loudspeaker)
                .add(Ontology.Battery, Ontology.DeviceComponent)
                .add(Ontology.Zone, Ontology.Entity)
                .build();
//        System.out.println(it.toMermaid());
//        System.out.println(it.inheritsFrom(Ontology.Person, Ontology.Entity));
//        System.out.println(it.inheritsFrom(Ontology.Person, Ontology.Service));
//        Set<Type> subs = it.getSubClasses(Ontology.Agent);
//        Set<Type> sups = it.getSuperClasses(Ontology.Agent);
//        System.out.println(subs);
    }


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
