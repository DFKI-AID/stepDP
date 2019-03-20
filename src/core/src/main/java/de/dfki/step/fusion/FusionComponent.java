package de.dfki.step.fusion;

import de.dfki.step.rengine.Token;
import org.pcollections.PSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FusionComponent {
    private static Logger log = LoggerFactory.getLogger(FusionComponent.class);
    private Duration tokenTimeout = Duration.ofMillis(1000);
    private List<Token> waitingTokens = new ArrayList<>();
    private List<Token> tokens = new ArrayList<>();
    private Collection<FusionNode> fusionNodes = new ArrayList<>();
    private Map<FusionNode, Function<Match, Token>> fusionOutputs = new HashMap<>();

    public static void main(String[] args) {

        //define how our multimodal inputs looks like
        InputNode gestureNode = new InputNode(t -> t.payloadEquals("gesture", "down"));
        InputNode windowsNode = new InputNode(t -> t.payloadEqualsOneOf("speech", "all_windows", "front left window"));
        InputNode fastNode = new InputNode(t -> t.payloadEquals("speech", "fast"));

//        InputNode focusNode = new InputNode(t -> t.payloadEquals("focus", "car"));
        InputNode focusNode = new InputNode(t -> t.payloadEquals("focus", "car"));

        InputNode windows = new InputNode(
                t -> {
                    List<String> candidates = (List<String>) t.get("candidates").get();
                    return candidates.contains("all_windows");
                });

        ParallelNode node = new ParallelNode()
                .add(gestureNode)
                .add(windowsNode)
                .add(new OptionalNode(fastNode))
                .add(focusNode);


        FusionComponent fc = new FusionComponent();
        fc.addFusionNode(node, match -> {
            List<String> origin = mergeAll("origin", String.class, match.getTokens());
            OptionalDouble confidence = mergeAll("confidence", Double.class, match.getTokens()).stream()
                    .mapToDouble(x -> x).average();
            Optional<String> what = get("speech", String.class, match.getTokens());
            //TODO resolve / transform id of the 'what' if necessary

            Token token = new Token()
                    .add("intent", "control_car_windows")
                    .add("origin", origin)
                    .add("obj", what.get());

            if(confidence.isPresent()) {
                token = token.add("confidence", confidence.getAsDouble());
            }

            return token;
        });

        InputNode intentNode= new InputNode(t -> t.has("intent"));
        fc.addFusionNode(intentNode, match -> {
            return match.getTokens().get(0);
        });


        var tokens = List.of(
//                new Token()
//                        .add("gesture", "down")
//                        .add("timestamp", 1000L)
//                        .add("origin", UUID.randomUUID().toString())
//                        .add("confidence", 0.2)
//                ,
                new Token()
                        .add("gesture", "down")
                        .add("timestamp", 1500L)
                        .add("origin", UUID.randomUUID().toString())
                        .add("confidence", 0.8)
                ,
                new Token()
                        .add("speech", "all_windows")
                        .add("timestamp", 2000L)
                        .add("origin", UUID.randomUUID().toString())
                        .add("confidence", 0.4)
//                ,
//                new Token()
//                        .add("speech", "front left window")
//                        .add("timestamp", 1000L)
//                        .add("origin", UUID.randomUUID().toString())
//                        .add("confidence", 0.8)
                ,
                new Token()
                        .add("focus", "car")
                        .add("timestamp", 1000L)
                        .add("origin", UUID.randomUUID().toString())
//                ,
//                new Token()
//                        .add("speech", "fast")
//                        .add("timestamp", 1000L)
//                        .add("origin", UUID.randomUUID().toString())
//                        .add("confidence", 0.4)
//                ,
//                new Token()
//                        .add("focus", "desktop")
//                        .add("timestamp", 1000L)
//                        .add("origin", UUID.randomUUID().toString())
//                        .add("confidence", 0.6)
        );


        tokens.forEach(t -> System.out.println(t));
        System.out.println();
//        var mv = new MatchVisitor();
//        var result = mv.accept(node, tokens);
//        result.forEach(r -> {
//            System.out.println(r);
//        });

        fc.addTokens(tokens);
        Collection<Token> intents = fc.update();

        intents.forEach(intent -> {
            System.out.println(intent);
        });
    }

    /**
     * Updates the fusion component by including newest inputs and merging them
     *
     * @return
     */
    public Collection<Token> update() {
        synchronized (this) {
            tokens.addAll(waitingTokens);
            waitingTokens.clear();
        }

        //remove old tokens
        //TODO switch to iteration based model like the rule system -> makes debugging easier
        var now = System.currentTimeMillis();
        tokens = tokens.stream()
                .filter(t -> t.get("timestamp", Long.class).get() + tokenTimeout.toMillis()< now)
                .collect(Collectors.toList());


        List<Token> intents = new ArrayList<>();
        var mv = new MatchVisitor();
        for(FusionNode fn : fusionNodes) {
            var fnc = fusionOutputs.get(fn);
            var result = mv.accept(fn, tokens);
            result.forEach(match -> {
                Token intent = fnc.apply(match);
                intents.add(intent);
            });
        }

        return intents;
    }


    public static <T> Optional<T> get(String id, Class<T> clazz, Collection<Token> tokens) {
        for (Token t : tokens) {
            if (t.has(id)) {
                return t.get(id, clazz);
            }
        }
        return Optional.empty();
    }

    /**
     * Extracts a field from all given tokens if available and the type matches
     *
     * @param id
     * @param clazz
     * @param tokens
     * @param <T>
     * @return The field value of all tokens if the field is set correctly
     */
    public static <T> List<T> getAll(String id, Class<T> clazz, Collection<Token> tokens) {
        List<T> result = new ArrayList<>();
        for (Token t : tokens) {
            if (t.has(id)) {
                result.add(t.get(id, clazz).get());
            }
        }
        return result;
    }

    /**
     * Extracts a field from all given tokens if available and merges them into one List.
     * If a field value is a collection, each element will be added individually.
     * <p>
     * e.g.
     * t1.origin = "kinect1"
     * t2.origin = ["hololens", "eye_tracker13"]
     * <p>
     * result.origin = ["kinect1", "hololens", "eye_tracker13"]
     *
     * @param id
     * @param clazz
     * @param tokens
     * @param <T>
     * @return The field value of all tokens if the field is set correctly
     */
    public static <T> List<T> mergeAll(String id, Class<T> clazz, Collection<Token> tokens) {
        List<T> result = new ArrayList<>();
        for (Token t : tokens) {
            if (t.has(id)) {
                Object obj = t.get(id).get();
                if (Collection.class.isAssignableFrom(obj.getClass())) {
                    Collection<Object> objCol = (Collection<Object>) obj;
                    for (Object innerObj : objCol) {
                        if (!clazz.isAssignableFrom(innerObj.getClass())) {
                            log.debug("can't merge token field: type mismatch. expected={} got={}", clazz, innerObj.getClass());
                            continue;
                        }
                        result.add((T) innerObj);
                    }
                    continue;
                }

                if (clazz.isAssignableFrom(obj.getClass())) {
                    result.add((T) obj);
                    continue;
                }

                log.debug("can't merge token fields: not ");
            }
        }
        return result;
    }

    public synchronized void addFusionNode(FusionNode node, Function<Match, Token> intentBuilder) {
        fusionNodes.add(node);
        fusionOutputs.put(node, intentBuilder);
    }


    public synchronized void addToken(Token token) {
        if (!token.has("timestamp", Long.class)) {
            if (token.has("timestamp", Number.class)) {
                //timestamp found, but with wrong type -> convert
                long timestamp = (Long) token.get("timestamp").get();
                token = token.add("timestamp", timestamp);
            } else {
                //none timestamp found -> use current time
                token = token.add("timestamp", System.currentTimeMillis());
            }
        }
        this.tokens.add(token);
    }

    public synchronized void addTokens(Collection<Token> tokens) {
        tokens.forEach(t -> addToken(t));
    }
}
