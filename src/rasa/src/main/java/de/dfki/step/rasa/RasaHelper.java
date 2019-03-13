package de.dfki.step.rasa;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RasaHelper {
    private static Logger log = LoggerFactory.getLogger(RasaHelper.class);
    private final URL url; //e.g. "http://localhost:5000/parse"
    private static final Map<String, String> cache = new HashMap<>();
    private boolean cacheEnabled = false;

    public RasaHelper(URL url) {
        this.url = url;
    }

    public void enableCache() {
        cacheEnabled = true;
    }

    public void disableCache() {
        cacheEnabled = false;
    }

    /**
     * text -> rasa json
     * @param q
     * @return
     * @throws IOException
     */
    public String nlu(String q) throws IOException {
        if(cacheEnabled && cache.containsKey(q)) {
            return cache.get(q);
        }

        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //addInputComponent request header
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
        String rspStr = response.toString();
        cache.put(q, rspStr);
        return rspStr;
    }

    public RasaResponse parseJson(String jsonString) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        RasaResponse rasaResponse = gson.fromJson(jsonString, RasaResponse.class);
        log.debug("Rasaresponse: {}", rasaResponse.toString());


        return rasaResponse;
    }
}
