package de.dfki.tocalog.framework;

import de.dfki.tocalog.dialog.IntentProducer;
import de.dfki.tocalog.dialog.MetaDialog;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.output.AllocationModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Entry point for the dialog application. See ProjectManager::Builder for creating a new instance.
 * Initializes input and dialog components by adding them to the event system and granting knowledge base access.
 */
public class ProjectManager implements Runnable {
    private final EventEngine eventEngine;
    private final KnowledgeBase knowledgeBase;
    private final List<InputComponent> inputComponents;
    private final MetaDialog dialog;

    protected ProjectManager(Builder builder) {
        this.eventEngine = builder.eventEngineBuilder.build();
        this.knowledgeBase = builder.knowledgeBase;
        this.dialog = builder.metaDialog;
        this.inputComponents = Collections.unmodifiableList(builder.inputComponents);
    }

    public EventEngine getEventEngine() {
        return eventEngine;
    }

    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }


    public Optional<InputComponent> getInputComponent(Predicate<InputComponent> filter) {
        return inputComponents.stream()
                .filter(filter)
                .findAny();
    }

    public void run() {
        Thread dialogThread = new Thread(dialog);
        dialogThread.setDaemon(true);
        dialogThread.start();
        eventEngine.run();
    }

    public static Builder create(MetaDialog dialog) {
        return new Builder(dialog);
    }

    public static class Builder {
        protected EventEngine.Builder eventEngineBuilder = EventEngine.build();
        protected KnowledgeBase knowledgeBase = new KnowledgeBase();
        protected List<InputComponent> inputComponents = new ArrayList<>();
        protected MetaDialog metaDialog; //TODO maybe accept dialog as arg


        protected Builder(MetaDialog dialog) {
            this.metaDialog = dialog;
        }


        public Builder add(InputComponent component) {
            eventEngineBuilder.addListener(component);
            inputComponents.add(component);
            return this;
        }

        public ProjectManager build() {
            //glue Inputs from event system to dialog
            eventEngineBuilder.addListener((ee, eve) -> {
                if (!eve.is(Input.class)) {
                    return;
                }
                metaDialog.getIntentProducer().add((Input) eve.get());
            });

            ProjectManager dc = new ProjectManager(this);
            metaDialog.init(new DialogComponent.Context() {
                @Override
                public KnowledgeBase getKnowledgeBase() {
                    return knowledgeBase;
                }

                @Override
                public AllocationModule getAllocatioModule() {
                    return null; //TODO
                }
            });

            for (InputComponent ic : inputComponents) {
                ic.init(new InputComponent.Context() {
                    @Override
                    public KnowledgeBase getKnowledgeBase() {
                        return dc.knowledgeBase;
                    }

                    @Override
                    public EventEngine getEventEngine() {
                        return dc.eventEngine;
                    }
                });
            }



            return dc;
        }
    }
}
