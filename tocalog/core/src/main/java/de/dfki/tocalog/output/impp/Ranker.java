package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.model.Service;
import de.dfki.tocalog.output.OutputAct;

/**
 */
public interface Ranker<T> {
    Rank rank(T t);
}
