package de.dfki.step.blackboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class Rule {
    private static final Logger log = LoggerFactory.getLogger(Rule.class);

    private Condition _condition;
    private boolean _active;
    private int _priority;
    private String _tags[];
    private final UUID _uuid = UUID.randomUUID();
    private String _name;
    private final List<RuleManager> _manager = new LinkedList<>();

    public UUID getUUID()
    {
        return _uuid;
    }

    /**
     * If there are tokens that meet the condition, this function is called to process these tokens
     * @param tokens A list of possible token combinations represented as an array. The "best fit" (regarding the Condition) is the first token array in the list
     */
    public abstract void onMatch(List<Token[]> tokens);

    public boolean isActive() {
        return _active;
    }

    /**
     * Set the active state of the rule. Only active rules will be used for matching
     * @param active
     */
    public void setActive(boolean active) {
        this._active = active;
    }

    public int getPriority() {
        return _priority;
    }

    /**
     * Set the priorty of the rule. Higher priority rules will be matched first. The smaller the number, the higher the priority
     * @param priority
     */
    public void setPriority(int priority) {
        this._priority = priority;
    }

    /**
     * Get the Tags of the rule. Can be used for e.g. ignoring some tokens
     * @return
     */
    public String[] getTags() {
        return _tags;
    }

    public void setTags(String[] tags) {
        this._tags = tags;
    }

    public Condition getCondition() {
        return _condition;
    }

    public void setCondition(Condition condition) {
        this._condition = condition;
    }

    /**
     * Get the name of the rule, e.g. for web representation
     * @return
     */
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public List<RuleManager> getRuleManager()
    {
        return this._manager;
    }

    public void addRuleManager(RuleManager manager)
    {
        this._manager.add(manager);
    }
}
