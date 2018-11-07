package de.dfki.tocalog;


import de.dfki.tocalog.core.*;
import de.dfki.tocalog.core.resolution.ObjectReferenceResolver;
import de.dfki.tocalog.core.resolution.PersonDeixisResolver;
import de.dfki.tocalog.core.resolution.PlaceDeixisResolver;
import de.dfki.tocalog.core.resolution.TimeDeixisResolver;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.rasa.RasaEntity;
import de.dfki.tocalog.rasa.RasaHelper;
import de.dfki.tocalog.rasa.RasaHypoProducer2;
import de.dfki.tocalog.rasa.RasaResponse;


import javax.ws.rs.HEAD;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainMK {



    public static void main(String[] args) throws Exception {
        RasaHelper helper = new RasaHelper(new URL("http://localhost:5000/parse"));
        TextInput text = new TextInput("Max bring the red box to Magdalena");
        List<Input> inputList = new ArrayList<>();
        inputList.add(text);
        Inputs inputs = new Inputs();
        inputs.add(inputList);
        RasaHypoProducer2 hp = new RasaHypoProducer2(helper);
        List<Hypothesis> hypotheses = hp.process(inputs);
        System.out.println("candidates: " + hypotheses.get(0).getSlots().get("property").getCandidates());
        System.out.println(hypotheses);
    }

    public static class MyHypoProducer implements HypothesisProducer {

        private List<Hypothesis> hypotheses = new ArrayList<>();
        private KnowledgeBase kb = new KnowledgeBase();

        public void createCandidateHypothesis() {
            List<Hypothesis> hypotheses = new ArrayList<>();
            Hypothesis hypothesis;
            Hypothesis.Builder builder = new Hypothesis.Builder("turnOn");
            Slot deviceSlot = new Slot("device");
            Collection<Entity> fans = kb.getKnowledgeMap(Ontology.Device)
                    .query(e -> e.get(Ontology.type).orElse("").equals("Fan"));
            deviceSlot.setCandidates(fans);

            builder.addSlot(deviceSlot);

            Hypothesis.Builder builder2 = new Hypothesis.Builder("handOver");
            Slot personSlot = new Slot("from");
            Slot entitySlot = new Slot("to");
            Slot objSlot = new Slot("what");

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
                        if (hypothesis.getIntent().equals(response.getIntent().getName())) {
                            for (Slot slot : hypothesis.getSlots().values()) {
                                String slotName = slot.name;
                                //no info from rasa regarding slot -> try to solve it with previous inputs?
                                if (response.getEntities().isEmpty()) {

                                }
                                for (RasaEntity entity : response.getEntities()) {

                                    String candidateValue = entity.getValue();
                                    //TODO: check generic slot type coresponds to entity type
                                    if (entity.getEntity().equals("Person")) {
                                    //    hypothesis = personDeixisResolver.resolve(hypothesis, slotName, candidateValue, inputs, input);
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
