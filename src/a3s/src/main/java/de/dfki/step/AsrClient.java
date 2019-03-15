package de.dfki.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.StandardWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Stream;


/**
 * Opens a websocket connect to the speech-recognition-service a retrieves
 * speech recognition results (json).
 *
 * Overwrite {@link AsrClient#onSpeechRecognition}
 */
public class AsrClient {
    private static Logger log = LoggerFactory.getLogger(AsrClient.class);
    private static Duration timeout = Duration.ofMillis(5000);
    private final URI uri;

    public AsrClient(String host, int port) {
        uri = URI.create(String.format("ws://%s:%d/asr", host, port));
    }

    public void init() {
        WebSocketClient wsc = new StandardWebSocketClient();

        log.info("Connecting to {}", uri);

        WebSocketHandler handler = new WebSocketHandler() {
            @Override
            public Mono<Void> handle(WebSocketSession session) {
                var input = session.receive().doOnNext(m -> {
                    String speechRecog = m.getPayloadAsText(StandardCharsets.UTF_8);
                    onSpeechRecognition(speechRecog);
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
        Mono<Void> req = wsc.execute(uri, handler)
                .doOnTerminate(() -> {
                    // "finished means connection lost"
                    log.info("Lost conenction to {}", uri);
                    init();
                });

        req
                .delaySubscription(Duration.ofMillis(1000))
                .subscribe();
    }

    protected void onSpeechRecognition(String json) {
        log.info("Retrieved {}", json);
    }


    public static void main(String[] args) throws InterruptedException {

        AsrClient client = new AsrClient("localhost", 9696);
        client.init();

        synchronized (AsrClient.class) {
            AsrClient.class.wait();
        }
    }
}
