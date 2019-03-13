package de.dfki.step.srgs;

/**
 */
public class RuleRef implements Node {
    private final String ruleName;

    public RuleRef(String ruleName) {
        this.ruleName = ruleName;
    }

    @Override
    public void write(NodeWriter nw) {
        nw.write(String.format("<ruleref uri=\"#%s\"/>", ruleName));
    }
}
