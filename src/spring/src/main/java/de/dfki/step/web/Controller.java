package de.dfki.step.web;

import de.dfki.step.dialog.Dialog;
import de.dfki.step.kb.semantic.Type;
import de.dfki.step.rm.sc.internal.StateChart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * REST controller that specifies the web api.
 * Web requests e.g. for the rule set is handled here.
 */
@RestController
public class Controller {
//    @Autowired
//    private ApplicationContext context;
    private Dialog dialog;

    @Autowired
    private AppConfig appConfig;

    @PostConstruct
    protected void init() {
        dialog = appConfig.getDialog(); //context.getBean(Dialog.class);
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

    @GetMapping(value = "/iteration")
    public Map<Object, Object> getIteration() {
        var rsp = new HashMap<Object, Object>();
        rsp.put("iteration", dialog.getIteration());
        return rsp;
    }

    @GetMapping(value = "/behavior/{id}", produces = "application/json")
    public ResponseEntity<String> getBehavior(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("not impl");
    }

    @GetMapping(value = "/behaviors", produces = "application/json")
    public ResponseEntity<Object> getBehaviors() {
        Map<String, StateChart> body = new HashMap<String, StateChart>();
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping(value = "/behavior/{id}/state", produces = "application/json")
    public ResponseEntity<String> getBehaviorState(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("not impl");
    }

}
