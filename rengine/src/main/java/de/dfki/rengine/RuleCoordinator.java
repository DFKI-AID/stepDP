package de.dfki.rengine;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
public class RuleCoordinator {
    private static final Logger log = LoggerFactory.getLogger(RuleCoordinator.class);
    public static final String consumes = "consumes";
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
    public void reset() {
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
                    Optional<Object> consumes1 = data.get(entry.getKey()).get(consumes);
                    for (var otherEntry : executeMap.entrySet()) {
                        if(entry.getValue() == otherEntry.getValue()) {
                            continue;
                        }

                        Optional<Object> consumes2 = data.get(otherEntry.getKey()).get(consumes);
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
    }
}
