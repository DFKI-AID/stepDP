package de.dfki.tocalog.input.rasa_input;

import java.util.List;

public class RasaResponse {

    private String jsonString;

    private RasaIntent rasaIntent;

    private List<RasaEntity> rasaEntityList;

    private List<RasaIntent> intentRankingList;

    private String requestString;


    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public RasaIntent getRasaIntent() {
        return rasaIntent;
    }

    public void setRasaIntent(RasaIntent rasaIntent) {
        this.rasaIntent = rasaIntent;
    }

    public List<RasaEntity> getRasaEntityList() {
        return rasaEntityList;
    }

    public void setRasaEntityList(List<RasaEntity> rasaEntityList) {
        this.rasaEntityList = rasaEntityList;
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
                "jsonString='" + jsonString + '\'' +
                ", rasaIntent=" + rasaIntent +
                ", rasaEntityList=" + rasaEntityList +
                ", intentRankingList=" + intentRankingList +
                ", requestString='" + requestString + '\'' +
                '}';
    }


}
