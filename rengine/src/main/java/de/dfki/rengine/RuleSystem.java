package de.dfki.rengine;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This component manages (add / remove / enable / disable) and execute rules.
 */
public class RuleSystem {
    private static final Logger log = LoggerFactory.getLogger(RuleSystem.class);
    private Clock clock = new Clock(500);
    private Set<Token> tokens = new HashSet<>(); //TODO maybe list?
    private PSequence<Rule> rules = TreePVector.empty();
    private Map<String, Rule> nameToRule = new HashMap<>();
    private BlockSystem blockSystem = new BlockSystem(clock);
    private Map<Rule, Integer> priorities = new HashMap<>();
    private Map<String, Boolean> volatileMap = new HashMap<>();
    private static final int DEFAULT_PRIORITY = 50;


    public void addToken(Token token) {
        log.debug("Adding {}", token);
        this.tokens.add(token);
    }

    public void removeToken(Token token) {
        log.debug("Removing {}", token);
        this.tokens.remove(token);
    }

    public void setTokens(Set<Token> tokens) {
        log.debug("Updating tokens: {}", tokens); //TODO print diff
        this.tokens = tokens;
    }

    public Optional<String> getName(Rule rule) {
        return nameToRule.entrySet().stream()
                .filter(entry -> entry.getValue() == rule)
                .map(entry -> entry.getKey())
                .findFirst();
    }

    public Set<Token> getTokens() {
        //TODO sync or use persistent data structure
        return Collections.unmodifiableSet(tokens);
    }

    public void addRule(String name, Rule rule) {
        if (nameToRule.containsKey(name)) {
            log.info("overwriting rule {}", name);
            this.removeRule(name);
        } else {
            log.info("adding rule {}", name);
        }

        rules = rules.plus(rule);
        nameToRule.put(name, rule);
    }

    public void removeRule(Rule rule) {
        rules = rules.minus(rule);
        getName(rule).ifPresent(name -> nameToRule.remove(name));
    }

    public void removeRule(String name) {
        Optional<Rule> rule = getRule(name);
        if (!rule.isPresent()) {
            return;
        }
        log.info("removing rule {}", name);
        rules = rules.minus(rule.get());
        this.nameToRule.remove(name);
    }

    public void enable(String ruleName) {
        Optional<Rule> rule = getRule(ruleName);
        if (!rule.isPresent()) {
            log.warn("Can'second enable rule: No rule found with name {}", ruleName);
            return;
        }
        this.enable(rule.get());
    }

    public void enable(Rule rule) {
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
        blockSystem.disable(rule);
    }

    public void disable(Rule rule, Duration duration) {
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
        this.priorities.put(rule.get(), priority);
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


//    public void disable(String name) {
//        Optional<Rule> rule = getRule(name);
//        if (!rule.isPresent()) {
//            System.out.println("can'second disable rule: no rule found with name: " + name);
//            return;
//        }
//        disable(rule.get());
//    }

//    public void disable(Rule rule) {
//        //TODO improve: disable for one iteration, disable for a certain amount of time, ...
//        System.out.println("blocking rule " + rule);
//        blockList.add(rule);
//    }

    public Optional<Rule> getRule(String name) {
        return Optional.ofNullable(nameToRule.get(name));
    }

    public void update() throws InterruptedException {
        int targetSnapshot = snapshotTarget.getAndSet(-1);
        if (targetSnapshot >= 0) {
            this.applySnapshot(targetSnapshot);
        }

        var state = this.createSnapshot();
        this.snapshots.put(clock.getIteration(), state);

        tokens.clear();
        //making a copy of the rule set, which allows to change the rule set within the update method
        ArrayList<Rule> rulesCopy = new ArrayList<>();
        rulesCopy.addAll(rules);

        rulesCopy.sort(Comparator.comparingInt(this::getPriority));

        for (Rule rule : rulesCopy) {
            if (blockSystem.isDisabled(rule)) {
                continue;
            }

            rule.update(this);
        }

        clock.inc();
        Thread.sleep((long) clock.getRate()); //TODO no precise, but sufficient to start with
    }

    public int getIteration() {
        return clock.getIteration();
    }

    public List<Rule> getRules() {
        return rules;
    }

    private Map<Integer, State> snapshots = new HashMap<>();

    private State createSnapshot() {
        State state = new State();
        state.blockSystem = blockSystem.copy();
//        state.tokens = new HashSet<>();
//        state.tokens.addAll(this.tokens)
//        state.rules = new ArrayList<>();
//        state.rules.addAll(this.rules);
        state.rules = this.rules; //persistent data structure
        state.iteration = clock.getIteration();
        state.priorities = new HashMap<>();
        state.priorities.putAll(this.priorities);
        state.nameToRule = new HashMap<>();
        state.nameToRule.putAll(this.nameToRule);
        return state;
    }

    private AtomicInteger snapshotTarget = new AtomicInteger(-1);

    public void rewind(int iteration) {
        snapshotTarget.set(iteration);
    }

    public Clock getClock() {
        return clock;
    }

    public void setVolatile(String rule, boolean vol) {
        volatileMap.put(rule, vol);
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

    //TODO move to separate class
    private void applySnapshot(int iteration) {
        log.info("rewinding to {}", iteration);
        //TODO check parameters
        State state = snapshots.get(iteration);
        this.clock.setIteration(state.iteration);
        this.blockSystem = state.blockSystem.copy();
        this.rules = TreePVector.empty();
        this.rules = this.rules.plusAll(state.rules);
        this.priorities = new HashMap<>();
        this.priorities.putAll(state.priorities);
        this.nameToRule = new HashMap<>();
        this.nameToRule.putAll(state.nameToRule);
    }

    private static class State {
        public BlockSystem blockSystem;
        //        public Set<Token> tokens;
        public List<Rule> rules;
        public int iteration;
        public Map<Rule, Integer> priorities;
        public Map<String, Rule> nameToRule;
    }

}
