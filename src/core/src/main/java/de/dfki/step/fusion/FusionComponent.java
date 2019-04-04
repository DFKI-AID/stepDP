package de.dfki.step.fusion;

import de.dfki.step.core.*;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FusionComponent implements Component {
    private static Logger log = LoggerFactory.getLogger(FusionComponent.class);
    private Map<String, FusionNode> fusionNodes = new HashMap<>();
    private Map<String, Function<Match, Token>> intentBuilder = new HashMap<>();
    private ComponentManager cm;
    private InputComponent ic;

    @Override
    public void init(ComponentManager cm) {
        this.cm = cm;
        this.ic = cm.retrieveComponent(InputComponent.class);
    }

    @Override
    public void deinit() {
    }

    @Override
    public void update() {
        PSet<Token> tokens = ic.getTokens();
        var fusedTokens = fuse(tokens);
        cm.retrieveComponent(TokenComponent.class).addTokens(fusedTokens);
    }

    /**
     * Updates the fusion component by including newest inputs and merging them
     *
     * @return
     */
    public Collection<Token> fuse(Set<Token> tokens) {
        if(tokens.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        //TODO impl additional visitor that checks if a InputNode is used at most once for a tree

        List<Token> intents = new ArrayList<>();
        var mv = new MatchVisitor();
        for(var entry : fusionNodes.entrySet()) {
            var fnc = intentBuilder.get(entry.getKey());
            var result = mv.accept(entry.getValue(), tokens);
            result.forEach(match -> {
                Token intent = fnc.apply(match);
                intents.add(intent);
            });
        }

        return intents;
    }

    @Override
    public Object createSnapshot() {
        //TODO impl;
        return null;
    }

    @Override
    public void loadSnapshot(Object snapshot) {
        //TODO impl
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

    public synchronized void addFusionNode(String id, FusionNode node, Function<Match, Token> intentBuilder) {
        fusionNodes.put(id, node);
        this.intentBuilder.put(id, intentBuilder);
    }

    public synchronized void removeFusionNode(String id) {
        fusionNodes.remove(id);
        intentBuilder.remove(id);
    }

    public static List<String> mergeOrigins(Collection<Token> tokens) {
        List<String> origin = Token.mergeFields("origin", String.class, tokens);
        return origin;
    }

    public static Optional<Double> mergeConfidence(Collection<Token> tokens) {
        OptionalDouble confidence = Token.mergeFields("confidence", Double.class, tokens).stream()
                .mapToDouble(x -> x).average();
        if(confidence.isPresent()) {
            return Optional.of(confidence.getAsDouble());
        }
        return Optional.empty();
    }

    /**
     * Builds a default token with the given intent, origin, confidence and timestamps from the match
     * @param match
     * @param intent
     * @return
     */
    public static Token defaultIntent(Match match, String intent) {
        List<String> origin = FusionComponent.mergeOrigins(match.getTokens());
        Optional<Double> confidence = FusionComponent.mergeConfidence(match.getTokens());
        Optional<Long> minTimestamp = match.getTokens().stream()
                .filter(t -> t.has("timestamp", Long.class))
                .map(t -> t.get("timestamp", Long.class).get())
                .reduce((x,y) -> Math.min(x,y));
        Optional<Long> maxTimestamp = match.getTokens().stream()
                .filter(t -> t.has("timestamp", Long.class))
                .map(t -> t.get("timestamp", Long.class).get())
                .reduce((x,y) -> Math.max(x,y));

        Token token = new Token()
                .add("intent", intent)
                .add("origin", origin)
                .addIfPresent("confidence", confidence)
                .addIfPresent("timestamp.min", minTimestamp)
                .addIfPresent("timestamp.max", maxTimestamp);
        return token;
    }
}
