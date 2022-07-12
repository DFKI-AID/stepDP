package de.dfki.step.web;

import de.dfki.step.blackboard.KBToken;
import de.dfki.step.dialog.Dialog;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.IUUID;
import de.dfki.step.kb.graph.Graph;
import de.dfki.step.kb.semantic.Type;
import de.dfki.step.rm.sc.StateChartManager;
import de.dfki.step.rm.sc.internal.StateChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.gson.Gson;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller that specifies the web api.
 * Web requests e.g. for the rule set is handled here.
 */
@RestController
public class Controller {
    //    @Autowired
//    private ApplicationContext context;
    private Dialog dialog;
    private static List<String> outputHistory = new ArrayList<String>();
    private static Map<String, String> exampleTokens = new LinkedHashMap<>();

    @Autowired
    private AppConfig appConfig;

    @PostConstruct
    protected void init() {
        dialog = appConfig.getDialog(); //context.getBean(Dialog.class);
    }

    public static void createSpeechUtterance(String text) {
        outputHistory.add(text);
    }

    public static void addExampleToken(String name, String json){exampleTokens.put(name, json);}

    public Controller()
    {
        // add standard examples
        addExampleToken("add greeting", "{\"type\": \"GreetingIntent\", \"userName\":\"Alice\"}");
        addExampleToken("add hello", "{\"type\": \"HelloIntent\"}");
        addExampleToken("add goodbye", "{\"type\": \"GoodbyeIntent\"}");
        addExampleToken("add bring intent with pizza", "{\"type\":\"BringIntent\",\"object\":{\"type\":\"Pizza\",\"sort\":\"Pizza Hawaii\"},\"recipientName\":\"Alice\"}");
        addExampleToken("add bring intent with water", "{\"type\":\"BringIntent\",\"object\":{\"type\":\"Water\",\"carbonated\":false},\"recipientName\":\"Alice\"}");
        addExampleToken("add bring intent", "{\"type\":\"BringIntent\",\"recipientName\":\"Alice\"}");
        addExampleToken("add pizza", "{\"type\":\"Pizza\",\"sort\":\"Hawaii\"}");
        addExampleToken("add tv order intent (uuid)", "{\"type\":\"OrderIntent\",\"tv\":\"insert-uuid-of-a-tv-instance-here\"}");
        addExampleToken("add tv order intent (name)", "{\"type\":\"OrderIntent\",\"tv\":\"tv1\"}");
    }

    @CrossOrigin
    @GetMapping(value = "/blackboard/active")
    public List<de.dfki.step.blackboard.IToken> getActiveTokens() {
        var tokens = dialog.getBlackboard().getActiveTokens();
        return tokens;
    }

    @CrossOrigin
    @GetMapping(value = "/blackboard/archived")
    public List<de.dfki.step.blackboard.IToken> getArchivedTokens() {
        var tokens = dialog.getBlackboard().getArchivedTokens();
        return tokens;
    }

    @CrossOrigin
    @GetMapping(value = "/blackboard/rules")
    public List<de.dfki.step.blackboard.Rule> getBlackboardRules() {
        var rules = dialog.getBlackboard().getRules();
        return rules;
    }

    @CrossOrigin
    @PostMapping(value = "/blackboard/addToken", consumes = "application/json")
    public ResponseEntity<String> addTokenToBlackboard(@RequestBody Map<String, Object> body) {

        // TODO type matching
        // TODO check if all required values are there
        // TODO check if types of nested objects are valid (e.g. inheritance from semantic tree definition)
        // should be possible to activate / deactivate these checks for performance reasons
        if (!body.containsKey("type")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing type");
        }

        Type type = dialog.getKB().getType((String)body.get("type"));

        if(type == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("type not found");
        }

        de.dfki.step.blackboard.BasicToken newT = new de.dfki.step.blackboard.BasicToken(this.dialog.getKB());
        newT.setType(type);
        newT.addAll(body);
        dialog.getBlackboard().addToken(newT);

        return ResponseEntity.ok("ok");
    }

    @CrossOrigin
    @PostMapping(value = "/blackboard/addKBToken", consumes = "application/json")
    public ResponseEntity<String> addKBTokenToBlackboard(@RequestBody Map<String, Object> body) {
        IKBObject obj = null;
        if (body.get("uuid") != null) {
            if (!(body.get("uuid") instanceof String))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("uuid must be string");
            try {
                UUID uuid = UUID.fromString(body.get("uuid").toString());
                obj = this.dialog.getKB().getInstance(uuid, false);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("uuid is invalid");
            }
        } else if (body.get("name") != null) {
            obj = this.dialog.getKB().getInstance(body.get("name").toString());
        }

        if (obj == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("kb object not found");

        KBToken newT = new KBToken(this.dialog.getKB(), obj);
        dialog.getBlackboard().addToken(newT);
        return ResponseEntity.ok("ok");
    }

    @CrossOrigin
    @GetMapping(value = "/kb/instances/mapping")
    public Map<String, String> getKBInstancesMapping() {
        Map<String, String> result = new HashMap<>();

        for(IUUID var : dialog.getKB().getUUIDMapping())
        {
            if(var instanceof IKBObject)
            {
                IKBObject var2 = (IKBObject)var;
                result.put(var2.getUUID().toString(), var2.getName());
            }
        }

        return result;
    }

    @CrossOrigin
    @GetMapping(value = "/kb/types/mapping")
    public Map<String, String> getKBTypesMapping() {
        Map<String, String> result = new HashMap<>();

        for(IUUID var : dialog.getKB().getUUIDMapping())
        {
            if(var instanceof Type)
            {
                Type var2 = (Type)var;
                result.put(var2.getUUID().toString(), var2.getName());
            }
        }

        return result;
    }

    @CrossOrigin
    @GetMapping(value = "/iteration")
    public Map<Object, Object> getIteration() {
        var rsp = new HashMap<Object, Object>();
        rsp.put("iteration", dialog.getIteration());
        return rsp;
    }

    @CrossOrigin
    @GetMapping(value = "/behavior/{id}", produces = "application/json")
    public ResponseEntity<String> getBehavior(@PathVariable("id") String id) {
        StateChartManager behavior = dialog.getBlackboard().getStateChartManager(id);
        if(behavior == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        StateChart sc = behavior.getEngine().getStateChart();
        Gson gson = new Gson();
        return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(sc));
    }

    @CrossOrigin
    @GetMapping(value = "/behaviors", produces = "application/json")
    public ResponseEntity<Object> getBehaviors() {
        Map<String, StateChartManager> scMans = dialog.getBlackboard().getAllStateChartManagers();
        Map<String, StateChart> body = scMans.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getEngine().getStateChart()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @CrossOrigin
    @GetMapping(value = "/behavior/{id}/state", produces = "application/json")
    public ResponseEntity<String> getBehaviorState(@PathVariable("id") String id) {
        StateChartManager behavior = dialog.getBlackboard().getStateChartManager(id);
        if(behavior == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        String currentState = behavior.getCurrentState();
        return ResponseEntity.status(HttpStatus.OK).body(String.format("{\"state\":\"%s\"}", currentState));
    }

    @CrossOrigin
    @GetMapping(value = "/output/history", produces = "application/json")
    public ResponseEntity<List<String>> getOutputHistory() {
        return ResponseEntity.status(HttpStatus.OK).body(outputHistory);
    }

    @CrossOrigin
    @GetMapping(value = "/blackboard/exampleTokens", produces = "application/json")
    public ResponseEntity<Map<String, String>> getExampleTokens() {
        return ResponseEntity.status(HttpStatus.OK).body(exampleTokens);
    }

    @CrossOrigin
    @GetMapping (value =  "/kb/graph/{graphName}/getEdges", produces = "application/json")
    public Collection<de.dfki.step.kb.graph.Edge> getEdgesFromGraph(@PathVariable("graphName") String graphName) {
        return dialog.getBlackboard().getGraph(graphName).getAllEdges();
    }

    @CrossOrigin
    @PostMapping (value = "/kb/graph/addEdge", consumes = "application/json")
    public ResponseEntity<String> addEdgeToGraph(@RequestBody Map<String, Object> body) {

        if (!body.containsKey("graphName")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing graphName");
        }

        Graph graph = this.dialog.getBlackboard().getGraph(body.get("graphName").toString());

        if (graph == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("graph not found");
        }


        UUID parentID = null;
        UUID childID = null;

        if (!body.containsKey("parent")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing parent");
        }
        if (!(body.get("parent") instanceof String))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("parent must be string");
        try {
            parentID = UUID.fromString(body.get("parent").toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("parent is invalid");
        }


        if (!body.containsKey("child")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing child");
        }
        if (!(body.get("child") instanceof String))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("child must be string");
        try {
            childID = UUID.fromString(body.get("child").toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("child is invalid");
        }

        if (parentID == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("parent not found");

        if (childID == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("child not found");

        IKBObject source = this.dialog.getKB().getInstance(parentID);
        IKBObject goal = this.dialog.getKB().getInstance(childID);

        if (body.containsKey("label")) {
            if (!(body.get("goal") instanceof String))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("label must be string");
            graph.createEdge(source, goal, (String)body.get("label"));
        } else {
            //TODO: change according to optional implementation
            graph.createEdge(source, goal, null);
        }

        return ResponseEntity.ok("ok");
    }

    @CrossOrigin
    @PostMapping (value = "/kb/graph/removeEdge", consumes = "application/json")
    public ResponseEntity<String> removeEdgeFromGraph(@RequestBody Map<String, Object> body) {
        //TODO: rework to take the UUID of the edge and create a mapping of UUID to edge in graph
        if (!body.containsKey("graphName")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing graphName");
        }

        Graph graph = this.dialog.getBlackboard().getGraph(body.get("graphName").toString());


        UUID ID = null;

        if (!body.containsKey("UUID")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing edge UUID");
        }
        if (!(body.get("UUID") instanceof String))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("UUID must be string");
        try {
            ID = UUID.fromString(body.get("UUID").toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("UUID is invalid");
        }

        if (ID == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("UUID not found");


        graph.deleteEdge(ID);

        return ResponseEntity.ok("ok");
    }

    @CrossOrigin
    @PostMapping (value = "/kb/graph/addGraph", consumes = "application/json")
    public ResponseEntity<String> addNewGraph(@RequestBody Map<String, Object> body) {

        if (!body.containsKey("graphName")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing graphName");
        }

        Graph graph = new Graph();

        this.dialog.getBlackboard().addGraph(body.get("graphName").toString(), graph);

        return ResponseEntity.ok("ok");
    }

    @CrossOrigin
    @GetMapping (value = "/kb/graph/getGraphNames", produces = "application/json")
    public Collection<String> getAllGraphNames() {
        return this.dialog.getBlackboard().getAllGraphs().keySet();
    }


}
