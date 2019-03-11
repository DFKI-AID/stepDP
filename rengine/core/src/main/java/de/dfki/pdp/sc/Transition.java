package de.dfki.pdp.sc;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;

/**
 *
 */
public class Transition {
    private String target;
    private String event;
    private String cond;
    private PSequence<String> scripts = TreePVector.empty();

    public PSequence<String> getScripts() {
        return scripts;
    }

    protected void addScript(String script) {
        scripts = scripts.plus(script);
    }

    public String getTarget() {
        return target;
    }

    protected void setTarget(String target) {
        this.target = target;
    }

    public String getCond() {
        return cond;
    }

    protected void setCond(String cond) {
        this.cond = cond;
    }

    public String getEvent() {
        return event;
    }

    protected void setEvent(String event) {
        this.event = event;
    }

    public boolean hasTarget() {
        return target != null && !target.isEmpty();
    }

    public boolean hasEvent() {
        return event != null && !event.isEmpty();
    }

    public boolean hasCond() {
        return cond != null && !cond.isEmpty();
    }

}
