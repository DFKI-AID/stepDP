package de.dfki.tocalog.mongoDB;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.dfki.tocalog.core.Hypothesis;
import de.dfki.tocalog.core.Inputs;
import de.dfki.tocalog.core.Slot;
import de.dfki.tocalog.core.resolution.ObjectReferenceResolver;
import de.dfki.tocalog.core.resolution.PersonReferenceResolver;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.output.Imp;
import de.dfki.tocalog.rasa.RasaHelper;
import de.dfki.tocalog.rasa.RasaHypoProcessor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MongoDBClient {
    public static final String COLLECTION_NAME = "objects";
    private String dbName;
    private MongoClient mongoClient;
    private final Morphia morphia;

    public MongoDBClient(String dbName, boolean dropOldDB) {
        String url = "mongodb://tractat:T!naTurn3r@172.16.60.241:27017";
        mongoClient = new MongoClient(url);
        this.dbName = dbName;
        if (dropOldDB) {
            mongoClient.dropDatabase(dbName);
        }
        morphia = new Morphia();
        morphia.mapPackage("de.dfki.tocalog.mongoDB");
    }

    public void close() {
        mongoClient.close();
    }

    public void deleteDB() {
        mongoClient.dropDatabase(dbName);
    }

    public void insertObject(PhysicalEntity obj) {
        final Datastore datastore = morphia.createDatastore(mongoClient, dbName);
        datastore.save(obj);
    }

    public PhysicalEntity getObjectById(int id) {
        final Datastore datastore = morphia.createDatastore(mongoClient, dbName);
        final Query<PhysicalEntity> query = datastore.createQuery(PhysicalEntity.class).field("_id").equal(id);
        final List<PhysicalEntity> resultList = query.asList();
        return resultList.get(0);
    }

    public void addNewAttribute(int id, Attribute attribute) {
        final Datastore datastore = morphia.createDatastore(mongoClient, dbName);
        UpdateOperations<PhysicalEntity> ops = datastore
                .createUpdateOperations(PhysicalEntity.class)
                .push("attributes", attribute);
        final Query<PhysicalEntity> updateQuery = datastore.createQuery(PhysicalEntity.class)
                .field("_id").equal(id);
        datastore.update(updateQuery, ops);
    }

    public static List<PhysicalEntity> createPhysicalObjects() {
        List<PhysicalEntity> entities = new ArrayList<>();
        List<Attribute> attributes = new ArrayList<>();

        Attribute type = new Attribute("a1", "type", "box");
        Attribute color = new Attribute("a2", "color", "red");
        Attribute size = new Attribute("a3", "size", "small");
        attributes.add(type);
        attributes.add(color);
        attributes.add(size);
        entities.add(new PhysicalEntity("p1", attributes));

        attributes = new ArrayList<>();
        type = new Attribute("b1", "type", "box");
        color = new Attribute("b2", "color", "green");
        size = new Attribute("b3", "size", "small");
        attributes.add(type);
        attributes.add(color);
        attributes.add(size);
        entities.add(new PhysicalEntity("p2", attributes));

        attributes = new ArrayList<>();
        type = new Attribute("b1", "type", "box");
        color = new Attribute("b2", "color", "green");
        size = new Attribute("b3", "size", "large");
        attributes.add(type);
        attributes.add(color);
        attributes.add(size);
        entities.add(new PhysicalEntity("p3", attributes));

        return entities;

    }


    public static List<RobotAction> createRobotActions() {
        List<RobotAction> actions = new ArrayList<>();
        RobotAction ra = new RobotAction("a1", "move");
        //TODO: location can be entity or directly xyz coordinates -> generics needed for Attributes! Why did it not work with Morphia?
        //TODO: name of robot as additional slot parameter?
        Slot slot = new Slot("location");
        slot.setSlotConstraint(new Slot.SlotTypeConstraint("PhysicalEntity"));
        ra.setActionHypothesis(Arrays.asList(slot));
        actions.add(ra);

        ra = new RobotAction("a2", "pick");
        slot = new Slot("entity");
        slot.setSlotConstraint(new Slot.SlotTypeConstraint("PhysicalEntity"));
        ra.setActionHypothesis(Arrays.asList(slot));
        actions.add(ra);


        ra = new RobotAction("a3", "drop");
        slot = new Slot("entity");
        slot.setSlotConstraint(new Slot.SlotTypeConstraint("PhysicalEntity"));
        ra.setActionHypothesis(Arrays.asList(slot));
        actions.add(ra);

        return actions;

    }

    public static void main(String[] args) throws UnknownHostException, MalformedURLException {

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://tractat:T!naTurn3r@172.16.60.241:27017"));
        MongoDatabase db = mongoClient.getDatabase("objectDB");
        MongoCollection<Document> collection = db.getCollection("PhysicalEntity");

        final Morphia morphia = new Morphia();
        morphia.mapPackage("de.dfki.tocalog.mongoDB");
        final Datastore datastore = morphia.createDatastore(mongoClient, "objectDB");

        List<PhysicalEntity> entities = createPhysicalObjects();
        for(PhysicalEntity entity: entities) {
            datastore.save(entity);
        }

        List<RobotAction> actions = createRobotActions();
        for(RobotAction action: actions) {
            datastore.save(actions);
        }

       /* final Query<PhysicalEntity> morphiaQuery = datastore.createQuery(PhysicalEntity.class);
        final List<PhysicalEntity> objects = morphiaQuery.asList();
        testObj = objects.get(0);
        System.out.println("morphia test object: " + testObj.toString());*/


        //rasa text input
        TextInput text = new TextInput("Change the color of the yellow lamp to red");
        text.setInitiator("speaker");
        List<Input> inputList = new ArrayList<>();
        inputList.add(text);
        Inputs inputs = new Inputs();
        inputs.add(inputList);
        RasaHelper helper = new RasaHelper(new URL("http://localhost:5000/parse"));

       /* KnowledgeBase knowledgeBase = createExampleKnowledgeBase();
        RasaHypoProcessor processor = new RasaHypoProcessor(knowledgeBase, helper);

        processor.setReferenceResolvers(List.of(new PersonReferenceResolver(knowledgeBase), new ObjectReferenceResolver(knowledgeBase)));
        Imp imp = new Imp(knowledgeBase);
        DeviceControlDC deviceControlDC = new DeviceControlDC(List.of(processor), imp);
        deviceControlDC.process(inputs);
*/
        mongoClient.close();
    }
}


