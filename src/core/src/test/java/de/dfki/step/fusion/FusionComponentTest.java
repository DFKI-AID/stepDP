package de.dfki.step.fusion;

import de.dfki.step.core.InputComponent;
import de.dfki.step.core.Schema;
import de.dfki.step.core.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 */
public class FusionComponentTest {

    @Test
    public void allWindows() {
        FusionComponent fc = new FusionComponent();

        // define how our multimodal inputs looks like
        // here we want to combine three unimodal inputs similar to the madmacs demonstrator:
        // hand gesture, focus and speech to control the car windows

        // using a schema to match the token
        Schema speechOnWindows = Schema.builder()
                .equalsOneOf(Schema.Key.of("gesture"), "down", "up")
                .build();
        InputNode gestureNode = new InputNode(speechOnWindows);

        // can also check the data of the token directly:
        InputNode windowsNode = new InputNode(t -> t.payloadEqualsOneOf("speech", "all_windows", "front left window"));
        InputNode focusNode = new InputNode(t -> t.payloadEquals("focus", "car"));
        ParallelNode node = new ParallelNode()
                .add(gestureNode)
                .add(windowsNode)
                .add(focusNode);

        // what should happen if the three input nodes match on the input history
        fc.addFusionNode("all_windows1", node, match -> {
            Token token = FusionComponent.defaultIntent(match, "control_car_windows");
            // the gesture node is always present because we did not use an OptionalNode
            Token gestureToken = match.getToken(gestureNode).get();
            token = token.add("direction", gestureToken.get("gesture", String.class).get());

            return token;
        });

        var tokens = Set.of(
                new Token()
                        .add("gesture", "down")
                        .add("timestamp", System.currentTimeMillis())
                        .add("origin", UUID.randomUUID().toString())
                        .add("confidence", 0.2)
                ,
                new Token()
                        .add("gesture", "up")
                        .add("timestamp", System.currentTimeMillis())
                        .add("origin", UUID.randomUUID().toString())
                        .add("confidence", 0.8)
                ,
                new Token()
                        .add("speech", "all_windows")
                        .add("timestamp", System.currentTimeMillis())
                        .add("origin", UUID.randomUUID().toString())
                        .add("confidence", 0.4)
//                ,
//                new Token()
//                        .add("speech", "front left window")
//                        .add("timestamp", 1000L)
//                        .add("origin", UUID.randomUUID().toString())
//                        .add("confidence", 0.8)
                ,
                new Token()
                        .add("focus", "car")
                        .add("timestamp", System.currentTimeMillis())
                        .add("origin", UUID.randomUUID().toString())
//                ,
//                new Token()
//                        .add("speech", "fast")
//                        .add("timestamp", 1000L)
//                        .add("origin", UUID.randomUUID().toString())
//                        .add("confidence", 0.4)
//                ,
//                new Token()
//                        .add("focus", "desktop")
//                        .add("timestamp", 1000L)
//                        .add("origin", UUID.randomUUID().toString())
//                        .add("confidence", 0.6)
        );


        Collection<Token> intents = fc.fuse(tokens);
        Assert.assertEquals(2, intents.size());
        intents.stream().forEach(t -> {
            Assert.assertTrue(t.payloadEqualsOneOf("direction", String.class, "up", "down"));
        });
    }


}
