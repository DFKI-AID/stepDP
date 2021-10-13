package de.dfki.step.kb;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import de.dfki.step.kb.semantic.PropBool;
import de.dfki.step.kb.semantic.PropFloat;
import de.dfki.step.kb.semantic.PropInt;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropReferenceArray;
import de.dfki.step.kb.semantic.PropString;
import de.dfki.step.kb.semantic.PropStringArray;
import de.dfki.step.kb.semantic.Type;

/**
 * Contains the definitions of the semantic tree types that are needed for
 * reference resolution.
 */
public class RRTypes {
	public static final String USER_INTENT = "UserIntent";
	public static final String AGENT = "Agent";
	public static final String REFERENCE = "Reference";
	public static final String SPAT_REF = "SpatialReference";
	public static final String LM_SPAT_REF = "LMSpatialReference";
	public static final String LM_SPAT_REF_INNER = "LMSpatialReferenceInner";
	public static final String BIN_SPAT_C = "BinarySpatialRelationConstraint";
	public static final String GROUP_REL_C = "GroupRelationConstraint";
	public static final String TYPE_C = "TypeConstraint";
	public static final String REGION_C = "RegionConstraint";
	public static final String POINTING_C = "PointingConstraint";
	public static final String SPAT_REF_TARGET = "PhysicalObject";
	public static final String CONTAINER = "Container";

	public enum BinSpatRelation {
		// TODO: add support for NEXT_TO etc.
		// TODO: find prettier solution than one single enum for topological and projective bin rels
		// rename (naming convention)
		// angle is relative to speaker axis
		in(null, null, 0, false), on(null, null, 0, false), leftOf(Axis.X, Axis.Z, 90), rightOf(Axis.X, Axis.Z, 270), inFrontOf(Axis.X, Axis.Z, 180), behindOf(Axis.X, Axis.Z, 0),  aboveOf(Axis.Z, Axis.Y, 90), belowOf(Axis.Z, Axis.Y, 270);//, NEXT_TO, ON, INSIDE_OF
		
		private Axis abscissa;
		private Axis ordinate;
		private double prototypeAngle;
		private boolean projective;
		
		BinSpatRelation(Axis axis1, Axis axis2, double prototypeAngle, boolean projective) {
			this.abscissa = axis1;
			this.ordinate = axis2;
			this.prototypeAngle = prototypeAngle;
			this.projective = projective;
		}
		BinSpatRelation(Axis axis1, Axis axis2, double prototypeAngle) {
			this(axis1, axis2, prototypeAngle, true);
		}

		/**
		 * @return prototype angle in radians
		 */
		public double getPrototypeAngle() {
			return Math.toRadians(this.prototypeAngle);
		}

		public Axis getAbscissa() {
			return this.abscissa;
		}

		public Axis getOrdinate() {
			return this.ordinate;
		}

		public boolean isProjective() {
			return this.projective;
		}
	};

	public enum Axis {
		X, Y, Z;

		public static Axis getRotationAxis(Axis abscissa, Axis ordinate) {
			if (X.equals(abscissa) || X.equals(ordinate)) {
				if (Y.equals(abscissa) || Y.equals(ordinate))
					return Z;
				else
					return Y;
			}
			else
				return X;
		}

		public static Vector2D get2DVec(Vector3D position, Axis abscissa, Axis ordinate) {
			if (position == null)
				return null;
			else
				return new Vector2D(abscissa.getValue(position), ordinate.getValue(position));
		}

		public double getValue(Vector3D position) {
			switch (this) {
			case X:
				return position.getX();
			case Y:
				return position.getY();
			case Z:
				return position.getZ();
			default:
				throw new IllegalStateException("not a valid axis");
			}
		}

	}

	public static boolean isSpatialReference(IKBObject obj, KnowledgeBase kb) {
		Type type = obj.getType();
		if (type == null)
				return false;
		else
			return type.isInheritanceFrom(kb.getType(RRTypes.SPAT_REF));
	}

	public static boolean isReference(IKBObject obj, KnowledgeBase kb) {
		return obj.getType().isInheritanceFrom(kb.getType(RRTypes.REFERENCE));
	}
	
	public static void addRRTypesToKB(KnowledgeBase kb) throws Exception{
			Type intent = new Type(USER_INTENT, kb);
			kb.addType(intent);

			Type vector3D = new Type("Vector3D", kb);
			vector3D.addProperty(new PropFloat("x", kb));
			vector3D.addProperty(new PropFloat("y", kb));
			vector3D.addProperty(new PropFloat("z", kb));

			Type transform = new Type("Transform", kb);
			transform.addProperty(new PropReference("position", kb, vector3D));
			transform.addProperty(new PropReference("rotation", kb, vector3D));
			kb.addType(transform);

			Type bb = new Type("BoundingBox", kb);
			bb.addProperty(new PropReference("center", kb, vector3D));
			bb.addProperty(new PropReference("extents", kb, vector3D));

			Type object = new Type(SPAT_REF_TARGET, kb);
			object.addProperty(new PropReference("transform", kb, transform));
			object.addProperty(new PropReference("boundingBox", kb, bb));
			kb.addType(object);

			Type agent = new Type(AGENT, kb);
			agent.addInheritance(object);
			kb.addType(agent);

			Type ref = new Type(REFERENCE, kb, true);
			ref.addProperty(new PropString("text", kb));
			kb.addType(ref);
	
			Type constraint = new Type("Constraint", kb, true);
			constraint.addProperty(new PropInt("priority", kb));
			kb.addType(constraint);

			Type typeConst = new Type(TYPE_C, kb, true);
			typeConst.addProperty(new PropString("refType", kb));
			typeConst.addInheritance(constraint);
			kb.addType(typeConst);
	
			Type spatRef = new Type(SPAT_REF, kb, true);
			spatRef.addInheritance(kb.getType("Reference"));
			spatRef.addProperty(new PropReference("speaker", kb, agent));
			spatRef.addProperty(new PropReferenceArray("constraints", kb, constraint));
			spatRef.addProperty(new PropBool("ambiguous", kb));
			spatRef.addProperty(new PropInt("cardinality", kb));
			kb.addType(spatRef);

			Type lmSpatRefInner = new Type(LM_SPAT_REF_INNER, kb, true);
			// FIXME: should it inherit from "Reference"?
			//lmSpatRef.addInheritance(kb.getType("Reference"));
			lmSpatRefInner.addProperty(new PropString("type", kb));
			lmSpatRefInner.addProperty(new PropInt("cardinality", kb));
			lmSpatRefInner.addProperty(new PropString("region", kb));
			lmSpatRefInner.addProperty(new PropString("groupRelation", kb));
			lmSpatRefInner.addProperty(new PropInt("ordinality", kb));
			lmSpatRefInner.addProperty(new PropString("attribute", kb));
			kb.addType(lmSpatRefInner);

			Type lmSpatRef = new Type(LM_SPAT_REF, kb, true);
			// FIXME: should it inherit from "Reference"?
			//lmSpatRef.addInheritance(kb.getType("Reference"));
			lmSpatRef.addProperty(new PropReference("intendedObjectReference", kb, lmSpatRefInner));
			lmSpatRef.addProperty(new PropReference("relatumObjectReference", kb, lmSpatRefInner));
			lmSpatRef.addProperty(new PropString("binarySpatialRelation", kb));
			kb.addType(lmSpatRef);

			Type relConst = new Type("RelationConstraint", kb);
			relConst.addInheritance(constraint);
			relConst.addProperty(new PropString("relation", kb));
			kb.addType(relConst);
			
			Type binSpatRelConst = new Type(BIN_SPAT_C, kb);
			binSpatRelConst.addProperty(new PropReference("relatumReference", kb, lmSpatRef));
			binSpatRelConst.addInheritance(relConst);
			kb.addType(binSpatRelConst);

			Type groupRelConst = new Type(GROUP_REL_C, kb);
			groupRelConst.addInheritance(relConst);
			kb.addType(groupRelConst);

			Type regionConst = new Type(REGION_C, kb);
			regionConst.addProperty(new PropString("region", kb));
			regionConst.addInheritance(constraint);
			kb.addType(regionConst);

			// FIXME: consider confidences
			Type pointingConst = new Type(POINTING_C, kb);
			pointingConst.addProperty(new PropStringArray("objectNames", kb));
			pointingConst.addInheritance(constraint);
			kb.addType(pointingConst);

			Type container = new Type(CONTAINER, kb);
			container.addProperty(new PropReferenceArray("contains", kb, object));
			container.addInheritance(object);
			kb.addType(container);
	}
}
