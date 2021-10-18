package de.dfki.step.rr.constraints;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import de.dfki.step.rr.constraints.BoundingBox2D.BBBorderType;

public class Border {
	private BBBorderType type;
	private Vector2D boxCenter;
	private Pair<Vector2D, Vector2D> corners;
	private Pair<Line, Line> bounds;
	private Double angleMin;
	private Double angleMax;

	// border of BoundingBox2D (rotated, not axis aligned)
	public Border(BBBorderType type, Vector2D boxCenter, Pair<Vector2D, Vector2D> corners) throws Exception {
		if (type == null || boxCenter == null || corners.getLeft() == null || corners.getRight() == null)
			throw new Exception("Cannot instantiate border with type, BB center or corners being null.");
		this.type = type;
		this.boxCenter = boxCenter;
		this.corners = corners;
	}
	
	public BBBorderType getType() {
		return type;
	}

	public Pair<Vector2D, Vector2D> getCorners() {
		return corners;
	}

	public Line getLine() {
		return new Line(corners.getLeft(), corners.getRight(), 0);
	}

	public Pair<Line, Line> getBounds() {
		if (bounds == null){
			Line line = this.getLine();
			double boundAngle = ProjectiveBinSpatComputer.addAngles(line.getAngle(), Math.toRadians(90.0));
			Line boundLeft = new Line(corners.getLeft(), boundAngle , 0);
			Line boundRight = new Line(corners.getRight(), boundAngle, 0);
			this.bounds = Pair.of(boundLeft, boundRight);
		}
		return bounds;
	}

	public boolean contains(Vector2D point) {
		// check that point lies between the corners of the border
		Vector2D left = corners.getLeft();
		Vector2D right = corners.getRight();
		double x = point.getX();
		double y = point.getY(); 
		double minX = Math.min(left.getX(), right.getX());
		double maxX = Math.max(left.getX(), right.getX());
		double minY = Math.min(left.getY(), right.getY());
		double maxY = Math.max(left.getY(), right.getY());
		return minX <= x && x <= maxX && minY <= y && y <= maxY;
	}
	
}
