package de.dfki.tocalog.core;

import de.dfki.tocalog.input.Input;

import java.util.Collection;

/**
 * TODO instead of inputs it could return a more sophisticated data structure?
 */
public interface InputComponent  {
    Collection<Input> process(Event event);
//    void init(Context context);
//
//    interface Context {
//        KnowledgeBase getKnowledgeBase();
//        EventEngine getEventEngine();
//    }
}
