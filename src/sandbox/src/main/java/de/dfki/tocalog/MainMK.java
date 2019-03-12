package de.dfki.tocalog;


import de.dfki.step.core.*;
import de.dfki.step.core.resolution.*;
import de.dfki.tocalog.examples.device_control.DeviceControlDC;
import de.dfki.step.input.Input;
import de.dfki.step.input.TextInput;
import de.dfki.step.input.pattern.InputPattern;
import de.dfki.step.input.pattern.PatternHypothesisProducer;
import de.dfki.step.kb.*;
import de.dfki.step.output.Imp;
import de.dfki.step.rasa.*;
import de.dfki.step.util.Vector3;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static de.dfki.step.kb.Ontology.Person;

public class MainMK {




    public static void main(String[] args) throws MalformedURLException {
        TextInput text = new TextInput("Change the color of the yellow lamp to red");
        text.setInitiator("speaker");
        List<Input> inputList = new ArrayList<>();
        inputList.add(text);
        Inputs inputs = new Inputs();
        inputs.add(inputList);
        RasaHelper helper = new RasaHelper(new URL("http://localhost:5000/parse"));

        KnowledgeBase knowledgeBase = createExampleKnowledgeBase();
        RasaHypoProcessor processor = new RasaHypoProcessor(knowledgeBase, helper);

        processor.setReferenceResolvers(List.of(new PersonReferenceResolver(knowledgeBase), new ObjectReferenceResolver(knowledgeBase)));
        Imp imp = new Imp(knowledgeBase);
        DeviceControlDC deviceControlDC = new DeviceControlDC(List.of(processor), imp);
        deviceControlDC.process(inputs);

      //  usePostRasaHypo(inputs);

      //  usePatternHypo(inputs);

        //   doExampleReferenceResolving();


    }

    public static void usePostRasaHypo(Inputs inputs) throws MalformedURLException {
        RasaHelper helper = new RasaHelper(new URL("http://localhost:5000/parse"));
        RasaHypoProducer rhp = new RasaHypoProducer(helper);
        List<Hypothesis> rasaHypos = rhp.process(inputs);
        System.out.println("rasaHypo: " + rasaHypos.toString());
        // RasaHypoProducer2 hp = new RasaHypoProducer2(helper, kb);
        // List<Hypothesis> hypotheses = hp.process(inputs);
        //System.out.println(hypotheses);
        PostRasaHypoProducer prhp = new PostRasaHypoProducer(rhp, createExampleKnowledgeBase());
        List<Hypothesis> hypotheses = prhp.process(inputs);
        System.out.println("final hypos: " + hypotheses);
    }

    public static void usePatternHypo(Inputs inputs) {
        List<InputPattern> patterns = new ArrayList<>();
        InputPattern bringPattern = new InputPattern("bring");
        bringPattern.setSlotTypes(new ArrayList<>(List.of(Ontology.Entity, Ontology.Person)));

        InputPattern bringPattern2 = new InputPattern("bring");
        bringPattern2.setSlotTypes(new ArrayList<>(List.of(Ontology.Entity, Ontology.Entity)));

        InputPattern turnOnPattern = new InputPattern("turn on");
        turnOnPattern.setSlotTypes(new ArrayList<>(List.of(Ontology.Entity)));

        patterns.add(bringPattern);
        patterns.add(bringPattern2);
        patterns.add(turnOnPattern);

        PatternHypothesisProducer patternHP = new PatternHypothesisProducer(createExampleKnowledgeBase(), patterns);
        List<Hypothesis> hypotheses = patternHP.process(inputs);

        System.out.println("final hypos: " + hypotheses);

    }

    public static KnowledgeBase createExampleKnowledgeBase() {
        KnowledgeBase kb = new KnowledgeBase();
        KnowledgeMap personMap =kb.getKnowledgeMap(Ontology.Person);
        KnowledgeMap sessionMap =kb.getKnowledgeMap(Ontology.Session);
        KnowledgeMap deviceMap =kb.getKnowledgeMap("device");

        /*Entity fan = new Entity().set(Ontology.id, "fan").set(Ontology.color, "green").set(Ontology.size, "small").set(Ontology.position, new Vector3(1.0, 1.0, 1.0));
        Entity fan2 = new Entity().set(Ontology.id, "fan2").set(Ontology.color, "red").set(Ontology.size, "big").set(Ontology.position, new Vector3(2.0, 1.0, 2.0));
        Entity loudspeaker = new Entity().set(Ontology.id, "loudspeaker"); //.set(Ontology.owner, Person.refTo("speaker"));
        deviceMap.add(fan);
        deviceMap.add(fan2);
        deviceMap.add(loudspeaker);*/
        Entity lamp1 = new Entity().set(Ontology.id, "lamp1").set(Ontology.color, "yellow").set(Ontology.size, "small").set(Ontology.brightness, 0.0)
                .set(Ontology.position, new Vector3(1.0, 1.0, 1.0)).set(Ontology.location, "kitchen").set(Ontology.owner, Person.refTo("speaker")).set(Ontology.name, "kitchen lamp");
        Entity lamp2 = new Entity().set(Ontology.id, "lamp2").set(Ontology.color, "red").set(Ontology.size, "big").set(Ontology.brightness, 0.0)
                .set(Ontology.position, new Vector3(2.0, 1.0, 2.0)).set(Ontology.location, "livingroom").set(Ontology.name, "livingroom lamp");
        Entity loudspeaker = new Entity().set(Ontology.id, "loudspeaker"); //.set(Ontology.owner, Person.refTo("speaker"));
        deviceMap.add(lamp1);
        deviceMap.add(lamp2);


        Entity speaker = new Entity()
                .set(Ontology.id, "speaker")
                .set(Ontology.position, new Vector3(0.0, 0.0, 0.0))
                .set(Ontology.gender, "male")
                .set(Ontology.name, "Tom");

        personMap.add(speaker);
        Entity pers1 = new Entity()
                .set(Ontology.id, "person1")
                .set(Ontology.position, new Vector3(1.0, 1.0, 1.0))
                .set(Ontology.gender, "male")
                .set(Ontology.name, "Max");
               // .set(Ontology.owned, fan.get(Ontology.id).get());

        personMap.add(pers1);
        Entity pers2 = new Entity()
                .set(Ontology.id, "person2")
                .set(Ontology.position, new Vector3(4.0, 4.0, 4.0))
                .set(Ontology.gender, "female")
                .set(Ontology.name, "Tina");
        personMap.add(pers2);
        Entity pers3 = new Entity()
                .set(Ontology.id, "person3")
                .set(Ontology.position, new Vector3(-2.0, -2.0, -2.0))
                .set(Ontology.gender, "female")
                .set(Ontology.name, "Sara");
        personMap.add(pers3);

        Entity session1 = new Entity().set(Ontology.id, "session1");
        PSet<String> agents = HashTreePSet.empty();
        agents = agents.plus(pers1.get(Ontology.id).get())
                .plus(speaker.get(Ontology.id).get())
                .plus(pers3.get(Ontology.id).get());
        session1 = session1.set(Ontology.agents, agents);
        sessionMap.add(session1);
        Entity session2 = new Entity().set(Ontology.id, "session2");
        PSet<String> agents2 = HashTreePSet.empty();
        agents2 = agents2.plus(pers2.get(Ontology.id).get());
        session2 = session2.set(Ontology.agents, agents2);
        sessionMap.add(session2);
        System.out.println("persons: " + personMap.getAll().toString());

        return kb;
    }

    public static void doExampleReferenceResolving() {

        KnowledgeBase kb = createExampleKnowledgeBase();
        //input: "Bring PERSONSLOT ENTITYSLOT"
        PersonReferenceResolver personReferenceResolver = new PersonReferenceResolver(kb);
        personReferenceResolver.setSpeakerId("speaker");
        personReferenceResolver.setInputString("me");
      //  ReferenceDistribution personDist = personReferenceResolver.getReferences();
       // System.out.println(personDist.toString());
        System.out.println("####################################");

//        PossessiveObjectReferenceResolver possessiveObjectReferenceResolver = new PossessiveObjectReferenceResolver(kb, Ontology.Device);
//        possessiveObjectReferenceResolver.setPersonDeixisResolver(personReferenceResolver);
//        ReferenceDistribution objectDist = new ReferenceDistribution();
//        // objectDist = possessiveObjectReferenceResolver.getReferences();
//
//        ObjectAttributesReferenceResolver objectAttributesReferenceResolver = new ObjectAttributesReferenceResolver(kb, Ontology.Device);
//        Map<Attribute, AttributeValue> map = new HashMap<>();
//        map.put(Ontology.color, new AttributeValue(Ontology.color.name, "green",Ontology.id ));
//        map.put(Ontology.size, new AttributeValue(Ontology.size.name, "big",Ontology.id ));
//        objectAttributesReferenceResolver.setAttributes(map);
//        objectDist = objectAttributesReferenceResolver.getReferences();
//
//        ClosenessReferenceResolver closenessReferenceResolver = new ClosenessReferenceResolver(kb, Ontology.Device);
//        closenessReferenceResolver.setSpeakerId("speaker");
//        objectDist = closenessReferenceResolver.getReferences();
        ObjectReferenceResolver objectRR = new ObjectReferenceResolver(kb);
        objectRR.setEntityType(Ontology.Device.name);
        objectRR.setInputString("my loudspeaker");
        Map<Attribute, AttributeValue> map = new HashMap<>();
      //  map.put(Ontology.size, new AttributeValue(Ontology.size.name, "big",Ontology.id ));
        // map.put(Ontology.size, new AttributeValue(Ontology.size.name, "big",Ontology.id ));
        objectRR.setAttrMap(map);
        objectRR.setSpeakerId("speaker");

        ReferenceDistribution objectDist = objectRR.getReferences();

        System.out.println(objectDist.toString());
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
//
            List<Hypothesis> resultHypos = new ArrayList<>();
//            PersonDeixisResolver personDeixisResolver = new PersonDeixisResolver(kb);
//            PlaceDeixisResolver placeDeixisResolver = new PlaceDeixisResolver(kb);
//            ObjectReferenceResolver objectReferenceResolver = new ObjectReferenceResolver(kb);
//
//
//            for (Input input : inputs.getInputs()) {
//                if (input instanceof TextInput) {
//                    RasaHelper helper = null;
//                    RasaResponse response = new RasaResponse();
//                    try {
//                        String jsonResult = helper.nlu(((TextInput) input).getText());
//                        response = helper.parseJson(jsonResult);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    for (Hypothesis hypothesis : hypotheses) {
//                        if (hypothesis.getIntent().equals(response.getIntent().getName())) {
//                            for (Slot slot : hypothesis.getSlots().values()) {
//                                String slotName = slot.name;
//                                //no info from rasa regarding slot -> try to solve it with previous inputs?
//                                if (response.getEntities().isEmpty()) {
//
//                                }
//                                for (RasaEntity entity : response.getEntities()) {
//
//                                    String candidateValue = entity.getValue();
//                                    //TODO: check generic slot type coresponds to entity type
//                                    if (entity.getEntity().equals("Person")) {
//                                    //    hypothesis = personDeixisResolver.resolve(hypothesis, slotName, candidateValue, inputs, input);
//                                    } else if (entity.getEntity().equals("Place")) {
//                                        hypothesis = placeDeixisResolver.resolve(hypothesis, slotName, candidateValue, inputs, input);
//                                    } else if (entity.getEntity().equals("Time")) {
//                                      //  hypothesis = timeDeixisResolver.resolve(hypothesis, slotName, candidateValue, inputs, input);
//                                        //otherwise assume it is an entity
//                                    } else {
//                                        hypothesis = objectReferenceResolver.resolve(hypothesis, slotName, candidateValue, inputs, input);
//                                    }
//                                }
//                            }
//                            resultHypos.add(hypothesis);
//
//                        }
//                    }
//                }
//            }


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
