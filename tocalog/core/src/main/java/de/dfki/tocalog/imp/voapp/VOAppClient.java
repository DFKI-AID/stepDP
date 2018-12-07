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
    public String allocate(Entity outputUnit) {
        String allocationId = allocateFreshId();
        KnowledgeMap km = this.kb.getKnowledgeMap(Ontology.Service);


        if (!DeviceSelector.unitScheme.matches(outputUnit)) {
            synchronized (this) {
                stateMap.put(allocationId, AllocationState.getError(String.format("invalid output unit %s for voapp", outputUnit)));
                return allocationId;
            }
        }

        Set<Entity> services = outputUnit.get(DeviceSelector.where).get();
        Entity output = outputUnit.get(DeviceSelector.what).get();

        for (Entity service : services) {
            if (!serviceScheme.matches(service)) {
                synchronized (this) {
                    stateMap.put(allocationId, AllocationState.getError(String.format("invalid service %s for voapp", service)));
                    return allocationId;
                }
            }
        }

        Ontology.Scheme outputScheme = Ontology.AbsScheme.builder()
                .equal(Ontology.modality, "image")
                .present(Ontology.uri)
                .build();
        if (!outputScheme.matches(output)) {
            synchronized (this) {
                stateMap.put(allocationId, AllocationState.getError(String.format("invalid output %s for voapp", output)));
                return allocationId;
            }
        }

        //TODO multiple contents
        Map<String, Object> payload = new HashMap<>();
        Map<String, String> content1 = new HashMap<>();
        content1.put("type", "img");
        content1.put("content", output.get(Ontology.uri).get().toString());
        payload.put("content", List.of(content1));
        payload.put("area", "\"n0\"");
        payload.put("cols", "100%");
        payload.put("rows", "100%");

        for (Entity service : services) {
            Mono<String> req = WebClient.create(String.format("http://%s:%d/display/%s/content",
                    host, port, service.get(Ontology.id).get()))
                    .method(HttpMethod.POST)
                    .body(BodyInserters.fromObject(payload))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(reqTimeout)
                    .doOnError(ex -> onPresentationInfo(allocationId, ex))
                    .doOnSuccess(r -> onPresentationInfo(allocationId, r));
            req.subscribe();
        }

        return allocationId;
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

        if (!OutputFactory.ImageOutputScheme.matches(output)) {
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
                Entity outputUnit = deviceSelector.process(new Entity()
                        .set(DeviceSelector.what, imageOutput)
                        .set(DeviceSelector.whom, Set.of(m1))
                ).orElse(null);
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
