package de.dfki.tocalog.framework;

import de.dfki.tocalog.kb.KnowledgeBase;

/**
 */
public interface InputComponent extends EventEngine.Listener {
    void init(Context context);

    interface Context {
        KnowledgeBase getKnowledgeBase();
        EventEngine getEventEngine();
    }
}
