package de.dfki.step.srgs;

import de.dfki.step.dialog.Component;
import de.dfki.step.dialog.Dialog;
import de.dfki.step.rengine.RuleSystem;

import java.util.Optional;

public class GrammarManagerComponent implements Component {
    private final GrammarManager grammarManager = new GrammarManager();
    private RuleSystem rs;

    @Override
    public void init(Dialog dialog) {
        this.rs = dialog.getRuleSystem();
    }

    @Override
    public void deinit() {

    }

    @Override
    public void update() {
        synchronized (grammarManager) {
            //TODO: better builder and then swap srgs.jsgf manager instance
            //TODO put into own behavior?
            grammarManager.deactivateAll();
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
