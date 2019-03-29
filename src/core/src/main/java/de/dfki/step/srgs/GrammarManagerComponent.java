package de.dfki.step.srgs;

import de.dfki.step.core.Component;
import de.dfki.step.core.ComponentManager;
import de.dfki.step.dialog.Dialog;
import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.rengine.RuleSystemComponent;

import java.util.Optional;

public class GrammarManagerComponent implements Component {
    private final GrammarManager grammarManager = new GrammarManager();
    private RuleSystemComponent rsc;

    @Override
    public void init(ComponentManager cm) {
        this.rsc = cm.retrieveComponent(RuleSystemComponent.class);
    }

    @Override
    public void deinit() {

    }

    @Override
    public void update() {
        synchronized (grammarManager) {
            //TODO: better builder and then swap de.dfki.step.srgs.jsgf manager instance
            //TODO put into own behavior?
            grammarManager.deactivateAll();
            RuleSystem rs = rsc.getRuleSystem();
            rs.getRules()
                    .forEach(rule -> {
                        Optional<String> name = rs.getName(rule);
                        if (!name.isPresent()) {
                            return;
                        }
                        grammarManager.setActive(name.get(), rs.isEnabled(rule));
                    });
        }
    }

    @Override
    public Object createSnapshot() {
        return null;
    }

    @Override
    public void loadSnapshot(Object snapshot) {

    }

    public Grammar createGrammar() {
        return grammarManager.createGrammar();
    }
}
