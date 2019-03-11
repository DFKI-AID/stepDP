package de.dfki.pdp.sc;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.List;

/**
 *
 */
public class OnEntry {
    private PSequence<String> scripts = TreePVector.empty();

    public List<String> getScripts() {
        return scripts;
    }

    protected void addScript(String script) {
        scripts = scripts.plus(script);
    }
}
