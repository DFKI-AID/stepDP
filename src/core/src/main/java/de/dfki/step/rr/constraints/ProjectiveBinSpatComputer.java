package de.dfki.step.rr.constraints;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.Line;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.RRTypes.Axis;
import de.dfki.step.rr.RRConfigParameters;
import de.dfki.step.rr.constraints.BoundingBox2D.BoundingBoxType;
import de.dfki.step.util.LogUtils;

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
    private static final Logger log = LoggerFactory.getLogger(ProjectiveBinSpatComputer.class);
	
	private PhysicalObject speaker;
	private Vector2D speakerPos;
	private PhysicalObject io;
	private PhysicalObject ro;
	private RRTypes.BinSpatRelation rel;
	private Double score;
	
	public ProjectiveBinSpatComputer(IKBObject speaker, IKBObject io, IKBObject ro, RRTypes.BinSpatRelation rel) {
		this.speaker = new PhysicalObject(speaker);
		// FIXME: should probably not instantiate a new PhysObj for the same object multiple times
		this.io = new PhysicalObject(io);
		this.ro = new PhysicalObject(ro);
		this.rel = rel;

	}

	public double computeScore() {
		if (score != null)
			return score;
		String ioName = this.io.getName();
		String roName = this.ro.getName();
		log.trace("Scoring " + ioName + " " + this.rel.name() + " " + roName);
		Double totalCP = Double.valueOf(0);
		Double totalBB = Double.valueOf(0);;
		Double totalPD = Double.valueOf(0);;
		for (Pair<Axis, Axis> plane : this.rel.getPlanes()) {
			Vector2D ioPos = io.getPosition2D(plane.getLeft(), plane.getRight());
			Vector2D roPos = ro.getPosition2D(plane.getLeft(), plane.getRight());
			Vector2D speakerPos = speaker.getPosition2D(plane.getLeft(), plane.getRight());
			if (ioPos == null ||roPos == null || speakerPos == null)
				return 0;
			Double curCP = getCP(plane, ioPos, roPos, speakerPos);
			Double curBB = getBB(plane, ioPos, roPos, speakerPos);
			Double curPD = getPD(plane, ioPos, roPos, speakerPos);
			if (totalCP ==  null || totalBB == null || totalPD == null)
				return 0;
			totalCP += curCP;
			totalBB += curBB;
			totalPD += curPD;
		}

		if (Double.valueOf(0).equals(score))
			return 0;
		else {
			return Math.pow(Math.E, -c * (w_cp * totalCP * totalCP + w_bb * totalBB * totalBB + w_pd * totalPD * totalPD));
		}
	}

	/**
	 * return Centre Point Angular Deviation (CP)
	 */
	public Double getCP(Pair<Axis,Axis> plane, Vector2D ioPos, Vector2D roPos, Vector2D speakerPos) {
		// FIXME: is this correct?
		if (ioPos.equals(roPos))
			return null;
		double protoAngle;
		if (plane.getLeft().equals(Axis.X) && plane.getRight().equals(Axis.Y))
			protoAngle = this.rel.getPrototypeAngle(plane);
		else {
			Line speakerAxis = new Line(speakerPos, roPos, 0);
			protoAngle = addAngles(speakerAxis.getAngle(), this.rel.getPrototypeAngle(plane));
		}
		Line objectAxis = new Line(roPos, ioPos, 0);
		Line prototypeAxis = new Line(roPos, protoAngle, 0);
		return deviation(objectAxis, prototypeAxis);
	}

	public static double addAngles(double a1, double a2) {
		double aSum = a1 + a2;
		return aSum >= 2 * Math.PI ? aSum - 2 * Math.PI : aSum;
	}

	/**
	 * return Bounding Box Angular Deviation (BB)
	 */
	public double getBB(Pair<Axis,Axis> plane, Vector2D ioPos, Vector2D roPos, Vector2D speakerPos) {
		double bb;
		Border protoBorder = computePrototypeBorder(plane, ioPos, roPos, speakerPos);
		Pair<Line, Line> protoBounds = Pair.of(protoBorder.getBounds().getLeft(), protoBorder.getBounds().getRight());
		Line ioAxisLeft = new Line(protoBorder.getCorners().getLeft(), ioPos, 0);
		Line ioAxisRight = new Line(protoBorder.getCorners().getRight(), ioPos, 0);
		double angleLeft = ioAxisLeft.getAngle() - protoBounds.getLeft().getAngle();
		double angleRight = ioAxisRight.getAngle() - protoBounds.getRight().getAngle();
		
		if (angleLeft < 0)
			angleLeft = 360 + angleLeft;
		if (angleRight < 0)
			angleRight = 360 + angleRight;

		// ro lies between the prototype boundaries
		if (angleLeft > Math.toRadians(270) && angleRight < Math.toRadians(90))
			bb = 0.0;
		else if (angleLeft < Math.toRadians(270))
			bb = deviation(protoBounds.getLeft(), ioAxisLeft);
		else
			bb = deviation(protoBounds.getRight(), ioAxisRight);
		if (bb > Math.toRadians(90))
			this.score = 0.0;
	
		return bb;
	}

	public Border computePrototypeBorder(Pair<Axis,Axis> plane, Vector2D ioPos, Vector2D roPos, Vector2D speakerPos) {
		BoundingBox2D roBB = this.ro.getBoundingBox(BoundingBoxType.getType(plane.getLeft(), plane.getRight()));
		Vector2D roCenter = roBB.getCenter();
		double protoBorderAngle;
		if (plane.getLeft().equals(Axis.X) && plane.getRight().equals(Axis.Y))
			protoBorderAngle = rel.getPrototypeAngle(plane);
		else {
			Line speakerAxis = new Line(speakerPos, roCenter, 0);
			Border behindOfBorder = roBB.getExitBorder(speakerPos, speakerAxis);
			protoBorderAngle = addAngles(behindOfBorder.getType().getBorderAngle(), rel.getPrototypeAngle(plane));
		}
		return roBB.getBorder(protoBorderAngle);
	}

	/**
	 * return Physical Distance (PD)
	 */
	public Double getPD(Pair<Axis,Axis> plane, Vector2D ioPos, Vector2D roPos, Vector2D speakerPos) {
		return Vector2D.distance(io.getPosition2D(plane.getLeft(), plane.getRight()), ro.getPosition2D(plane.getLeft(), plane.getRight()));
	}

	private double deviation(Line x, Line y) {
		double diff = Math.abs(x.getAngle() - y.getAngle());
       return diff > Math.PI ? 2 * Math.PI - diff : diff;
	}

}
