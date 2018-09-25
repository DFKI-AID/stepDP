package de.dfki.tocalog;

import de.dfki.tocalog.dialog.Intent;
import de.dfki.tocalog.core.DialogComponent;
import de.dfki.tocalog.core.Event;
import de.dfki.tocalog.core.EventEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates simple device control
 */
public class DeviceControlBehavior implements DialogComponent {
    private static Logger log = LoggerFactory.getLogger(DeviceControlBehavior.class);
    private boolean fanOn = false;
    private boolean tvOn = false;
    private boolean radioOn = false;
    private boolean lampOn = false;

    @Override
    public void init(Context context) {
//        context.getKnowledgeBase()
    }

    @Override
    public boolean onIntent(Intent intent) {
        if (!intent.getType().equals("turnOn")) {
            return false;
        }

        if(intent.getAccusative().getEntities().isEmpty()) {
            log.warn("could not find entities in turn on request");
            //TODO could ask for more information
            return true;
        }

        for (String device : intent.getAccusative().getEntities()) {
            log.info("turning on \"{}\"", device);
        }

        return true;
    }

    @Override
    public void onEvent(EventEngine engine, Event event) {

    }
}
