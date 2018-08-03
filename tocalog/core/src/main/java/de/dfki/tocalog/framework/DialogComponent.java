package de.dfki.tocalog.framework;

import de.dfki.tocalog.kb.KnowledgeBase;

/**
 */
public interface DialogComponent extends EventEngine.Listener {
    void init(Context context);

    interface Context {
        ProjectManager getProjectManager();

        default KnowledgeBase getKnowledgeBase() {
            return getProjectManager().getKnowledgeBase();
        }
    }
}
