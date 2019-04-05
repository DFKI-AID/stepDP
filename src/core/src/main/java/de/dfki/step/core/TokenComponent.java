package de.dfki.step.core;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;

/**
 * Forward tokens into the dialog core. In general, each token should represent an intent of a user.
 * However, it can also contain arbitrary data. Rules may check which tokens are available and
 * react accordingly.
 */
public class TokenComponent implements Component {
    private static Logger log = LoggerFactory.getLogger(TokenComponent.class);
    private PSet<Token> tokens = HashTreePSet.empty();
    //tokens that are used for the next iteration
    private PSet<Token> waitingTokens = HashTreePSet.empty();

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
        log.debug("Adding token {}", token);
        waitingTokens = waitingTokens.plus(token);
    }
}
