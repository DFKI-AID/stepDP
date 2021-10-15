package de.dfki.step.kb;

import de.dfki.step.kb.RRTypes.Axis;

// TODO: rename to clarify that spatial regions and spatial group relations have the same values?
public enum SpatialRegion {
	// rename (naming convention)
	// TODO: make configurable if left-handed or right-handed coordinate system is used
	left(Axis.X, false), right(Axis.X, true), front(Axis.Z, false), back(Axis.Z, true), top(Axis.Y, true), bottom(Axis.Y, false), middle_left(Axis.X, null), middle_front(Axis.Z, null), middle_top(Axis.Y, null);
	
	private Axis axis;
	private Boolean positive;
	
	SpatialRegion(Axis axis, Boolean positive) {
		this.axis = axis;
		this.positive = positive;
	}

	public Axis getAxis() {
		return this.axis;
	}

	public Boolean positive() {
		return this.positive;
	}

	public boolean isMiddle() {
		return this.equals(middle_front) ||
			   this.equals(middle_left)  ||
			   this.equals(middle_top);
	}

	public static SpatialRegion getRegion(Axis axis, Boolean positive) {
		if (axis == null)
				return null;
		for (SpatialRegion reg : SpatialRegion.values())
			if (reg.axis.equals(axis) && SpatialRegion.positiveMatches(reg, positive))
				return reg;
		return null;
	}
	
	private static boolean positiveMatches(SpatialRegion reg, Boolean positive) {
		return (reg.positive() == null && positive == null) ||
				reg.positive().equals(positive);
	}
}