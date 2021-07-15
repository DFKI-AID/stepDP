package de.dfki.step.blackboard;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.IKBObjectWriteable;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.PropString;
import de.dfki.step.kb.semantic.Type;

public class TokenTest {
	private Board board = new Board();
	private KnowledgeBase kb = new KnowledgeBase(board);

	public KnowledgeBase getKB() {
		return this.kb;
	}

	public Board getBlackboard() {
		return this.board;
	}
	
	@Before
	public void setUp() throws Exception {
        Type GreetingIntent = new Type("GreetingIntent", this.getKB());
        GreetingIntent.addInheritance(this.getKB().getType("UserIntent"));
        this.getKB().addType(GreetingIntent);

        Type PickIntent = new Type("PickIntent", this.getKB());
        PickIntent.addInheritance(this.getKB().getType("UserIntent"));
        PickIntent.addProperty(new PropString("bringTo", this.getKB()));
        this.getKB().addType(PickIntent);

        Type Bottle = new Type("Bottle", this.getKB());
        Bottle.addProperty(new PropString("material", this.getKB()));
        this.getKB().addType(Bottle);

        IKBObjectWriteable Bottle42 = this.getKB().createInstance("Bottle42", Bottle);
        Bottle42.setString("material", "glas");
        Bottle42.setInteger("size", 500);
	}

	@Test
	public void testCreateTokenWithSameContent() {
		// test simple basic token
        BasicToken t1 = new BasicToken(this.getKB());
        t1.setType(this.getKB().getType("GreetingIntent"));
        BasicToken t1Copy = (BasicToken) t1.createTokenWithSameContent();
        Assert.assertEquals(t1Copy.getType(), t1.getType());
        Assert.assertEquals(t1Copy.getPayload(), t1.getPayload());

        // test kb token
        IKBObject Bottle42 = this.getKB().getInstance("Bottle42");
        KBToken t2 = new KBToken(this.getKB(), Bottle42);
        IToken t2Copy = (KBToken) t2.createTokenWithSameContent();
        Assert.assertEquals(t2Copy.getType(), t2.getType());
        Assert.assertEquals(t2Copy.getString("material"), Bottle42.getString("material"));
        Assert.assertTrue(t2Copy.getInteger("size").equals(Bottle42.getInteger("size")));

        // test basic token with inner object
        BasicToken t3 = new BasicToken(this.getKB());
        t3.setType(this.getKB().getType("PickIntent"));
        Map<String, Object> payload = Map.of(
				"object", Map.of("type","Bottle",
						 "material", "glas",
						 "size", 500
						),
				"bringTo", "sara",
				"additionalInfo", "blubb"
        		);
		t3.addAll(payload);
        IToken t3Copy = t3.createTokenWithSameContent();
        Assert.assertEquals(t3Copy.getType(), t3.getType());
        IKBObject copy = t3Copy.getResolvedReference("object");
        Map<String, Object> original = (Map<String, Object>) payload.get("object");
        Assert.assertEquals(copy.getType().getName(), original.get("type"));
        Assert.assertEquals(copy.getString("material"), original.get("material"));
        Assert.assertTrue(copy.getInteger("size").equals(original.get("size")));
        Assert.assertEquals(t3Copy.getString("bringTo"), payload.get("bringTo"));
        Assert.assertEquals(t3Copy.getString("additionalInfo"), payload.get("additionalInfo"));
        

        // test basic token with inner kb object
        BasicToken t4 = new BasicToken(this.getKB());
		t4.setType(this.getKB().getType("PickIntent"));
        t4.addAll(Map.of(
				"object", Bottle42.getUUID().toString()
				)
	   	);
        IToken t4Copy = t4.createTokenWithSameContent();
        Assert.assertEquals(t4Copy.getType(), t4.getType());
        Assert.assertEquals(t4Copy.getResolvedReference("object"), Bottle42);

	}
}
