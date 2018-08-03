package de.dfki.tocalog.framework;

import de.dfki.tocalog.kb.KnowledgeBase;

/**
 */
public abstract class AbstractDialogComponent implements DialogComponent {
    private ProjectManager dc;

    @Override
    public void init(Context context) {
        this.dc = context.getProjectManager();
    }

    protected ProjectManager getProjectManager() {
        return dc;
    }

    protected KnowledgeBase getKnowledgeBase() { return dc.getKnowledgeBase(); }
}
