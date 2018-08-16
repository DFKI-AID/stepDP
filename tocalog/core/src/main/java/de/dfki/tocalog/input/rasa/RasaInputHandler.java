package de.dfki.tocalog.input.rasa;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class RasaInputHandler {

    /**
     * text -> rasa json
     * @param q
     * @return
     * @throws IOException
     */
    public String nlu(String q) throws IOException {

        String url = "http://localhost:5000/parse";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Tocalog");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        String payload = String.format("{\"q\":\"%s\"}\n", q);
        byte[] binPayload = payload.getBytes(StandardCharsets.UTF_8);
        con.getOutputStream().write(binPayload);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
        return response.toString();
    }

    public RasaResponse parseJson(String jsonString) {
        Gson gson = new Gson();
        JsonObject obj = new JsonParser().parse(jsonString).getAsJsonObject();

        //get intent
        JsonObject jsonIntent = obj.getAsJsonObject("intent");
        RasaIntent rasaIntent = gson.fromJson(jsonIntent, RasaIntent.class);
        System.out.println(rasaIntent.getName());

        //get entities
        JsonArray jsonEntities = obj.getAsJsonArray("entities");
        List<RasaEntity> rasaEntities = new ArrayList<>();
        if(jsonEntities.size() > 0) {
            for (JsonElement entity : jsonEntities) {
                rasaEntities.add(gson.fromJson(entity, RasaEntity.class));
            }

            System.out.println(rasaEntities.get(0).getValue());
            System.out.println(rasaEntities.get(0).getEntity());
        }

        //get intent ranking
        JsonArray jsonIntentRanking = obj.getAsJsonArray("intent_ranking");
        List<RasaIntent> rasaIntents = new ArrayList<>();
        if(jsonIntentRanking.size() > 0) {
            for (JsonElement intent : jsonIntentRanking) {
                rasaIntents.add(gson.fromJson(intent, RasaIntent.class));
            }

            System.out.println(rasaIntents.get(0).getName());
            System.out.println(rasaIntents.get(1).getName());
        }

        //get text
        String text = obj.get("text").getAsString();

        //create rasa response object
        RasaResponse rasaResponse = new RasaResponse();
        rasaResponse.setJsonString(jsonString);
        rasaResponse.setRasaIntent(rasaIntent);
        rasaResponse.setRasaEntityList(rasaEntities);
        rasaResponse.setIntentRankingList(rasaIntents);
        rasaResponse.setRequestString(text);
        System.out.println("Rasaresponse: " + rasaResponse.toString());

        return rasaResponse;

    }

    public abstract void handleIntent(RasaResponse response);


}
