package de.dfki.tocalog.rasa;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RasaHelper {
    private static Logger log = LoggerFactory.getLogger(RasaHelper.class);

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

        log.debug("Sending 'GET' request to URL {}", url);
        int responseCode = con.getResponseCode();
        log.debug("Response Code: {}", responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        log.debug("Response: {}", response.toString());
        return response.toString();
    }

    public RasaResponse parseJson(String jsonString) {
        Gson gson = new Gson();
        JsonObject obj = new JsonParser().parse(jsonString).getAsJsonObject();

        //get intent
        JsonObject jsonIntent = obj.getAsJsonObject("intent");
        RasaIntent rasaIntent = gson.fromJson(jsonIntent, RasaIntent.class);
        log.debug("rasaIntent={}", rasaIntent);

        //get entities
        JsonArray jsonEntities = obj.getAsJsonArray("entities");
        List<RasaEntity> rasaEntities = new ArrayList<>();
        if(jsonEntities.size() > 0) {
            for (JsonElement entity : jsonEntities) {
                rasaEntities.add(gson.fromJson(entity, RasaEntity.class));
            }

            log.debug("{}", rasaEntities.get(0).getValue());
            log.debug("{}", rasaEntities.get(0).getEntity());
        }

        //get intent ranking
        JsonArray jsonIntentRanking = obj.getAsJsonArray("intent_ranking");
        List<RasaIntent> rasaIntents = new ArrayList<>();
        if(jsonIntentRanking.size() > 0) {
            for (JsonElement intent : jsonIntentRanking) {
                rasaIntents.add(gson.fromJson(intent, RasaIntent.class));
            }

            log.debug("{}", rasaIntents.get(0).getName());
            log.debug("{}", rasaIntents.get(1).getName());
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
        log.debug("Rasaresponse: {}", rasaResponse.toString());

        return rasaResponse;
    }
}
