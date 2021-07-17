package de.dfki.step.blackboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.blackboard.rules.SpatialReferenceResolutionRule;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.semantic.PropBool;
import de.dfki.step.kb.semantic.PropFloat;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropString;
import de.dfki.step.kb.semantic.Type;

public class ReferenceResolutionTest {
	private KnowledgeBase kb;

	@Before
	public void setUp() throws Exception {
		kb = new KnowledgeBase(new Board());

		// Physical Object Type
		Type physObj = kb.getType("PhysicalObject");

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
		food.addInheritance(kb.getType("PhysicalObject"));
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
		kb.createInstance("beer1", kb.getType("Beer"));
		kb.createInstance("pizza1", kb.getType("Pizza"));
	}

	@Test
	public void testReferenceResolution() throws Exception {
		PatternBuilder builder = new PatternBuilder(RRTypes.USER_INTENT, kb);
		Pattern p = builder.hasRecursiveType(RRTypes.SPAT_REF)
						   .build();

		BasicToken match1 = new BasicToken(kb);
		match1.setType(kb.getType(RRTypes.SPAT_REF));
		match1.addAll(Map.of("refType", "Food"));

		BasicToken match2 = new BasicToken(kb);
		match2.setType(kb.getType("PickUpIntent"));
		match2.addAll(Map.of(
				"fragile", false,
				"object", Map.of("type",RRTypes.SPAT_REF,
								 "refType", "Food",
								 "ambiguous", "false"
								),
				"bringTo", Map.of("type","Human",
						 		  "name", "Harry"
						)
				)
	   	);
	
		BasicToken match3 = new BasicToken(kb);
		match3.setType(kb.getType("PickUpIntent"));
		match3.addAll(Map.of(
				"fragile", true,
				"placeAt", Map.of(
						  "type","Location",
				 		  "relatum", Map.of(
				 				  		"type", RRTypes.SPAT_REF,
				 				  		"refType", "Drink",
				 				  		"ambiguous", true
				 				  ),
				 		  "relation", "leftOf"
				 		  )
				 		  
				)
	   	);
		
		de.dfki.step.blackboard.Rule r =  new SpatialReferenceResolutionRule(this.kb);
		IToken[] tArray = List.of(match1).toArray(new IToken[] {});
		List<IToken[]> tList = new ArrayList<IToken[]>();
		tList.add(tArray);
		tArray = List.of(match2).toArray(new IToken[] {});
		tList.add(tArray);
		tArray = List.of(match3).toArray(new IToken[] {});
		tList.add(tArray);
		r.onMatch(tList, this.kb.getBlackboard());
		List<IToken> tokens = this.kb.getBlackboard().getActiveTokens();
		Assert.assertTrue(tokens.size() == 3);
		IToken t = tokens.get(0);
		System.out.print("Juuhuu");
	}
}
