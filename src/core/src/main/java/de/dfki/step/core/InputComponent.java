package de.dfki.step.core;

import de.dfki.step.fusion.FusionComponent;
import de.dfki.step.rengine.CoordinationComponent;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Input is stored in this component such that
 * a fusion component can read from it {@link FusionComponent#update()}
 * or
 * the coordinator can remove consumed tokens {@link CoordinationComponent#update()}
 *
 * TODO consume could also invalidate inputs that are older than the origin
 */
public class InputComponent implements Component {
    private static Logger log = LoggerFactory.getLogger(InputComponent.class);
    private Duration tokenTimeout = Duration.ofMillis(10000);

    private PSet<Token> tokens = HashTreePSet.empty();
    //tokens that are used for the next iteration
    private PSet<Token> waitingTokens = HashTreePSet.empty();
    private PSet<Token> volatileTokens = HashTreePSet.empty();
    private PSet<Token> currentTokens = HashTreePSet.empty();

    @Override
    public void init(ComponentManager cm) {

    }

    @Override
    public void deinit() {

    }

    @Override
    public void update() {

        //removing all tokens that were used last round
        synchronized(this) {
            tokens = tokens.plusAll(waitingTokens);
            waitingTokens = HashTreePSet.empty();
            currentTokens = tokens.plusAll(volatileTokens);
            volatileTokens = HashTreePSet.empty();
        }



        //remove old tokens
        //TODO switch to iteration based model like the rule system -> makes debugging easier
//        var now = System.currentTimeMillis();
//        for(Token t : tokens.stream()
//                .filter(t -> t.get("timestamp", Long.class).get() + tokenTimeout.toMillis() > now)
//                .collect(Collectors.toList())) {
//            tokens = tokens.minus(t);
//        }
    }

    @Override
    public Object createSnapshot() {
        return tokens;
    }

    @Override
    public void loadSnapshot(Object snapshot) {
        this.tokens = (PSet<Token>) snapshot;
    }

    public PSet<Token> getTokens() {
        return currentTokens;
    }

//    public void consume(Object token) {
//        log.info("consuming token {}", token);
//        this.tokens = this.tokens.minus(token);
//    }

    /**
     * Remove all tokens that one of the origins in their origin field
     * @param origins
     */
    public void consume(Collection<Object> origins) {
        List<Token> consumedTokens = tokens.stream()
                .filter(t -> t.payloadEquals("origin", origins))
                .collect(Collectors.toList());

        consumedTokens.addAll(tokens.stream()
                .filter(t -> t.has("origin", Collection.class))
                .collect(Collectors.toList()));

        log.info("consuming tokens {}", consumedTokens.stream()
                .map(Objects::toString)
                .reduce("", (x, y) -> x + "," + y));
        this.tokens = this.tokens.minusAll(consumedTokens);
    }

    public synchronized void addTokens(Collection<Token> tokens) {
        tokens.forEach(t -> addToken(t));
    }

    public synchronized void addToken(Token token) {
        token = ensureTimestamp(token);
        token = ensureOrigin(token);
        log.debug("Adding token {}", token);
        waitingTokens = waitingTokens.plus(token);
    }

    /**
     * Tokens added via this function will only survive one iteration. e.g. use this for
     * continuous input that does not trigger something on its own like visual focus.
     * @param token
     */
    public synchronized void addVolatileToken(Token token) {
        token = ensureTimestamp(token);
        token = ensureOrigin(token);
        log.debug("Adding volatile token {}", token);
        volatileTokens = volatileTokens.plus(token);
    }

    public static Token ensureTimestamp(Token token) {
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
        return token;
    }

    public static Token ensureOrigin(Token token) {
        if (!token.has("origin")) {
            token = token.add("origin", List.of(UUID.randomUUID().toString()));
        }
        return token;
    }

    /**
     * Sets the maximal duration of input tokens.
     * Tokens that are older than this timeout will be removed
     * @param timeout
     */
    public synchronized void setTimeout(Duration timeout) {
        this.tokenTimeout = timeout;
    }
}
