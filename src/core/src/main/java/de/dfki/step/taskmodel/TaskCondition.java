package de.dfki.step.taskmodel;


public interface TaskCondition {
    boolean test();


 class CheckIsPresentCondition implements TaskCondition
{

    private String entityId;

    public CheckIsPresentCondition(String entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean test() {
        return  false; //HOW to check if entity is in kb if only id is given?
    }
}

}