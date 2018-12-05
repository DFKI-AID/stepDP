package de.dfki.tocalog.core;

import de.dfki.tocalog.kb.KMHistory;
import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.pcollections.PSet;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

/**
 */
public class KMEventProducer implements KMHistory.Callback, EventProducer {
    private final KMHistory history;
    private PSet<Function<KMHistory.Entry, Optional<Event>>> producers = HashTreePSet.empty();
    private ConcurrentLinkedDeque<Event> events = new ConcurrentLinkedDeque<>();

    public KMEventProducer(KMHistory history) {
        this.history = history;
        this.history.addCallback(this);
    }

    @Override
    public void on(KMHistory.Entry entry) {
        for (Function<KMHistory.Entry, Optional<Event>> producer : producers) {
            Optional<Event> eve = producer.apply(entry);
            if (!eve.isPresent()) {
                continue;
            }
            events.add(eve.get());
        }
    }

    @Override
    public Optional<Event> nextEvent() {
        return Optional.ofNullable(events.poll());
    }

    public synchronized void registerHook(Function<KMHistory.Entry, Optional<Event>> producer) {
        this.producers = this.producers.plus(producer);
    }

    /**
     * Note: the producers might still be used after deregistering if there is currently an ongoing add
     * @param producer
     */
    public synchronized void deregisterHook(Function<KMHistory.Entry, Optional<Event>> producer) {
        this.producers = this.producers.minus(producer);
    }
}
