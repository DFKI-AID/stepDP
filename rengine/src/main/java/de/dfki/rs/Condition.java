package de.dfki.rs;

import java.util.Objects;

/**
 *
 */
public interface Condition {
    boolean test();


    public static void main(String[] args) {
        String s = "blue";
        Condition c = () -> Objects.equals(s, "red");

        var c2 = new Condition() {

            @Override
            public boolean test() {
                String s = "blue"; //from db
                return Objects.equals(s, "red");
            }
        };

//        c.test()
    }
}
