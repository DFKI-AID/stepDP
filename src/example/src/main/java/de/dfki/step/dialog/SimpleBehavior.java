package de.dfki.step.dialog;

import de.dfki.step.core.ComponentManager;
import de.dfki.step.core.TokenComponent;
import de.dfki.step.rengine.CoordinationComponent;
import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.rengine.RuleSystemComponent;
import de.dfki.step.sc.SimpleStateBehavior;

import java.net.URISyntaxException;
import java.util.Set;

public class SimpleBehavior extends SimpleStateBehavior {
    public SimpleBehavior() throws URISyntaxException {
        super("/sc/simple");
    }

    @Override
    public void init(ComponentManager cm) {
        super.init(cm);

        RuleSystemComponent rsc = cm.retrieveComponent(RuleSystemComponent.class);
        TokenComponent tc = cm.retrieveComponent(TokenComponent.class);
        CoordinationComponent cc = cm.retrieveComponent(CoordinationComponent.class);
        rsc.addRule("simpleRule", () -> {
            tc.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "simple"))
                    .findFirst()
                    .ifPresent(t -> {
                        cc.add(() -> {
                            getStateHandler().fire("simple");
                        });
                    });
        });
    }

    @Override
    public Set<String> getActiveRules(String state) {
        return Set.of("simpleRule");
    }
}
