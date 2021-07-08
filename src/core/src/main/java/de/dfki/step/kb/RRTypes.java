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

	public enum BinaryRelation {
		LEFT_OF, RIGHT_OF, IN_FRONT_OF, BEHIND_OF, ABOVE_OF, BELOW_OF, NEXT_TO, ON, INSIDE_OF
	};

	public static void addRRTypesToKB(KnowledgeBase kb) throws Exception{
			Type intent = new Type(USER_INTENT, kb);
			kb.addType(intent);

			Type ref = new Type(REFERENCE, kb, true);
			ref.addProperty(new PropString("text", kb));
			kb.addType(ref);
	
			Type constraint = new Type("Constraint", kb, true);
			constraint.addProperty(new PropInt("priority", kb));
			kb.addType(constraint);
	
			Type spatRef = new Type(SPAT_REF, kb, true);
			spatRef.addInheritance(kb.getType("Reference"));
			spatRef.addProperty(new PropReferenceArray("constraints", kb, constraint));
			spatRef.addProperty(new PropBool("ambiguous", kb));
			kb.addType(spatRef);

			Type relConst = new Type("RelationConstraint", kb);
			relConst.addInheritance(constraint);
			relConst.addProperty(new PropString("relation", kb));
			kb.addType(relConst);
			
			Type binSpatRelConst = new Type("BinarySpatialRelationConstraint", kb);
			binSpatRelConst.addProperty(new PropReference("relatumReference", kb, spatRef));
			binSpatRelConst.addInheritance(relConst);
			kb.addType(binSpatRelConst);
	}
}
