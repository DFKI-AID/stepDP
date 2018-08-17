package de.dfki.tocalog.dialog;

import de.dfki.tocalog.input.Input;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class CIntentProducer implements IntentProducer {
    private List<IntentProducer> intentProducers = new ArrayList<>();
    private Iterator<IntentProducer> iter;

    @Override
    public void add(Input input) {
        for(IntentProducer ip : intentProducers) {
            ip.add(input);
        }
    }

    @Override
    public Optional<Intent> getIntent() {
        if(iter == null || !iter.hasNext()) {
            iter = intentProducers.iterator();
        }

        while(iter.hasNext()) {
            Optional<Intent> intent = iter.next().getIntent();
            if(intent.isPresent()) {
                return intent;
            }
        }

        return Optional.empty();
    }

    public void add(IntentProducer producer) {
        this.intentProducers.add(producer);
    }
}
