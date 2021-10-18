package de.dfki.step.rr;

import java.util.HashMap;
import java.util.Map;

import de.dfki.step.kb.SpatialRegion;

/**
 * Parameters used by the reference resolution algorithm. The optimum values
 * of these parameters might depend on the scenario. 
 * For more information about the parameters concerning binary spatial relations
 * and spatial regions, refer to the publication that the concerning computations
 * are based on:
 * Vivien Mast, Zoe Falomir, and Diedrich Wolter. 2016. Probabilistic reference and grounding with
 * PRAGR for dialogues with robots. Journal of Experimental and Theoretical Artificial Intelligence 28,
 * 5 (2016), 889–911
 */
public class RRConfigParameters {
	// specificity for binary spatial relations
	public double BINSPATREL_C = 1.0;
	// weight of centre point angular deviation for binary spatial relations
	public double BINSPATREL_W_CP = 0.009;
	// weight of physical distance for binary spatial relations
	public double BINSPATREL_W_PD = 0.1;
	// weight of bounding box angular deviation for binary spatial relations
	public double BINSPATREL_W_BB = 0.6;

	// specificity for spatial regions
	public double SPATREG_C = 1;

	// objects are considered visible if the percentage of visible pixels exceeds this threshold
	public float VISIBILITY_THRESHOLD = 0.5f;

	// which direction should be default when its not specified in a group relation, e.g. the second shelf
	public SpatialRegion DEFAULT_DIR = SpatialRegion.left;
	// exceptions (type name is case sensitive!; does not work yet with inheritance)
	public Map<String, SpatialRegion> DIR_EXCEPTIONS = new HashMap<String, SpatialRegion>(Map.of("ShelfLevel", SpatialRegion.bottom));
}
