package de.dfki.rengine.web;

import de.dfki.rengine.Intent;
import de.dfki.rengine.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
                            return new Rule(id, rs.getPriority(r))
                                    .setActive(rs.isEnabled(r))
                                    .setTags(settings.app.getTagSystem().getTags(id));
                        })

                .sorted(Comparator.comparingInt(r -> r.priority))
                .collect(Collectors.toList());
        return rules;
    }

    @GetMapping(value = "/tokens")
    public List<Token> getTokens() {
        var rs = settings.app.getRuleSystem();
        var tokens = settings.app.getRuleSystem().getTokens().stream()
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
        settings.app.getRuleSystem().rewind(iteration);
    }


    public static class AsrRequestBody {
        public String text;
    }

    @PostMapping(value = "/input/intent")
    public void postIntent(@RequestParam Map<String, Object> payload) {
        String intentStr = Optional.ofNullable((String) payload.get("intent")).orElse("unknown");
        payload.remove("intent");
        Intent intent = new Intent(intentStr, payload);
        settings.app.addIntent(intent);
    }

    @GetMapping(value = "/grammar", produces = "application/xml")
    public String getGrammar() {
        String grammar = settings.app.getGrammarManager().createGrammar().toString();
        return grammar;
    }

}
