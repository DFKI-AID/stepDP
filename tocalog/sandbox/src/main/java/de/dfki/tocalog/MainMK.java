package de.dfki.tocalog;


import de.dfki.tocalog.rasa.RasaResponse;
import de.dfki.tocalog.rasa.RasaResponseHandler;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainMK {



    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);

        MainMK http = new MainMK();

        System.out.println("\nTesting  - Send Http POST request");
        String responseString = http.sendGet();
        RasaResponseHandler responseHandler = new RasaResponseHandler();
        RasaResponse response = responseHandler.parseJson(responseString);
//        responseHandler.handleIntent(response);

    }


    // HTTP GET request
    private String sendGet() throws Exception {

        String url = "http://localhost:5000/parse?q=Max+turn+on+the+lamp&project=current&model=nlu";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //addInputComponent request header
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
