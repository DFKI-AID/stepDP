package de.dfki.step.rr.constraints;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import de.dfki.step.rr.constraints.BoundingBox.BBElementType;

public class Border {
	private BBElementType type;
	private Vector2D boxCenter;
	private Pair<Vector2D, Vector2D> corners;
	private Pair<Line, Line> bounds;
	private Double angleMin;
	private Double angleMax;

	public Border(BBElementType type, Vector2D boxCenter, Pair<Vector2D, Vector2D> corners) throws Exception {
		if (type == null || boxCenter == null || corners.getLeft() == null || corners.getRight() == null)
			throw new Exception("Cannot instantiate border with type, BB center or corners being null.");
		this.type = type;
		this.boxCenter = boxCenter;
		this.corners = corners;
	}
	
	public BBElementType getType() {
		return type;
	}

	public Pair<Vector2D, Vector2D> getCorners() {
		return corners;
	}

	public Pair<Line, Line> getBounds() {
		if (bounds == null){
			Line boundLeft = new Line(corners.getLeft(), type.getBorderAngle(), 0);
			Line boundRight = new Line(corners.getRight(), type.getBorderAngle(), 0);
			this.bounds = Pair.of(boundLeft, boundRight);
		}
		return bounds;
	}

	public double getAngleMin() {
		if (angleMin == null) {
			computeAngles();
		}
		return angleMin;
	}

	public boolean contains(double angle) {
		if (getAngleMin() <= getAngleMax())
			return getAngleMin() <= angle && getAngleMax() > angle;
		else
			return getAngleMin() <= angle || getAngleMax() > angle;
	}

	private void computeAngles() {
		Line centerCorner1 = new Line(boxCenter, corners.getLeft(), 0);
		Line centerCorner2 = new Line(boxCenter, corners.getRight(), 0);
		// FIXME: consider rotation of the object with regard to abscissa
		// - or is extents relative to local coordinate system??
		double angle1 = centerCorner1.getAngle();
		double angle2 = centerCorner2.getAngle();
		// FIXME: how to do this in a smart way and avoid this corner case?
		if (angle1 >= Math.toRadians(0) && angle1 < Math.toRadians(90) &&
			angle2 >= Math.toRadians(270) && angle2 < Math.toRadians(360)) {
			angleMax = angle1;
			angleMin = angle2;
		}
		else if (angle2 >= Math.toRadians(0) && angle2 < Math.toRadians(90) &&
				 angle1 >= Math.toRadians(270) && angle1 < Math.toRadians(360)) {
			angleMax = angle2;
			angleMin = angle1;
		}
		else if (angle1 < angle2) {
			angleMin = angle1;
			angleMax = angle2;
		} else {
			angleMin = angle2;
			angleMax = angle1;
		}
	}

	public double getAngleMax() {
		if (angleMax == null){
			computeAngles();
		}
		return angleMax;
	}
	
}
