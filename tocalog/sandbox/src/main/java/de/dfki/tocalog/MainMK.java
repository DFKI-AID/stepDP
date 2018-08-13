package de.dfki.tocalog;


import de.dfki.tocalog.input.rasa_input.RasaInputHandler;
import de.dfki.tocalog.input.rasa_input.RasaResponse;
import de.dfki.tocalog.input.rasa_input.RasaResponseHandler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainMK {



    public static void main(String[] args) throws Exception {

        MainMK http = new MainMK();

        System.out.println("\nTesting  - Send Http POST request");
        String responseString = http.sendGet();
        RasaResponseHandler responseHandler = new RasaResponseHandler();
        RasaResponse response = responseHandler.parseJson(responseString);
        responseHandler.handleIntent(response);

    }


    // HTTP GET request
    private String sendGet() throws Exception {

        String url = "http://localhost:5000/parse?q=Max+turn+on+the+lamp&project=current&model=nlu";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Java Client");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

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



}
