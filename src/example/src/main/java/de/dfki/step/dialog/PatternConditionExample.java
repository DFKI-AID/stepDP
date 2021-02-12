package de.dfki.step.dialog;


import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.blackboard.conditions.PatternCondition;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.blackboard.rules.SimpleRule;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.PropBool;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropString;
import de.dfki.step.kb.semantic.Type;

public class PatternConditionExample extends Dialog {
	
	public PatternConditionExample() throws Exception {
		KnowledgeBase kb = this.getKB();
		
		// extend semantic tree
		kb.addType(new Type("PhysicalObject", kb));

		// food types
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
		
		// drink types
		Type drink = new Type("Drink", kb);
		drink.addInheritance(kb.getType("PhysicalObject"));
		kb.addType(drink);
		Type water = new Type("Water", kb);
		water.addProperty(new PropBool("carbonated", kb));
		water.addInheritance(kb.getType("Drink"));
		kb.addType(water);
		Type beer = new Type("Beer", kb);
		beer.addInheritance(kb.getType("Drink"));
		kb.addType(beer);

		// other physical objects
		Type knife = new Type("Knife", kb);
		knife.addInheritance(kb.getType("PhysicalObject"));
		kb.addType(knife);

		// intent types
		Type bringIntent = new Type("BringIntent", kb);
		bringIntent.addProperty(new PropReference("object", kb, kb.getType("PhysicalObject")));
		bringIntent.addProperty(new PropString("recipientName", kb));
		kb.addType(bringIntent);
		
		// this tag helps us to group the rules that we will define
 		String tag = "BringRule";
 		
		// rule 1: match BringIntent(object:Food, recipientName:String)
		PatternBuilder builder = new PatternBuilder("BringIntent", kb);
		builder.hasNonNullProperties("recipientName")
		   	   .addPatternForProperty("object")
					.hasType("Food")
			   .endPropertyPattern();
		Pattern p = builder.build();
		
		Rule rule = new SimpleRule(tokens -> {
			BasicToken intent = tokens[0];
			// a token should only trigger one of the rules with the tag
			// "BringRule", so we add the tag to the token's ignoreRuleTags
			intent.getIgnoreRuleTags().add(tag);
			String name = intent.getString("recipientName");
			System.out.println("Here you go! Enjoy your meal, " + name);
		});
		rule.setCondition(new PatternCondition(p));
		rule.setName("BringFoodRule");
		// the priority of more specific rules should be higher
		// e.g. BringRule(object:Food) should have higher prio
		// than BringRule
		// note: higher prio means a lower int value
		rule.setPriority(100);
		rule.getTags().add(tag);
 		this.getBlackboard().addRule(rule);
 		
		// rule 2: match BringIntent(object:Drink, recipientName:String)
	    builder = new PatternBuilder("BringIntent", kb);
		builder.hasNonNullProperties("recipientName")
			   .addPatternForProperty("object")
					.hasType("Drink")
			   .endPropertyPattern();
		p = builder.build();
		
		rule = new SimpleRule(tokens -> {
			BasicToken intent = tokens[0];
			String name = intent.getString("recipientName");
			System.out.println("Here you go! Enjoy your drink, " + name);
			intent.getIgnoreRuleTags().add(tag);
		});
		rule.setCondition(new PatternCondition(p));
		rule.setName("BringDrinkRule");
		rule.setPriority(100);
		rule.getTags().add(tag);
 		this.getBlackboard().addRule(rule);

		// rule 3: match any BringIntent that does not match the other more specific rules
		p = new PatternBuilder("BringIntent", kb).build();
	
		rule = new SimpleRule(tokens -> {
			BasicToken intent = tokens[0];
			intent.getIgnoreRuleTags().add(tag);
			IKBObject object = intent.getResolvedReference("object");
			if (object == null) {
				System.out.println("Which object should I bring to you?");
			    return;
			}
			String name = intent.getString("recipientName");
			if (name == null) {
				System.out.println("Please tell me your name.");
			    return;
			}
			System.out.println("Here you go, " + name);
		});
		rule.setCondition(new PatternCondition(p));
		rule.setName("GeneralBringRule");
		// here, we set a lower prio because this rule should only be triggered
		// if the other, more specific, rules do not match
		rule.setPriority(200);
		rule.getTags().add(tag);
 		this.getBlackboard().addRule(rule);
	}

}
