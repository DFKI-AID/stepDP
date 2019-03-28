package de.dfki.step.fusion;

import de.dfki.step.dialog.Dialog;
import de.dfki.step.rengine.Rule;
import de.dfki.step.core.Token;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.function.Predicate;

public class FusionFactory {

    public Builder createMultimodal(String ruleName) {
        return new Builder(ruleName);
    }

    public static class Builder {
        private final String ruleName;
        private PMap<String, Predicate<Token>> preds = HashTreePMap.empty();
        private PMap<String, Boolean> conditions = HashTreePMap.empty();

        public Builder(String ruleName) {
            this.ruleName = ruleName;
        }

        public Builder add(Predicate<Token> cond) {
            throw new UnsupportedOperationException("not impl");
        }

        public Rule build(Dialog dialog) {
            final var pred = this.preds;
            final var conds = this.conditions;

            Rule rule = () -> {
//                dialog./

            };

            return rule;
        }
    }

    public static void main(String[] args) {
        var builder = new Builder("allWindows");
        builder.add(t -> t.payloadEquals("sp", "all windows"));
        builder.add(t -> t.payloadEquals("gesture", "down"));
    }
}
