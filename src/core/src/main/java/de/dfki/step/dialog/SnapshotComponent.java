package de.dfki.step.dialog;

import de.dfki.step.core.Component;
import de.dfki.step.core.ComponentManager;
import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.util.Clock;
import de.dfki.step.util.ClockComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SnapshotComponent implements Component {
    protected final Map<Component, Map<Long, Object>> snapshots = new HashMap<>();
    private ClockComponent cc;
    protected final AtomicLong snapshotTarget = new AtomicLong(-1);
    private ComponentManager cm;

    @Override
    public void init(ComponentManager cm) {
        cc = cm.getComponent("clock", ClockComponent.class).get();
        this.cm = cm;
        createSnapshot(0);
    }

    @Override
    public void deinit() {
    }

    @Override
    public void update() {
        applySnapshot();
    }

    @Override
    public void afterUpdate() {
        createSnapshot(cc.getClock().getIteration());
    }

    @Override
    public Object createSnapshot() {
        //atm no snapshot is created
        //after rewind the old snapshot are still available. however they will be overwritten
        return null;
    }

    @Override
    public void loadSnapshot(Object snapshot) {
    }

    protected void applySnapshot() {
        long targetSnapshot = snapshotTarget.getAndSet(-1);
        if (targetSnapshot < 0) {
            return;
        }

        List<Component> components = cm.getComponents(Component.class);

        for (Component comp : components) {
            var snapshot = snapshots.get(comp).get(targetSnapshot);
            comp.loadSnapshot(snapshot);
        }
    }

    protected void createSnapshot(long iteration) {
        List<Component> components = cm.getComponents(Component.class);


        for (Component comp : components) {
            Object snapshot = comp.createSnapshot();
            if (!snapshots.containsKey(comp)) {
                snapshots.put(comp, new HashMap<>());
            }
            snapshots.get(comp).put(iteration, snapshot);
        }
    }

    public void rewind(long iteraton) {
        this.snapshotTarget.set(iteraton);
    }
}
