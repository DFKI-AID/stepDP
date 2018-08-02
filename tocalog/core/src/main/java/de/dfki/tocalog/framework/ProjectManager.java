package de.dfki.tocalog.framework;

import de.dfki.tocalog.kb.KnowledgeBase;

import java.util.*;
import java.util.function.Predicate;

/**
 * Entry point for the dialog application. See ProjectManager::Builder for creating a new instance.
 * Initializes input and dialog components by adding them to the event system and granting knowledge base access.
 */
public class ProjectManager implements Runnable {
    private final EventEngine eventEngine;
    private final KnowledgeBase knowledgeBase;
    private final List<DialogComponent> dialogComponents;
    private final List<InputComponent> inputComponents;


    protected ProjectManager(Builder builder) {
        this.eventEngine = builder.eventEngineBuilder.build();
        this.knowledgeBase = builder.knowledgeBase;
        this.dialogComponents = Collections.unmodifiableList(builder.dialogComponents);
        this.inputComponents = Collections.unmodifiableList(builder.inputComponents);
    }

    public EventEngine getEventEngine() {
        return eventEngine;
    }

    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    public Optional<DialogComponent> getModule(Predicate<DialogComponent> filter) {
        return dialogComponents.stream()
                .filter(filter)
                .findAny();
    }

    public void run() {
        eventEngine.run();
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {
        protected EventEngine.Builder eventEngineBuilder = EventEngine.build();
        protected KnowledgeBase knowledgeBase = new KnowledgeBase();
        protected List<DialogComponent> dialogComponents = new ArrayList<>();
        protected List<InputComponent> inputComponents = new ArrayList<>();

        public Builder() {
        }

        public Builder add(DialogComponent fusionModule) {
            eventEngineBuilder.addListener(fusionModule);
            dialogComponents.add(fusionModule);
            return this;
        }

        public Builder add(InputComponent component) {
            eventEngineBuilder.addListener(component);
            inputComponents.add(component);
            return this;
        }

        public ProjectManager build() {
            ProjectManager dc = new ProjectManager(this);
            for(DialogComponent fm : dialogComponents) {
                fm.init(() -> dc);
            }
            for(InputComponent ic : inputComponents) {
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
