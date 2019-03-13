package de.dfki.step.web;

import com.google.gson.Gson;
import de.dfki.step.dialog.Behavior;
import de.dfki.step.dialog.Dialog;
import de.dfki.step.dialog.StateBehavior;
import de.dfki.step.rengine.Token;
import de.dfki.step.sc.StateChart;
import org.pcollections.PSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private AppConfig appConfig;

    @PostConstruct
    protected void init() {
        dialog = appConfig.getDialog(); //context.getBean(Dialog.class);

    }

    @GetMapping(value = "/rules")
    public List<Rule> getRules() {
        var rs = dialog.getRuleSystem();
        var rules = dialog.getRuleSystem().getRules().stream()
                .map(r -> {
                    String id = rs.getName(r).orElse("unknown");
                    return new Rule(id)
                            .setActive(rs.isEnabled(r))
                            .setTags(dialog.getTagSystem().getTags(id));
                })
                .collect(Collectors.toList());
        return rules;
    }

    @GetMapping(value = "/tokens")
    public List<Token> getTokens() {
        var tokens = dialog.getTokens().stream()
                .collect(Collectors.toList());
        return tokens;
    }

    @GetMapping(value = "/iteration")
    public Map<Object, Object> getIteration() {
        var rsp = new HashMap<Object, Object>();
        rsp.put("iteration", dialog.getRuleSystem().getIteration());
        return rsp;
    }

    @PostMapping(value = "/rewind/{iteration}")
    public void rewind(@PathVariable("iteration") int iteration) {
        dialog.rewind(iteration);
    }


    public static class AsrRequestBody {
        public String text;
    }

    @PostMapping(value = "/input/intent", consumes = "application/json")
    public ResponseEntity<String> postIntent(@RequestBody Map<String, Object> body) {
        if (!body.containsKey("intent")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing intent");
        }


        Token intentToken = new Token().addAll(body);
        dialog.addToken(intentToken);
        return ResponseEntity.ok("ok");
    }

    @GetMapping(value = "/grammar", produces = "application/xml")
    public String getGrammar() {
        String grammar = dialog.getGrammarManager().createGrammar().toString();
        return grammar;
    }


    @GetMapping(value = "/behavior/{id}", produces = "application/json")
    public ResponseEntity<String> getBehavior(@PathVariable("id") String id) {
        Optional<Behavior> behavior = dialog.getBehavior(id);
        if(!behavior.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if(behavior.get() instanceof StateBehavior) {
            StateBehavior sb = (StateBehavior) behavior.get();
            StateChart sc = sb.getStateHandler().getEngine().getStateChart();
            Gson gson = new Gson();
            return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(sc));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("not impl");
    }

    @GetMapping(value = "/behavior/{id}/state", produces = "application/json")
    public ResponseEntity<String> getBehaviorState(@PathVariable("id") String id) {
        Optional<Behavior> behavior = dialog.getBehavior(id);
        if(!behavior.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if(behavior.get() instanceof StateBehavior) {
            StateBehavior sb = (StateBehavior) behavior.get();
            String currentState = sb.getStateHandler().getCurrentState();
            return ResponseEntity.status(HttpStatus.OK).body(String.format("{\"state\":\"%s\"}", currentState));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("not impl");
    }

    @GetMapping(value = "/output/history", produces = "application/json")
    public ResponseEntity<List<String>> getOutputHistory() {
        PSequence payload = dialog.outputHistory;
        return ResponseEntity.status(HttpStatus.OK).body(payload);
    }


}
