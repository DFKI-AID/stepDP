package de.dfki.step.fusion;

import de.dfki.step.core.Component;
import de.dfki.step.core.ComponentManager;
import de.dfki.step.core.TokenComponent;
import de.dfki.step.core.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FusionComponent implements Component {
    private static Logger log = LoggerFactory.getLogger(FusionComponent.class);
    private Duration tokenTimeout = Duration.ofMillis(10000);
    private List<Token> waitingTokens = new ArrayList<>();
    private List<Token> tokens = new ArrayList<>();
    private Map<String, FusionNode> fusionNodes = new HashMap<>();
    private Map<String, Function<Match, Token>> intentBuilder = new HashMap<>();
    private ComponentManager cm;

    @Override
    public void init(ComponentManager cm) {
        this.cm = cm;
    }

    @Override
    public void deinit() {
    }

    @Override
    public void update() {
        var tokens = fuse();
        cm.retrieveComponent(TokenComponent.class).addTokens(tokens);
    }

    /**
     * Updates the fusion component by including newest inputs and merging them
     *
     * @return
     */
    public Collection<Token> fuse() {
        //TODO testing
        //this.addToken(new Token().add("focus", "task1"));

        synchronized (this) {
            tokens.addAll(waitingTokens);
            waitingTokens.clear();
        }
        //remove old tokens
        //TODO switch to iteration based model like the rule system -> makes debugging easier
        var now = System.currentTimeMillis();
        tokens = tokens.stream()
                .filter(t -> t.get("timestamp", Long.class).get() + tokenTimeout.toMillis() > now)
                .collect(Collectors.toList());

        if(tokens.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

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

        tokens.clear(); //TODO until consumed by coordinator

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
        this.waitingTokens.add(token);
    }

    public synchronized void addTokens(Collection<Token> tokens) {
        tokens.forEach(t -> addToken(t));
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
     * Builds a default token with the given intent, origin and confidence from the match
     * @param match
     * @param intent
     * @return
     */
    public static Token defaultIntent(Match match, String intent) {
        List<String> origin = FusionComponent.mergeOrigins(match.getTokens());
        Optional<Double> confidence = FusionComponent.mergeConfidence(match.getTokens());

        Token token = new Token()
                .add("intent", intent)
                .add("origin", origin)
                .addIfPresent("confidence", confidence);
        return token;
    }
}
