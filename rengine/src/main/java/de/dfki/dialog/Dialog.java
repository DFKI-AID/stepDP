package de.dfki.dialog;

import de.dfki.dialog.grammar.GrammarManager;
import de.dfki.rengine.RuleSystem;
import de.dfki.rengine.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;

/**
 *
 */
public abstract class Dialog implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Dialog.class);

    protected final RuleSystem rs = new RuleSystem();
    protected final TagSystem<String> tagSystem = new TagSystem();
    protected final GrammarManager grammarManager = new GrammarManager();


    public RuleSystem getRuleSystem() {
        return rs;
    }

    public TagSystem<String> getTagSystem() {
        return tagSystem;
    }

    public GrammarManager getGrammarManager() {
        return grammarManager;
    }

    public abstract void init();

    public abstract void update();

    public abstract void deinit();

    /**
     * Updates the global grammar based on the rules that are currently active
     *
     * @param rs
     */
    public void updateGrammar(RuleSystem rs) {
        synchronized (grammarManager) {
            //TODO: better builder and then swap grammar manager instance
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
    public void run() {
        init();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                updateGrammar(rs);
                update();
                rs.update();
            } catch (InterruptedException e) {
                log.warn("Dialog update interrupted. Quitting.");
                log.debug("Dialog update interrupted. Quitting.", e);
            }
        }
        deinit();
    }
}
