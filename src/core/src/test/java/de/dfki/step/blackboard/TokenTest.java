package de.dfki.step.blackboard;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.Type;

public class TokenTest {
    private Board board = new Board();
    private KnowledgeBase kb = new KnowledgeBase(board);

    @Rule
    public ExpectedException exc = ExpectedException.none();

    @Test
    public void testCreateCopyWithChanges() throws Exception {
        kb.addType(new Type("Pizza", kb));
        IKBObject pizza1 = kb.createInstance("pizza1", kb.getType("Pizza"));
        IKBObject pizza2 = kb.createInstance("pizza1", kb.getType("Pizza"));

        BasicToken original = new BasicToken(kb);
        original.setType(this.kb.getRootType());
        original.addAll(Map.of(
                           "object", pizza1.getUUID().toString(),
                           "recipient", "Lara",
                           "time", Map.of(
                                   "start", Map.of(
                                           "hour", 12,
                                           "minute", 30
                                           ),
                                   "end", Map.of(
                                           "hour", 13,
                                           "minute", 0
                                           )
                                   ),
                           "targetLocation", Map.of(
                                   "x", 3, 
                                   "y", 3,
                                   "z", 1
                                   ),
                           "notes", "cut pizza"
                        ));
        Map<String, Object> newValues =  new HashMap<String, Object>(
                                            Map.of(
                                                "count", 2,
                                                "startLocation", Map.of(
                                                        "x", 5, 
                                                        "y", 9,
                                                        "z", 0
                                                        ),
                                                "recipient", "Joachim",
                                                "time", Map.of(
                                                        "end", Map.of(
                                                                "minute", 30
                                                                )
                                                        ),
                                                "targetLocation", Map.of(
                                                        "x", 4, 
                                                        "y", 5,
                                                        "z", 6
                                                        )
                                            )
                                          );
        Map<String, Object> correct = new HashMap<String, Object>(
                                        Map.of(
                                            "type", this.kb.getRootType().getName(),
                                            "count", 2,
                                            "startLocation", Map.of(
                                                    "x", 5, 
                                                    "y", 9,
                                                    "z", 0
                                                    ),
                                            "object", pizza1.getUUID().toString(),
                                            "recipient", "Joachim",
                                            "time", Map.of(
                                                    "start", Map.of(
                                                            "hour", 12,
                                                            "minute", 30
                                                            ),
                                                    "end", Map.of(
                                                            "hour", 13,
                                                            "minute", 30
                                                            )
                                                    ),
                                            "targetLocation", Map.of(
                                                    "x", 4, 
                                                    "y", 5,
                                                    "z", 6
                                                    ),
                                            "notes", "cut pizza"
                                         )
                                        );
        Assert.assertEquals(((BasicToken) original.internal_createCopyWithChanges(newValues)).getPayload(), correct);

        newValues.put("object", pizza2.getUUID().toString());
        correct.put("object", pizza2.getUUID().toString());
        Assert.assertEquals(((BasicToken) original.internal_createCopyWithChanges(newValues)).getPayload(), correct);

        newValues.put("object", Map.of("size", 30));
        exc.expect(Exception.class);
        original.internal_createCopyWithChanges(newValues);
    }

}
