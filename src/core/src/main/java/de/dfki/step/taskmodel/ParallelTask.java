package de.dfki.step.taskmodel;

import java.util.List;

public class ParallelTask extends Task {

    private List<Task> actions1;
    private List<Task> actions2;

    public ParallelTask(List<Task> actions1, List<Task> actions2) {
        this.actions1 = actions1;
        this.actions2 = actions2;
    }

    public void execute() {
        int length = Math.max(actions1.size(), actions2.size());
        for(int i = 0; i < length; i++) {
            if(i < actions1.size()) {
                actions1.get(i).execute();
            }
            if(i < actions2.size()) {
                actions2.get(i).execute();
            }
        }
    }
}
