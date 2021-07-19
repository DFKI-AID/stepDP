package de.dfki.step.rr.constraints;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.RRTypes;

public class BinSpatComputer {
	public static final Vector3D SPEAKER_POS_3D = new Vector3D(0, 0, 0);
	public static final Vector2D SPEAKER_POS_2D = new Vector2D(SPEAKER_POS_3D.getX(), SPEAKER_POS_3D.getY());
	private PhysicalObject io;
	private PhysicalObject ro;
	private RRTypes.BinaryRelation rel;
	private Double cp;
	private Double bb;
	private Double pb;
	
	public BinSpatComputer(IKBObject io, IKBObject ro, RRTypes.BinaryRelation rel) {
		this.io = new PhysicalObject(io);
		this.ro = new PhysicalObject(ro);
		this.rel = rel;
	}

	public double computeScore() {
		// TODO: implement
		this.cp = getCP();
		if (this.cp ==  null)
			return 0;
		else
			return 2 * Math.PI - this.cp;
	}

	/**
	 * return Centre Point Angular Deviation (CP)
	 */
	public Double getCP() {
		if (this.cp == null) {
			Vector2D ioPos = this.io.getPosition2D();
			Vector2D roPos = this.ro.getPosition2D();
			if (ioPos == null || roPos == null || ioPos.equals(roPos))
				return null;
			Line speakerAxis = new Line(SPEAKER_POS_2D, roPos, 0);
			Line objectAxis = new Line(roPos, ioPos, 0);
			double protoAngle = speakerAxis.getAngle() + this.rel.getPrototypeAngle();
			Line prototypeAxis = new Line(roPos, protoAngle, 0);
			this.cp = deviation(objectAxis, prototypeAxis);
		}

		return this.cp;
	}

	/**
	 * return Bounding Box Angular Deviation (BB)
	 */
	public double getBB() {
		// TODO: implement
		return 1;
	}

	/**
	 * return Physical Distance (PD)
	 */
	public double getPD() {
		// TODO: implement
		return 1;
	}

	private double deviation(Line x, Line y) {
		double diff = Math.abs(x.getAngle() - y.getAngle());      // This is either the distance or 360 - distance
       return diff > Math.PI ? 2 * Math.PI - diff : diff;
	}

}
