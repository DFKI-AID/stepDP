package de.dfki.rs;

/**
 */
public class Tuple<S, T> {
    public final S first;
    public final T second;

    public Tuple(S s, T t) {
        this.first = s;
        this.second = t;
    }

    public static <S, T> Tuple<S, T> of(S first, T second) {
        return new Tuple<>(first, second);
    }
}
