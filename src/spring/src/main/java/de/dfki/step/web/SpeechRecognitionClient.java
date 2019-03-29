package de.dfki.step.web;

import de.dfki.step.core.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.StandardWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * Opens a websocket connect to the speech-recognition-service a retrieves
 * speech recognition results (json).
 *
 */
public class SpeechRecognitionClient {
    private static Logger log = LoggerFactory.getLogger(SpeechRecognitionClient.class);
    private static Duration timeout = Duration.ofMillis(5000);
    private final URI asrResultUri, grammarUri;
    private final Consumer<Token> callback;
    private String grammar;
    private String grammarName;
    private final String app;

    public SpeechRecognitionClient(String app, String host, int port, Consumer<Token> callback) {
        this.callback = callback;
        asrResultUri = URI.create(String.format("ws://%s:%d/asr", host, port));
        grammarUri = URI.create(String.format("ws://%s:%d/grammar/%s", host, port, app));
        this.app = app;
    }

    public void init() {
        WebSocketClient wsc = new StandardWebSocketClient();

        log.info("Connecting to {}", asrResultUri);

        WebSocketHandler handler = new WebSocketHandler() {
            @Override
            public Mono<Void> handle(WebSocketSession session) {
                var input = session.receive().doOnNext(m -> {
                    String speechRecog = m.getPayloadAsText(StandardCharsets.UTF_8);
                    try {
                        Token t = Token.fromJson(speechRecog);
                        if(!t.payloadEquals("de/dfki/step/app", app)) {
                            // speech recognition from another de.dfki.step.app
                            return;
                        }
                        callback.accept(t);
                    } catch(Exception ex) {
                        log.warn("could not parse response from speech recognition service. json={}", speechRecog, ex);
                        return;
                    }

                }).doOnError(ex -> {
                    System.out.println(ex);
                }).then();

                WebSocketMessage heartbeat = session.textMessage("heartbeat");
                Stream<WebSocketMessage> hearbeatStream =Stream.generate(() -> heartbeat);
                Flux<WebSocketMessage> hearbeatFlux = Flux.fromStream(hearbeatStream)
                        .delayElements(Duration.ofMillis(250));

                var output = session.send(hearbeatFlux)
                        .doOnSuccess(s -> {
                            System.out.println(s);
                        })
                        .doOnError(ex -> {
                            ex.printStackTrace();
                        });

                return Mono.zip(input, output).then();
            }
        };
        Mono<Void> req = wsc.execute(asrResultUri, handler)
                .doOnTerminate(() -> {
                    // "finished means connection lost"
                    log.info("Lost connection to {}", asrResultUri);
                    init();
                });

        req
                .delaySubscription(Duration.ofMillis(1000))
                .subscribe();
    }


    public void initGrammar() {
        String srgs = null;
        String name = null;
        synchronized (this) {
            srgs = this.grammar;
            name = this.grammarName;
        }

        if(srgs == null || name == null) {
            log.error("can't init grammar without a valid name or grammar content. got {} {}", srgs, null);
            throw new IllegalArgumentException("can't init grammar without a valid name or grammar content.");
        }

        log.debug("trying to update grammar for {}", name);
        String finalName = name;
        WebClient.create(grammarUri.toString() + "/" + name)
                .method(HttpMethod.POST)
                .syncBody(srgs)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(ex -> log.warn("error while uploading grammar: {}", ex.getMessage()))
                .doOnSuccess(s -> log.debug("successfully updated grammar for {}: rsp={}", finalName, s))
                .doFinally(x -> initGrammar())
                .delaySubscription(Duration.ofMillis(2000))
                .timeout(timeout)
                .subscribe(s -> {}, e -> {});
    }

    public synchronized void setGrammar(String name, String grammar) {
        this.grammar = grammar;
        this.grammarName = name;
    }

    public static void main(String[] args) throws InterruptedException, IOException {

//        SpeechRecognitionClient client = new SpeechRecognitionClient("localhost", 9696,
//                (s)-> log.info("Retrieved {}", s));
//        client.init();
//
//        synchronized (SpeechRecognitionClient.class) {
//            SpeechRecognitionClient.class.wait();
//        }

        String s = "{\"utterance\":\"the first task\",\"confidence\":0.45802411437034607,\"semantic\":{\"out\":{\"task\":{\"value\":\"first\",\"confidence\":0.9970117},\"taskConfidence\":0.9970117},\"outConfidence\":0.4580241},\"milliseconds\":1553090661189,\"success\":false,\"grammar\":\"task\",\"original\":{\"out\":{\"task\":{}}}}";
        Token t = Token.fromJson(s);
        System.out.println(t);
    }

//    public static class RecResult {
//        private String utterance;
//        private double confidence;
//        private String
//    }
}
