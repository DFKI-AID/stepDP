package de.dfki.dialog;

import de.dfki.rengine.Token;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 *
 */
public class TimeBehavior implements Behavior {
    private Dialog dialog;

    @Override
    public void init(Dialog dialog) {
        this.dialog = dialog;
        dialog.getRuleSystem().addRule("request_time", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "time_request"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        var now = LocalDateTime.now();
                        var tts = "it is " + now.getHour() + ":" + now.getMinute(); //TODO improve
                        dialog.present(new PresentationRequest(tts));
                        sys.disable("request_time", Duration.ofMillis(3000));
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
