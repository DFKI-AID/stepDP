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
	public static final float c = 1.0f;
	public static final float w_cp = 0.5f;
	public static final float w_pd = 0.07f;
	
	private PhysicalObject io;
	private PhysicalObject ro;
	private RRTypes.BinSpatRelation rel;
	private Double cp;
	private Double bb;
	private Double pd;
	
	public BinSpatComputer(IKBObject io, IKBObject ro, RRTypes.BinSpatRelation rel) {
		this.io = new PhysicalObject(io);
		this.ro = new PhysicalObject(ro);
		this.rel = rel;
	}

	public double computeScore() {
		// TODO: add BB and distance measures
		cp = getCP();
		pd = getPD();
		if (cp ==  null || pd == null)
			return 0;
		else
			return Math.pow(Math.E, -c * (w_cp * cp * cp + w_pd * pd * pd));
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
			// TODO: check if this is correct
			protoAngle = protoAngle > 2 * Math.PI ? protoAngle - 2 * Math.PI : protoAngle;
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
	public Double getPD() {
		Vector2D ioPos = this.io.getPosition2D();
		Vector2D roPos = this.ro.getPosition2D();
		if (ioPos == null || roPos == null || ioPos.equals(roPos))
			return null;
		return Vector2D.distance(ioPos, roPos);
	}

	private double deviation(Line x, Line y) {
		double diff = Math.abs(x.getAngle() - y.getAngle());      // This is either the distance or 360 - distance
       return diff > Math.PI ? 2 * Math.PI - diff : diff;
	}

}
