package de.dfki.step.kb;

import de.dfki.step.util.Vector3;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class DataStoreTest {


    @Test
    public void testAsync() {
        DataStore ds = new DataStore();
        ds.checkMutability(true);
        MyDataEntry human1 = new MyDataEntry(ds, "w1");

        MyDataEntry human1OtherThread = new MyDataEntry(ds, "w1");
        human1OtherThread.setVisualFocus("car");
        human1OtherThread.save();

        human1.setPosition(new Vector3(0, 0, 0));
        human1.save();

        Assert.assertEquals(human1.getVisualFocus(), "car");
        Assert.assertTrue(!human1OtherThread.getPosition().isPresent());
        human1OtherThread.reload();
        Assert.assertEquals(human1OtherThread.getPosition().get(), new Vector3(0, 0, 0));
    }

    @Test
    public void testDistance() {
        DataStore ds = new DataStore();
        MyDataEntry human1 = new MyDataEntry(ds, "worker1");

        human1.setPosition(new Vector3(0, 0, 0));
        human1.save();

        Assert.assertEquals(human1.getPosition().get(), new Vector3(0, 0, 0));

        human1 = new MyDataEntry(ds, "worker1");
        Assert.assertEquals(human1.getPosition().get(), new Vector3(0, 0, 0));


        MyDataEntry robot1 = new MyDataEntry(ds, "mir100-1");
        robot1.setPosition(new Vector3(100, 0, 0));
        robot1.save();

        double distance = robot1.getDistance(human1).get();
        Assert.assertEquals(100, (int) distance);
    }

    @Test
    public void list() {
        DataStore<Object> ds = new DataStore();
        MyDataEntry box1 = new MyDataEntry(ds, "box1");
        box1.setColors(List.of("red", "blue"));
        box1.save();

        MyDataEntry box2 = new MyDataEntry(ds, "box2");
        box2.setColors(List.of("red", "green"));
        box2.save();
//        box2.set("colors.confidence", 0.4);


        List<DataEntry> candidates = ds.primaryIds()
//                .filter(e -> e.getKey().matches(".*\\.colors"))
                .map(id -> new MyDataEntry(ds, id))
                .filter(d -> d.getColors().contains("red"))
                .collect(Collectors.toList());

        Assert.assertEquals(candidates.size(), 2);
    }
}
