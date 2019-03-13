package de.dfki.pdp.nlg;

import com.google.gson.*;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class NLGenerator {
    public static class RTask {
        public final String type;
        public final Map<String, String> data = new HashMap<>();

        public RTask(String type) {
            this.type = type;
        }

        public RTask add(String key, String value) {
            this.data.put(key, value);
            return this;
        }
    }

    public static void main(String[] args) {
        Gson gson = new Gson();

        var resStream = NLGenerator.class.getResourceAsStream("/rtasks.json");
        JsonElement jelement = new JsonParser().parse(new InputStreamReader(resStream));
        JsonArray tasks = jelement.getAsJsonArray();



    }
}
