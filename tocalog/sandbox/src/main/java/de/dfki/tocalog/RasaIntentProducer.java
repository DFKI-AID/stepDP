package de.dfki.tocalog;

import de.dfki.tocalog.dialog.Intent;
import de.dfki.tocalog.core.Event;
import de.dfki.tocalog.core.EventEngine;
import de.dfki.tocalog.core.InputComponent;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.rasa.RasaEntity;
import de.dfki.tocalog.rasa.RasaHelper;
import de.dfki.tocalog.rasa.RasaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

/**
 */
public class RasaIntentProducer implements InputComponent {
    private static Logger log = LoggerFactory.getLogger(RasaIntentProducer.class);
    private RasaHelper rasaHelper = new RasaHelper();

    protected Optional<Intent> onRasaResponse(TextInput ti, RasaResponse rasaResponse) {
        if (rasaResponse.getRasaIntent().getConfidence() < 0.4) {
            log.info("confidence to low for rasa name {}: {}", rasaResponse.getRasaIntent().getName(),
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
//                    name.addLocation(rasaEntity.getValue());
                }
            }
            return Optional.of(intent);
        }
        return Optional.empty();
    }

    @Override
    public Collection<Input> process(Event event) {
        if (!event.is(TextInput.class)) {
            return Collections.EMPTY_SET;
        }

        TextInput input = (TextInput) event.get();

        Collection result = new HashSet();
        try {
            String rasaJson = rasaHelper.nlu(input.getText());
            RasaResponse rasaRsp = rasaHelper.parseJson(rasaJson);
            onRasaResponse(input, rasaRsp).ifPresent(
                    i -> result.add(Event.build(i).build())
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
