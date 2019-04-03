package de.dfki.step.sc;

import de.dfki.step.core.ComponentManager;
import de.dfki.step.core.TagSystem;
import de.dfki.step.core.TagSystemComponent;
import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.rengine.RuleSystemComponent;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 */
public abstract class SimpleStateBehavior implements StateBehavior {
    private static final Logger log = LoggerFactory.getLogger(SimpleStateBehavior.class);
    protected ComponentManager cm;
    protected RuleSystem rs;
    protected TagSystem<String> tagSystem;
    protected SCHandler stateHandler;
    protected final Supplier<StateChart> scLoader;

    /**
     * @param scFile   The scxml file described the state chart
     */
    public SimpleStateBehavior(File scFile) {
        this.scLoader = () -> {
            try {
                StateChart sc = Parser.loadStateChart(scFile);
                return sc;
            } catch (Exception e) {
                log.error("could not load state chart from {}", scFile, e);
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Looks into the resources folder (src/main/resources/YOUR_PATH/YOUR_FILE) for two files with the
     * suffix *.csv and *.scxml
     *
     * @param resourceStr
     */
    public SimpleStateBehavior(String resourceStr) throws URISyntaxException {
        if (resourceStr.endsWith(".scxml")) {
            resourceStr = resourceStr.substring(0, resourceStr.length() - 6);
        }
        InputStream scStream = SimpleStateBehavior.class.getResourceAsStream(resourceStr + ".scxml");
        String finalResourceStr = resourceStr;
        this.scLoader = () -> {
            try {
                return Parser.loadStateChart(scStream);
            } catch (Exception e) {
                log.error("could not load state chart from {}", finalResourceStr, e);
                throw new RuntimeException(e);
            }
        };
    }


    @Override
    public void init(ComponentManager cm) {
        this.cm = cm;
        rs = cm.retrieveComponent(RuleSystemComponent.class).getRuleSystem();
        tagSystem = cm.retrieveComponent(TagSystemComponent.class);

        try {
            //load the state chart
            StateChart sc = scLoader.get();
            SCEngine engine = new SCEngine(sc);
            //add this class as bridge to check for conditions and functions
            engine.addFunctions(this);
            engine.addConditions(this);
            stateHandler = new SCHandler(rs, tagSystem, engine);

            // Load the rule activation map -> rules are tagged by the state names
            stateHandler.init();
            updateActiveRules();
        } catch (Exception e) {
            log.error("Could not load state chart: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected void updateActiveRules() {
        Collection<String> states = stateHandler.getStates();
        for (String state : states) {
            Set<String> rules = getActiveRules(state);
            for (String rule : rules) {
                tagSystem.addTag(rule, state);
            }
        }

    }


    @Override
    public void deinit() {
//        this.stateHandler.quit();
        //TODO impl
    }

    @Override
    public void update() {
        updateActiveRules();
    }

    @Override
    public PMap<String, Object> createSnapshot() {
        PMap<String, Object> snapshot = HashTreePMap.empty();
        snapshot = snapshot.plus("state_handler", stateHandler.createSnapshot());
        return snapshot;
    }

    @Override
    public void loadSnapshot(Object snapshot) {
        var snapshotMap = (Map<String, Object>) snapshot;
        try {
            Object shSnapshot = snapshotMap.get("state_handler");
            stateHandler.loadSnapshot((SCEngine.ObjState) shSnapshot);
        } catch (Exception ex) {
            throw new RuntimeException("could not reload snapshot", ex);
        }
    }

    @Override
    public SCHandler getStateHandler() {
        return stateHandler;
    }

    public ComponentManager getComponentManager() {
        return cm;
    }

    public RuleSystem getRs() {
        return rs;
    }

    public TagSystem<String> getTagSystem() {
        return tagSystem;
    }

    /**
     * Get the names of the rules that should be active in the given state
     *
     * @param state
     * @return
     */
    public abstract Set<String> getActiveRules(String state);
}
