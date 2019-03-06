package de.dfki.rengine;

import org.pcollections.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This component manages (add / remove / enable / disable) and execute rules.
 * TODO use persistent data structures
 */
public class RuleSystem {
    private static final Logger log = LoggerFactory.getLogger(RuleSystem.class);
    private final Clock clock;
    private boolean updateActive = false;

    private PSet<Token> tokens = HashTreePSet.empty();
    private PSequence<Rule> rules = TreePVector.empty();
    private PMap<String, Rule> nameToRule = HashTreePMap.empty();
    private BlockSystem blockSystem;
    private PMap<Rule, Integer> priorities = HashTreePMap.empty();
    private PMap<String, Boolean> volatileMap = HashTreePMap.empty();
    private static final int DEFAULT_PRIORITY = 50;

    public RuleSystem(Clock clock) {
        this.clock = clock;
        this.blockSystem = new BlockSystem(clock);
    }


    public void addToken(Token token) {
        log.debug("Adding token {}", token);
        tokens = tokens.plus(token);
    }

    public void removeToken(Token token) {
        log.debug("Removing token {}", token);
        tokens = tokens.minus(token);
    }

//    public void setTokens(Set<Token> tokens) {
//        log.debug("Updating tokens: {}", tokens); //TODO print diff
//        this.tokens = tokens;
//    }

    public Optional<String> getName(Rule rule) {
        return nameToRule.entrySet().stream()
                .filter(entry -> entry.getValue() == rule)
                .map(entry -> entry.getKey())
                .findFirst();
    }

    public Set<Token> getTokens() {
        return tokens;
    }

    public void addRule(String name, Rule rule) {
        if (nameToRule.containsKey(name)) {
            log.info("Overwriting rule: {}", name);
            this.removeRule(name);
        } else {
            log.info("Adding rule: {}", name);
        }

        rules = rules.plus(rule);
        nameToRule = nameToRule.plus(name, rule);
    }

    public void removeRule(Rule rule) {
        rules = rules.minus(rule);
        getName(rule).ifPresent(name -> nameToRule = nameToRule.minus(name));
    }

    public void removeRule(String name) {
        Optional<Rule> rule = getRule(name);
        if (!rule.isPresent()) {
            return;
        }
        log.info("Removing rule: {}", name);
        rules = rules.minus(rule.get());
        nameToRule = nameToRule.minus(name);
    }

    public void enable(String ruleName) {
        Optional<Rule> rule = getRule(ruleName);
        if (!rule.isPresent()) {
            log.warn("Can't enable rule: No rule found with name {}", ruleName);
            return;
        }
        this.enable(rule.get());
    }

    public void enable(Rule rule) {
        log.info("Enabling rule: {}", getName(rule).orElse(null));
        blockSystem.enable(rule);
    }

    public boolean isEnabled(Rule rule) {
        return blockSystem.isEnabled(rule);
    }

    public void disable(String ruleName) {
        Optional<Rule> rule = getRule(ruleName);
        if (!rule.isPresent()) {
            log.warn("Can'second disable rule: No rule found with name {}", ruleName);
            return;
        }
        this.disable(rule.get());
    }

    public void disable(Rule rule) {
        log.info("Disabling rule: {}", getName(rule).orElse(null));
        blockSystem.disable(rule);
    }

    public void disable(Rule rule, Duration duration) {
        log.info("Disabling rule {}", getName(rule));
        blockSystem.disable(rule, clock.convert(duration));
    }

    public void disable(String ruleName, Duration duration) {
        Optional<Rule> rule = getRule(ruleName);
        if (!rule.isPresent()) {
            log.warn("Can'second enable rule: No rule found with name {}", ruleName);
            return;
        }
        this.disable(rule.get(), duration);
    }

    public void setPriority(String ruleName, int priority) {
        var rule = getRule(ruleName);
        if (!rule.isPresent()) {
            log.warn("Can't set priority for {}: No rule available with the given name", ruleName);
            return;
        }
        priorities = priorities.plus(rule.get(), priority);
    }

    public int getPriority(String ruleName) {
        var rule = getRule(ruleName);
        if (!rule.isPresent()) {
            return DEFAULT_PRIORITY;
        }
        return getPriority(rule.get());
    }

    public int getPriority(Rule rule) {
        if (!priorities.containsKey(rule)) {
            return DEFAULT_PRIORITY;
        }
        return priorities.get(rule);
    }

    public Optional<Rule> getRule(String name) {
        return Optional.ofNullable(nameToRule.get(name));
    }

    public void update() {
        updateActive = true;

        tokens = HashTreePSet.empty();
        //making a copy of the rule set, which allows to change the rule set within the update method
        ArrayList<Rule> rulesCopy = new ArrayList<>();
        rulesCopy.addAll(rules);
        rulesCopy.sort(Comparator.comparingInt(this::getPriority));

        for (Rule rule : rulesCopy) {
            if (blockSystem.isDisabled(rule)) {
                continue;
            }

            if (!this.rules.contains(rule)) {
                // this rule was removed during the update method should not be considered here anymore
                continue;
            }
            rule.update(this);
        }
        updateActive = false;
    }

    public int getIteration() {
        return clock.getIteration();
    }

    public List<Rule> getRules() {
        return rules;
    }


    public Snapshot createSnapshot() {
        Snapshot state = new Snapshot();
        state.blockSystem = blockSystem.copy();
        state.rules = this.rules;
        state.iteration = clock.getIteration();
        state.priorities = this.priorities;
        state.nameToRule = this.nameToRule;
        state.volatileMap = this.volatileMap;
        return state;
    }

    public void applySnapshot(Snapshot snapshot) {
        if (updateActive) {
            throw new IllegalStateException("Can't apply snapshot while updating the RuleSystem");
        }
        this.clock.setIteration(snapshot.iteration);
        this.blockSystem = snapshot.blockSystem.copy();
        this.rules = snapshot.rules;
        this.priorities = snapshot.priorities;
        this.nameToRule = snapshot.nameToRule;
        this.volatileMap = snapshot.volatileMap;
    }

    public Clock getClock() {
        return clock;
    }

    public void setVolatile(String rule, boolean vol) {
        volatileMap = volatileMap.plus(rule, vol);
    }

    /**
     * Volatile rules should be removed instead of disabled
     *
     * @param rule
     * @return
     */
    public boolean isVolatile(String rule) {
        return Optional.ofNullable(volatileMap.get(rule)).orElse(false);
    }


    public static class Snapshot {
        public BlockSystem blockSystem;
        //        public Set<Token> tokens;
        public PSequence<Rule> rules;
        public int iteration;
        public PMap<Rule, Integer> priorities;
        public PMap<String, Rule> nameToRule;
        public PMap<String, Boolean> volatileMap;

    }

}
