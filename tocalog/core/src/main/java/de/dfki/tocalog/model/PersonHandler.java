package de.dfki.tocalog.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 */
public class PersonHandler {

    public static Optional<Integer> findAge(Map<String, Object> data) {
        return Optional.ofNullable(getAge(data));
    }

    public static Integer getAge(Map<String, Object> data) {
        return (Integer) data.get("age");
    }
    public static void setAge(Map<String, Object> data, int age) {
        data.put("age", age);
    }

    public static void main(String[] args) {
        Map<String, Object> person = new HashMap<>();
        Map<String, Object> building = new HashMap<>();
        setAge(building, 123);

        if(findAge(person).orElse(0) > 240) {
            //
        }
    }
}
