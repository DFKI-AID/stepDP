package de.dfki.step.input;

import de.dfki.step.core.Clock;
import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.core.Token;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * User input is forwarded as {@link Token} into the InputHandler, which uses
 * rules for fusion and intent construction.
 */
public class InputHandler implements Runnable {
    private static Logger log = LoggerFactory.getLogger(InputHandler.class);
    private PSet<Token> tokens = HashTreePSet.empty();
    //tokens that are used for the next iteration
    private RuleSystem ruleSystem = new RuleSystem(new Clock(100));
    private PSet<Token> activeTokens;

    public synchronized void addTokens(Collection<Token> tokens) {
        log.debug("Adding tokens {}", tokens);
        this.tokens = this.tokens.plusAll(tokens);
    }

    public synchronized void addToken(Token token) {
        log.debug("Adding token {}", token);
        tokens = tokens.plus(token);
    }

    public void run() {

        while(!Thread.currentThread().isInterrupted()) {
            synchronized (this) {
                activeTokens = tokens;
                tokens = HashTreePSet.empty();
            }
            ruleSystem.update();
        }
    }

    public Collection<Token> getTokens() {
        return activeTokens;
    }
}
