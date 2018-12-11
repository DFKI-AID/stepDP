package de.dfki.tocalog.imp.voapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.output.Imp;
import de.dfki.tocalog.output.Output;
import de.dfki.tocalog.output.OutputComponent;
import de.dfki.tocalog.output.OutputFactory;
import de.dfki.tocalog.output.impp.AllocationState;
import de.dfki.tocalog.output.impp.DeviceSelector;
import de.dfki.tocalog.output.impp.OutputUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO multiple outputs
 * TODO duration of outputs
 * <p>
 * [1] polls available displays+services and writes them into into the KB
 * [2] keeps track presenting visual content
 */
public class VOAppClient implements OutputComponent {
    private static final Logger log = LoggerFactory.getLogger(VOAppClient.class);
    private static final String serviceType = "voapp-display";
    private static final Ontology.Scheme serviceScheme = Ontology.AbsScheme.builder()
            .equal(Ontology.service, serviceType)
            .present(Ontology.id)
            .build();
    private final Duration reqTimeout = Duration.ofMillis(8000);
    private final KnowledgeBase kb;
    private final String host = "localhost";
    private final int port = 50001;
    private long displaysHash = 0;
    private Map<String, AllocationState> stateMap = new HashMap<>();

    public VOAppClient(KnowledgeBase kb) {
        this.kb = kb;
        this.pollDisplaysAsync(reqTimeout).subscribe();
    }

    @Override
    public String allocate(OutputUnit outputUnit) {
        String allocationId = allocateFreshId();
        KnowledgeMap km = this.kb.getKnowledgeMap(Ontology.Service);

        if (outputUnit.getServices().isEmpty()) {
            log.warn("can't present {}: no services", outputUnit);
//            synchronized (this) {
//                allocationStates = allocationStates.plus(id, AllocationState.getError("no services assigned"));
//            }
            return allocationId;
        }

        Set<Entity> services = outputUnit.getServices();
        Entity output = outputUnit.getOutput();

        for (Entity service : services) {
            if (!serviceScheme.matches(service)) {
                synchronized (this) {
                    String msg = String.format("invalid service %s for voapp", service);
                    log.warn(msg);
                    stateMap.put(allocationId, AllocationState.getError(msg));
                    return allocationId;
                }
            }
        }


        //TODO multiple contents
        Map<String, Object> payload = new HashMap<>();
        try {
            Map<String, Object> content1 = createContent(output, services);
            payload.put("duration", "6000"); //TODO duration
            payload.put("content", content1);
        } catch (Exception ex) {
            log.warn("could not allocate visual output: {}", ex.getMessage());
            synchronized (this) {
                stateMap.put(allocationId, AllocationState.getError(ex.getMessage()));
                return allocationId;
            }
        }

        Mono<String> req = WebClient.create(String.format("http://%s:%d/session/%s",
                host, port, allocationId))
                .method(HttpMethod.POST)
                .body(BodyInserters.fromObject(payload))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(reqTimeout)
                .doOnError(ex -> onPresentationInfo(allocationId, ex))
                .doOnSuccess(r -> onPresentationInfo(allocationId, r));
        req.subscribe();


        synchronized (this) {
            //TODO change to init and track
            stateMap.put(allocationId, AllocationState.getInit());
        }

        getSessionStateAsync(allocationId, "none").subscribe();

        return allocationId;
    }

    protected Mono<String> getSessionStateAsync(String session, String state) {
        return WebClient.create(String.format("http://%s:%d/session/%s/state/%s", host, port, session, state))
                .method(HttpMethod.GET)
                .header("Prefer", "wait=" + reqTimeout.dividedBy(2).toMillis())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(reqTimeout)
                .doOnSuccess(x -> onSessionInfo(session, state, x))
                .doOnError(x -> onSessionInfo(session, x));
    }

    protected void onSessionInfo(String session, Throwable ex) {
        log.info("could not retrieve state for session {}. cause={}", session, ex.getMessage());
        synchronized (this) {
            stateMap.put(session, AllocationState.getError(ex.getMessage()));
        }
    }

    protected void onSessionInfo(String session, String oldState, String rsp) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(rsp);
            String currentState = jsonNode.get("state").asText();
            if (!Objects.equals(oldState, currentState)) {
                log.info("state change for session {}: {}->{}", session, oldState, currentState);
                updateSessionInfo(session, currentState);
            }

            if (!isFinished(session)) {
                getSessionStateAsync(session, currentState).subscribe();
            }
        } catch (Exception ex) {
            onSessionInfo(session, ex);
        }
    }

    /**
     * @param session
     * @return true iff the presentation for the given session finished
     */
    protected synchronized boolean isFinished(String session) {
        return Optional.ofNullable(stateMap.get(session)).orElse(AllocationState.NONE).finished();
    }

    /**
     * updates the allocation state of the session by parsing the state retrieved from the service
     *
     * @param session
     * @param state
     */
    protected synchronized void updateSessionInfo(String session, String state) {
        AllocationState allocationState = AllocationState.getNone();
        switch (state) {
            case "success":
                allocationState = AllocationState.getSuccess();
                break;
            case "presenting":
                allocationState = AllocationState.getPresenting();
                break;
            case "none":
                allocationState = AllocationState.getNone();
                break;
            case "failure":
                allocationState = AllocationState.getError("error N/A"); //TODO impl: transfer error cause
                break;
            case "init":
                allocationState = AllocationState.getInit();
                break;
            default:
                String msg = String.format("unhandled state from remote: %s. assuming error", state);
                log.warn(msg);
                allocationState = AllocationState.getError(msg);
        }
        stateMap.put(session, allocationState);
    }

    protected Map<String, Object> createContent(Entity output, Set<Entity> services) {
        Map<String, String> contentUnit = new HashMap<>();
        if (OutputFactory.TextOutputScheme.matches(output)) {
            contentUnit.put("type", "text");
            contentUnit.put("content", output.get(Ontology.utterance).get());
        } else if (OutputFactory.ImageOutputScheme.matches(output)) {
            contentUnit.put("type", "img");
            contentUnit.put("content", output.get(Ontology.uri).get().toString());
        } else {
            throw new IllegalArgumentException("unsupported output for voapp: " + output);
        }

        Map<String, Object> content = new HashMap<>();
        for(Entity service : services) {
            String id = service.get(Ontology.id).get();
            List<Object> contentUnits = new ArrayList<>();
            contentUnits.add(contentUnit);
            content.put(id, contentUnits);
        }

        return content;
    }

    protected String allocateFreshId() {
        while (true) {
            String allocationId = UUID.randomUUID().toString().substring(0, 8);
            synchronized (this) {
                if (stateMap.containsKey(allocationId)) {
                    continue;
                }

                stateMap.put(allocationId, AllocationState.getNone());
                return allocationId;
            }
        }
    }

    @Override
    public AllocationState getAllocationState(String id) {
        synchronized (this) {
            if (!stateMap.containsKey(id)) {
                return AllocationState.getNone();
            }
            return stateMap.get(id);
        }
    }

    @Override
    public boolean supports(Entity output, Entity service) {
        if (!serviceScheme.matches(service)) {
            return false;
        }

        if (!OutputFactory.ImageOutputScheme.matches(output) &&
                !OutputFactory.TextOutputScheme.matches(output)) {
            return false;
        }
        return true;
    }


    protected void onPresentationInfo(String allocationId, String rsp) {
        //TODO
    }

    protected void onPresentationInfo(String allocationId, Throwable ex) {
        //TODO
        log.warn("could not present visual content. id={} error={}", allocationId, ex.getMessage());
    }


    protected Mono<String> pollDisplaysAsync(Duration timeout) {
        return WebClient.create(String.format("http://%s:%d/displays/%d", host, port, displaysHash))
                .method(HttpMethod.GET)
                .header("Prefer", "wait=" + timeout.dividedBy(2).toMillis())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(timeout)
                .doOnError(ex -> onDisplayInfo(ex))
                .doOnSuccess(r -> onDisplayInfo(r))
                .doFinally(st -> pollDisplaysAsync(timeout));
    }

    protected void onDisplayInfo(String rsp) {
        try {
            //Write displays into KB
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(rsp);
            KnowledgeMap km = kb.getKnowledgeMap(Ontology.Service);

            List<Entity> displayServices = new ArrayList<>();
            for (JsonNode displayNode : jsonNode.get("displays")) {
                String id = displayNode.get("id").textValue();
                Entity service = new Entity()
                        .set(Ontology.id, id)
//                        .set(Ontology.uri, URI.create("http://172.16.59.0:60000"))
                        .set(Ontology.type2, Ontology.Service)
                        .set(Ontology.service, serviceType)
                        .set(Ontology.timestamp, System.currentTimeMillis());
                displayServices.add(service);
            }

            //update KB
            for (Entity displayService : displayServices) {
                km.add(displayService);
            }

            //find old display services and remove them
            Ontology.Scheme scheme = Ontology.AbsScheme.builder().equal(Ontology.service, serviceType).build();
            km.getStream()
                    .filter(x -> scheme.matches(x))
                    .filter(x -> !displayServices.contains(x))
                    .forEach(s -> km.remove(s.get(Ontology.id).get()));


            this.displaysHash = jsonNode.get("hash").asInt();

            //initiate request
            pollDisplaysAsync(reqTimeout).subscribe();
        } catch (IOException e) {
            log.warn("could not parse display rsp.  error={} got={}", e.getMessage(), rsp);
            onDisplayInfo(new Exception(e.getMessage() + " rsp=" + rsp));
        }
    }

    protected void onDisplayInfo(Throwable ex) {
        log.warn("could not parse display rsp.  error={}", ex.getMessage());
        //remove displays from KB
        Ontology.Scheme scheme = Ontology.AbsScheme.builder().equal(Ontology.service, serviceType).build();
        kb.getKnowledgeMap(Ontology.Service).removeIf(x -> scheme.matches(x));
        //TODO add ongoing presentations
        //initiate request with a certain delay
        pollDisplaysAsync(reqTimeout).delaySubscription(Duration.ofMillis(1000)).subscribe();
    }

    protected void updateAllocationState() {
        //compare presentations with services in the kb
        //TODO
    }


    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @SpringBootApplication
    public static class App implements ApplicationRunner {
        @Override
        public void run(ApplicationArguments args) throws Exception {
            KnowledgeBase kb = new KnowledgeBase();

            VOAppClient client = new VOAppClient(kb);

            Imp imp = new Imp(kb);
            imp.addOutputComponent(client);
            KnowledgeMap userKm = kb.getKnowledgeMap(Ontology.Agent);
            final Entity m1 = new Entity().set(Ontology.id, "m1");
            userKm.add(m1);

            while (true) {
                Ontology.Scheme scheme = Ontology.AbsScheme.builder().equal(Ontology.service, serviceType).build();
                List<Entity> services = kb.getKnowledgeMap(Ontology.Service).getStream()
                        .filter(x -> scheme.matches(x))
                        .collect(Collectors.toList());

                Entity imageOutput = (new OutputFactory()).createImageOutput(new URI("http:/files/sleeping.png"));

                DeviceSelector deviceSelector = new DeviceSelector(imp);

                OutputUnit outputUnit = new OutputUnit(imageOutput, Set.of(m1));
                outputUnit = deviceSelector.process(outputUnit).orElse(null);
                if (outputUnit == null) {
                    System.out.println("no device available");
                    Thread.sleep(500);
                    continue;
                }

                client.allocate(outputUnit);
//                imp.allocate(outputUnit);

//                for (Entity service : services) {
//                    client.allocate(output, service);
//                }

                Thread.sleep(5000);
            }

        }
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(App.class);
    }
}
