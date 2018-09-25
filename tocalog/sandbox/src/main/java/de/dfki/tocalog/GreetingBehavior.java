package de.dfki.tocalog;

import de.dfki.tecs.util.IntervalHelper;
import de.dfki.tocalog.dialog.Intent;
import de.dfki.tocalog.dialog.sc.State;
import de.dfki.tocalog.dialog.sc.StateChart;
import de.dfki.tocalog.dialog.sc.StateChartEvent;
import de.dfki.tocalog.dialog.sc.Transition;
import de.dfki.tocalog.core.DialogComponent;
import de.dfki.tocalog.core.Event;
import de.dfki.tocalog.core.EventEngine;
import de.dfki.tocalog.output.*;
import de.dfki.tocalog.output.impp.Allocation;
import de.dfki.tocalog.output.impp.OutputNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class GreetingBehavior implements DialogComponent {
    private Logger log = LoggerFactory.getLogger(GreetingBehavior.class);
    private StateChart sc;
    private IntervalHelper ih = new IntervalHelper(500);
    private long greetedTimestamp;
    private long greetedTimeout = 3000L;

    @Override
    public void init(Context context) {
//            TelegramBot tb = (TelegramBot) context.getProjectManager().getInputComponent(ic -> ic instanceof TelegramBot).get();
        IMPP impp = context.getAllocatioModule();
        State ngs = new State("NotGreeted") {
            @Override
            protected void onEntry() {

            }

            @Override
            protected void onExit() {

            }
        };
        State gs = new State("Greeted") {

            @Override
            protected void onEntry() {
                greetedTimestamp = System.currentTimeMillis();
            }

            @Override
            protected void onExit() {

            }
        };
        Transition greetingTransition = new Transition("greetings", ngs, gs) {
            @Override
            public boolean fires(StateChartEvent eve) {
                if (!eve.getIntent().isPresent()) {
                    return false;
                }
                Intent intent = eve.getIntent().get();
                if (!intent.getType().equals("greeting")) {
                    return false;
                }

                String msg = "hi ";
                if (intent.getNominative().getEntities().size() == 1) {
                    msg += intent.getNominative().getEntities().get(0);
                } else if (intent.getNominative().getEntities().size() == 2) {
                    msg += intent.getNominative().getEntities().get(0);
                    msg += " and ";
                    msg += intent.getNominative().getEntities().get(1);
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
                Allocation allocation = impp.allocate(node);

                while (!allocation.getAllocationState().finished()) {
                    allocation.updateAllocationState();
                    System.out.println(allocation.getAllocationState());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                //log.info("would output something :)");
//                    out.allocate(node);
//                    out.send("hi " + intent.getNominative());
                return true;
            }
        };

        Transition resetTransition = new Transition("reset", gs, ngs) {
            @Override
            public boolean fires(StateChartEvent eve) {
                if (greetedTimestamp + 3000 < System.currentTimeMillis()) {
                    log.debug("resetting greeting");
                    return true;
                }
                return false;
            }
        };

        sc = StateChart.create()
                .setInitialState(ngs)
                .addTransition(greetingTransition)
                .addTransition(resetTransition)
                .build();
    }

    @Override
    public boolean onIntent(Intent intent) {
        return sc.onEvent(new StateChartEvent(intent));
    }

    @Override
    public void onEvent(EventEngine engine, Event event) {
        if (!ih.shouldExecute()) {
            return;
        }
//        sc.onEvent(new StateChartEvent()); //will trigger the reset transition at some point
    }
}
