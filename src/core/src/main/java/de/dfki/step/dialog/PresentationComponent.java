package de.dfki.step.dialog;

import de.dfki.step.core.Component;
import de.dfki.step.core.ComponentManager;
import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.rengine.RuleSystemComponent;
import de.dfki.step.rengine.Token;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PresentationComponent implements Component {
    private static Logger log = LoggerFactory.getLogger(PresentationComponent.class);
    private RuleSystemComponent rs;
    private MetaFactory mf;
    private PSequence outputHistory = TreePVector.empty();

    @Override
    public void init(ComponentManager cm) {
        this.rs = cm.retrieveComponent(RuleSystemComponent.class);
        this.mf = new MetaFactory(cm);
    }

    @Override
    public void deinit() {

    }

    @Override
    public void update() {

    }

    @Override
    public Object createSnapshot() {
        return null;
    }

    @Override
    public void loadSnapshot(Object snapshot) {

    }

    public void present(Token token) {
        if(!token.payloadEquals("type", "tts")) {
            log.warn("unsupported output type: {}", token.get("type"));
            return;
        }
        String output = token.get("utterance", String.class).orElse("output not available");

//        String utterance = t.getAny("utterance").toString();
        System.out.println("System: " + output);
        rs.removeRule("request_repeat_tts");
        mf.createRepeatRule( "request_repeat_tts", output);

        mf.createSnapshot();
        outputHistory = outputHistory.plus(output);
    }

    public static Token simpleTTS(String utterance) {
        return Token.builder()
                .add("type", "tts")
                .add("utterance", utterance)
                .build();
    }

    public PSequence getOutputHistory() {
        return outputHistory;
    }
}
