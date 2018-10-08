package de.dfki.tocalog;

import de.dfki.tecs.util.IntervalHelper;
import de.dfki.tocalog.core.*;
import de.dfki.tocalog.dialog.sc.StateChart;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.EKnowledgeMap;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.model.Agent;
import de.dfki.tocalog.model.Confidence;
import de.dfki.tocalog.model.Entity;
import de.dfki.tocalog.model.Person;
import de.dfki.tocalog.output.*;
import de.dfki.tocalog.output.impp.Allocation;
import de.dfki.tocalog.output.impp.OutputNode;
import de.dfki.tocalog.rasa.RasaHelper;
import de.dfki.tocalog.rasa.RasaResponse;
import fastily.jwiki.core.Conf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GreetingBehavior implements DialogComponent {
    public static final String INTENT = "greeting";
    private final KnowledgeBase kb;
    private final Imp imp;
    private Logger log = LoggerFactory.getLogger(GreetingBehavior.class);
    private StateChart sc;
    private IntervalHelper ih = new IntervalHelper(500);
    private long greetedTimestamp;
    private long greetedTimeout = 3000L;
    private HashSet<String> greeted = new HashSet<>();
    private final RasaHelper rasaHelper;

    public GreetingBehavior(Imp imp, RasaHelper rasaHelper) {
        this.imp = imp;
        this.kb = imp.getKb();
        this.rasaHelper = rasaHelper;
    }

    public Optional<DialogFunction> greet(Input input) {


        Collection<Entity> entities = slot.get().findMatches(kb);
        List<Agent> agents = entities.stream()
                .filter(e -> e instanceof Agent)
                .filter(a -> !greeted.contains(a.getId().orElse("")))
                .map(e -> (Agent) e)
                .collect(Collectors.toList());

        if (agents.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new DialogFunction() {
            @Override
            public Collection<Input> consumedInputs() {
                return Set.of(input);
            }

            @Override
            public Object getOrigin() {
                return GreetingBehavior.this;
            }

            @Override
            public void run() {
                greet(agents);
            }

            @Override
            public Optional<Confidence> getConfidence() {
                return Optional.of(Confidence.HIGH);
            }
        });
    }

    protected void greet(List<Agent> agents) {
        String msg = "hi ";
        if (agents.size() == 1) {
            msg += agents.get(0).getName().orElse(""); //TODO ugly if name not present
        } else if (agents.size() == 2) {
            msg += agents.get(0).getName().orElse("");
            msg += " and ";
            msg += agents.get(1).getName().orElse("");
        } else {
            msg += "together!";
        }
        OutputNode node =
                OutputNode.buildNode(OutputNode.Semantic.complementary)
                        .addNode(OutputNode.buildNode(new TextOutput(msg)).build())
                        .addNode(OutputNode.buildNode(OutputNode.Semantic.optional)
                                .addNode(OutputNode.buildNode(new TextOutput("second msg!")).build())
                                .build()
                        )
                        .addNode(OutputNode.buildNode(OutputNode.Semantic.optional)
                                .addNode(OutputNode.buildNode(new SpeechOutput("blaaa blubb")).build())
                                .build())
                        .addNode(OutputNode.buildNode(new ImageOutput(new File("/Users/yk/Desktop/direct.jpg"))).build())
                        .build();
        Allocation allocation = imp.allocate(node);

        //TODO should not block here
        while (!allocation.getAllocationState().finished()) {
            allocation.updateAllocationState();
            System.out.println(allocation.getAllocationState());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Optional<DialogFunction> process(Inputs inputs) {
        for (Input input : inputs.getInputs()) {
            if (inputs.isConsumed(input)) {
                continue;
            }

            if (!(input instanceof TextInput)) {
                continue;
            }

            try {
                String rasaJson = rasaHelper.nlu(((TextInput) input).getText());
                RasaResponse rrsp = rasaHelper.parseJson(rasaJson);
                if (!rrsp.getRasaIntent().getName().equals(INTENT)) {
                    continue;
                }
                greet(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }
}
