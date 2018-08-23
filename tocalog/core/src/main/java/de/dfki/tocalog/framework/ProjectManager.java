package de.dfki.tocalog.framework;

import de.dfki.tocalog.dialog.MetaDialog;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.output.IMPP;
import de.dfki.tocalog.output.OutputComponent;

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
        protected IMPP impp = new IMPP(knowledgeBase);

        protected Builder(MetaDialog dialog) {
            this.metaDialog = dialog;
        }


        public Builder addInputComponent(InputComponent component) {
            inputComponents.add(component);
            return this;
        }

        public Builder addOutputComponent(OutputComponent component) {
            impp.addOutputComponent(component);
            return this;
        }

        public ProjectManager build() {
            //connect to event queue
            for(InputComponent ic : inputComponents) {
                eventEngineBuilder.addListener(ic);
            }
            eventEngineBuilder.addListener(metaDialog);

            ProjectManager dc = new ProjectManager(this);

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
            metaDialog.init(new DialogComponent.Context() {
                @Override
                public KnowledgeBase getKnowledgeBase() {
                    return knowledgeBase;
                }

                @Override
                public IMPP getAllocatioModule() {
                    return impp;
                }
            });

            return dc;
        }
    }
}
