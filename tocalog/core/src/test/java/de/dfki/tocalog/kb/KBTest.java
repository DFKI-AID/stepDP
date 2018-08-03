package de.dfki.tocalog.kb;

import de.dfki.tocalog.model.Entity;
import de.dfki.tocalog.model.Person;
import org.junit.Assert;
import org.junit.Test;

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
}
