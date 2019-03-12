package de.dfki.step.output.impp;


/**
 */
public interface Ranker<T> {
    Rank rank(T t);
}
