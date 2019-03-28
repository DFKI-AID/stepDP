package de.dfki.step.fusion;

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
            List<String> origin = FusionComponent.mergeOrigins(match.getTokens());
            Optional<Double> confidence = FusionComponent.mergeConfidence(match.getTokens());

            Token token = new Token()
                    .add("intent", "control_car_windows")
                    .add("origin", origin)
                    .addIfPresent("confidence", confidence);

            return token;
        });

        var tokens = List.of(
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





        fc.addTokens(tokens);
        Collection<Token> intents = fc.fuse();
        Assert.assertEquals(2, intents.size());
    }


}
