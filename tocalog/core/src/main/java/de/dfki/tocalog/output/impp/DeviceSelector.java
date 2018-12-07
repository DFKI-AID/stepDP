package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.kb.*;
import de.dfki.tocalog.output.Imp;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class DeviceSelector {
    private final long timeout = 4000;
//    public static final Attribute<Entity> deviceCompAttr = new Attribute("imp/devicecomp");
//    public static final Attribute<Entity> deviceAttr = new Attribute("imp/device");
//    public static final Attribute<Entity> serviceAttr = new Attribute("imp/service");
    public static final Ontology.Scheme whereScheme = Ontology.AbsScheme.builder()
            .present(Ontology.id)
//            .present(deviceAttr)
//            .present(serviceAttr)
//            .present(deviceCompAttr)
            .build();
    public static final Attribute<Set<Entity>> whom = new Attribute<>("imp/whom");
    public static final Attribute<Set<Entity>> where = new Attribute<>("imp/where");
    public static final Attribute<Entity> what = new Attribute<>("imp/what");
    public static final Attribute<Long> maxRank = new Attribute<>("imp/maxRank");
    public static final Attribute<Long> minRank = new Attribute<>("imp/minRank");
    public static final Attribute<Double> score= new Attribute<>("imp/score");
    public static Ontology.Scheme unitScheme = Ontology.AbsScheme.builder()
            .present(where)
            .present(whom)
            .present(what)
            .present(maxRank)
            .present(minRank)
            .build();
    private final Imp imp;
    private final KnowledgeBase kb;


    public DeviceSelector(Imp imp) {
        this.imp = imp;
        this.kb = imp.getKb();
    }


    /**
     * (what whom) => (what whom where rank)
     * @param outputUnit
     * @return
     */
    public Optional<Entity> process(Entity outputUnit) {
        Optional<Entity> unit = rankDevices(outputUnit).stream()
                .map(x -> x.set(score, ((double) x.get(minRank).get() / Math.max(x.get(maxRank).get(), 1))))
                .sorted(Comparator.comparing(x -> x.get(score).get()))
                .findFirst();
        return unit;
    }


    /**
     * combines matching services, devices and device component into one entity.
     * It attaches the service and device info to an entity
     *
     * @return
     */
    protected Set<Entity> findTargets() {
        KnowledgeMap serviceMap = kb.getKnowledgeMap(Ontology.Service);
        KnowledgeMap deviceMap = kb.getKnowledgeMap(Ontology.Device);
        long now = System.currentTimeMillis();

        return kb.getKnowledgeMap(Ontology.Service).getStream()
                //filter services that are probably not available
                .filter(x -> x.get(Ontology.timestamp).orElse(0l) + timeout > now)
//                .map(x -> new Entity().set(serviceAttr, x))
                //filter if device is missing
                //TODO think about whether the device and service is necessary
//                .filter(x -> x.get(deviceCompAttr).get().hasAttribute(Ontology.device))
//                .map(x -> x.set(deviceAttr, deviceMap.get(x.get(deviceCompAttr, Ontology.device).get()).orElse(null)))
//                .filter(x -> x.hasAttribute(deviceAttr))
                //filter if service is missing
//                .filter(x -> x.get(deviceCompAttr).get().hasAttribute(Ontology.service))
//                .map(x -> x.set(serviceAttr, serviceMap.get(x.get(deviceCompAttr, Ontology.service).get()).orElse(null)))
//                .filter(x -> x.hasAttribute(serviceAttr))

                //now we have a triple of the form: (comp, device, service)
                .map(x -> {
                    //for debugging
                    whereScheme.validate(x);
                    return x;
                })
                .collect(Collectors.toSet());
    }


    /**
     * (what whom) => set of (what whom where rank)
     *
     * @param outputUnit
     * @return
     */
    protected Set<Entity> rankDevices(Entity outputUnit) {
        Ontology.AbsScheme scheme = Ontology.AbsScheme.builder().present(what).present(whom).build();
        scheme.validate(outputUnit);

        Set<Entity> targets = findTargets();
        Set<Entity> units = targets.stream()
                // filter if service can't present the given output
                .filter(x -> imp.supports(outputUnit.get(what).get(), x))
                // atm only unimodal output
                .map(x -> outputUnit.set(where, Set.of(x)))
                .map(x -> x.set(maxRank, 0l))
                .map(x -> x.set(minRank, 0l))
                // now we have output base with the form of (whom what where rank_min rank_max...)
                .map(x -> rank(x))
                .collect(Collectors.toSet());


        //TODO atm no combine / powerset

        return units;
    }

    /**
     * increases the min and max ranking of an assignment
     *
     * @param entity
     * @return
     */
    protected Entity rank(Entity entity) {
        unitScheme.validate(entity);
        //TODO ranking
        return entity;
    }


    /**
     * Resets the internal state. For the next run, all devices are read from the KB
     */
    public void reset() {
        //TODO
    }
}
