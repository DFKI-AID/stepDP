package de.dfki.tocalog;

import de.dfki.tocalog.dialog.Intent;
import de.dfki.tocalog.framework.Event;
import de.dfki.tocalog.framework.EventEngine;
import de.dfki.tocalog.framework.InputComponent;
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
public class RasaIntentProducer implements InputComponent {
    private static Logger log = LoggerFactory.getLogger(RasaIntentProducer.class);
    private RasaHelper rasaHelper = new RasaHelper();

    protected Optional<Intent> onRasaResponse(TextInput ti, RasaResponse rasaResponse) {
        if (rasaResponse.getRasaIntent().getConfidence() < 0.4) {
            log.info("confidence to low for rasa intent {}: {}", rasaResponse.getRasaIntent().getName(),
                    rasaResponse.getRasaIntent().getConfidence());
            return null;
        }

        if (rasaResponse.getRasaIntent().getName().equals("greeting")) {
            Intent intent = new Intent(Intent.CommunicativeFunction.Statement, rasaResponse.getRasaIntent().getName());
            ti.getSource().ifPresent(s -> intent.addNominative(s));
            return Optional.of(intent);
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
            return Optional.of(intent);
        }
        return Optional.empty();
    }

    @Override
    public void init(Context context) {
    }

    @Override
    public void onEvent(EventEngine engine, Event event) {
        if (!event.is(TextInput.class)) {
            return;
        }

        TextInput input = (TextInput) event.get();

        try {
            String rasaJson = rasaHelper.nlu(input.getText());
            RasaResponse rasaRsp = rasaHelper.parseJson(rasaJson);
            onRasaResponse(input, rasaRsp).ifPresent(i -> engine.submit(Event.build(i).build()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
