package de.dfki.step.blackboard;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.Rule;

import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.semantic.PropBool;
import de.dfki.step.kb.semantic.PropFloat;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropString;
import de.dfki.step.kb.semantic.Type;

public class PatternBuilderTest {
	private KnowledgeBase kb;

    @Rule
    public ExpectedException exc = ExpectedException.none();
	
	@Before
	public void setUp() throws Exception {
		kb = new KnowledgeBase(new Board());

		// Physical Object Type
		Type coord = new Type("3DCoordinates", kb);
		coord.addProperty(new PropFloat("x", kb));
		coord.addProperty(new PropFloat("y", kb));
		coord.addProperty(new PropFloat("z", kb));
		kb.addType(coord);
		Type physObj = new Type("PhysicalObject", kb);
		physObj.addProperty(new PropReference("position", kb, kb.getType("3DCoordinates")));
		kb.addType(physObj);

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
		Type topping = new Type("Topping", kb);
		topping.addProperty(new PropBool("double", kb));
		kb.addType(topping);
		Type coldTopping = new Type("ColdTopping", kb);
		coldTopping.addInheritance(kb.getType("Topping"));
		kb.addType(coldTopping);
		Type hotTopping = new Type("HotTopping", kb);
		hotTopping.addInheritance(kb.getType("Topping"));
		kb.addType(hotTopping);
		
		Type cheese = new Type("Cheese", kb);
		cheese.addProperty(new PropString("sort", kb));
		cheese.addInheritance(kb.getType("HotTopping"));
		kb.addType(cheese);
		Type pizza = new Type("Pizza", kb);
		pizza.addProperty(new PropString("sort", kb));
		pizza.addProperty(new PropReference("topping", kb, kb.getType("Topping")));
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
		kb.createInstance("pizza1", kb.getType("Pizza"));
	}
	
	@Test
	public void testSingleType() throws Exception {
		PatternBuilder builder = new PatternBuilder("GeneralBringIntent", kb);
		Pattern p = builder.build();
		
		BasicToken exactType = new BasicToken(kb);
		exactType.setType(kb.getType("GeneralBringIntent"));
		BasicToken subType = new BasicToken(kb);
		subType.setType(kb.getType("ObjectBringIntent"));
		BasicToken superType = new BasicToken(kb);
		superType.setType(kb.getType("Intent"));
		BasicToken otherType = new BasicToken(kb);
		otherType.setType(kb.getType("PhysicalObject"));
		BasicToken nullType = new BasicToken(kb);
		
		Assert.assertTrue(p.matches(exactType));
		Assert.assertTrue(p.matches(subType));
		Assert.assertFalse(p.matches(superType));
		Assert.assertFalse(p.matches(otherType));
		Assert.assertFalse(p.matches(nullType));
		Assert.assertFalse(p.matches(null));
	}
	
	@Test
	public void testNonNullProps() throws Exception {
		PatternBuilder builder = new PatternBuilder("ObjectBringIntent", kb);
		// FIXME: this will make the test fail until the kb
		// is adjusted such that subtype inherit properties
		builder.hasNonNullProperties("object", "recipientName");
		Pattern p = builder.build();
		
		BasicToken bothNull = new BasicToken(kb);
		BasicToken objectNull = new BasicToken(kb);
		objectNull.setType(kb.getType("ObjectBringIntent"));
		objectNull.addAll(Map.of("recipientName","Lara","urgent",false));
		BasicToken nameNull = new BasicToken(kb);
		nameNull.setType(kb.getType("ObjectBringIntent"));
	    nameNull.addAll(Map.of("object", Map.of("position", Map.of("x",1,"y",2,"z",3))));
		BasicToken bothNonNull = new BasicToken(kb);
		bothNonNull.setType(kb.getType("ObjectBringIntent"));
	    bothNonNull.addAll(Map.of("recipientName","Lara",
	    		"object", Map.of("position",Map.of("x",1,"y",2,"z",3))));
	    
	    Assert.assertFalse(p.matches(bothNull));
	    Assert.assertFalse(p.matches(objectNull));
	    Assert.assertFalse(p.matches(nameNull));
	    Assert.assertTrue(p.matches(bothNonNull));
	}
	
	@Test
	public void testNestedPattern() throws Exception {
		PatternBuilder builder = new PatternBuilder("ObjectBringIntent", kb);
		// FIXME: this will make the test fail until the kb
		// is adjusted such that subtype inherit properties
		builder.hasNonNullProperties("recipientName")
			   .addPatternForProperty("object")
			   		.hasType("Pizza")
			   		.hasNonNullProperties("sort")
			   		.addPatternForProperty("topping")
			   			.hasType("HotTopping")
			   			.hasNonNullProperties("double")
			   		.endPropertyPattern()
			   	.endPropertyPattern();
		Pattern p = builder.build();
		
		BasicToken match1 = new BasicToken(kb);
		match1.setType(kb.getType("ObjectBringIntent"));
		match1.addAll(Map.of("recipientName","Lara",
							"object", Map.of("type","Pizza",
											 "sort", "Hawaii",
											 "topping", Map.of("type","Cheese",
													 		   "double",true,
													 		   "sort", "gouda"
													    	  )
											)
							)
				   	);

		BasicToken match2 = new BasicToken(kb);
		match2.setType(kb.getType("ObjectBringIntent"));
		match2.addAll(Map.of("recipientName","Lara",
				"object", Map.of("type","Pizza",
								 "sort", "Hawaii",
								 "topping", Map.of("type","Cheese",
										 		   "double",true
										    	  )
								)
				)
	   	);
		
		BasicToken sortNull = new BasicToken(kb);
		sortNull.setType(kb.getType("ObjectBringIntent"));
		sortNull.addAll(Map.of("recipientName","Lara",
				"object", Map.of("type","Pizza",
								 "topping", Map.of("type","Cheese",
										 		   "double",true,
										 		   "sort", "gouda"
										    	  )
								)
				)
	   	);
		
		BasicToken toppingNull = new BasicToken(kb);
		toppingNull.setType(kb.getType("ObjectBringIntent"));
		toppingNull.addAll(Map.of("recipientName","Lara",
				"object", Map.of("type","Pizza",
								 "sort", "Hawaii"
								)
				)
	   	);
		
		BasicToken wrongInnerType = new BasicToken(kb);
		wrongInnerType.setType(kb.getType("ObjectBringIntent"));
		wrongInnerType.addAll(Map.of("recipientName","Lara",
				"object", Map.of("type","Water",
								 "carbonated", true
								)
				)
	   	);
		
		Assert.assertTrue(p.matches(match1));
		Assert.assertTrue(p.matches(match2));
		Assert.assertFalse(p.matches(wrongInnerType));
		Assert.assertFalse(p.matches(sortNull));
		Assert.assertFalse(p.matches(toppingNull));
	}
	
	@Test
	public void testInvalidType() throws Exception {
		exc.expect(Exception.class);
		PatternBuilder builder = new PatternBuilder("Typo", kb);
	}

	@Test
	public void testConflictingType() throws Exception {
		PatternBuilder builder = new PatternBuilder("ObjectBringIntent", kb);
		exc.expect(Exception.class);
		builder.hasType("GeneralBringIntent").build();
	}

	@Test
	public void testInvalidProperty() throws Exception {
		PatternBuilder builder = new PatternBuilder("GeneralBringIntent", kb);
		exc.expect(Exception.class);
		builder.hasNonNullProperties("object").build();
	}
	
	@Test
	public void testMatchingTokenWithKBReference() throws Exception {
		PatternBuilder builder = new PatternBuilder("ObjectBringIntent", kb);
		Pattern p = builder.addPatternForProperty("object")
								.hasType("Pizza")
						   .endPropertyPattern()
						   .build();

		BasicToken token = new BasicToken(kb);
		token.setType(kb.getType("ObjectBringIntent"));
		token.addAll(Map.of("object",kb.getInstance("pizza1").getUUID().toString()));
		
		Assert.assertTrue(p.matches(token));
	}

	@Test
	public void testInnerTypePattern() throws Exception {
		PatternBuilder builder = new PatternBuilder(RRTypes.USER_INTENT, kb);
		Pattern p = builder.hasInnerType(RRTypes.SPAT_REF)
						   .build();
		
		BasicToken noMatch = new BasicToken(kb);
		noMatch.setType(kb.getType("ObjectBringIntent"));
		noMatch.addAll(Map.of("recipientName","Lara",
				"object", Map.of("type","Pizza",
								 "sort", "Hawaii",
								 "topping", Map.of("type","Cheese",
										 		   "double",true
										    	  )
								)
				)
	   	);

		BasicToken match1 = new BasicToken(kb);
		match1.setType(kb.getType(RRTypes.SPAT_REF));

		BasicToken match2 = new BasicToken(kb);
		match2.setType(kb.getType("PickUpIntent"));
		match2.addAll(Map.of(
				"fragile", false,
				"object", Map.of("type",RRTypes.SPAT_REF,
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
				 				  		"ambiguous", true
				 				  ),
				 		  "relation", "leftOf"
				 		  )
				 		  
				)
	   	);

		Assert.assertFalse(p.matches(noMatch));
		Assert.assertTrue(p.matches(match1));
		Assert.assertTrue(p.matches(match2));
		Assert.assertTrue(p.matches(match3));
	}
}
