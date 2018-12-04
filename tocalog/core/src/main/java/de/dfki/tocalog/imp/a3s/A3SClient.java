package de.dfki.tocalog.imp.a3s;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dfki.tocalog.kb.*;
import de.dfki.tocalog.output.OutputComponent;
import de.dfki.tocalog.output.OutputFactory;
import de.dfki.tocalog.output.impp.AllocationState;
import org.pcollections.HashPMap;
import org.pcollections.IntTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 */
public class A3SClient implements OutputComponent {
    private static final Logger log = LoggerFactory.getLogger(A3SClient.class);
    private static final String serviceType = "a3s-playback";
    private static final Duration reqTimeout = Duration.ofMillis(4000);
    private static final Duration reqInterval = Duration.ofMillis(1000);
    private static final Duration pollTimeout = Duration.ofMillis(8000);
    private final KnowledgeMap km;
    private String host = "localhost";
    private int port = 50000;
    private PMap<String, AllocationState> allocationStates = HashPMap.empty(IntTreePMap.empty());
    private PMap<String, AllocationState> oldAllocationStates = HashPMap.empty(IntTreePMap.empty());


    public A3SClient(KnowledgeBase kb) {
        this.km = kb.getKnowledgeMap(Ontology.Service);
        //TODO fixed entities. maybe load from config
        Entity service1 = new Entity()
                .set(Ontology.id, "p1")
                .set(Ontology.uri, URI.create("http://localhost:60000"))
                .set(Ontology.type2, Ontology.Service)
                .set(Ontology.service, serviceType)
                .set(Ontology.timestamp, 0l);

        km.add(service1);

        for (Entity entity : km.getAll()) {
            updatePlayerState(entity.get(Ontology.id).get());
        }
    }


    @Override
    public String allocate(Entity output, Entity service) {
        log.info("allocating {} on {}", output, service);
        //TODO multiple services
        String id = "s1"; // for debugging UUID.randomUUID().toString().substring(0, 8);
        synchronized (this) {
            allocationStates = allocationStates.plus(id, AllocationState.getInit());
        }

        if (!service.get(Ontology.id).isPresent()) {
            String errMsg = MessageFormatter.format("Can't output {} on {}. No id found in service.",
                    output, service).getMessage();
            log.warn(errMsg);
            synchronized (this) {
                allocationStates = allocationStates.plus(id, AllocationState.getError(errMsg));
            }
        }
//TODO use output
        createAudioSession(id, service.get(Ontology.id).get());
        return id;
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
    public boolean handles(Entity output, Entity service) {
        return false;
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
//            JsonNode state = jsonNode.get("state");
//            if (Objects.equals("failed", state.asText("failed"))) {
//                onPlayerInfo(id, new Exception("a3s connection failed"));
//                return;
//            }

            km.update(id, Ontology.timestamp, System.currentTimeMillis());
            //TODO write service information into KB
        } catch (Exception e) {
            e.printStackTrace();
            onPlayerInfo(id, e);
        }
    }

    protected void onPlayerInfo(String id, Throwable ex) {
        //tag as N/A in KB:
        km.unset(id, Ontology.timestamp);

        //TODO: maybe remove from a3s service?
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
        //TODO
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


    protected void createAudioSession(String session, String... players) {
        List<Mono<String>> monos = new ArrayList<>();
        for (String player : players) {
            monos.add(deletePlayer(player));
        }

        for (String player : players) {
            monos.add(addPlayer(player, "172.16.59.0", 60000)); //TODO
        }

        monos.add(deleteSession(session));

        //TODO content
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> audio = new HashMap<>();
        audio.put("type", "file");
        audio.put("path", "hello");
        body.put("audio", audio);
        body.put("connections", Arrays.asList(players));
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
        if(getAllocationState(id).finished()) {
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
            if(error instanceof TimeoutException) {
                as = AllocationState.getTimeout();
            } else {
                as = AllocationState.getError(errMsg);
            }

            allocationStates = allocationStates.plus(id, as);
        }
        //TODO types like timeout
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

    @SpringBootApplication
    public static class App implements ApplicationRunner {

        @Override
        public void run(ApplicationArguments args) throws Exception {
            KnowledgeBase kb = new KnowledgeBase();
            KnowledgeMap km = kb.getKnowledgeMap(Ontology.Service);
            A3SClient client = new A3SClient(kb);


            Entity speechOutput = new OutputFactory().createSpeechOutput("hello world");
            String allocationId = client.allocate(speechOutput, km.get("p1").get());


            AllocationState as = AllocationState.getNone();
            while (true) {
                synchronized (client.getClass()) {
                    client.getClass().wait(500);
                }
//                String s = client.km.getAll().stream().map(e -> e.toString())
//                        .reduce("", (x, y) -> x + " " + y);
//                System.out.println(s);

                AllocationState currentState = client.getAllocationState(allocationId);
                if(currentState != as) {
                    as = currentState;
                    System.out.println(allocationId + " " + as);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(App.class);
    }


}
