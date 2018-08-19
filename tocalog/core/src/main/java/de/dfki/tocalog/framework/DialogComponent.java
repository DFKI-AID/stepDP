package de.dfki.tocalog.framework;

import de.dfki.tocalog.dialog.Intent;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.output.OutputComponent;

/**
 */
public interface DialogComponent {
    void init(Context context);

    /**
     * @param intent
     * @return true iff the intent was consumed by this component
     */
    boolean onIntent(Intent intent);

    void update();

    interface Context {
//        ProjectManager getProjectManager();

        KnowledgeBase getKnowledgeBase();

        OutputComponent getAllocatioModule();
    }
}
