package de.dfki.tocalog.dialog;

import de.dfki.tocalog.core.*;

import java.util.*;

//TODO create interface
public class MetaDialog {
    private List<DialogComponent> dialogComponents = new ArrayList<>();

    public MetaDialog() {
    }


    public Set<String> on(Hypotheses hypotheses) {
        Set<String> consumed = new HashSet<>();
        for(Hypothesis h : hypotheses.getHypotheses()) {
            for (DialogComponent dc : dialogComponents) {
                Optional<DialogFunction> df = dc.process(h);

                //no real coordination, just execute first DialogFunction
                if(df.isPresent()) {
                    df.get().run();
                    if(df.get().consumesHypothesis()) {
                        consumed.add(h.getId());
                    }
                    break;
                }
            }
        }
        return consumed;
    }

    public void add(DialogComponent dialogComponent) {
        this.dialogComponents.add(dialogComponent);
    }
}
