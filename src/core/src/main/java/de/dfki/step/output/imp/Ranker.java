package de.dfki.step.output.imp;


/**
 */
public interface Ranker<T> {
    Rank rank(T t);
}
