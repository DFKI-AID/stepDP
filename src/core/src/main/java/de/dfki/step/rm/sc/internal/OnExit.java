package de.dfki.step.rm.sc.internal;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.List;

/**
 *
 */
public class OnExit {
    private PSequence<String> scripts = TreePVector.empty();

    public List<String> getScripts() {
        return scripts;
    }

    protected void addScript(String script) {
        scripts = scripts.plus(script);
    }
}
