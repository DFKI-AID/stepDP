package de.dfki.dialog;

import de.dfki.rengine.RuleSystem;
import de.dfki.sc.Parser;
import de.dfki.sc.SCEngine;
import de.dfki.sc.SCMain;
import de.dfki.sc.StateChart;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * TODO the rule activation map should be placed in the scxml file. Maybe if there is an own scxml editor
 */
public class SimpleStateBehavior implements StateBehavior {
    private static final Logger log = LoggerFactory.getLogger(SimpleStateBehavior.class);
    private final File scFile;
    private final File ruleFile;
    protected Dialog dialog;
    protected RuleSystem rs;
    protected TagSystem<String> tagSystem;
    protected SCHandler stateHandler;

    /**
     * @param scFile   The scxml file described the state chart
     * @param ruleFile The rule file described in which state which rules are active
     */
    public SimpleStateBehavior(File scFile, File ruleFile) {
        this.scFile = scFile;
        this.ruleFile = ruleFile;
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
        URL scxmlResource = SCMain.class.getResource(resourceStr + ".scxml");
        URL csvResource = SCMain.class.getResource(resourceStr + ".csv");
        this.scFile = new File(scxmlResource.toURI());
        this.ruleFile = new File(csvResource.toURI());
    }


    @Override
    public void init(Dialog dialog) {
        this.dialog = dialog;
        rs = dialog.getRuleSystem();
        tagSystem = dialog.getTagSystem();

        URL resource = SCMain.class.getResource("/sc/task_behavior.scxml");
        try {
            //load the state chart
            StateChart sc = Parser.loadStateChart(resource);
            SCEngine engine = new SCEngine(sc);
            //add this class as bridge to check for conditions and functions
            engine.addFunctions(this);
            engine.addConditions(this);
            stateHandler = new SCHandler(dialog, engine);

            // Load the rule activation map -> rules are tagged by the state names
            Map<String, Set<String>> ruleActivation = Parser.loadRuleActivationMap(this.ruleFile);
            for (var entry : ruleActivation.entrySet()) {
                String state = entry.getKey();
                for (String rule : entry.getValue()) {
                    tagSystem.addTag(rule, state);
                }
            }
            stateHandler.init();
        } catch (Exception e) {
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
