package de.dfki.tocalog.framework;

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
}
