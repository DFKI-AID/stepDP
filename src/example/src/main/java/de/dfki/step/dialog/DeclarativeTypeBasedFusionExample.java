package de.dfki.step.dialog;


import java.time.Duration;

import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.blackboard.conditions.PatternCondition;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.blackboard.rules.DeclarativeTypeBasedFusionRule;
import de.dfki.step.blackboard.rules.SimpleRule;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.PropBool;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropString;
import de.dfki.step.kb.semantic.Type;

public class DeclarativeTypeBasedFusionExample extends Dialog {
	
	public DeclarativeTypeBasedFusionExample() throws Exception {
		KnowledgeBase kb = this.getKB();
		
		// Physical Object Type
		Type physObj = new Type("PhysicalObject", kb);
		kb.addType(physObj);

		// Food Types
		Type food = new Type("Food", kb);
		food.addInheritance(kb.getType("PhysicalObject"));
		kb.addType(food);
		Type pizza = new Type("Pizza", kb);
		pizza.addProperty(new PropString("sort", kb));
		pizza.addInheritance(kb.getType("Food"));
		kb.addType(pizza);
		Type spaghetti = new Type("Spaghetti", kb);
		spaghetti.addInheritance(kb.getType("Food"));
		kb.addType(spaghetti);

		// Intent Types
		kb.addType(new Type("Intent", kb));
		Type bringIntent = new Type("BringIntent", kb);
		bringIntent.addInheritance(kb.getType("Intent"));
		bringIntent.addProperty(new PropString("recipientName", kb));
		kb.addType(bringIntent);
		
		// Fusion Result Types
		Type bringObject = new Type("BringObject", kb);
		bringObject.addProperty(new PropReference("object", kb, kb.getType("PhysicalObject")));
		bringObject.addProperty(new PropReference("intent", kb, kb.getType("BringIntent")));
		kb.addType(bringObject);

 		
 		// rule 0: fuse BringIntent with PhysicalObject
 		Pattern p1 = new PatternBuilder("BringIntent", kb).build();
 		Pattern p2 = new PatternBuilder("PhysicalObject", kb).build();
 		Type resultType = kb.getType("BringObject");
 		long fusionInterval = Duration.ofMinutes(10).toMillis();
		
		Rule rule = new DeclarativeTypeBasedFusionRule(p1, p2, resultType, fusionInterval);
		rule.setName("BringObjectFusionRule");
		// fusion rule should usually be triggered before other rules
		rule.setPriority(50);
 		this.getBlackboard().addRule(rule);

		// rule 1: match BringObject
		Pattern p = new PatternBuilder("BringObject", kb).build();
	
		rule = new SimpleRule(tokens -> {
			BasicToken intent = tokens[0];
			IKBObject object = intent.getResolvedReference("object");
			if (object == null) {
				System.out.println("Which object should I bring to you?");
			    return;
			}
			String name = intent.getResolvedReference("intent").getString("recipientName");
			System.out.println("Here is your " + object.getType().getName() + ", " + name);
		});
		rule.setCondition(new PatternCondition(p));
		rule.setName("BringRule");
 		this.getBlackboard().addRule(rule);
	}

}
