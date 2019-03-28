package de.dfki.step.core;

import org.junit.Assert;
import org.junit.Test;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TokenTest {

    @Test
    public void simpleValue() {
        Token token = new Token()
                .add("key", 123);

        var opt = token.get("key");
        Assert.assertTrue(opt.isPresent());
        Assert.assertEquals(opt.get(), 123);
    }

    @Test
    public void nestedMap() {
        PMap<String, Object> data = HashTreePMap.empty();
        data = data.plus("b", 123);

        Token token = new Token()
                .add("a", data);

        var opt = token.get("a", "b");
        Assert.assertTrue(opt.isPresent());
        Assert.assertEquals(opt.get(), 123);
    }

    @Test
    public void nextedToken() {
        Token innerToken = new Token()
                .add("b", 123);

        Token token = new Token()
                .add("a", innerToken);

        var opt = token.get("a", "b");
        Assert.assertTrue(opt.isPresent());
        Assert.assertEquals(opt.get(), 123);
    }

    @Test
    public void jsonTest() {
        String json = "{\"age\": 123, \"a\":{\"b\": 567}}";
        Token token = null;
        try {
            token = Token.fromJson(json);

            var age = token.get("age");
            Assert.assertTrue(age.isPresent());
            Assert.assertEquals(age.get(), 123);

            var val = token.get("a", "b");
            Assert.assertTrue(val.isPresent());
            Assert.assertEquals(val.get(), 567);

        } catch (IOException e) {
            Assert.fail("could not parse json into token: " + e);
        }

    }
}
