package de.dfki.tocalog.dialog;

import de.dfki.tocalog.framework.DialogComponent;
import de.dfki.tocalog.framework.Event;
import de.dfki.tocalog.framework.EventEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MetaDialog implements EventEngine.Listener {
    private DialogComponent.Context context;
    private List<DialogComponent> dialogComponents = new ArrayList<>();

    public MetaDialog() {
    }

    public void init(DialogComponent.Context context) {
        this.context = context;
        dialogComponents.forEach(dc -> dc.init(context));
    }

    public void addDialogComponent(DialogComponent dc) {
        this.dialogComponents.add(dc);
    }

    @Override
    public void onEvent(EventEngine engine, Event event) {
        if(!event.is(Intent.class)) {
            for(DialogComponent dc : dialogComponents) {
                dc.onEvent(engine, event);
            }
            return;
        }

        //TODO coordination
        for(DialogComponent dc : dialogComponents) {
            if(dc.onIntent((Intent) event.get())) {
                //consumed
                return;
            }
        }
    }
}