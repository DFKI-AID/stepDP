package de.dfki.step.rr.constraints;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.RRTypes;

public class PhysicalObject {
	private IKBObject physObj;
	private Vector3D position;

	PhysicalObject(IKBObject physObj) {
		this.physObj = physObj;
		this.position = this.extractPosition(physObj);
	}

	private Vector3D extractPosition(IKBObject obj) {
		IKBObject transform = physObj.getResolvedReference("transform");
		if (transform == null) {
			return null;
		}
		IKBObject position = transform.getResolvedReference("position");
		if (position == null)
			return null;
		double x = position.getFloat("x");
		double y = position.getFloat("z");
		// in unity, y is the height
		double z = position.getFloat("y");
		return new Vector3D(x, y, z);
	}
	
	public Vector3D getPosition3D() {
		return this.position;
	}

	public Vector2D getPosition2D() {
		if (this.position == null)
			return null;
		else
			return new Vector2D(this.position.getX(), this.position.getY());
	}

	public double getPositionOn(RRTypes.Axis axis) {
		switch (axis) {
		case X:
			return this.position.getX();
		case Y:
			return this.position.getY();
		case Z:
			return this.position.getZ();
		default:
			throw new IllegalStateException("not a valid axis");
		}
	}

	public double getX() {
		return this.position.getX();
	}

	public double getY() {
		return this.position.getY();
	}

	public double getZ() {
		return this.position.getZ();
	}
}
