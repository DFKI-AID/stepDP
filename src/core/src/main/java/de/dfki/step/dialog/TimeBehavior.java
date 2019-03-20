package de.dfki.step.dialog;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 *
 */
public class TimeBehavior implements Behavior {
    private long timeout = 3000L;
    private Dialog dialog;

    @Override
    public void init(Dialog dialog) {
        this.dialog = dialog;
        dialog.getRuleSystem().addRule("request_time", () -> {
            dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "time_request"))
                    .findFirst()
                    .ifPresent(t -> {

                        dialog.getRuleCoordinator().add(() -> {
                            //output time via tts and disable the rule x seconds
                            var now = LocalDateTime.now();
                            var tts = "The time is " + now.getHour() + ":" + now.getMinute(); //TODO improve
                            dialog.present(new PresentationRequest(tts));
                            dialog.getRuleSystem().disable("request_time", Duration.ofMillis(timeout));
                        }).attach("origin", t);
                    });
        });
        dialog.getTagSystem().addTag("request_time", "meta");
    }

    @Override
    public void deinit() {
        dialog.getRuleSystem().removeRule("request_time");
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
