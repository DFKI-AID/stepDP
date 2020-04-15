package de.dfki.step.core;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.List;
import java.util.UUID;

/**
 * Forward tokens into the dialog core. In general, each token should represent an intent of a user.
 * However, it can also contain arbitrary data. Rules may check which tokens are available and
 * react accordingly.
 *
 * TODO maybe rename, because the InputComponent has similar capabilties. It should be clear that this class
 * forwards tokens into the dialog core (rules).
 */
public class TokenComponent implements Component {
    private static Logger log = LoggerFactory.getLogger(TokenComponent.class);
    private PSet<Token> tokens = HashTreePSet.empty();
    //tokens that are used for the next iteration
    private PSet<Token> waitingTokens = HashTreePSet.empty();
    private ClockComponent cc;

    @Override
    public void init(ComponentManager cm) {
        cc = cm.retrieveComponent(ClockComponent.class);
    }

    @Override
    public void deinit() {
    }

    @Override
    public void update() {
        //removing all tokens that were used last round
        synchronized(this) {
            waitingTokens = waitingTokens.minusAll(tokens);
            tokens = waitingTokens;
        }
    }

    @Override
    public Object createSnapshot() {
        //nothing stored because the tokens are removed after each iteration
        return null;
    }

    @Override
    public void loadSnapshot(Object snapshot) {
    }

    public PSet<Token> getTokens() {
        return tokens;
    }

    /**
     * Add tokens which are used during the next iteration
     * @param tokens
     */
    public synchronized void addTokens(Collection<Token> tokens) {
        log.debug("Adding tokens {}", tokens.stream()
                .map(Objects::toString)
                .reduce("", (x, y) -> x + ", " + y));
        waitingTokens = waitingTokens.plusAll(tokens);
    }

    /**
     * Add a token which used during the next iteration
     * @param token
     */
    public synchronized void addToken(Token token) {
        token = addTimestamp(token);
        token = ensureOrigin(token);
        log.debug("Adding token {}", token);
        waitingTokens = waitingTokens.plus(token);
    }

    public Token addTimestamp(Token token) {
        if (!token.has("timestamp")) {
            long iteration = cc.getIteration();
            token = token.add("timestamp", iteration);
        }

        return token;
    }

    public static Token ensureOrigin(Token token) {
        if (!token.has("origin")) {
            token = token.add("origin", List.of(UUID.randomUUID().toString()));
        }
        return token;
}
