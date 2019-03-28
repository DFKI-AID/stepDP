package de.dfki.step.core;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Checks whether a token matches a certain pattern: e.g. if fields are set, the values of the fields
 * or the dependence between the field values.
 *
 * For usage, see the unit tests
 */
public class Schema {
    private final PSequence<Pred> predicates;

    public Schema(PSequence<Pred> predicates) {
        this.predicates = predicates;
    }

    public boolean matches(Token token) {
        boolean result = predicates.stream()
                .map(p -> p.pred.test(token))
                .reduce(true, (a, b) -> a && b);
        return result;
    }

    public static class Builder {
        private PSequence<Pred> predicates = TreePVector.empty();
        private boolean optional = false;

        public Schema build() {
            return new Schema(predicates);
        }

        public Builder add(Predicate<Token> p) {
            var pred = new Pred(p);
            pred.optional = this.optional;
            this.optional = false;
            predicates = predicates.plus(pred);
            return this;
        }

        public Builder describe(String description) {
            //TODO
            return this;
        }

        public Builder optional() {
            this.optional = true;
            return this;
        }

        public Builder isSet(String key, Class type) {
            add(t -> t.get(key, type).isPresent());
            return this;
        }

        public Builder isSet(String key) {
            add(t -> t.get(key).isPresent());
            return this;
        }


        public Builder isSet(Key key) {
            add(t -> t.get(key.getKeys()).isPresent());
            return this;
        }

        public Builder isSet(Key key, Class type) {
            add(t -> t.get(type, key.getKeys()).isPresent());
            return this;
        }

        public Builder lessThan(Key key, int number) {
            compare(key, number, Integer.class, (x, y) -> x < y);
            return this;
        }

        public Builder greaterThan(Key key, int number) {
            compare(key, number, Integer.class, (x, y) -> x > y);
            return this;
        }

        public <T> Builder compare(Key key, T value, Class<T> clazz, BiFunction<T, T, Boolean> comp) {
            add(t -> {
                var opt = t.get(clazz, key.getKeys());
                if (!opt.isPresent()) {
                    return false;
                }
                return comp.apply(opt.get(), value);
            });
            return this;
        }
    }

    /**
     * e.g. a.b.c with each of them is a field name
     */
    public static class Key {
        private List<String> keys;

        public Key(List<String> keys) {
            if (keys.isEmpty()) {
                throw new IllegalArgumentException("keys can't be empty");
            }
            this.keys = Collections.unmodifiableList(keys);
        }

        public static Key of(String... keys) {
            return new Key(List.of(keys));
        }

        public static Key parse(String key) {
            return of(key.split("."));
        }

        public List<String> getKeys() {
            return keys;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "keys=" + keys.stream().reduce("", (x, y) -> x + "." + y) +
                    '}';
        }
    }

    private static class Pred {
        protected final Predicate<Token> pred;
        protected boolean optional;

        private Pred(Predicate<Token> pred) {
            this.pred = pred;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
