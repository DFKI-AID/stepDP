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
    private final KnowledgeBase knowledgeBase;
    private final MetaDialog dialog;
    private final List<EventProducer> eventProducers;
    private final List<SensorComponent> sensorComponents;
    private final List<Store> stores;
    private final List<InputComponent> inputComponents;
    private final List<HypothesisProducer> hypothesisProducers;
    private final Object monitor;
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
        this.monitor = builder.monitor;
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

        Hypotheses hypotheses = new Hypotheses();
        hypothesisProducers.forEach(hp -> hp.process(inputs).ifPresent(ht -> hypotheses.add(ht)));
//        Optional<HypothesisTree> hypothesisTree = hypotheses.stream().reduce(HypothesisTree::or);
//        if (!hypothesisTree.isPresent()) {
//            return;
//        }

        Hypotheses resolvedHypotheses = resolution.process(hypotheses);
        Set<String> consumedHypos = dialog.on(resolvedHypotheses);

        // get all inputs that were consumed in form of a hypothesis
        Set<String> consumedInputs = new HashSet<>();
        consumedHypos.forEach(hid -> resolvedHypotheses.getHypothesis(hid)
                .ifPresent(h -> consumedInputs.addAll(h.getInputs())));


        //TODO atm: simply removes all inputs. However, they might still be interesting and put into
        //something like a discourse history?
        inputs.remove(consumedInputs);

    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        //        protected EventEngine.Builder eventEngineBuilder = EventEngine.build();
        protected KnowledgeBase knowledgeBase;
        private List<EventProducer> eventProducers = new ArrayList<>();
        private List<SensorComponent> sensorComponents = new ArrayList();
        private List<Store> stores = new ArrayList<>();
        protected List<InputComponent> inputComponents = new ArrayList<>();
        private List<HypothesisProducer> hypothesisProducers;
        protected MetaDialog metaDialog; //TODO maybe accept dialog as arg
        protected IMPP impp;
        private Resolution resolution;
        private Object monitor = new Object();

        protected Builder() {

        }


        public Builder addInputComponent(InputComponent component) {
            inputComponents.add(component);
            return this;
        }

        public Builder addOutputComponent(OutputComponent component) {
            impp.addOutputComponent(component);
            return this;
        }

        public KnowledgeBase getKnowledgeBase() {
            return knowledgeBase;
        }

        public Builder setKnowledgeBase(KnowledgeBase knowledgeBase) {
            this.knowledgeBase = knowledgeBase;
            return this;
        }

        public MetaDialog getMetaDialog() {
            return metaDialog;
        }

        public Builder setMetaDialog(MetaDialog metaDialog) {
            this.metaDialog = metaDialog;
            return this;
        }

        public IMPP getImpp() {
            return impp;
        }

        public Builder setImpp(IMPP impp) {
            this.impp = impp;
            return this;
        }

        public Resolution getResolution() {
            return resolution;
        }

        public void setResolution(Resolution resolution) {
            this.resolution = resolution;
        }

        public DialogApp build() {
            DialogApp dc = new DialogApp(this);
            return dc;
        }

        public Object getMonitor() {
            return monitor;
        }
    }
}
