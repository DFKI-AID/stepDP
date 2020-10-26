package de.dfki.step.web;

import com.google.gson.Gson;
import de.dfki.step.core.*;
import de.dfki.step.dialog.Dialog;
import de.dfki.step.output.PresentationComponent;
import de.dfki.step.sc.StateBehavior;
import de.dfki.step.sc.StateChart;
import de.dfki.step.srgs.GrammarManagerComponent;
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
    public ResponseEntity rewind(@PathVariable("iteration") int iteration) {
        Optional<SnapshotComponent> snapshotComp = dialog.getComponent(SnapshotComponent.class);
        if(!snapshotComp.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SnapshotComponent not available");
        }
        snapshotComp.get().rewind(iteration);
        return ResponseEntity.ok("ok");
    }


    public static class AsrRequestBody {
        public String text;
    }

    @PostMapping(value = "/input/intent", consumes = "application/json")
    public ResponseEntity<String> postIntent(@RequestBody Map<String, Object> body) {
        if (!body.containsKey("intent")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing intent");
        }

        Optional<TokenComponent> tc = dialog.getComponent(TokenComponent.class);
        if(!tc.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("TokenComponent not available");
        }


        Token intentToken = new Token().addAll(body);
        tc.get().addToken(intentToken);
        return ResponseEntity.ok("ok");
    }

    @PostMapping(value = "/input", consumes = "application/json")
    public ResponseEntity<String> postInput(@RequestBody Map<String, Object> body) {

        Optional<InputComponent> ic = dialog.getComponent(InputComponent.class);
        if(!ic.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("InputComponent not available");
        }

        Token token = new Token().addAll(body);
        ic.get().addToken(token);
        return ResponseEntity.ok("ok");
    }

    @GetMapping(value = "/grammar", produces = "application/xml")
    public String getGrammar() {
        var grammarManager = dialog.getComponents(GrammarManagerComponent.class).stream().findFirst();
        if(!grammarManager.isPresent()) {
            return "";
        }
        String grammar = grammarManager.get().createGrammar().toString();
        return grammar;
    }


    @GetMapping(value = "/behavior/{id}", produces = "application/json")
    public ResponseEntity<String> getBehavior(@PathVariable("id") String id) {
        Optional<Component> behavior = dialog.getComponent(id);
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

    @GetMapping(value = "/behaviors", produces = "application/json")
    public ResponseEntity<Object> getBehaviors() {
        Map<String, StateBehavior> scs = dialog.getComponentsMap(StateBehavior.class);
        Map<String, StateChart> body = scs.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getStateHandler().getEngine().getStateChart()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping(value = "/behavior/{id}/state", produces = "application/json")
    public ResponseEntity<String> getBehaviorState(@PathVariable("id") String id) {
        Optional<Component> behavior = dialog.getComponent(id);
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
        Optional<PresentationComponent> pc = dialog.getComponent(PresentationComponent.class);
        if(!pc.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.EMPTY_LIST);
        }
        PSequence payload = pc.get().getOutputHistory();
        return ResponseEntity.status(HttpStatus.OK).body(payload);
    }


}
