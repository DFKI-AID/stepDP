package de.dfki.rengine;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class BlockSystem {
    private static final Logger log = LoggerFactory.getLogger(BlockSystem.class);
    private final Clock clock;
    private PMap<Rule, RuleBlocker> blockRules = HashTreePMap.empty();

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
            blockRules = blockRules.minus(rule);
        }
        return disabled;
    }

    public void disable(Rule rule, long iteration) {
        //TODO maybe replace duration with iterations: in the sense of translating them
        // this would help to 'go back' in the dialog; or use a custom clock
        log.info("Disabling: {}", rule);
        blockRules = blockRules.plus(rule, new RuleBlocker() {
            long until = clock.getIteration() + iteration;

            @Override
            public boolean isBlocked() {
                return clock.getIteration() < until;
            }
        });
    }

    public void disable(Rule rule) {
        log.debug("Disabling: {}", rule);
        blockRules = blockRules.plus(rule, () -> true);
    }

    public void enable(Rule rule) {
        if (!isDisabled(rule)) {
            return;
        }

        log.debug("Enabling: {}", rule);
        blockRules = blockRules.minus(rule);
    }

    public BlockSystem copy() {
        var copy = new BlockSystem(this.clock);
        copy.blockRules = this.blockRules;
        return copy;
    }
}
