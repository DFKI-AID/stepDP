package de.dfki.tocalog.framework;

/**
 */
public abstract class AbstractDialogComponent implements DialogComponent {
    private ProjectManager dc;

    @Override
    public void init(ProjectManager dialogCore) {
        this.dc = dialogCore;
    }

    protected ProjectManager getDialogCore() {
        return dc;
    }
}
