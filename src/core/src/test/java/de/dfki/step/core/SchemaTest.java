package de.dfki.step.core;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.function.Predicate;

public class SchemaTest {

    @Test
    public void humanSchema() {
		/*
        // define schema as a set of constraints
        Schema schema = Schema.builder()
                .describe("string as id")
                .isSet(Schema.Key.of("id"), String.class)

                .describe("age as integer")
                .greaterThan(Schema.Key.of("age"), 0)

                .describe("if age and birthday are set, they have to match")
                .add(t -> {
                    // if age is set, then the birthday has to match
                    Optional<Integer> age = t.get("age", Integer.class);
                    if (!age.isPresent()) {
                        return true;
                    }
                    Optional<LocalDate> date = t.get("birthday", LocalDate.class);
                    if (!date.isPresent()) {
                        return true;
                    }

                    var period = Period.between(date.get(), LocalDate.now());
                    return period.getYears() == age.get();
                })
                .build();

        Token human1 = Token.builder()
                .add("id", "m1")
                .add("age", 21)
                .add("birthday", LocalDate.of(1999, 1, 1))
                .build();
        Assert.assertTrue(schema.matches(human1));


        var human2 = human1.add("age", 22);
        Assert.assertFalse(schema.matches(human2));

        var human3 = human1.remove("id");
        Assert.assertFalse(schema.matches(human3));

        var human4 = human1.add("age", "FIVE");
        Assert.assertFalse(schema.matches(human4));
		*/
    }
}
