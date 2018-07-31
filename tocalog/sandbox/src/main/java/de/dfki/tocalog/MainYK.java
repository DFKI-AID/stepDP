package de.dfki.tocalog;

import de.dfki.tocalog.framework.ProjectManager;
import de.dfki.tocalog.framework.AbstractDialogComponent;
import de.dfki.tocalog.framework.DialogComponent;
import de.dfki.tocalog.kb.*;
import de.dfki.tocalog.framework.Event;
import de.dfki.tocalog.framework.EventEngine;
import de.dfki.tocalog.model.Person;
import de.dfki.tocalog.dialog.sc.State;
import de.dfki.tocalog.dialog.sc.StateChart;
import de.dfki.tocalog.dialog.sc.Transition;
import de.dfki.tocalog.model.VisualFocus;
import de.dfki.tocalog.output.Output;
import de.dfki.tocalog.output.impp.OutputNode;
import de.dfki.tocalog.output.SpeechOutput;
import de.dfki.tocalog.output.TextOutput;
import de.dfki.tocalog.output.impp.PresentableVisitor;
import de.dfki.tocalog.output.impp.PresenterVisitor;
import de.dfki.tocalog.output.impp.PrintVisitor;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 */
public class MainYK {
    public static void main(String[] args) throws IOException, InterruptedException {
//        statechart(args);
//        impp(args);
        framework(args);
    }

    public static void impp(String[] args) {
        Output output1 = new SpeechOutput("hello world");
        Output output2 = new TextOutput("hello world 2");
        Output output3 = new TextOutput("how are you?");
        Output output4 = new SpeechOutput("how are you?");

        OutputNode node =
                OutputNode.buildNode(OutputNode.Semantic.concurrent)
                    .addNode(OutputNode.buildNode(OutputNode.Semantic.complementary)
                            .addNode(OutputNode.buildNode(output1).addService("s456").build())
                            .addNode(OutputNode.buildNode(output2).addService("s1").build())
                            .build())
                    .addNode(OutputNode.buildNode(OutputNode.Semantic.alternative)
                            .addNode(OutputNode.buildNode(output3)
                                    .addService("blubb-textview")
                                    .build())
                            .addNode(OutputNode.buildNode(output4)
                                    .addService("blubb-loudspeaker1")
                                    .addService("blubb-loudspeaker2")
                                    .build())
                            .build())
                    .build();

        boolean isPresentable = new PresentableVisitor().isPresentable(node);
        System.out.println("can be presented: " + isPresentable);

        PrintVisitor printVisitor = new PrintVisitor();
        System.out.println(printVisitor.print(node));


        PresenterVisitor pv = new PresenterVisitor();
        pv.visit(node);
        node = pv.getResult();

        System.out.println(printVisitor.print(node));
    }

    public static void framework(String[] args) throws InterruptedException, IOException {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);


        KnowledgeStore<VisualFocus> ks = new KnowledgeStore<>();
        /**/

        DialogComponent fusion1 = new DialogComponent() {
            private KnowledgeStore<VisualFocus> ks;

            @Override
            public void init(Context context) {
                ks = context.getProjectManager().getKnowledgeBase().initKnowledgeStore(VisualFocus.class);
            }

            @Override
            public void onEvent(EventEngine engine, Event event) {
                //if event is visual focus event
                ks.put("mechanic1", VisualFocus.create().setId("mechanic1").setFocus("car"));
            }
        };

        PSBridge psBridge = PSBridge.build()
                .subscribe("SheepEvent")
                .subscribe("BinaryEvent")
                .build();

        ProjectManager dc = ProjectManager.build()
                .add(fusion1)
                .add(psBridge)
                .build();


        Thread timeThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    continue;
                }

                Person p = Person.create()
                        .setId("mechanic1")
                        .setName("der mechaniker");
//                ks.put(p);
                dc.getEventEngine().submit(Event.build(p).build());
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();

        dc.run();
    }


    public static void statechart(String[] args) throws IOException {
        State stateA = State.create("A")
                .onEntry(() -> {
                    System.out.println("entered A!");
                })
                .onExit(() -> {

                })
                .build();

        State stateB = State.create("B").build();
        State stateC = State.create("C").build();
        State stateD = State.create("D").build();
        State stateHello = State.create("Goal").build();

        Transition.Iface helloSaid = event -> {
            //TODO check that hello was said
            return true;
        };

        Transition.Iface t2 = event -> {
            return true;
        };

        StateChart sc = StateChart.create()
                .setInitialState(stateA)
                .addTransition("in hello", stateA, stateB, helloSaid)
                .addTransition("out hello", stateB, stateA, e -> {
                    return false;
                })
                .addTransition("finish", stateB, stateC, t2)
                .addTransition("D", stateC, stateD, t2)
                .addTransition("back", stateD, stateA, t2)
                .addTransition("test", stateD, stateHello, helloSaid)
                .build();


        String mmGraph = sc.toMermaid();
        FileUtils.writeStringToFile(new File("~/tmp/graph.txt"), mmGraph, Charset.defaultCharset());
    }


}
