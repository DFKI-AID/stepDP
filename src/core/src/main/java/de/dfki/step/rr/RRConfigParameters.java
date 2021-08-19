package de.dfki.step.rr;

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
	public static final double BINSPATREL_C = 1.0;
	// weight of centre point angular deviation for binary spatial relations
	public static final double BINSPATREL_W_CP = 0.009;
	// weight of physical distance for binary spatial relations
	public static final double BINSPATREL_W_PD = 0.045;
	// weight of bounding box angular deviation for binary spatial relations
	public static final double BINSPATREL_W_BB = 0.6;

	// specificity for spatial regions
	public static final double SPATREG_C = 0.0001;
}
