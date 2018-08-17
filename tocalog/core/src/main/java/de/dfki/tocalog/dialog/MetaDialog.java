package de.dfki.tocalog.dialog;

import de.dfki.tocalog.framework.DialogComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MetaDialog implements Runnable {
    private DialogComponent.Context context;
    private List<DialogComponent> dialogComponents = new ArrayList<>();
    private CIntentProducer intentProducer = new CIntentProducer();

    public MetaDialog() {
    }

    public void init(DialogComponent.Context context) {
        this.context = context;
        dialogComponents.forEach(dc -> dc.init(context));
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            dialogComponents.forEach(dc -> dc.update()); //TODO update rate

            Optional<Intent> intent = intentProducer.getIntent();
            if (!intent.isPresent()) {
                synchronized (this) {
                    try {
                        this.wait(500); //TODO give intent producer a "notify object"
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                continue;
            }

            for (DialogComponent dc : dialogComponents) {
                if (dc.onIntent(intent.get())) {
                    break;
                }
            }
        }
    }

    public void addDialogComponent(DialogComponent dc) {
        this.dialogComponents.add(dc);
    }

    public void addIntentProducer(IntentProducer intentProducer) {
        this.intentProducer.add(intentProducer);
    }

    public CIntentProducer getIntentProducer() {
        return intentProducer;
    }
}
