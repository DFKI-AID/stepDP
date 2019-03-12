package de.dfki.tocalog;

import de.dfki.tocalog.core.Event;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.SensorComponent;
import de.dfki.tocalog.kb.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO use Scheme or keep KnowledgeList private (not in KB)
 */
public class KinectComp implements SensorComponent, ReferenceResolver {
    public static final String VisualFocus = "tocalog/kinect/VisualFocus";
    public static final Attribute<String> Source = new Attribute<>("tocalog/kinect/visualSource");
    public static final Attribute<String> Target = new Attribute<>("tocalog/kinect/visualTarget");

    private long focusTimeout = 5000L;
    private KnowledgeList visualFocusKL;
    private String id = "";

    public KinectComp(KnowledgeBase kb) {
        this.visualFocusKL = kb.getKnowledgeList(VisualFocus);
    }

    @Override
    public void process(Event event) {
        //TODO parse network from kinect
    }

    public ReferenceDistribution getReferences() {
        //TODO custom filter function could reduce number of entries
        //TODO use confidence of entries for final confidence; atm only the number of entries is used
        //TODO use timestamp for calculating confidence

        visualFocusKL.removeOld(focusTimeout);
        //collect all targets the agent is 'currently' looking at
        Set<String> targets = visualFocusKL.stream()
                .filter(e -> e.get(Source).orElse("").equals(id))
                .filter(e -> e.get(Target).isPresent())
                .map(e -> e.get(Target).get())
                .collect(Collectors.toSet());


        // count the number entries for each whom
        ReferenceDistribution focusDistribution = new ReferenceDistribution();
        for (String target : targets) {
            long count = visualFocusKL.stream()
                    .filter(e -> e.get(Source).orElse("").equals(id))
                    .filter(e -> e.get(Target).orElse("").equals(target))
                    .count();
            focusDistribution.getConfidences().put(target, (double) count);
        }

        // count the total number of entries and calculate the confidence by dividing the count by the total count
        Optional<Double> totalCount = focusDistribution.getConfidences().values().stream().reduce((d1, d2) -> d1 + d2);
        if (!totalCount.isPresent()) {
            return focusDistribution;
        }
        for (String target : targets) {
            focusDistribution.getConfidences().put(target, focusDistribution.getConfidences().get(target) / totalCount.get());
        }

        return focusDistribution;
    }

    public void setId(String id) {
        this.id = id;
    }
}
