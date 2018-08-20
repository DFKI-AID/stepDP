package de.dfki.tocalog;

import de.dfki.tocalog.dialog.Intent;
import de.dfki.tocalog.framework.DialogComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates simple device control
 */
public class DeviceControlBehavior implements DialogComponent {
    private static Logger log = LoggerFactory.getLogger(DeviceControlBehavior.class);

    @Override
    public void init(Context context) {

    }

    @Override
    public boolean onIntent(Intent intent) {
        return false;
    }

    @Override
    public void update() {

    }
}
