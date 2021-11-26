package de.dfki.step.rr.constraints;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.math3.geometry.euclidean.twod.Line;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.RRTypes.Axis;
import de.dfki.step.rr.RRConfigParameters;
import de.dfki.step.rr.constraints.BoundingBox2D.BoundingBoxType;

/**
 * 
 * Computation of confidences for binary spatial relations based on the following publication:
 * Vivien Mast, Zoe Falomir, and Diedrich Wolter. 2016. Probabilistic reference and grounding with
 * PRAGR for dialogues with robots. Journal of Experimental and Theoretical Artificial Intelligence 28,
 * 5 (2016), 889–911
 *
 */
public class ProjectiveBinSpatComputer {
	private double c;
	private double w_cp;
	private double w_bb;
	private double w_pd;
    private static final Logger log = LoggerFactory.getLogger(ProjectiveBinSpatComputer.class);
    private RRConfigParameters config;
	
	private PhysicalObject speaker;
	private PhysicalObject io;
	private PhysicalObject ro;
	private RRTypes.BinSpatRelation rel;
	private Double score;

	public class Total {
		public String io;
		public String ro;
		public CurScore total;
		public Double objectScore;
		public Map<Pair<Axis, Axis>, CurScore> partialScores = new HashMap<Pair<Axis, Axis>, CurScore>();

		public Total(String io, String ro) {
			this.io = io;
			this.ro = ro;
		}
	}

	public class CurScore {
		public Double cp;
		public Double bb;
		public Double pd;

		public CurScore(Double cp, Double bb, Double pd) {
			super();
			this.cp = cp;
			this.bb = bb;
			this.pd = pd;
		}
	}
	
	public ProjectiveBinSpatComputer(IKBObject speaker, IKBObject io, IKBObject ro, RRTypes.BinSpatRelation rel, RRConfigParameters config) {
		this.config = config;
		this.c = config.BINSPATREL_C;
		this.w_cp = config.BINSPATREL_W_CP;
		this.w_bb = config.BINSPATREL_W_BB;
		this.w_pd = config.BINSPATREL_W_PD;
		this.speaker = new PhysicalObject(speaker);
		this.io = new PhysicalObject(io);
		this.ro = new PhysicalObject(ro);
		this.rel = rel;

	}

	public double computeScore() {
		if (score != null)
			return score;
		String ioName = this.io.getName();
		String roName = this.ro.getName();
		Total total = new Total(ioName, roName);
		log.trace("Scoring " + ioName + " " + this.rel.name() + " " + roName);
		Double totalCP = Double.valueOf(0);
		Double totalBB = Double.valueOf(0);
		Double totalPD = Double.valueOf(0);
		// compute angular deviation (CP,BB) and distance (PD) for two planes, e.g. side view and top view for "behindOf"
		// or front view and top view for "leftOf"
		for (Pair<Axis, Axis> plane : this.rel.getPlanes()) {
			Vector2D ioPos = io.getPosition2D(plane.getLeft(), plane.getRight());
			Vector2D roPos = ro.getPosition2D(plane.getLeft(), plane.getRight());
			Vector2D speakerPos = speaker.getPosition2D(plane.getLeft(), plane.getRight());
			if (ioPos == null ||roPos == null || speakerPos == null)
				return 0;
			Double curCP = getCP(plane, ioPos, roPos, speakerPos);
			Double curBB = getBB(plane, ioPos, roPos, speakerPos);
			Double curPD = getPD(plane, ioPos, roPos, speakerPos);
			if (totalCP ==  null || totalBB == null || totalPD == null || curCP == null || curBB == null || curPD == null)
				return 0;
			CurScore curScore = new CurScore(curCP, curBB, curPD);
			total.partialScores.put(plane, curScore);
			totalCP += curCP;
			totalBB += curBB;
			totalPD += curPD;
		}

		total.total = new CurScore(totalCP, totalBB, totalPD);
		if (Double.valueOf(0).equals(score))
			total.objectScore = 0.0;
		else {
			total.objectScore = Math.pow(Math.E, -c * (w_cp * totalCP * totalCP + w_bb * totalBB * totalBB + w_pd * totalPD * totalPD));
		}
		try {
			log.trace(new ObjectMapper().writeValueAsString(total));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return total.objectScore;
	}

	/**
	 * return Centre Point Angular Deviation (CP)
	 */
	public Double getCP(Pair<Axis,Axis> plane, Vector2D ioPos, Vector2D roPos, Vector2D speakerPos) {
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
			angleLeft = Math.toRadians(360) + angleLeft;
		if (angleRight < 0)
			angleRight = Math.toRadians(360) + angleRight;

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
