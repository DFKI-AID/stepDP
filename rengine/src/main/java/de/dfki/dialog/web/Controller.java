package de.dfki.dialog.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.dfki.dialog.Behavior;
import de.dfki.dialog.StateBehavior;
import de.dfki.rengine.Token;
import de.dfki.sc.StateChart;
import org.pcollections.PSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@RestController
public class Controller {
    @Autowired
    private ApplicationContext context;
    private AppConfig.Settings settings;

    @PostConstruct
    protected void init() {
        settings = context.getBean(AppConfig.Settings.class);

    }

    @GetMapping(value = "/rules")
    public List<Rule> getRules() {
        var rs = settings.app.getRuleSystem();
        var rules = settings.app.getRuleSystem().getRules().stream()
                .map(r -> {
                    String id = rs.getName(r).orElse("unknown");
                    return new Rule(id)
                            .setActive(rs.isEnabled(r))
                            .setTags(settings.app.getTagSystem().getTags(id));
                })
                .collect(Collectors.toList());
        return rules;
    }

    @GetMapping(value = "/tokens")
    public List<Token> getTokens() {
        var tokens = settings.app.getTokens().stream()
                .collect(Collectors.toList());
        return tokens;
    }

    @GetMapping(value = "/iteration")
    public Map<Object, Object> getIteration() {
        var rsp = new HashMap<Object, Object>();
        rsp.put("iteration", settings.app.getRuleSystem().getIteration());
        return rsp;
    }

    @PostMapping(value = "/rewind/{iteration}")
    public void rewind(@PathVariable("iteration") int iteration) {
        settings.app.rewind(iteration);
    }


    public static class AsrRequestBody {
        public String text;
    }

    @PostMapping(value = "/input/intent", consumes = "application/json")
    public ResponseEntity<String> postIntent(@RequestBody Map<String, Object> body) {
//        JsonParser parser = new JsonParser();
//        JsonObject obj = parser.parse(body).getAsJsonObject();
//        if(obj.get("intent") == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing intent");
//        }
//
//        HashMap<String, Object> payload = new HashMap<>();
//        payload.put("json", obj);
//        Intent intent = new Intent(obj.get("intent").getAsString(), payload);
//        settings.app.addIntent(intent);

        if (!body.containsKey("intent")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing intent");
        }


        Token intentToken = new Token().addAll(body);
        settings.app.addIntent(intentToken);
        return ResponseEntity.ok("ok");
    }

    @GetMapping(value = "/grammar", produces = "application/xml")
    public String getGrammar() {
        String grammar = settings.app.getGrammarManager().createGrammar().toString();
        return grammar;
    }


    @GetMapping(value = "/behavior/{id}", produces = "application/json")
    public ResponseEntity<String> getBehavior(@PathVariable("id") String id) {
        Optional<Behavior> behavior = settings.app.getBehavior(id);
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
        Optional<Behavior> behavior = settings.app.getBehavior(id);
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
        PSequence payload = settings.app.outputHistory;
        return ResponseEntity.status(HttpStatus.OK).body(payload);
    }


}
