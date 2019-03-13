package de.dfki.step.rasa;

import java.util.List;

public class RasaResponse {

    private RasaIntent intent;

    private List<RasaEntity> entities;

    private List<RasaIntent> intentRankingList;

    private String requestString;


    public RasaIntent getIntent() {
        return intent;
    }

    public void setIntent(RasaIntent intent) {
        this.intent = intent;
    }

    public List<RasaEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<RasaEntity> entities) {
        this.entities = entities;
    }

    public List<RasaIntent> getIntentRankingList() {
        return intentRankingList;
    }

    public void setIntentRankingList(List<RasaIntent> intentRankingList) {
        this.intentRankingList = intentRankingList;
    }

    public String getRequestString() {
        return requestString;
    }

    public void setRequestString(String requestString) {
        this.requestString = requestString;
    }

    @Override
    public String toString() {
        return "RasaResponse{" +
                ", intent=" + intent +
                ", entities=" + entities +
                ", intentRankingList=" + intentRankingList +
                ", requestString='" + requestString + '\'' +
                '}';
    }


}
