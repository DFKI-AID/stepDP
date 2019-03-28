package de.dfki.step.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dfki.step.kb.*;
import de.dfki.step.output.OutputComponent;
import de.dfki.step.output.OutputFactory;
import de.dfki.step.output.imp.AllocationState;
import de.dfki.step.output.imp.OutputUnit;
import org.pcollections.HashPMap;
import org.pcollections.IntTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * TODO delay subscription on error
 */
public class A3SClient implements OutputComponent {
    public static final Attribute<String> loudspeaker = new Attribute<>("a3s/loudspeaker");
    private static final Logger log = LoggerFactory.getLogger(A3SClient.class);
    public static final String serviceType = "a3s-playback";
    private static final Duration reqTimeout = Duration.ofMillis(4000);
    private static final Duration reqInterval = Duration.ofMillis(1000);
    private static final Duration pollTimeout = Duration.ofMillis(8000);
    private static final Ontology.Scheme serviceScheme = Ontology.AbsScheme.builder()
            .present(Ontology.id)
            .present(Ontology.uri)
            .matches(Ontology.service, x -> Objects.equals(x, serviceType))
            .build();
    private final KnowledgeMap km;
    private final KnowledgeBase kb;
    private final String host = "172.16.60.241";
    private final int port = 50000;
    private PMap<String, AllocationState> allocationStates = HashPMap.empty(IntTreePMap.empty());
    private PMap<String, AllocationState> oldAllocationStates = HashPMap.empty(IntTreePMap.empty());


    public A3SClient(KnowledgeBase kb) {
        this.kb = kb;
        this.km = kb.getKnowledgeMap(Ontology.Service);

        for (Entity entity : km.getAll()) {
            updatePlayerState(entity.get(Ontology.id).get());
        }
    }


    @Override
    public String allocate(OutputUnit outputUnit) {
        log.info("allocating {}", outputUnit);
        //TODO multiple services
        String id = UUID.randomUUID().toString().substring(0, 8);
        synchronized (this) {
            allocationStates = allocationStates.plus(id, AllocationState.getInit());
        }

        if (outputUnit.getServices().isEmpty()) {
            log.warn("can't present {}: no services", outputUnit);
            synchronized (this) {
                allocationStates = allocationStates.plus(id, AllocationState.getError("no services assigned"));
            }
            return id;
        }

        try {
            createAudioSession(id, outputUnit);
        } catch (Exception ex) {
            log.warn("could not of audio session: {}", ex.getMessage());
            synchronized (this) {
                allocationStates = allocationStates.plus(id, AllocationState.getError(ex));
            }
        }
        return id;
    }

    @Override
    public void deallocate(String allocationId) {
        WebClient.create(String.format("http://%s:%d/session/%s", host, port, allocationId))
                .method(HttpMethod.DELETE)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(reqTimeout)
                .doOnError(ex -> log.warn("error while de-allocating session: {}", ex.getMessage()))
                .subscribe();
    }

    @Override
    public AllocationState getAllocationState(String id) {
        AllocationState as = allocationStates.get(id);
        if (as != null) {
            return as;
        }

        AllocationState oldAs = oldAllocationStates.get(id);
        if (oldAs != null) {
            return oldAs;
        }
        return AllocationState.getNone();
    }

    @Override
    public boolean supports(Entity output, Entity service) {
        if (!serviceScheme.matches(service)) {
            return false;
        }

        if (!OutputFactory.FileOutputScheme.matches(output)) {
            return false;
        }
        //TODO other types e.g. TTS
        return true;
    }


    protected void updatePlayerState(String id) {
        //TODO return immediately if player should not be used anymore

        getPlayerStateAsync(id, reqTimeout)
                .delaySubscription(reqInterval)
                .subscribe();

    }

    protected Mono<String> getPlayerStateAsync(String id, Duration timeout) {
        Entity entity = km.get(id).orElse(Entity.empty);
        Optional<URI> uri = entity.get(Ontology.uri);
        if (!uri.isPresent()) {
            log.warn("Can't check a3s-player state for {}: no uri available in {}", id, entity);
            return Mono.empty();
        }
        String host = uri.get().getHost();
        int port = uri.get().getPort();

        return WebClient.create(String.format("http://%s:%d/info", host, port))
                .method(HttpMethod.GET)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(timeout)
                .doOnError(ex -> onPlayerInfo(id, ex))
                .doOnSuccess(r -> onPlayerInfo(id, r))
                .doFinally(st -> updatePlayerState(id));
    }

    protected void onPlayerInfo(String id, String rsp) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(rsp);

            String device = jsonNode.get("id").asText();
            String comp = jsonNode.get("deviceName").asText();
            if (device.isEmpty()) {
                log.warn("incomplete response from a3s-playback-service. device info is missing");
                return;
            }
            if (comp.isEmpty()) {
                log.warn("incomplete response from a3s-playback-service. device component info is missing");
                return;
            }


            Optional<Entity> currentEntry = km.get(id);
            if (!currentEntry.isPresent()) {
                //TODO remove if is clear where the initial service comes from
                return;
            }
            Entity service = currentEntry.get()
                    .set(Ontology.device, device)
                    .set(loudspeaker, comp)
                    .set(Ontology.timestamp, System.currentTimeMillis());

            km.add(service);
            //TODO write service (/device) information into KB
        } catch (Exception e) {
            e.printStackTrace();
            onPlayerInfo(id, e);
        }
    }

    protected void onPlayerInfo(String id, Throwable ex) {
        //tag as N/A in KB:
        km.unset(id, Ontology.timestamp);
        km.unset(id + "-loudspeaker", Ontology.timestamp);
    }

    protected Mono<String> deletePlayer(String id) {
        Mono<String> deletePlayer = WebClient.create(String.format("http://%s:%d/player/%s", host, port, id))
                .method(HttpMethod.DELETE)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(reqTimeout);
        return deletePlayer;
    }

    protected Mono<String> addPlayer(String id, String host, int port) {
        Map<String, Object> body = new HashMap<>();
        body.put("host", host);
        body.put("port", port);

        Mono<String> addPlayer = WebClient.create(String.format("http://%s:%d/player/%s", this.host, this.port, id))
                .method(HttpMethod.POST)
                .body(BodyInserters.fromObject(body))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(reqTimeout);
        return addPlayer;
    }

    protected Mono<String> deleteSession(String id) {
        Mono<String> deleteSession = WebClient.create(String.format("http://%s:%d/session/%s", host, port, id))
                .method(HttpMethod.DELETE)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(reqTimeout);
        return deleteSession;
    }

    /**
     * converts an audio output entity into an object that can be read by the a3s-service.
     *
     * @param output
     * @return
     */
    protected Object convert(Entity output) {
        OutputFactory.FileOutputScheme.validate(output);

        Map<String, Object> audio = new HashMap<>();
        audio.put("type", "file");
        audio.put("path", output.get(Ontology.file));
        return audio;
    }


    protected void createAudioSession(String session, OutputUnit outputUnit) {
        Set<Entity> playbackServices = outputUnit.getServices();
        Entity output = outputUnit.getOutput();

        for (Entity playbackService : playbackServices) {
            serviceScheme.validate(playbackService);
        }

        List<Mono<String>> monos = new ArrayList<>();
        for (Entity player : playbackServices) {
            monos.add(deletePlayer(player.get(Ontology.id).get()));
        }

        for (Entity player : playbackServices) {
            URI uri = player.get(Ontology.uri).get();
            monos.add(addPlayer(player.get(Ontology.id).get(), uri.getHost(), uri.getPort()));
        }

        monos.add(deleteSession(session));

        Map<String, Object> body = new HashMap<>();
        body.put("audio", convert(output));
        body.put("connections", playbackServices.stream()
                .map(x -> x.get(Ontology.id))
                .collect(Collectors.toList())
        );
        Mono<String> addSession = WebClient.create(String.format("http://%s:%d/session/%s", host, port, session))
                .method(HttpMethod.POST)
                .body(BodyInserters.fromObject(body))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(reqTimeout);
        monos.add(addSession);


        //run the whole cascade of requests:
        // removing and adding the players
        // add the session
        Flux.mergeSequential(monos, 1, 1)
                .doOnError(ex -> {
                    System.out.println(ex.getMessage());
                })
                .doFinally((st) -> {
                    updateSessionState(session);
                }).subscribe();

    }

    protected void updateSessionState(String id) {
        updateSessionState(id, "PREPARE");
    }


    protected void updateSessionState(String id, String state) {
        if (getAllocationState(id).finished()) {
            return;
        }

        pollSessionStateAsync(id, state)
                .delaySubscription(reqInterval)
                .subscribe();
    }

    protected Mono<String> pollSessionStateAsync(String id, String state) {
        return WebClient.create(String.format("http://%s:%d/session/%s/%s", host, port, id, state))
                .method(HttpMethod.GET)
                .header("Prefer", String.format("wait=%d", pollTimeout.toMillis()))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(pollTimeout.plus(reqTimeout))
                .doOnError(ex -> onSessionState(id, ex))
                .doOnSuccess(r -> onSessionState(id, r));
    }

    protected void onSessionState(String id, String rsp) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(rsp);
            JsonNode state = jsonNode.get("state");
            if (Objects.equals("failed", state.asText("failed"))) {
                onSessionState(id, new Exception("a3s-service connection failed"));
                return;
            }

            log.info("state: {}={}", id, state);
            synchronized (this) {
                AllocationState as;
                switch (state.asText()) {
                    case "STREAM_ERROR":
                        as = AllocationState.getError("TODO transfer cause");
                        break;
                    case "STREAM_SUCCESS":
                        as = AllocationState.getSuccess();
                        break;
                    default:
                        as = AllocationState.getPresenting();
                        break;
                }

                allocationStates = allocationStates.plus(id, as);
            }

            updateSessionState(id, state.asText());
        } catch (Exception e) {
            e.printStackTrace();
            onSessionState(id, e);
        }
    }

    protected void onSessionState(String id, Throwable error) {
        String errMsg = MessageFormatter.format("could not retrieve session state for {}. cause: {}",
                id, error.getMessage()).getMessage();
        log.warn(errMsg);
        synchronized (this) {
            if (getAllocationState(id).failed()) {
                //don't overwrite first error
                return;
            }

            AllocationState as;
            if (error instanceof TimeoutException) {
                as = AllocationState.getTimeout();
            } else {
                as = AllocationState.getError(errMsg);
            }

            allocationStates = allocationStates.plus(id, as);
        }
        //TODO other error types (like timeout)
    }

    /**
     * Removes all finished sessions from the a3s-service and the local tracking
     */
    protected void removeSession(String id) {
        String call = String.format("http://%s:%d/session/%s", host, port, id);
        log.info("removing session: {}", call);
        Mono<String> removeSession = WebClient.create(call)
                .method(HttpMethod.DELETE)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(reqTimeout)
                .doOnError(ex -> log.warn("could not remove session: {}", ex.getMessage()))
                .doOnSuccess(x -> log.info("removed session {}", id));

        removeSession.subscribe();
    }

    /**
     * removes all sessions on the a3s-service => use for clean startup
     */
    protected void purgeSessions() {
        log.info("purging all sessions");
        Mono<String> purgeSessions = WebClient.create(String.format("http://%s:%d/session", host, port))
                .method(HttpMethod.DELETE)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(reqTimeout)
                .doOnError(ex -> log.warn("could not purge sessions: {}", ex.getMessage()))
                .doOnSuccess(x -> log.info("purge all sessions"));

        purgeSessions.subscribe();
    }

    protected synchronized void removeFinishedSessions() {
        for (Map.Entry<String, AllocationState> entry : allocationStates.entrySet()) {
            if (entry.getValue().finished()) {
                removeSession(entry.getKey());
                allocationStates = allocationStates.minus(entry.getKey());
                oldAllocationStates = oldAllocationStates.plus(entry.getKey(), entry.getValue());
            }
        }
    }

//    @SpringBootApplication
//    public static class App implements ApplicationRunner {
//
//        public static void runCommand(String... cmd) throws IOException, InterruptedException {
//            ProcessBuilder pb = new ProcessBuilder(cmd);
//            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
//            Process p = pb.start();
//            p.waitFor();
//        }
//
//        @Override
//        public void run(ApplicationArguments args) throws Exception {
//            KnowledgeBase kb = new KnowledgeBase();
//            KnowledgeMap serviceKm = kb.getKnowledgeMap(Ontology.Service);
//            //TODO fixed entities
//            Entity p1 = new Entity()
//                    .set(Ontology.id, "p1")
//                    .set(Ontology.uri, URI.of("http://172.16.59.0:60000"))
//                    .set(Ontology.type2, Ontology.Service)
//                    .set(Ontology.service, serviceType)
//                    .set(Ontology.timestamp, 0l);
//            serviceKm.add(p1);
//
//            KnowledgeMap deviceKm = kb.getKnowledgeMap(Ontology.Device);
//            deviceKm.add(new Entity()
//                    .set(Ontology.id, "macbook")
//            );
////            KnowledgeMap compKm = kb.getKnowledgeMap(Ontology.DeviceComponent);
////            compKm.add(new Entity()
////                    .set(Ontology.id, "macbook-loudspeaker")
////                    .set(Ontology.type2, Ontology.Loudspeaker)
////                    .set(Ontology.device, "macbook")
////                    .set(Ontology.service, "p1")
////            );
//            KnowledgeMap userKm = kb.getKnowledgeMap(Ontology.Agent);
//            final Entity m1 = new Entity().set(Ontology.id, "m1");
//            userKm.add(m1);
//
//            de.dfki.step.web.A3SClient client = new de.dfki.step.web.A3SClient(kb);
//            Imp imp = new Imp(kb);
//            imp.addOutputComponent(client);
//
////            SystemUtils.IS_OS_MAC
//
//            while (true) {
//                Scanner scanner = new Scanner(System.in);
//                System.out.println("give me some text: ");
//                String tts = scanner.nextLine();
//                System.out.println();
//
//                long start = System.currentTimeMillis();
//                String dir = System.getProperty("user.dir");
//                String outPath = dir + "/sample.aiff";
//                String wavPath = dir + "/sample.wav";
//                runCommand("say", String.format("\"%s\"", tts), "-o", outPath);
//                System.out.println("elapsed gen : " + (System.currentTimeMillis() - start));
//
//                //convert to wav ; better: should be done on the same machine that runs the a3s-service
//                start = System.currentTimeMillis();
//                runCommand("sox", outPath, "-t", "wavpcm", "-r", "48000", "-c", "2", "-b", "16", wavPath);
//                System.out.println("elapsed convert : " + (System.currentTimeMillis() - start));
//
//                //upload file
//                start = System.currentTimeMillis();
//                runCommand("curl", "-XPOST", "-F", String.format("data=@%s", wavPath),
//                        String.format("http://%s:%d/files/sample", client.host, client.port));
//                System.out.println();
//                System.out.println("elapsed upload : " + (System.currentTimeMillis() - start));
//
//                start = System.currentTimeMillis();
//                Entity speechOutput = new OutputFactory().createFileOutput("sample");
//
//
//                DeviceSelector deviceSelector = new DeviceSelector(imp);
//                OutputUnit outputUnit = deviceSelector.process(
//                        new OutputUnit(speechOutput, Set.of(m1))).orElse(null);
//                if (outputUnit == null) {
//                    System.out.println("no device available");
//                    continue;
//                }
//
////                Entity whereEntity = new Entity()
////                        .set(DeviceSelector.serviceAttr, p1);
////                Entity outputUnit = new Entity()
////                        .set(DeviceSelector.what, speechOutput)
////                        .set(DeviceSelector.where, Set.of(whereEntity));
//                String allocationId = client.allocate(outputUnit);
//
//
//                AllocationState as = AllocationState.getNone();
//                while (true) {
//                    synchronized (client.getClass()) {
//                        client.getClass().wait(3);
//                    }
////                String s = client.km.getAll().stream().map(e -> e.toString())
////                        .reduce("", (x, y) -> x + " " + y);
////                System.out.println(s);
//
//                    AllocationState currentState = client.getAllocationState(allocationId);
//                    if (currentState != as) {
//                        as = currentState;
//                        System.out.println(allocationId + " " + as);
//                        if (as.presenting()) {
//                            System.out.println("elapsed present : " + (System.currentTimeMillis() - start));
//                        }
//                    }
//
//
//                    if (as.finished()) {
//                        break;
//                    }
//                }
//
//
//            }
//
//
//        }
//    }
//
//    public static void main(String[] args) throws InterruptedException {
//        SpringApplication.run(App.class);
//    }


    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
