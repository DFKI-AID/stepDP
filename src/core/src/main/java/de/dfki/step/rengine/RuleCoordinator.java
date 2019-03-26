package de.dfki.step.rengine;

import de.dfki.step.core.Component;
import de.dfki.step.core.ComponentManager;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Decides which rule will fire based on mutual resource consumption.
 * Rules may register functions ("what they would do if they are executed")
 * Rules may attach additional data for coordination (e.g. origin resource x)
 *
 * 'origin' is either an Object or a Collection of Objects
 */
public class RuleCoordinator implements Component {
    private static final Logger log = LoggerFactory.getLogger(RuleCoordinator.class);
    public static final String origin = "origin";
    public static final String priority = "priority";
    protected PMap<String, Runnable> functions;
    protected PMap<String, Token> data;
    private PMap<Rule, Integer> priorities = HashTreePMap.empty();

    public RuleCoordinator() {
        this.reset();
    }

    /**
     * Resets the internal state to prepare for the next iteration
     */
    protected void reset() {
        this.functions = HashTreePMap.empty();
        this.data = HashTreePMap.empty();
    }

    protected RuleCoordinator attach(String id, String key, Object obj) {
        data = data.plus(id, data.get(id).add(key, obj));
        return this;
    }

    public DataAttacher add(Runnable fnc) {
        String id = UUID.randomUUID().toString().substring(0, 10);
        return this.add(id, fnc);
    }

    public DataAttacher add(String id, Runnable fnc) {
        this.functions = this.functions.plus(id, fnc);
        this.data = this.data.plus(id, new Token());

        return new DataAttacher() {
            @Override
            public DataAttacher attach(String key, Object obj) {
                RuleCoordinator.this.attach(id, key, data);
                return this;
            }
        };
    }

    @Override
    public void init(ComponentManager cm) {

    }

    @Override
    public void deinit() {

    }

    @Override
    public void beforeUpdate() {
        reset();
    }

    /**
     * Iterates over all rule functions that were added since the last update call and fires rules
     * based on the priority and intersection of the origin
     */
    public void update() {
        // simple coordination = no coordination, just execute all
//        functions.entrySet().forEach(entry -> {
//            log.info("executing {}", entry.getKey());
//            entry.getValue().run();
//        });

        Map<String, Runnable> executeMap = new HashMap<>();
        executeMap.putAll(this.functions);

        Function<String, Double> getPriority = s ->
                data.get(s).get(priority).filter(p -> p instanceof  Double).map(p -> (Double) p).orElse(0.0);

        this.functions.entrySet().stream()
                //sort functions after priority, lowest priority are handled first
                .sorted(Comparator.comparingDouble(e -> getPriority.apply(e.getKey())))
                .forEach(entry -> {
                    // check whether other functions consume the same resource
                    Optional<Object> consumes1 = data.get(entry.getKey()).get(origin);
                    for (var otherEntry : executeMap.entrySet()) {
                        if(entry.getValue() == otherEntry.getValue()) {
                            continue;
                        }

                        Optional<Object> consumes2 = data.get(otherEntry.getKey()).get(origin);
                        if(consumeCollides(consumes1, consumes2)) {
                            //two functions consume the same resource
                            //hence the first (lower priority) is removed
                            executeMap.remove(entry.getKey());
                            break;
                        }
                    }
                });

        executeMap.entrySet().forEach(entry -> {
            log.info("executing {}", entry.getKey());
            entry.getValue().run();
        });
    }

    @Override
    public Object createSnapshot() {
        return null;
    }

    @Override
    public void loadSnapshot(Object snapshot) {

    }

    protected boolean consumeCollides(Optional<Object> consumes1, Optional<Object> consumes2) {
        Collection<Object> consumption1 = convert(consumes1);
        Collection<Object> consumption2 = convert(consumes2);

        Set<Object> intersection = consumption1.stream()
                .filter(x -> consumption2.contains(x))
                .collect(Collectors.toSet());

        return !intersection.isEmpty();
    }

    protected Collection<Object> convert(Optional<Object> obj) {
        if(!obj.isPresent()) {
            return Collections.EMPTY_SET;
        }

        if(obj.get() instanceof Collection) {
            return (Collection) obj.get();
        } else {
            return List.of(obj.get());
        }
    }

    public interface DataAttacher {
        DataAttacher attach(String key, Object obj);

        /**
         * Attaches the origin (list of object specifying how a token or intent was created).
         * e.g. a gesture g1 and speech input s1 are merge into an intent i1, which then triggers a rule to fire.
         * The rule coordinator will decide which rules will finally fire based on the origin=[g1, s1, i1]. If the
         * origin overlaps with other rules, only one may fire. But this depends on the coordination strategy.
         * @param token
         * @return
         */
        default DataAttacher attachOrigin(Token token) {
            List<Object> newOrigin = new ArrayList<>();

            Optional<Collection> tokenOrigins = token.get(origin, Collection.class);
            if(tokenOrigins.isPresent()) {
                newOrigin.addAll(tokenOrigins.get());
            } else {
                Optional<Object> tokenOrigin = token.get(origin);
                if(tokenOrigin.isPresent()) {
                    newOrigin.add(tokenOrigin.get());
                }
            }

            this.attach("origin", newOrigin);
            return this;
        }
    }

}
