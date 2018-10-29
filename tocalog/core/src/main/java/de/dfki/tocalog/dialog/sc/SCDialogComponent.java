package de.dfki.tocalog.dialog.sc;

import de.dfki.tocalog.core.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 */
public class SCDialogComponent implements DialogComponent {
    private final StateChart sc;
    private LHypothesisProducer hp;
    private final double minConfidence = 0.5;

    public SCDialogComponent(StateChart sc, HypothesisProducer... hps) {
        this.sc = sc;
        this.hp = new LHypothesisProducer(hps);
    }

    @Override
    public Optional<DialogFunction> process(Inputs inputs) {
        List<Hypothesis> hypotheses = hp.process(inputs);
        if (hypotheses.isEmpty()) {
            return Optional.empty();
        }

        List<Hypothesis> sortedHypos = hypotheses.stream()
                .filter(h -> h.getConfidence().getConfidence() > minConfidence)
                .sorted((h1, h2) -> Confidence.getComparator().compare(h1.getConfidence(), h2.getConfidence()))
                .collect(Collectors.toList());

        //TODO check if multiple hypos have high confidence

        for (Hypothesis h : sortedHypos) {
            if (sc.canUpdate(h.getIntent())) {
                //TODO origin does not work if multiple SCDialogComponent are used
                //TODO entities are missing
                return Optional.of(new AbsDialogFunction(this, inputs, h.getInputs()) {
                    @Override
                    public void run() {
                        sc.update(h.getIntent());
                    }
                });
            }
        }

        return Optional.empty();
    }
}
