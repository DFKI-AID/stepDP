package de.dfki.step.blackboard;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.MultiValuedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.dfki.step.blackboard.patterns.NonNullPropertiesPattern;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.blackboard.rules.DeclarativeTypeBasedFusionRule;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropString;
import de.dfki.step.kb.semantic.Type;

public class DeclarativeTypeBasedFusionTest {
	private Board board = new Board();
	private KnowledgeBase kb = new KnowledgeBase(board);

    @org.junit.Rule
    public ExpectedException exc = ExpectedException.none();
	
	@Before
	public void setUp() throws Exception {
		// Physical Object Type
		Type physObj = kb.getType("PhysicalObject");

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

		// Gesture Type
		Type gesture = new Type("Gesture", kb);
		gesture.addProperty(new PropReference("targetObject", kb, kb.getType("PhysicalObject")));
		kb.addType(gesture);
		
		// Fusion Result Types
		Type bringObject = new Type("BringObject", kb);
		bringObject.addProperty(new PropReference("object", kb, kb.getType("PhysicalObject")));
		bringObject.addProperty(new PropReference("intent", kb, kb.getType("BringIntent")));
		kb.addType(bringObject);
		// just for testing purposes (not a good example for the design of a semantic tree)
		Type bringObject2 = new Type("BringObject2", kb);
		bringObject2.addProperty(new PropReference("gesture", kb, kb.getType("Gesture")));
		bringObject2.addProperty(new PropReference("intent", kb, kb.getType("BringIntent")));
		kb.addType(bringObject2);
		// just for testing purposes (not a good example for the design of a semantic tree)
		Type bringPizza = new Type("BringPizza", kb);
		bringPizza.addProperty(new PropReference("pizza", kb, kb.getType("Pizza")));
		bringPizza.addProperty(new PropReference("intent", kb, kb.getType("BringIntent")));
		kb.addType(bringPizza);
		
		// Instances
		kb.createInstance("pizza1", kb.getType("Pizza"));
	}

	@Test
	public void testSimpleMatch() throws Exception {
		Pattern p1 = new PatternBuilder("BringIntent", kb).build();
		Pattern p2 = new PatternBuilder("PhysicalObject", kb).build();
		Type resultType = kb.getType("BringObject");
		long fusionInterval = Duration.ofSeconds(5).toMillis();
		Rule r = new DeclarativeTypeBasedFusionRule(p1, p2, resultType, fusionInterval);
		BasicToken t1 = new BasicToken(kb);
		t1.setType(kb.getType("BringIntent"));
		BasicToken t2 = new BasicToken(kb);
		t2.setType(kb.getType("PhysicalObject"));
		List<IToken> tokens = List.of(t2, t1);
		List<IToken[]> matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
	
		Assert.assertTrue(matches.size() == 1);
		IToken[] match = matches.get(0);
		Assert.assertTrue(match[0] == t1);
		Assert.assertTrue(match[1] == t2);
	}

	@Test
	public void testNoMatchOutsideFusionInterval() throws Exception {
		Pattern p1 = new PatternBuilder("BringIntent", kb).build();
		Pattern p2 = new PatternBuilder("PhysicalObject", kb).build();
		Type resultType = kb.getType("BringObject");
		long fusionInterval = Duration.ofMillis(200).toMillis();
		Rule r = new DeclarativeTypeBasedFusionRule(p1, p2, resultType, fusionInterval);
		BasicToken t1 = new BasicToken(kb);
		t1.setType(kb.getType("BringIntent"));
		Thread.sleep(Duration.ofSeconds(1).toMillis());
		BasicToken t2 = new BasicToken(kb);
		t2.setType(kb.getType("PhysicalObject"));
		List<IToken> tokens = List.of(t2, t1);
		List<IToken[]> matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
	
		Assert.assertTrue(matches.isEmpty());
	}

	@Test
	public void testMostRecentMatch() throws Exception {
		Pattern p1 = new PatternBuilder("BringIntent", kb).build();
		Pattern p2 = new PatternBuilder("PhysicalObject", kb).build();
		Type resultType = kb.getType("BringObject");
		long fusionInterval = Duration.ofSeconds(10).toMillis();
		Rule r = new DeclarativeTypeBasedFusionRule(p1, p2, resultType, fusionInterval);
		BasicToken t1 = new BasicToken(kb);
		t1.setType(kb.getType("BringIntent"));
		BasicToken t2 = new BasicToken(kb);
		t2.setType(kb.getType("BringIntent"));
		BasicToken t3 = new BasicToken(kb);
		t3.setType(kb.getType("PhysicalObject"));
		List<IToken> tokens = List.of(t3, t2, t1);
		List<IToken[]> matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
	
		Assert.assertTrue(matches.size() == 1);
		IToken[] match = matches.get(0);
		Assert.assertTrue(match[0] == t2);
		Assert.assertTrue(match[1] == t3);
	}

	@Test
	public void testUsedTokensAreNotMatched() throws Exception {
		Pattern p1 = new PatternBuilder("BringIntent", kb).build();
		Pattern p2 = new PatternBuilder("PhysicalObject", kb).build();
		Type resultType = kb.getType("BringObject");
		long fusionInterval = Duration.ofSeconds(10).toMillis();
		Rule r = new DeclarativeTypeBasedFusionRule(p1, p2, resultType, fusionInterval);
		BasicToken t1 = new BasicToken(kb);
		t1.setType(kb.getType("BringIntent"));
		BasicToken t2 = new BasicToken(kb);
		t2.setType(kb.getType("PhysicalObject"));
		LinkedList<IToken> tokens = new LinkedList<IToken>(List.of(t2, t1));

		// before use, tokens are matched
		List<IToken[]> matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
		Assert.assertTrue(matches.size() == 1);
		// use tokens
		r.onMatch(matches, board);
		
		// used tokens are not matched anymore when new token comes in
		BasicToken t3 = new BasicToken(kb);
		t3.setType(kb.getType("PhysicalObject"));
		tokens.addFirst(t3);
		matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
		Assert.assertTrue(matches.isEmpty());
	}

	@Test
	public void testOldUnusedTokensAreNotMatched() throws Exception {
		Pattern p1 = new PatternBuilder("BringIntent", kb).build();
		Pattern p2 = new PatternBuilder("PhysicalObject", kb).build();
		Type resultType = kb.getType("BringObject");
		long fusionInterval = Duration.ofSeconds(10).toMillis();
		Rule r = new DeclarativeTypeBasedFusionRule(p1, p2, resultType, fusionInterval);
		BasicToken t1 = new BasicToken(kb);
		t1.setType(kb.getType("BringIntent"));
		BasicToken t2 = new BasicToken(kb);
		t2.setType(kb.getType("PhysicalObject"));
		BasicToken t3 = new BasicToken(kb);
		t3.setType(kb.getType("PhysicalObject"));
		BasicToken t4 = new BasicToken(kb);
		t4.setType(kb.getType("BringIntent"));
		BasicToken t5 = new BasicToken(kb);
		t5.setType(kb.getType("PhysicalObject"));
		
		// first interaction: e.g. user says "bring this" and points at 2 objects
		// our fusion algorithm assumes that the most recent object is the one that the user intended
		LinkedList<IToken> tokens = new LinkedList<IToken>(List.of(t3, t2, t1));
		List<IToken[]> matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
		Assert.assertTrue(matches.size() == 1);
		IToken[] match = matches.get(0);
		Assert.assertTrue(match[0] == t1);
		Assert.assertTrue(match[1] == t3);
		// use tokens
		r.onMatch(matches, board);
		
		// second interaction: user says "bring this" again but has not pointed yet to an object
		// the new intent t4 should not be fused with the old, unused token t2
		tokens.addFirst(t4);
		matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
		Assert.assertTrue(matches.isEmpty());
		
		// the user now points at the object corresponding to his last intent => now there's a match
		tokens.addFirst(t5);
		matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
		Assert.assertTrue(matches.size() == 1);
		match = matches.get(0);
		Assert.assertTrue(match[0] == t4);
		Assert.assertTrue(match[1] == t5);
	}

	@Test
	public void testFusionResult() throws Exception {
		Pattern p1 = new PatternBuilder("BringIntent", kb).build();
		Pattern p2 = new PatternBuilder("PhysicalObject", kb).build();
		Type resultType = kb.getType("BringObject");
		long fusionInterval = Duration.ofSeconds(10).toMillis();
		Rule r = new DeclarativeTypeBasedFusionRule(p1, p2, resultType, fusionInterval);
		board.addRule(r);
		BasicToken t1 = new BasicToken(kb);
		t1.setType(kb.getType("BringIntent"));
		t1.addAll(Map.of("recipientName","Alice"));
		BasicToken t2 = new BasicToken(kb);
		t2.setType(kb.getType("Pizza"));
		t2.addAll(Map.of("sort", "Hawaii"));
		List<IToken> tokens = List.of(t2, t1);
		
		// check fusion result
		board.addToken(t1);
		board.addToken(t2);
		board.update();
		Assert.assertTrue(board.getActiveTokens().size() == 3);
		List<IToken> results = board.getTokensByType(resultType, false);
		Assert.assertTrue(results.size() == 1);
		IToken result = results.get(0);

		IKBObject intent = result.getResolvedReference("intent");
		Assert.assertTrue(intent != null);
		Assert.assertTrue(intent.getType().getName() == "BringIntent");
		Assert.assertTrue(intent.getString("recipientName").equals("Alice"));
		IKBObject object = result.getResolvedReference("object");
		Assert.assertTrue(object != null);
		Assert.assertTrue(object.getType().getName().equals("Pizza"));
		Assert.assertTrue(object.getString("sort").equals("Hawaii"));
		
		// also test fusion meta information (origin tokens & resulting tokens)
		Assert.assertTrue(result.getOriginTokens().contains(t1));
		Assert.assertTrue(result.getOriginTokens().contains(t2));
		Assert.assertTrue(result.getProducer() == r.getUUID());
		for (BasicToken t : List.of(t1, t2)) {
			MultiValuedMap<UUID, IToken> resultingTokens = t.getResultingTokens();
			Collection<IToken> resultTokens = resultingTokens.get(r.getUUID());
			Assert.assertTrue(resultTokens.size() == 1);
			Assert.assertTrue(resultTokens.stream().findFirst().get() == result);
		}
	}

	@Test
	public void testMultipleMatchesinMultipleIterations() throws Exception {
		Pattern p1 = new PatternBuilder("BringIntent", kb).build();
		Pattern p2 = new PatternBuilder("PhysicalObject", kb).build();
		Type resultType = kb.getType("BringObject");
		long fusionInterval = Duration.ofSeconds(10).toMillis();
		Rule r = new DeclarativeTypeBasedFusionRule(p1, p2, resultType, fusionInterval);
		BasicToken t1 = new BasicToken(kb);
		t1.setType(kb.getType("BringIntent"));
		t1.addAll(Map.of("recipientName","Alice"));
		BasicToken t2 = new BasicToken(kb);
		t2.setType(kb.getType("Pizza"));
		t2.addAll(Map.of("sort", "Hawaii"));
		BasicToken t3 = new BasicToken(kb);
		t3.setType(kb.getType("BringIntent"));
		t3.addAll(Map.of("recipientName","Bob"));
		BasicToken t4 = new BasicToken(kb);
		t4.setType(kb.getType("Pizza"));
		t4.addAll(Map.of("sort", "Salami"));
		BasicToken noMatch = new BasicToken(kb);
		noMatch.setType(kb.getType("Gesture"));

		// only one token => no match yet
		LinkedList<IToken> tokens = new LinkedList<IToken>(List.of(t1));
		List<IToken[]> matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
		Assert.assertTrue(matches.isEmpty());

		// add 2nd token => first matching pair
		tokens.addFirst(t2);
		matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
		Assert.assertTrue(matches.size() == 1);
		// use tokens
		r.onMatch(matches, board);

		// add 3rd token => no match because other tokens were already used
		tokens.addFirst(t3);
		matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
		Assert.assertTrue(matches.isEmpty());

		// add non-matching token => still no match
		tokens.addFirst(noMatch);
		matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
		Assert.assertTrue(matches.isEmpty());

		// add 4th token => second matching pair
		tokens.addFirst(t4);
		matches = r.getCondition().generateMatches(tokens.stream(), r.getTags(), r.getUUID());
		Assert.assertTrue(matches.size() == 1);
		IToken[] match = matches.get(0);
		Assert.assertTrue(match[0] == t3);
		Assert.assertTrue(match[1] == t4);
	}

	@Test
	public void testFusionWithInnerKBObject() throws Exception {
		Pattern p1 = new PatternBuilder("BringIntent", kb).build();
		Pattern p2 = new PatternBuilder("Gesture", kb).build();
		Type resultType = kb.getType("BringObject2");
		long fusionInterval = Duration.ofSeconds(10).toMillis();
		Rule r = new DeclarativeTypeBasedFusionRule(p1, p2, resultType, fusionInterval);
		board.addRule(r);
		BasicToken t1 = new BasicToken(kb);
		t1.setType(kb.getType("BringIntent"));
		t1.addAll(Map.of("recipientName","Alice"));
		BasicToken t2 = new BasicToken(kb);
		t2.setType(kb.getType("Gesture"));
		t2.addAll(Map.of("targetObject", kb.getInstance("pizza1").getUUID().toString()));
		List<IToken> tokens = List.of(t2, t1);

		// check result
		board.addToken(t1);
		board.addToken(t2);
		board.update();
		Assert.assertTrue(board.getActiveTokens().size() == 3);
		List<IToken> results = board.getTokensByType(resultType, false);
		Assert.assertTrue(results.size() == 1);
		IToken result = results.get(0);

		IKBObject intent = result.getResolvedReference("intent");
		Assert.assertTrue(intent != null);
		Assert.assertTrue(intent.getType().getName() == "BringIntent");
		Assert.assertTrue(intent.getString("recipientName").equals("Alice"));
		IKBObject gesture = result.getResolvedReference("gesture");
		Assert.assertTrue(gesture != null);
		Assert.assertTrue(gesture.getType().getName().equals("Gesture"));
		// FIXME: this will fail until our TokenObject class supports KB references
		Assert.assertTrue(gesture.getReference("targetObject").equals(kb.getInstance("pizza1").getUUID()));
		IKBObject object = gesture.getResolvedReference("targetObject");
		Assert.assertTrue(object != null);
		IKBObject pizza1 = kb.getInstance("pizza1");
		Assert.assertTrue(object.getType().getName().equals("Pizza"));
		Assert.assertTrue(object == pizza1);
	}	

	@Test
	public void testInvalidOriginPatterns() throws Exception {
		Pattern invalid = new NonNullPropertiesPattern();
		Pattern p2 = new PatternBuilder("PhysicalObject", kb).build();
		Type resultType = kb.getType("BringObject");
		long fusionInterval = Duration.ofSeconds(5).toMillis();
		exc.expect(Exception.class);
		Rule r = new DeclarativeTypeBasedFusionRule(invalid, p2, resultType, fusionInterval);
	}

	@Test
	public void testInvalidResultType() throws Exception {
		Pattern p1 = new PatternBuilder("BringIntent", kb).build();
		Pattern p2 = new PatternBuilder("PhysicalObject", kb).build();
		// p2 matches any physical object but "BringPizza" expects a pizza
		Type resultType = kb.getType("BringPizza");
		long fusionInterval = Duration.ofSeconds(5).toMillis();
		exc.expect(Exception.class);
		Rule r = new DeclarativeTypeBasedFusionRule(p1, p2, resultType, fusionInterval);
	}
}
