package de.dfki.rengine;

import java.util.*;

/**
 *
 */
public class State {
    private final String state;
    private final Set<String> rules;
    private final Map<String, Boolean> volatileMap;

    protected State(Builder builder) {
        this.state = builder.state;
        this.rules = Collections.unmodifiableSet(
                new HashSet<>(builder.rules));
        this.volatileMap = Collections.unmodifiableMap(
                new HashMap<>(builder.volatileMap));
    }

    public String getId() {
        return state;
    }

    public Set<String> getRules() {
        return rules;
    }

    public boolean isVolatile(String rule) {
        return volatileMap.getOrDefault(rule, false);
    }

    public static class Builder {
        private final String state;
        private final StateHandler stateHandler;
        private final Set<String> rules = new HashSet<>();
        private final Map<String, Boolean> volatileMap = new HashMap<>();

        public Builder(StateHandler stateHandler, String state) {
            this.stateHandler = stateHandler;
            this.state = state;
        }

        public Builder addRule(String rule) {
            return this.addRule(rule, false);
        }

        public Builder addRule(String rule, boolean vol) {
            this.rules.add(rule);
            volatileMap.put(rule, vol);
            return this;
        }

        public void finish() {
            this.stateHandler.addState(this.build());
        }

        protected State build() {
            return new State(this);
        }
    }
}
