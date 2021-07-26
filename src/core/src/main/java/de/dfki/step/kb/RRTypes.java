package de.dfki.step.kb;

import de.dfki.step.kb.semantic.PropBool;
import de.dfki.step.kb.semantic.PropInt;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropReferenceArray;
import de.dfki.step.kb.semantic.PropString;
import de.dfki.step.kb.semantic.Type;

/**
 * Contains the definitions of the semantic tree types that are needed for
 * reference resolution.
 */
public class RRTypes {
	public static final String USER_INTENT = "UserIntent";
	public static final String REFERENCE = "Reference";
	public static final String SPAT_REF = "SpatialReference";
	public static final String LM_SPAT_REF = "LMSpatialReference";
	public static final String LM_SPAT_REF_INNER = "LMSpatialReferenceInner";
	public static final String BIN_SPAT_C = "BinarySpatialRelationConstraint";
	public static final String TYPE_C = "TypeConstraint";
	public static final String REGION_C = "RegionConstraint";
	public static final String SPAT_REF_TARGET = "PhysicalObject";

	public enum BinSpatRelation {
		// TODO: add support for NEXT_TO etc.
		// rename (naming convention)
		leftOf(90), rightOf(270), inFrontOf(180), behindOf(0); //, ABOVE_OF, BELOW_OF //, NEXT_TO, ON, INSIDE_OF
		
		private double prototypeAngle;
		
		BinSpatRelation(double prototypeAngle) {
			this.prototypeAngle = prototypeAngle;
		}

		/**
		 * @return prototype angle in radians
		 */
		public double getPrototypeAngle() {
			return Math.toRadians(this.prototypeAngle);
		}
	};

	public enum SpatialRegion {
		// TODO: add support for front etc.
		// rename (naming convention)
		left(90), right(270); // front(180), back(0), top, bottom, middle;
		
		private double prototypeAngle;
		
		SpatialRegion(double prototypeAngle) {
			this.prototypeAngle = prototypeAngle;
		}

		/**
		 * @return prototype angle in radians
		 */
		public double getPrototypeAngle() {
			return Math.toRadians(this.prototypeAngle);
		}
	};

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

			Type position = new Type("Position", kb);
			position.addProperty(new PropInt("x", kb));
			position.addProperty(new PropInt("y", kb));
			position.addProperty(new PropInt("z", kb));
			kb.addType(position);

			Type rotation = new Type("Rotation", kb);
			rotation.addProperty(new PropInt("x", kb));
			rotation.addProperty(new PropInt("y", kb));
			rotation.addProperty(new PropInt("z", kb));
			kb.addType(rotation);

			Type transform = new Type("Transform", kb);
			transform.addProperty(new PropReference("position", kb, position));
			transform.addProperty(new PropReference("rotation", kb, rotation));
			kb.addType(transform);

			Type extension = new Type("Extension", kb);
			extension.addProperty(new PropInt("x", kb));
			extension.addProperty(new PropInt("y", kb));
			extension.addProperty(new PropInt("z", kb));

			Type object = new Type(SPAT_REF_TARGET, kb);
			object.addProperty(new PropReference("transform", kb, transform));
			object.addProperty(new PropReference("extension", kb, extension));
			kb.addType(object);

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
			spatRef.addProperty(new PropReferenceArray("constraints", kb, typeConst));
			spatRef.addProperty(new PropBool("ambiguous", kb));
			kb.addType(spatRef);

			Type lmSpatRefInner = new Type(LM_SPAT_REF_INNER, kb, true);
			// FIXME: should it inherit from "Reference"?
			//lmSpatRef.addInheritance(kb.getType("Reference"));
			lmSpatRefInner.addProperty(new PropString("type", kb));
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

			Type regionConst = new Type(REGION_C, kb);
			regionConst.addProperty(new PropString("region", kb));
			regionConst.addInheritance(constraint);
			kb.addType(regionConst);
	}
}
