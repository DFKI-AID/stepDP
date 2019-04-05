package de.dfki.step.fusion;

import de.dfki.step.core.Token;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Match {
    private PMap<InputNode, Token> tokens;

    public Match() {
        this.tokens = HashTreePMap.empty();
    }

    public Match(InputNode node, Token token) {
        this.tokens = HashTreePMap.empty();
        tokens = tokens.plus(node, token);
    }

    public Match(Map<InputNode, Token> tokens) {
        this.tokens = HashTreePMap.empty();
        this.tokens = this.tokens.plusAll(tokens);
    }

    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    public Match add(Match other) {
        //TODO throw exception on key conflict
        var tokens = this.tokens.plusAll(other.tokens);
        return new Match(tokens);
    }

    public List<Token> intersects(Match other) {
        List<Token> it = tokens.values().stream()
                .filter(t -> other.tokens.values().contains(t))
                .collect(Collectors.toList());
        return it;
    }

    @Override
    public String toString() {
        return "Match{" +
                "tokens=" + tokens.values().stream().map(Token::toString).reduce("", (t1,t2) -> t1 + t2) +
                '}';
    }

    public Collection<Token> getTokens() {
        return tokens.values();
    }

    public Optional<Token> getToken(InputNode node) {
        return Optional.of(tokens.get(node));
    }

}
