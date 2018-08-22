package de.dfki.tocalog.kb;

import de.dfki.tocalog.input.SourceWeightFusion;
import de.dfki.tocalog.model.Entity;
import de.dfki.tocalog.model.Person;
import de.dfki.tocalog.model.Triple;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

/**
 */
public class KBTest {

    @Test
    public void limitTest() {
        EKnowledgeSet<Person> kset = new EKnowledgeSet("a");
        try {
            KnowledgeSet.Locked<Person> l = kset.lock();
            for (int i = 0; i < 51; i++) {
                kset.add(Person.create().setId("mechanic" + i).setTimestamp(i * 1000L));
            }
            kset.limitTimestamp(30);

            Assert.assertTrue(l.getSize() == 30);
            Assert.assertFalse(l.getStream().filter(e -> e.getTimestamp().orElse(0l) == 0l).findAny().isPresent());
            Assert.assertTrue(l.getStream().filter(e -> e.getTimestamp().orElse(0l) == 50000l).findAny().isPresent());
            Assert.assertFalse(l.getStream().filter(e -> e.getTimestamp().orElse(0l) == 51000l).findAny().isPresent());
        } finally {
            kset.unlock();
        }
    }

    @Test
    public void weightFusionTest() {
        KnowledgeBase kb = new KnowledgeBase();
        KnowledgeBase.Key<Triple> key = KnowledgeBase.getKey(Triple.class, "focus");
        SourceWeightFusion<Triple> swf = new SourceWeightFusion<>(kb, key);
        swf.setMinConfidence(0.6);
        swf.setWeight("kinect", 5);
        swf.setWeight("hololens", 7);

        EKnowledgeMap km = kb.getKnowledgeMap(key);

        //test filter missing confidence
        km.add(Triple.create().setSource("kinect").setObject("k"));
        km.add(Triple.create().setSource("hololens").setObject("h"));
        Assert.assertFalse(swf.get().isPresent());

        //test setting confidence
        km.add(Triple.create().setSource("kinect").setObject("k").setConfidence(0.5));
        km.add(Triple.create().setSource("hololens").setObject("h").setConfidence(0.8));
        Optional<Triple> result = swf.get();
        Assert.assertTrue(result.isPresent());
        Assert.assertTrue(result.get().getObject().get().equals("h"));
        swf.setMinConfidence(0.2);

        //adapt weight
        swf.setWeight("kinect", 9);
        result = swf.get();
        Assert.assertTrue(result.isPresent());
        Assert.assertTrue(result.get().getObject().get().equals("k"));

    }
}
