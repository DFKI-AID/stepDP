package de.dfki.step.core;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Checks whether a token matches a certain pattern: e.g. if fields are set, the values of the fields
 * or the dependence between the field values.
 * <p>
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

        public Builder equals(Key key, Object value) {
            return test(key, Object.class, x -> Objects.equals(x, value));
        }

        public Builder equals(String key, Object value) {
            return test(Key.of(key), Object.class, x -> Objects.equals(x, value));
        }

        public Builder equalsOneOf(Key key, List<Object> values) {
            return test(key, Object.class, x -> {
                for (Object obj : values) {
                    if (Objects.equals(obj, x)) {
                        return true;
                    }
                }
                return false;
            });
        }

        public Builder equalsOneOf(Key key, Object... values) {
            return this.equalsOneOf(key, List.of(values));
        }

        public Builder lessThan(Key key, int number) {
            return test(key, Integer.class, x -> x < number);
        }

        public Builder greaterThan(Key key, int number) {
            return test(key, Integer.class, x -> x > number);
        }

        public Builder startsWith(Key key, String prefix) {
            return test(key, String.class, x -> x.startsWith(prefix));
        }

        public <T> Builder test(String key, Class<T> clazz, Predicate<T> pred) {
            return test(Key.of(key), clazz, pred);
        }

        public <T> Builder test(Key key, Class<T> clazz, Predicate<T> pred) {
            add(t -> {
                var opt = t.get(clazz, key.getKeys());
                if (!opt.isPresent()) {
                    return false;
                }
                return pred.test(opt.get());
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
