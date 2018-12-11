package de.dfki.tocalog;

import de.dfki.tocalog.core.*;
import de.dfki.tocalog.dialog.sc.*;
import de.dfki.tocalog.imp.a3s.A3SClient;
import de.dfki.tocalog.imp.voapp.VOAppClient;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.output.Imp;
import de.dfki.tocalog.output.OutputFactory;
import de.dfki.tocalog.output.impp.*;
import de.dfki.tocalog.rasa.RasaHelper;
import de.dfki.tocalog.rasa.RasaHypoProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 */
@SpringBootApplication
public class MainYK implements ApplicationRunner {
    private static Logger log = LoggerFactory.getLogger(MainYK.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(MainYK.class);
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
//        statechart(args);
        imp();
//        framework(args);
    }

    public static void runCommand(String... cmd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();
        p.waitFor();
    }

    public static void imp() throws IOException, InterruptedException, URISyntaxException {
        KnowledgeMap services = new KnowledgeMap();
        Entity consoleService = new Entity()
                .set(Ontology.id, "c1")
                .set(Ontology.service, "console");
        services.add(consoleService);

        OutputFactory of = new OutputFactory();
//        OutputUnit output1 = new OutputUnit(of.createTTSOutput("hello world"), Collections.EMPTY_SET);
//        OutputUnit output2 = new OutputUnit(of.createTextOutput("hello world 2"));
//                Collections.EMPTY_SET, Set.of(consoleService));
//        OutputUnit output3 = new OutputUnit(of.createTextOutput("how are you?"));
//        OutputUnit output4 = new OutputUnit(of.createTTSOutput("how are you?"));


//        OutputNode node =
//                OutputNode.build(OutputNode.Semantic.complementary).setId("abcdef")
//                        .addNode(OutputNode.build(output1))
//                        .addNode(OutputNode.build(output2))
//                        .build();

//        OutputNode node =
//                OutputNode.build(OutputNode.Semantic.concurrent)
//                        .addNode(OutputNode.build(OutputNode.Semantic.complementary).setId("abcdef")
//                                .addNode(OutputNode.build(output1))
//                                .addNode(OutputNode.build(output2))
//                                .build())
//                        .addNode(OutputNode.build(OutputNode.Semantic.alternative)
//                                .addNode(OutputNode.build(output3))
//                                .addNode(OutputNode.build(output4))
//                                .build())
//                        .build();

//        ConsoleAssigner consoleAssigner = new ConsoleAssigner(services);
//        consoleAssigner.assignConsoleService(node);
//        System.out.println(PrintVisitor.print(node));
//
//        boolean isPresentable = new PresentableVisitor().isPresentable(node);
//        System.out.println("can be presented: " + isPresentable);
//
//        System.out.println(PrintVisitor.print(node));
//
//
//        PresenterVisitor pv = new PresenterVisitor();
//        pv.visit(node);
//        node = pv.getResult();
//
//        System.out.println(PrintVisitor.print(node));
//
//        OutputNode nodeCopy = new CopyVisitor().copy(node).build();
//        System.out.println(PrintVisitor.print(nodeCopy));
//
//        Optional<OutputNode> singleNode = new FindNodeVisitor(n -> n.getId().equals("abcdef")).find(nodeCopy);
//        singleNode.ifPresent(n -> System.out.println(PrintVisitor.print(n)));


        KnowledgeBase kb = new KnowledgeBase();
        KnowledgeMap serviceKm = kb.getKnowledgeMap(Ontology.Service);
        //TODO fixed entities
        Entity p1 = new Entity()
                .set(Ontology.id, "a3s-playback1")
                .set(Ontology.uri, URI.create("http://pi-madmacs7:60000"))
                .set(Ontology.type2, Ontology.Service)
                .set(Ontology.service, A3SClient.serviceType)
                .set(Ontology.timestamp, 0l);
        serviceKm.add(p1);

        KnowledgeMap deviceKm = kb.getKnowledgeMap(Ontology.Device);
        deviceKm.add(new Entity()
                .set(Ontology.id, "macbook")
        );
//            KnowledgeMap compKm = kb.getKnowledgeMap(Ontology.DeviceComponent);
//            compKm.add(new Entity()
//                    .set(Ontology.id, "macbook-loudspeaker")
//                    .set(Ontology.type2, Ontology.Loudspeaker)
//                    .set(Ontology.device, "macbook")
//                    .set(Ontology.service, "p1")
//            );
        KnowledgeMap userKm = kb.getKnowledgeMap(Ontology.Agent);
        final Entity m1 = new Entity().set(Ontology.id, "m1");
        userKm.add(m1);

        A3SClient a3sClient = new A3SClient(kb);
        Imp imp = new Imp(kb);
        imp.addOutputComponent(a3sClient);

        VOAppClient voClient = new VOAppClient(kb);
        imp.addOutputComponent(voClient);


//            SystemUtils.IS_OS_MAC

        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("give me some text: ");
            String tts = scanner.nextLine();
            System.out.println();

            long start = System.currentTimeMillis();
            String dir = System.getProperty("user.dir");
            String outPath = dir + "/sample.aiff";
            String wavPath = dir + "/sample.wav";
            runCommand("say", String.format("\"%s\"", tts), "-o", outPath);
            System.out.println("elapsed gen : " + (System.currentTimeMillis() - start));

            //convert to wav ; better: should be done on the same machine that runs the a3s-service
            start = System.currentTimeMillis();
            runCommand("sox", outPath, "-t", "wavpcm", "-r", "48000", "-c", "2", "-b", "16", wavPath);
            System.out.println("elapsed convert : " + (System.currentTimeMillis() - start));

            //upload file
            start = System.currentTimeMillis();
            runCommand("curl", "-XPOST", "-F", String.format("data=@%s", wavPath),
                    String.format("http://%s:%d/files/sample", a3sClient.getHost(), a3sClient.getPort()));
            System.out.println();
            System.out.println("elapsed upload : " + (System.currentTimeMillis() - start));


            Set<Entity> target = Set.of(m1);
            start = System.currentTimeMillis();
            OutputUnit speechOutput = new OutputUnit(of.createFileOutput("sample"), target);
            OutputUnit textOutput = new OutputUnit(of.createTextOutput(tts), target);
            OutputUnit imageOutput = new OutputUnit(of.createImageOutput(new URI("http:/files/sleeping.png")));


            OutputNode node =
                    OutputNode.build(OutputNode.Semantic.complementary).setId("abcdef")
                            .addNode(OutputNode.build(imageOutput))
                            .addNode(OutputNode.build(speechOutput))
                            .addNode(OutputNode.build(textOutput))
                            .build();


            Allocation allocation = imp.allocate(node);
            log.info("allocated: {}", allocation);

            while(true) {
                AllocationState state = imp.getState(allocation);
                System.out.println(state);
                if(state.finished()) {
                    break;
                }
                Thread.sleep(500);
            }


//
//            DeviceSelector deviceSelector = new DeviceSelector(imp);
//            OutputUnit outputUnit = deviceSelector.process(
//                    new OutputUnit(speechOutput, Set.of(m1))).orElse(null);
//            if (outputUnit == null) {
//                System.out.println("no device available");
//                continue;
//            }

//                Entity whereEntity = new Entity()
//                        .set(DeviceSelector.serviceAttr, p1);
//                Entity outputUnit = new Entity()
//                        .set(DeviceSelector.what, speechOutput)
//                        .set(DeviceSelector.where, Set.of(whereEntity));


        }
    }


    public static void statechart(String[] args) throws IOException {

        ConsoleReader consoleReader = new ConsoleReader();
        consoleReader.start();

        StateChart.Callback<Hypothesis> callback = new StateChart.Callback<Hypothesis>() {
            @Override
            public void onFire(StateChart.FireEvent<Hypothesis> event) {
                System.out.println("ok: " + event.event);
            }
        };

        StateChart<Hypothesis> sc = new StateChart.Builder<Hypothesis>()
                .setInitialState("A")
                .addTransition("A", "B", (h -> {
                    if (!h.getIntent().equals("turnOn")) {
                        return false;
                    }
                    Collection<Entity> devices = h.getSlot("device").orElse(Slot.Empty).getCandidates();
                    Set<Entity> fans = devices.stream()
                            .filter(d -> d.get(Ontology.name).orElse("").equals("fan"))
                            .collect(Collectors.toSet());
                    if (fans.isEmpty()) {
                        return false;
                    }
                    return true;
                }))
                .addTransition("B", "A", (h -> h.getIntent().equals("turnOff")))
                .setCallback(callback)
                .build();


        StateChart<Hypothesis> scGreeting = new StateChart.Builder<Hypothesis>()
                .setInitialState("Greeting")
                .addTransition("Greeting", "Greeting", (h -> {
                    if (!h.getIntent().equals("greeting")) {
                        return false;
                    }
                    return true;
                }))
                .build();

        HypothesisProducer rasaHp = new RasaHypoProducer(new RasaHelper(new URL("http://localhost:5000/parse")));
        DialogComponent dc = new SCDialogComponent(sc, rasaHp);

        DialogApp app = DialogApp.create()
                .addEventProducer(consoleReader)
                .addInputComponent(consoleReader)
                .addDialogComponent(dc)
                .addDialogComponent(new SCDialogComponent(scGreeting, rasaHp))
                .build();

        app.run();
//        String mmGraph = sc.toMermaid();
//        FileUtils.writeStringToFile(new File("~/tmp/graph.txt"), mmGraph, Charset.defaultCharset());
    }


}
