package de.dfki.tocalog.output.impp;


/**
 */
public interface Ranker<T> {
    Rank rank(T t);
}
