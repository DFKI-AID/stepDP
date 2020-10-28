package de.dfki.step.deprecated.kb;

import de.dfki.step.util.Vector3;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class specifies (one part of) our data model.
 */
public class MyDataEntry extends DataEntry {
    public MyDataEntry(DataStore<Object> dataStore, String id) {
        super(dataStore, id);
    }

    public Optional<Vector3> getPosition() {
        return get("pos", Vector3.class);
    }

    public void setPosition(Vector3 pos) {
        set("pos", pos);
    }

    public Optional<Double> getDistance(MyDataEntry dataEntry) {
        return getDistance(dataEntry.getId());
    }

    public Optional<Double> getDistance(String otherId) {
        var pos1 = getPosition();
        var pos2 = getDataView().get(otherId + ".pos", Vector3.class);
        if (!pos1.isPresent() || !pos2.isPresent()) {
            return Optional.empty();
        }
        return pos1.map(x -> x.getDistance(pos2.get()));
    }

    //could also be a distribution instead of a string
    public String getVisualFocus() {
        return get("visual_focus", String.class).orElse("");
    }

    public void setVisualFocus(String focus) {
        set("visual_focus", focus);
    }


    public List<String> getColors() {
        return get("colors", List.class).orElse(Collections.EMPTY_LIST);
    }

    public void setColors(List<String> colors) {
        set("colors", colors);
    }
}
