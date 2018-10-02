package de.dfki.tocalog;

import de.dfki.tecs.util.IntervalHelper;
import de.dfki.tocalog.core.*;
import de.dfki.tocalog.dialog.Intent;
import de.dfki.tocalog.dialog.sc.State;
import de.dfki.tocalog.dialog.sc.StateChart;
import de.dfki.tocalog.dialog.sc.StateChartEvent;
import de.dfki.tocalog.dialog.sc.Transition;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.model.Agent;
import de.dfki.tocalog.model.Entity;
import de.dfki.tocalog.output.*;
import de.dfki.tocalog.output.impp.Allocation;
import de.dfki.tocalog.output.impp.OutputNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class GreetingBehavior implements DialogComponent {
    public static final String INTENT = "greeting";
    private final KnowledgeBase kb;
    private final IMPP imp;
    private Logger log = LoggerFactory.getLogger(GreetingBehavior.class);
    private StateChart sc;
    private IntervalHelper ih = new IntervalHelper(500);
    private long greetedTimestamp;
    private long greetedTimeout = 3000L;
    private HashSet<String> greeted = new HashSet<>();

    public GreetingBehavior(IMPP imp) {
        this.imp = imp;
        this.kb = imp.getKb();
    }

    @Override
    public Optional<DialogFunction> process(Hypothesis h) {
        if (!h.getIntent().equals(INTENT)) {
            return Optional.empty();
        }

        Optional<Hypothesis.Slot> slot = h.getSlot("target");
        if (!slot.isPresent()) {
            log.warn("could not find '{}' slot in hypothesis: {}", "target", h);
            return Optional.empty();
        }

        Collection<Entity> entities = slot.get().findMatches(kb);
        List<Agent> agents = entities.stream()
                .filter(e -> e instanceof Agent)
                .filter(a -> !greeted.contains(a.getId().orElse("")))
                .map(e -> (Agent) e)
                .collect(Collectors.toList());

        if(agents.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(() -> greet(agents));
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
    public Collection<Class<HypothesisProducer>> getRelevantHypoProducers() {
        return Collections.EMPTY_SET; //TODO
    }
}
