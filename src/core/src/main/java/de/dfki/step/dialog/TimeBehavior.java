package de.dfki.step.dialog;

import de.dfki.step.core.Component;
import de.dfki.step.core.ComponentManager;
import de.dfki.step.core.TagSystemComponent;
import de.dfki.step.core.TokenComponent;
import de.dfki.step.output.PresentationComponent;
import de.dfki.step.rengine.RuleCoordinator;
import de.dfki.step.rengine.RuleSystemComponent;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Simple behavior that outputs the time on time_request intents
 */
public class TimeBehavior implements Component {
    private long timeout = 3000L;
    private ComponentManager cm;

    @Override
    public void init(ComponentManager cm) {
        this.cm = cm;
        var rsc = cm.retrieveComponent(RuleSystemComponent.class);
        var rcc = cm.retrieveComponent(RuleCoordinator.class);
        var tc = cm.retrieveComponent(TokenComponent.class);
        var pc = cm.retrieveComponent(PresentationComponent.class);

        rsc.getRuleSystem().addRule("request_time", () -> {
            tc.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "time_request"))
                    .findFirst()
                    .ifPresent(t -> {

                        rcc.add(() -> {
                            //output time via tts and disable the rule x seconds
                            var now = LocalDateTime.now();
                            var tts = "The time is " + now.getHour() + ":" + now.getMinute(); //TODO improve
                            pc.present(PresentationComponent.simpleTTS(tts));
                            rsc.getRuleSystem().disable("request_time", Duration.ofMillis(timeout));
                        }).attach("origin", t);
                    });
        });

        cm.getComponent(TagSystemComponent.class).ifPresent(tsc -> {
            tsc.addTag("request_time", "meta");
        });
    }



    @Override
    public void deinit() {
        cm.getComponent(RuleSystemComponent.class).ifPresent(rs -> {
            rs.getRuleSystem().removeRule("request_time");
        });
    }

    @Override
    public void update() {
    }

    @Override
    public Object createSnapshot() {
        //independent, therefore no change
        return null;
    }

    @Override
    public void loadSnapshot(Object snapshot) {
    }
}
