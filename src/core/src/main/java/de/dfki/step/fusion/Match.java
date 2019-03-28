package de.dfki.step.fusion;

import de.dfki.step.core.Token;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Match {
    private PSequence<Token> tokens;

    /**
     * @param tokens All tokens that are necessary to specify this match
     */
    public Match(PSequence<Token> tokens) {
        this.tokens = tokens;
    }

    public Match(Token token) {
        tokens = TreePVector.empty();
        tokens = tokens.plus(token);
    }

    public Match(Collection<Token> tokens) {
        this.tokens = TreePVector.empty();
        this.tokens = this.tokens.plusAll(tokens);
    }

    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    public Match add(Match other) {
        var tokens = this.tokens.plusAll(other.tokens);
        return new Match(tokens);
    }

    public Match add(List<Token> tokens) {
        return new Match(this.tokens.plusAll(tokens));
    }

    public Match intersects(Match other) {
        List<Token> it = tokens.stream()
                .filter(t -> other.tokens.contains(t))
                .collect(Collectors.toList());
        return new Match(it);
    }

    @Override
    public String toString() {
        return "Match{" +
                "tokens=" + tokens.stream().map(Token::toString).reduce("", (t1,t2) -> t1 + t2) +
                '}';
    }

    public PSequence<Token> getTokens() {
        return tokens;
    }
}
