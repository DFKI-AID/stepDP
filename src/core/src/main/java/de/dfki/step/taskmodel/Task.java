package de.dfki.step.taskmodel;


import de.dfki.step.kb.Entity;

public abstract class Task extends Entity {

    public abstract void execute();

    public static String exectutionState;

    private String executingAgent;

    private long creationTime;

    private String importanceLevel;


}
