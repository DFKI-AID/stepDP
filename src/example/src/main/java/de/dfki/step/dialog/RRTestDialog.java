package de.dfki.step.dialog;


import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.blackboard.Board;
import de.dfki.step.blackboard.Condition;
import de.dfki.step.blackboard.IToken;
import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.conditions.PatternCondition;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.blackboard.rules.SimpleRule;
import de.dfki.step.blackboard.rules.SpatialReferenceResolutionRule;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.IKBObjectWriteable;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.semantic.PropBool;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropString;
import de.dfki.step.kb.semantic.Type;
import de.dfki.step.rr.RRConfigParameters;
import de.dfki.step.web.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 */
public class RRTestDialog extends Dialog {
    private static final Logger log = LoggerFactory.getLogger(RRTestDialog.class);

    public RRTestDialog() {
        try {
        	this.defineSemanticTree();
        	KnowledgeBase kb = this.getKB();
    		Map<String, Object> ref = this.buildBinarySpatialReference(kb.getType("Spaghetti"), RRTypes.BinSpatRelation.rightOf);
    		BasicToken match2 = new BasicToken(kb);
    		match2.setType(kb.getType("PickUpIntent"));
    		match2.addAll(Map.of(
    				"fragile", false,
    				"object", ref,
    				"bringTo", Map.of("type","Human",
    						 		  "name", "Harry"
    						)
    				)
    	   	);
    		this.getBlackboard().addToken(match2);

    		de.dfki.step.blackboard.Rule r =  new SpatialReferenceResolutionRule(kb,0, new RRConfigParameters());
    		this.getBlackboard().addRule(r);
 
            Rule pickUpRule = new SimpleRule(tokens -> {
            	IToken t = tokens[0];
            	IKBObject obj = t.getResolvedReference("object");
                System.out.println("Picking up " + obj.getName());
                t.getOriginTokens().stream().forEach(to -> to.setActive(false));
            }, "PickUpRule");
            Pattern p = new PatternBuilder("PickUpIntent", this.getKB())
            				.addPatternForProperty("object")
            					.hasType("PhysicalObject")
        					.endPropertyPattern()
            				.build();
            pickUpRule.setCondition(new PatternCondition(p));
            this.getBlackboard().addRule(pickUpRule);

            Rule rrFailureRule = new SimpleRule(tokens -> {
            	System.out.println("I don't know which object I should pick up");
            }, "rrFailureRule");
            Pattern p2 = new PatternBuilder("PickUpIntent", this.getKB())
            				.hasRecursiveType(RRTypes.SPAT_REF)
            				.build();
            Condition c = new PatternCondition(p2);
            c.setMinTokenAge(1000);
            rrFailureRule.setCondition(c);
            this.getBlackboard().addRule(rrFailureRule);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void update() {
        super.update();
    }
    
	public void defineSemanticTree() throws Exception {
		KnowledgeBase kb = this.getKB();

		// Physical Object Type
		Type physObj = kb.getType(RRTypes.SPAT_REF_TARGET);

		// Location Type
		Type loc = new Type("Location", kb);
		loc.addProperty(new PropReference("relatum", kb, physObj));
		loc.addProperty(new PropString("relation", kb));
		loc.addProperty(new PropReference("container", kb, physObj));
		kb.addType(loc);

		// Human Type
		Type human = new Type("Human", kb);
		human.addProperty(new PropString("name", kb));
		kb.addType(human);
		
		// Food Types
		Type food = new Type("Food", kb);
		food.addInheritance(physObj);
		kb.addType(food);

		Type pizza = new Type("Pizza", kb);
		pizza.addProperty(new PropString("sort", kb));
		pizza.addInheritance(kb.getType("Food"));
		kb.addType(pizza);
		Type spaghetti = new Type("Spaghetti", kb);
		spaghetti.addInheritance(kb.getType("Food"));
		kb.addType(spaghetti);

		// Drink Types
		Type drink = new Type("Drink", kb);
		drink.addInheritance(kb.getType("PhysicalObject"));
		drink.addProperty(new PropBool("withIce", kb));
		kb.addType(drink);
		Type water = new Type("Water", kb);
		water.addProperty(new PropBool("carbonated", kb));
		water.addInheritance(kb.getType("Drink"));
		kb.addType(water);
		Type beer = new Type("Beer", kb);
		beer.addInheritance(kb.getType("Drink"));
		kb.addType(beer);

		// Other Physcial Object Types
		Type knife = new Type("Knife", kb);
		knife.addInheritance(kb.getType("PhysicalObject"));
		kb.addType(knife);

		// Intent Types
		kb.addType(new Type("Intent", kb));
		Type generalBringIntent = new Type("GeneralBringIntent", kb);
		generalBringIntent.addInheritance(kb.getType("Intent"));
		generalBringIntent.addProperty(new PropString("recipientName", kb));
		kb.addType(generalBringIntent);
		Type bringIntent = new Type("ObjectBringIntent", kb);
		bringIntent.addInheritance(kb.getType("GeneralBringIntent"));
		bringIntent.addProperty(new PropReference("object", kb, kb.getType("PhysicalObject")));
		kb.addType(bringIntent);
		Type pickUpIntent = new Type("PickUpIntent", kb);
		pickUpIntent.addInheritance(kb.getType(RRTypes.USER_INTENT));
		pickUpIntent.addProperty(new PropReference("object", kb, physObj));
		pickUpIntent.addProperty(new PropBool("fragile", kb));
		pickUpIntent.addProperty(new PropReference("bringTo", kb, human));
		pickUpIntent.addProperty(new PropReference("placeAt", kb, loc));
		kb.addType(pickUpIntent);
		
		// instances
		IKBObjectWriteable beer1 = kb.createInstance("beer1", kb.getType("Beer"));
		beer1 = this.setPosition(beer1, 6, 3.5f, 7);
		//kb.createInstance("pizza1", kb.getType("Pizza"));
		IKBObjectWriteable o2 = kb.createInstance("obj2", knife);
		o2 = this.setPosition(o2, 2.5f, 5, 7);
		IKBObjectWriteable o3 = kb.createInstance("obj3", knife);
		o3 = this.setPosition(o3, 9, 2, 7);
	}

	private IKBObjectWriteable setPosition(IKBObjectWriteable obj, float x, float y, float z) {
		KnowledgeBase kb = this.getKB();
		IKBObjectWriteable pos = kb.createInstance("pos1", kb.getType("Position"));
		pos.setFloat("x", x);
		pos.setFloat("y", y);
		pos.setFloat("z", z);
		IKBObjectWriteable transform = kb.createInstance("transform1", kb.getType("Transform"));
		transform.setReference("position", pos.getUUID());
		obj.setReference("transform", transform.getUUID());
		return obj;
	}

	private Map<String, Object> buildBinarySpatialReference(Type ioType, RRTypes.BinSpatRelation rel) {
		KnowledgeBase kb = this.getKB();
		Map<String, Object> m1 = Map.of(
				"type", RRTypes.TYPE_C,
				"refType", "Drink"
			 );
		BasicToken c1 = new BasicToken(kb);
		c1.setType(kb.getType(RRTypes.TYPE_C));
		c1.addAll(m1);
		kb.getBlackboard().addToken(c1);
		String[] cs = List.of(c1.getUUID().toString()).toArray(new String[] {});
		Map<String, Object> constraintMap = Map.of(
											"type", RRTypes.BIN_SPAT_C,
											"relatumReference", Map.of(
															"type", RRTypes.SPAT_REF,
															"constraints", cs
														),
											"relation", rel.name()
										 );
		BasicToken constraint = new BasicToken(kb);
		constraint.setType(kb.getType(RRTypes.BIN_SPAT_C));
		constraint.addAll(constraintMap);
		kb.getBlackboard().addToken(constraint);
		Map<String, Object> typeConstraintMap = Map.of(
				"type", RRTypes.TYPE_C,
				"refType", ioType.getName()
			 );
        BasicToken typeConstraint = new BasicToken(kb);
		typeConstraint.setType(kb.getType(RRTypes.TYPE_C));
		typeConstraint.addAll(typeConstraintMap);
		kb.getBlackboard().addToken(typeConstraint);
		String[] constraints = List.of(constraint.getUUID().toString(), typeConstraint.getUUID().toString()).toArray(new String[] {});
		Map<String, Object> ref = Map.of(
									"type", RRTypes.SPAT_REF,
									"constraints", constraints
								  );
		return ref;
	}
}

