package de.dfki.tocalog;

import de.dfki.tocalog.dialog.Intent;
import de.dfki.tocalog.dialog.IntentProducer;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.rasa.RasaEntity;
import de.dfki.tocalog.rasa.RasaHelper;
import de.dfki.tocalog.rasa.RasaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

/**
 */
public class RasaIntentProducer implements IntentProducer {
    private static Logger log = LoggerFactory.getLogger(RasaIntentProducer.class);
    private Queue<Intent> intentQueue = new ArrayDeque<>();
    private RasaHelper rasaHelper = new RasaHelper();

    @Override
    public void add(Input input) {
        if (!(input instanceof TextInput)) {
            return;
        }

        try {
            TextInput ti = (TextInput) input;
            String rasaJson = rasaHelper.nlu(ti.getText());
            RasaResponse rasaRsp = rasaHelper.parseJson(rasaJson);
            onRasaResponse(ti, rasaRsp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Optional<Intent> getIntent() {
        if (intentQueue.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(intentQueue.poll());
    }

    protected void onRasaResponse(TextInput ti, RasaResponse rasaResponse) {
        if (rasaResponse.getRasaIntent().getConfidence() < 0.4) {
            log.info("confidence to low for rasa intent {}: {}", rasaResponse.getRasaIntent().getName(),
                    rasaResponse.getRasaIntent().getConfidence());
            return;
        }

        if (rasaResponse.getRasaIntent().getName().equals("greeting")) {
            Intent intent = new Intent(Intent.CommunicativeFunction.Statement, rasaResponse.getRasaIntent().getName());
            ti.getSource().ifPresent(s -> intent.addNominative(s));
            intentQueue.add(intent);
        }

        if (rasaResponse.getRasaIntent().getName().equals("turnOn")) {
            Intent intent = new Intent(Intent.CommunicativeFunction.Request, rasaResponse.getRasaIntent().getName());
            ti.getSource().ifPresent(s -> intent.addNominative(s));
            for (RasaEntity rasaEntity : rasaResponse.getRasaEntityList()) {
                if (rasaEntity.getEntity().equals("device")) {
                    intent.addAccusative(rasaEntity.getValue());
                }
                if (rasaEntity.getEntity().equals("location")) {
//                    intent.addLocation(rasaEntity.getValue());
                }
            }
            intentQueue.add(intent);
        }
    }
}
