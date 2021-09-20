package de.dfki.step.rr.constraints;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.Line;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.RRTypes.Axis;
import de.dfki.step.rr.RRConfigParameters;

/**
 * 
 * Computation of confidences for binary spatial relations based on the following publication:
 * Vivien Mast, Zoe Falomir, and Diedrich Wolter. 2016. Probabilistic reference and grounding with
 * PRAGR for dialogues with robots. Journal of Experimental and Theoretical Artificial Intelligence 28,
 * 5 (2016), 889–911
 *
 */
public class ProjectiveBinSpatComputer {
	private static final double c = RRConfigParameters.BINSPATREL_C;
	private static final double w_cp = RRConfigParameters.BINSPATREL_W_CP;
	private static final double w_bb = RRConfigParameters.BINSPATREL_W_BB;
	private static final double w_pd = RRConfigParameters.BINSPATREL_W_PD;
	
	private PhysicalObject speaker;
	private Vector2D speakerPos;
	private PhysicalObject io;
	private Vector2D ioPos;
	private PhysicalObject ro;
	private Vector2D roPos;
	private RRTypes.BinSpatRelation rel;
	private Double cp;
	private Double bb;
	private Double pd;
	private Double score;
	
	public ProjectiveBinSpatComputer(IKBObject speaker, IKBObject io, IKBObject ro, RRTypes.BinSpatRelation rel) {
		this.speaker = new PhysicalObject(speaker);
		this.io = new PhysicalObject(io);
		this.ro = new PhysicalObject(ro);
		this.rel = rel;
		this.ioPos = this.io.getPosition2D(this.rel.getAbscissa(),this.rel.getOrdinate());
		this.roPos = this.ro.getPosition2D(this.rel.getAbscissa(),this.rel.getOrdinate());
		this.speakerPos = this.speaker.getPosition2D(this.rel.getAbscissa(),this.rel.getOrdinate());
		if (ioPos == null ||roPos == null || speakerPos == null)
			this.score = 0.0;
	}

	public double computeScore() {
		if (score != null)
			return score;
		cp = getCP();
		bb = getBB();
		pd = getPD();
		if (Double.valueOf(0).equals(score) || cp ==  null || bb == null || pd == null)
			return 0;
		else {
			return Math.pow(Math.E, -c * (w_cp * cp * cp + w_bb * bb * bb + w_pd * pd * pd));
		}
	}

	/**
	 * return Centre Point Angular Deviation (CP)
	 */
	public Double getCP() {
		if (this.cp != null) 
			return cp;

		Vector2D speakerPos = this.speaker.getPosition2D(this.rel.getAbscissa(),this.rel.getOrdinate());
		if (ioPos.equals(roPos))
			return null;
		Line speakerAxis = new Line(speakerPos, roPos, 0);
		Line objectAxis = new Line(roPos, ioPos, 0);
		double protoAngle = addAngles(speakerAxis.getAngle(), this.rel.getPrototypeAngle());
		Line prototypeAxis = new Line(roPos, protoAngle, 0);
		this.cp = deviation(objectAxis, prototypeAxis);


		return this.cp;
	}

	private double addAngles(double a1, double a2) {
		double aSum = a1 + a2;
		return aSum >= 2 * Math.PI ? aSum - 2 * Math.PI : aSum;
	}

	/**
	 * return Bounding Box Angular Deviation (BB)
	 */
	public double getBB() {
		if (this.bb != null) 
			return this.bb;

		Border protoBorder = computePrototypeBorder();
		Pair<Line, Line> protoBounds = Pair.of(protoBorder.getBounds().getLeft(), protoBorder.getBounds().getRight());
		Line ioAxisLeft = new Line(protoBorder.getCorners().getLeft(), ioPos, 0);
		Line ioAxisRight = new Line(protoBorder.getCorners().getRight(), ioPos, 0);
		double angleLeft = ioAxisLeft.getAngle() - protoBounds.getLeft().getAngle();
		double angleRight = ioAxisRight.getAngle() - protoBounds.getRight().getAngle();

		// ro lies between the prototype boundaries
		if (angleLeft > Math.toRadians(270) && angleRight < Math.toRadians(90))
			this.bb = 0.0;
		else if (angleLeft < Math.toRadians(270))
			this.bb = deviation(protoBounds.getLeft(), ioAxisLeft);
		else
			this.bb = deviation(protoBounds.getRight(), ioAxisRight);
		if (this.bb > Math.toRadians(90))
			this.score = 0.0;
	
		return this.bb;
	}

	public Border computePrototypeBorder() {
		BoundingBox roBB = this.ro.getBoundingBox();
		Vector2D roCenter = roBB.getCenter2D(this.rel.getAbscissa(), this.rel.getOrdinate());
		Line speakerAxis = new Line(speakerPos, roCenter, 0);
		Line roAbscissa = new Line(roCenter, this.rel.getAbscissa().getValue(io.getRotation()), 0);
		double speakerBBAngle = addAngles(roAbscissa.getAngle(), speakerAxis.getAngle());
		Border behindOfBorder = roBB.getBorder(speakerBBAngle, this.rel.getAbscissa(), this.rel.getOrdinate());
		double protoBorderAngle = addAngles(behindOfBorder.getType().getBorderAngle(), rel.getPrototypeAngle());
		return roBB.getBorder(protoBorderAngle, this.rel.getAbscissa(), this.rel.getOrdinate());

	}

	/**
	 * return Physical Distance (PD)
	 */
	public Double getPD() {
		return Vector2D.distance(ioPos, roPos);
	}

	private double deviation(Line x, Line y) {
		double diff = Math.abs(x.getAngle() - y.getAngle());
       return diff > Math.PI ? 2 * Math.PI - diff : diff;
	}

}
