package de.dfki.step.fusion;

import de.dfki.step.core.*;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

/**
 * Uses {@link FusionNode} to match input pattern for creating intents.
 */
public class FusionComponent implements Component {
    private static Logger log = LoggerFactory.getLogger(FusionComponent.class);
    private PMap<String, FusionNode> fusionNodes = HashTreePMap.empty();
    private PMap<String, Function<Match, Token>> intentBuilders = HashTreePMap.empty();
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
            var fnc = intentBuilders.get(entry.getKey());
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
        return List.of(fusionNodes, intentBuilders);
    }

    @Override
    public void loadSnapshot(Object snapshot) {
        List lsnapshot = (List) snapshot;
        this.fusionNodes = (PMap<String, FusionNode>) lsnapshot.get(0);
        this.intentBuilders = (PMap<String, Function<Match, Token>>) lsnapshot.get(1);
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
        fusionNodes = fusionNodes.plus(id, node);
        intentBuilders = intentBuilders.plus(id, intentBuilder);
    }

    public synchronized void removeFusionNode(String id) {
        fusionNodes = fusionNodes.minus(id);
        intentBuilders = intentBuilders.minus(id);
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
