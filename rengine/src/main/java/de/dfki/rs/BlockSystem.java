package de.dfki.rs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class BlockSystem {
    private static final Logger log = LoggerFactory.getLogger(BlockSystem.class);
    private final Clock clock;
    private Map<Rule, BlockRule> blockRules = new HashMap<>();

    public BlockSystem(Clock clock) {
        this.clock = clock;
    }

    public boolean isEnabled(Rule rule) {
        return !isDisabled(rule);
    }

    public boolean isDisabled(Rule rule) {
        if (!blockRules.containsKey(rule)) {
            return false;
        }

        boolean disabled = blockRules.get(rule).isBlocked();
        if (!disabled) {
            //unblocking rule
            log.info("Re-enabling: {}", rule);
            blockRules.remove(rule);
        }
        return disabled;
    }

    public void disable(Rule rule, long iteration) {
        //TODO maybe replace duration with iterations: in the sense of translating them
        // this would help to 'go back' in the dialog; or use a custom clock
        log.info("Disabling: {}", rule);
        blockRules.put(rule, new BlockRule() {
            long until = clock.getIteration() + iteration;

            @Override
            public boolean isBlocked() {
                return clock.getIteration() < until;
            }
        });
    }

    public void disable(Rule rule) {
        log.info("Disabling: {}", rule);
        blockRules.put(rule, () -> true);
    }

    public void enable(Rule rule) {
        if (!isDisabled(rule)) {
            return;
        }

        log.info("Enabling: {}", rule);
        blockRules.remove(rule);
    }

    public BlockSystem copy() {
        var copy = new BlockSystem(this.clock);
        copy.blockRules.putAll(this.blockRules);
        return copy;
    }
}
