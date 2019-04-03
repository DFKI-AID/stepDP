package de.dfki.step.sc;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

public class SCEngineTest {

    /**
     * Transition from a state to itself
     */
    @Test
    public void selfTransition() throws IOException, URISyntaxException {
        InputStream scStream = SCEngineTest.class.getResourceAsStream("/selftransition.scxml");
        StateChart sc = Parser.loadStateChart(scStream);
        SCEngine engine = new SCEngine(sc);

        AtomicInteger condCount = new AtomicInteger(0);
        AtomicInteger onFireCount = new AtomicInteger(0);
        AtomicInteger onEnterCount = new AtomicInteger(0);
        AtomicInteger onExitCount = new AtomicInteger(0);


        engine.addFunction("onFire", () -> {
            onFireCount.incrementAndGet();
        });

        engine.addFunction("onEnter", () -> {
            onEnterCount.incrementAndGet();
        });

        engine.addFunction("onExit", () -> {
            onExitCount.incrementAndGet();
        });


        engine.addCondition("cond",() -> {
            condCount.incrementAndGet();
            return true;
        });

        engine.fire("event1");
        engine.fire("event1");
        engine.fire("event2");


        Assert.assertEquals(condCount.get(), 2);
        Assert.assertEquals(onEnterCount.get(), 2);
        Assert.assertEquals(onFireCount.get(), 2);
        Assert.assertEquals(onExitCount.get(), 3);

    }
}
