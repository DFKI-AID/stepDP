package de.dfki.step.web;

import de.dfki.step.blackboard.KBToken;
import de.dfki.step.dialog.Dialog;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.IUUID;
import de.dfki.step.kb.IKBObjectWriteable;
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

    @Autowired
    private AppConfig appConfig;

    @PostConstruct
    protected void init() {
        dialog = appConfig.getDialog(); //context.getBean(Dialog.class);
    }

    public static void createSpeechUtterance(String text) {
        outputHistory.add(text);
    }

    @GetMapping(value = "/blackboard/active")
    public List<de.dfki.step.blackboard.IToken> getActiveTokens() {
        var tokens = dialog.getBlackboard().getActiveTokens();
        return tokens;
    }

    @GetMapping(value = "/blackboard/archived")
    public List<de.dfki.step.blackboard.IToken> getArchivedTokens() {
        var tokens = dialog.getBlackboard().getArchivedTokens();
        return tokens;
    }

    @GetMapping(value = "/blackboard/rules")
    public List<de.dfki.step.blackboard.Rule> getBlackboardRules() {
        var rules = dialog.getBlackboard().getRules();
        return rules;
    }

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

    @GetMapping(value = "/iteration")
    public Map<Object, Object> getIteration() {
        var rsp = new HashMap<Object, Object>();
        rsp.put("iteration", dialog.getIteration());
        return rsp;
    }

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

    @GetMapping(value = "/behaviors", produces = "application/json")
    public ResponseEntity<Object> getBehaviors() {
    	Map<String, StateChartManager> scMans = dialog.getBlackboard().getAllStateChartManagers();
        Map<String, StateChart> body = scMans.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getEngine().getStateChart()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping(value = "/behavior/{id}/state", produces = "application/json")
    public ResponseEntity<String> getBehaviorState(@PathVariable("id") String id) {
      StateChartManager behavior = dialog.getBlackboard().getStateChartManager(id);
      if(behavior == null) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }

      String currentState = behavior.getCurrentState();
      return ResponseEntity.status(HttpStatus.OK).body(String.format("{\"state\":\"%s\"}", currentState));
    }

    @GetMapping(value = "/output/history", produces = "application/json")
    public ResponseEntity<List<String>> getOutputHistory() {
        return ResponseEntity.status(HttpStatus.OK).body(outputHistory);
    }
}
