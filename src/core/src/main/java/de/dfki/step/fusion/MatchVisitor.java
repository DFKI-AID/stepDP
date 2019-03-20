package de.dfki.step.fusion;

import de.dfki.step.rengine.Token;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Matches the input history against a\ FusionNode
 */
public class MatchVisitor implements FusionNode.Visitor {
    private List<Token> tokens;
    private Map<Token, Boolean> marked;
    private List<Match> matches;

    public List<Match> accept(FusionNode fusionNode, List<Token> tokens) {
        this.tokens = tokens;
        marked = new HashMap<>();
        fusionNode.accept(this);
        return matches;
    }

    @Override
    public void visit(InputNode input) {
        matches = new ArrayList<>();
        tokens.stream()
                .filter(t -> input.matches(t))
                .forEach(t -> matches.add(new Match(List.of(t))));
    }

    @Override
    public void visit(ParallelNode input) {
        List<List<Match>> candidates = new ArrayList<>();


        for (FusionNode child : input.getChildren()) {
            child.accept(this);
            candidates.add(matches);
        }


        matches = merge(candidates, new Match(TreePVector.empty()), 0);
    }

    @Override
    public void visit(OptionalNode node) {
        matches = new ArrayList<>();
        node.getChild().accept(this);
        if(matches.isEmpty()) {
            matches.add(new Match(new Token().add("optional", "optional")));
        }
    }

    /**
     * Find matches by checking whether the same token is consumed by another node.
     *
     * @param candidates
     * @param match
     * @param i          The index of the candidate that is processed
     * @return
     */
    protected PSequence<Match> merge(List<List<Match>> candidates, Match match, int i) {
        PSequence<Match> out = TreePVector.empty();
        if (i >= candidates.size()) {
            out = out.plus(match);
            return out;
        }

        for (Match c : candidates.get(i)) {
            if (!match.intersects(c).isEmpty()) {
                //token is already consumed by another node
                continue;
            }

            Match wNew = match.add(c);

            PSequence<Match> outTmp = merge(candidates, wNew, i + 1);
            out = out.plusAll(outTmp);
        }

        return out;
    }
}
