package de.dfki.step.taskmodel;

import java.util.List;

public class ConditionalTask extends Task {

    private List<Task> ifTasks;
    private List<Task> elseTasks;
    private TaskCondition condition;


    public ConditionalTask(List<Task> ifTasks, List<Task> elseTasks, TaskCondition condition) {
        this.ifTasks = ifTasks;
        this.elseTasks = elseTasks;
        this.condition = condition;
    }

    public void execute() {
        if(condition.test()) {
            exeuteIF();
        } else {
            executeELSE();
        }
    }


    public void exeuteIF() {
        for(Task task : ifTasks) {
            task.execute();
        }
    }

    public void executeELSE() {
        for(Task task : elseTasks) {
            task.execute();
        }
    }



}
