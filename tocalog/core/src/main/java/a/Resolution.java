package a;

import de.dfki.tocalog.core.Hypothesis;

/**
 * TODO: maybe: individual functions, maybe lower level e.g. working on speech input?
 */
public interface Resolution {
    Hypothesis process(Hypothesis hypothesis);
}
