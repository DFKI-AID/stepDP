package de.dfki.tocalog.core;

import de.dfki.tocalog.dialog.MetaDialog;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.output.IMPP;
import de.dfki.tocalog.output.OutputComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;

/**
 * Entry point for the dialog application. See ProjectManager::Builder for creating a new instance.
 * Initializes input and dialog components by adding them to the event system and granting knowledge base access.
 */
public class DialogApp implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DialogApp.class);
    private static final long INPUT_TIMEOUT = 5000L;
//    private final EventEngine eventEngine;
    private final KnowledgeBase knowledgeBase;
    private final MetaDialog dialog;
    private final List<EventProducer> eventProducers;
    private final List<SensorComponent> sensorComponents;
    private final List<Store> stores;
    private final List<InputComponent> inputComponents;
    private final List<HypothesisProducer> hypothesisProducers;
    private final Object monitor = new Object();
    private Inputs inputs = new Inputs();
    private final Resolution resolution;


    protected DialogApp(Builder builder) {
//        this.eventEngine = builder.eventEngineBuilder.build();
        this.knowledgeBase = builder.knowledgeBase;
        this.dialog = builder.metaDialog;
        this.inputComponents = Collections.unmodifiableList(builder.inputComponents);
        this.sensorComponents = builder.sensorComponents;
        this.stores = builder.stores;
        this.eventProducers = builder.eventProducers;
        this.hypothesisProducers = builder.hypothesisProducers;
        this.resolution = builder.resolution;
    }

//    public EventEngine getEventEngine() {
//        return eventEngine;
//    }

    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }


    public Optional<InputComponent> getInputComponent(Predicate<InputComponent> filter) {
        return inputComponents.stream()
                .filter(filter)
                .findAny();
    }

    public Object getNotifyObject() {
        return monitor;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            update();
        }
    }

    public void update() {
        // get new events and sleep if no new data is available
        List<Event> events = new ArrayList<>();
        eventProducers.forEach(ep -> ep.nextEvent().ifPresent(e -> events.add(e)));
        if (events.isEmpty()) {
            synchronized (monitor) {
                try {
                    monitor.wait(50);
                } catch (InterruptedException e) {
                    log.error("interrupted {}", this.getClass(), e.toString());
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            return;
        }

        inputs.removeOld(INPUT_TIMEOUT);

        for (Event event : events) {
            //udpate sensor data
            sensorComponents.forEach(
                    sc -> sc.process(event).ifPresent(
                            si -> stores.forEach(s -> s.process(si))
                    )
            );

            //process inputs
            for (InputComponent ic : inputComponents) {
                Collection<Input> tmpInputs = ic.process(event);
                if (tmpInputs.isEmpty()) {
                    continue;
                }

                inputs.add(tmpInputs);
            }
        }

        // derive hypothesis
        if (inputs.isEmpty()) {
            return;
        }
        List<Hypothesis> hypothesisTrees = new ArrayList<>();
        hypothesisProducers.forEach(hp -> hp.process(inputs).ifPresent(ht -> hypothesisTrees.add(ht)));
        Optional<Hypothesis> hypothesisTree = hypothesisTrees.stream().reduce(Hypothesis::or);
        if (!hypothesisTree.isPresent()) {
            return;
        }

        Hypothesis resolvedHypothesisTree = resolution.process(hypothesisTree.get());

        //TODO dialog

        //TODO remove inputs if consumed by the dialog
        //TODO better: just mark them as consumed but keep 'em for 'correcting' an output
    }

    public static Builder create(MetaDialog dialog) {
        return new Builder(dialog);
    }

    public static class Builder {
//        protected EventEngine.Builder eventEngineBuilder = EventEngine.build();
        protected KnowledgeBase knowledgeBase = new KnowledgeBase();
        private List<EventProducer> eventProducers = new ArrayList<>();
        private List<SensorComponent> sensorComponents = new ArrayList();
        private List<Store> stores = new ArrayList<>();
        protected List<InputComponent> inputComponents = new ArrayList<>();
        private List<HypothesisProducer> hypothesisProducers;
        protected MetaDialog metaDialog; //TODO maybe accept dialog as arg
        protected IMPP impp = new IMPP(knowledgeBase);
        private Resolution resolution;

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

        public DialogApp build() {
            //connect to event queue
//            for (InputComponent ic : inputComponents) {
//                eventEngineBuilder.addListener(ic);
//            }
//            eventEngineBuilder.addListener(metaDialog);

            DialogApp dc = new DialogApp(this);

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
