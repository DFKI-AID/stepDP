package de.dfki.step.taskmodel;

public abstract class AtomicTask extends Task {


    private String name;

    public AtomicTask(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

}
