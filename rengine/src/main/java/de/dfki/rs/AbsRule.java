package de.dfki.rs;


/**
 */
public class AbsRule implements Rule {
    private final String name;
    private final Rule impl;

    public AbsRule(Rule impl, String name) {
        this.impl = impl;
        this.name = name;
    }

    @Override
    public String toString() {
        return "AbsRule{" +
                "name='" + name + '\'' +
                ", impl=" + impl +
                '}';
    }

    @Override
    public void update(RuleSystem system) {
        impl.update(system);
    }
}
