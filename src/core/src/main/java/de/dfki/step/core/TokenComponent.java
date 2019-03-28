package de.dfki.step.core;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

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
        //TODO impl
        return null;
    }

    @Override
    public void loadSnapshot(Object snapshot) {

    }

    public PSet<Token> getTokens() {
        return tokens;
    }

    public synchronized void addTokens(Collection<Token> tokens) {
        log.debug("Adding token {}", tokens);
        waitingTokens = waitingTokens.plusAll(tokens);
    }

    public synchronized void addToken(Token token) {
        log.debug("Adding token {}", token);
        waitingTokens = waitingTokens.plus(token);
    }
}
