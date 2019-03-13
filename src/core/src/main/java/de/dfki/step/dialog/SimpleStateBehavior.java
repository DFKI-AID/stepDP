package de.dfki.step.dialog;

import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.sc.Parser;
import de.dfki.step.sc.SCEngine;
import de.dfki.step.sc.SCMain;
import de.dfki.step.sc.StateChart;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * TODO the rule activation map should be placed in the scxml file. Maybe if there is an own scxml editor
 */
public class SimpleStateBehavior implements StateBehavior {
    private static final Logger log = LoggerFactory.getLogger(SimpleStateBehavior.class);
    protected Dialog dialog;
    protected RuleSystem rs;
    protected TagSystem<String> tagSystem;
    protected SCHandler stateHandler;
    protected final Supplier<StateChart> scLoader;
    protected final Supplier<Map<String, Set<String>>> ruleLoader;

    /**
     * @param scFile   The scxml file described the state chart
     * @param ruleFile The rule file described in which state which rules are active
     */
    public SimpleStateBehavior(File scFile, File ruleFile) {
        this.scLoader = () -> {
            try {
                StateChart sc = Parser.loadStateChart(scFile);
                return sc;
            } catch (Exception e) {
                log.error("could not load state chart from {}", scFile, e);
                throw new RuntimeException(e);
            }
        };
        this.ruleLoader = () -> {
            try {
                Map<String, Set<String>> ruleActivation = Parser.loadRuleActivationMap(ruleFile);
                return ruleActivation;
            } catch (Exception e) {
                log.error("could not load ruleActivation from {}", ruleFile, e);
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
        if (resourceStr.endsWith(".csv")) {
            resourceStr = resourceStr.substring(0, resourceStr.length() - 4);
        }
        InputStream scStream = SimpleStateBehavior.class.getResourceAsStream(resourceStr + ".scxml");
        InputStream raStream = SimpleStateBehavior.class.getResourceAsStream(resourceStr + ".csv");
        String finalResourceStr = resourceStr;
        this.scLoader = () -> {
            try {
                return Parser.loadStateChart(scStream);
            } catch (Exception e) {
                log.error("could not load state chart from {}", finalResourceStr, e);
                throw new RuntimeException(e);
            }
        };
        this.ruleLoader = () -> {
            try {
                return Parser.loadRuleActivationMap(raStream);
            } catch (Exception e) {
                log.error("could not load ruleActivation from {}", finalResourceStr, e);
                throw new RuntimeException(e);
            }
        };
    }


    @Override
    public void init(Dialog dialog) {
        this.dialog = dialog;
        rs = dialog.getRuleSystem();
        tagSystem = dialog.getTagSystem();

        try {
            //load the state chart
            StateChart sc = scLoader.get();
            SCEngine engine = new SCEngine(sc);
            //add this class as bridge to check for conditions and functions
            engine.addFunctions(this);
            engine.addConditions(this);
            stateHandler = new SCHandler(dialog, engine);

            // Load the rule activation map -> rules are tagged by the state names
            Map<String, Set<String>> ruleActivation = ruleLoader.get();
            for (var entry : ruleActivation.entrySet()) {
                String state = entry.getKey();
                for (String rule : entry.getValue()) {
                    tagSystem.addTag(rule, state);
                }
            }
            stateHandler.init();
        } catch (Exception e) {
            log.error("Could not load state chart: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public void deinit() {
//        this.stateHandler.quit();
        //TODO impl
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

    public Dialog getDialog() {
        return dialog;
    }

    public RuleSystem getRs() {
        return rs;
    }

    public TagSystem<String> getTagSystem() {
        return tagSystem;
    }
}
