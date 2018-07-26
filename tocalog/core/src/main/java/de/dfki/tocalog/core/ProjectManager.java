package de.dfki.tocalog.core;

import de.dfki.tocalog.core.kb.KnowledgeBase;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 */
public class ProjectManager implements Runnable {
    private EventEngine eventEngine;
    private KnowledgeBase knowledgeBase;
    private final Set<DialogComponent> fusionModules;


    protected ProjectManager(Builder builder) {
        this.eventEngine = builder.eventEngineBuilder.build();
        this.knowledgeBase = builder.knowledgeBase;
        this.fusionModules = Collections.unmodifiableSet(builder.fusionModules);
    }

    public EventEngine getEventEngine() {
        return eventEngine;
    }

    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    public Optional<DialogComponent> getModule(Predicate<DialogComponent> filter) {
        return fusionModules.stream()
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
        protected Set<DialogComponent> fusionModules = new HashSet<>();

        public Builder() {
        }

        public Builder addFusionModule(DialogComponent fusionModule) {
            eventEngineBuilder.addListener(fusionModule);
            fusionModules.add(fusionModule);
            return this;
        }

        public ProjectManager build() {
            ProjectManager dc = new ProjectManager(this);
            for(DialogComponent fm : fusionModules) {
                fm.init(dc);
            }
            return dc;
        }
    }
}
