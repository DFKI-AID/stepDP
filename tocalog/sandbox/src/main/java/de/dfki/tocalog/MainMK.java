package de.dfki.tocalog;


import de.dfki.tocalog.core.*;
import de.dfki.tocalog.core.resolution.ObjectReferenceResolver;
import de.dfki.tocalog.core.resolution.PersonDeixisResolver;
import de.dfki.tocalog.core.resolution.PlaceDeixisResolver;
import de.dfki.tocalog.core.resolution.TimeDeixisResolver;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.model.Device;
import de.dfki.tocalog.model.Entity;
import de.dfki.tocalog.model.Person;
import de.dfki.tocalog.rasa.RasaEntity;
import de.dfki.tocalog.rasa.RasaHelper;
import de.dfki.tocalog.rasa.RasaResponse;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainMK {



    public static void main(String[] args) throws Exception {
       /* BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);

        MainMK http = new MainMK();

        System.out.println("\nTesting  - Send Http POST request");
        String responseString = http.sendGet();
        RasaResponseHandler responseHandler = new RasaResponseHandler();
        RasaResponse response = responseHandler.parseJson(responseString);*/
//        responseHandler.handleIntent(response);



    }

    public static class MyHypoProducer implements HypothesisProducer {

        private List<Hypothesis> hypotheses = new ArrayList<>();
        private KnowledgeBase kb;

        public void createCandidateHypothesis() {
            List<Hypothesis> hypotheses = new ArrayList<>();
            Hypothesis hypothesis;
            Hypothesis.Builder builder = new Hypothesis.Builder("turnOn");
            CustomSlot<Device> deviceSlot = new CustomSlot<>();
            builder.addSlot("device", deviceSlot);

            Hypothesis.Builder builder2 = new Hypothesis.Builder("handOver");
            CustomSlot<Person> personSlot = new CustomSlot<>();
            CustomSlot<Entity> entitySlot = new CustomSlot<>();
            builder2.addSlot("toPerson", personSlot);
            builder2.addSlot("entity", entitySlot);

            hypotheses.add(builder.build());
            hypotheses.add(builder2.build());
        }


        @Override
        public List<Hypothesis> process(Inputs inputs) {

            List<Hypothesis> resultHypos = new ArrayList<>();
            PersonDeixisResolver personDeixisResolver = new PersonDeixisResolver(kb);
            PlaceDeixisResolver placeDeixisResolver = new PlaceDeixisResolver(kb);
            TimeDeixisResolver timeDeixisResolver = new TimeDeixisResolver(kb);
            ObjectReferenceResolver objectReferenceResolver = new ObjectReferenceResolver(kb);


            for (Input input : inputs.getInputs()) {
                if (input instanceof TextInput) {
                    RasaHelper helper = null;
                    RasaResponse response = new RasaResponse();
                    try {
                        String jsonResult = helper.nlu(((TextInput) input).getText());
                        response = helper.parseJson(jsonResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (Hypothesis hypothesis : hypotheses) {
                        if (hypothesis.getIntent().equals(response.getRasaIntent().getName())) {
                            for (Slot slot : hypothesis.getSlots().values()) {
                                String slotName = slot.name;
                                //no info from rasa regarding slot -> try to solve it with previous inputs?
                                if (response.getRasaEntityList().isEmpty()) {

                                }
                                for (RasaEntity entity : response.getRasaEntityList()) {

                                    String candidateValue = entity.getValue();
                                    //TODO: check generic slot type coresponds to entity type
                                    if (entity.getEntity().equals("Person")) {
                                        hypothesis = personDeixisResolver.resolve(hypothesis, slotName, candidateValue, inputs, input);
                                    } else if (entity.getEntity().equals("Place")) {
                                        hypothesis = placeDeixisResolver.resolve(hypothesis, slotName, candidateValue, inputs, input);
                                    } else if (entity.getEntity().equals("Time")) {
                                        hypothesis = timeDeixisResolver.resolve(hypothesis, slotName, candidateValue, inputs, input);
                                        //otherwise assume it is an entity
                                    } else {
                                        hypothesis = objectReferenceResolver.resolve(hypothesis, slotName, candidateValue, inputs, input);
                                    }
                                }
                            }
                            resultHypos.add(hypothesis);

                        }
                    }
                }
            }


            return resultHypos;

        }
    }



    public static class CustomSlot<T> extends Slot<T> {

        @Override
        public Collection<T> findMatches() {
           return new ArrayList<T>();
        }
    }

    public static class DeviceSlot<Device> extends Slot<Device> {


        @Override
        public Collection<Device> findMatches() {
            return null;
        }
    }



    // HTTP GET request
   /* private String sendGet() throws Exception {

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

    }*/



}
