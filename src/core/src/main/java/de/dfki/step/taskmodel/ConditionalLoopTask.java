package de.dfki.step.taskmodel;

import java.util.List;

public class ConditionalLoopTask extends Task {


    private List<Task> tasks;
    private boolean until;
    private TaskCondition condition;



    public ConditionalLoopTask(List<Task> tasks, TaskCondition condition, boolean until) {
        this.tasks = tasks;
        this.condition = condition;
        this.until = until;
    }

    public void execute() {
       if(until) {
           while (condition.test()) {
               for (Task a : tasks) {
                   a.execute();
               }
           }
       }else {
           while (!condition.test()) {
               for (Task a : tasks) {
                   a.execute();
               }
           }
       }
    }


}
