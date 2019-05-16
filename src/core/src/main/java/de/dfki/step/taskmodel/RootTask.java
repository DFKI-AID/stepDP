package de.dfki.step.taskmodel;

import java.util.LinkedList;
import java.util.List;

public class RootTask extends Task {

    private List<Task> tasks =  new LinkedList<Task>();
    private String name;

    public RootTask(String name) {
        this.name = name;
    }

    @Override
    public void execute() {
        Task.exectutionState = "EXECUTING";
        for(Task task : tasks) {
            task.execute();
        }
        Task.exectutionState = "FINISHED";
    }


    public String getName() {
        return name;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

}
