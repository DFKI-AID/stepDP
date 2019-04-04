package de.dfki.step.sc;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

/**
 * Manages the state of state chart. If events are fired (@link {@link #fire(String)},
 * it checks conditions to fire transitions. OnEntry and OnExit functions are fired as well.
 *
 * TODO onEnter in intial is not called
 */
public class SCEngine {
    private static final Logger log = LoggerFactory.getLogger(SCEngine.class);
    private final StateChart stateChart;
    private ObjState objState = new ObjState("N/A");

    public static class ObjState {
        private final String currentState;
        private PMap<String, BooleanSupplier> conditions = HashTreePMap.empty();
        private PMap<String, Runnable> functions = HashTreePMap.empty();

        public ObjState(String currentState) {
            this.currentState = currentState;
        }

        public ObjState setState(String state) {
            return copy(state);
        }

        public ObjState addCondition(String id, BooleanSupplier condition) {
            ObjState newState = copy();
            newState.conditions = conditions.plus(id, condition);
            return newState;
        }

        public ObjState addFunction(String id, Runnable onEntry) {
            ObjState newState = copy();
            newState.functions = functions.plus(id, onEntry);
            return newState;
        }

        public ObjState copy() {
            return copy(currentState);
        }

        private ObjState copy(String currentState) {
            ObjState newState = new ObjState(currentState);
            newState.conditions = conditions;
            newState.functions = functions;
            return newState;
        }
    }

    public ObjState createSnapshot() {
        return objState;
    }

    public void loadSnapshot(ObjState objState) {
        this.objState = objState;
    }


    public SCEngine(StateChart stateChart) {
        this.stateChart = stateChart;
        reset();
    }

    /**
     * Fires an event into the state chart. Transitions are triggered if the event matches and their conditions are
     * fulfilled. On-entry and on-exit functions of states are called as will if available.
     * <p>
     * TODO if multiple transitions are available, they could be chosen randomly?
     *
     * @param event
     * @return true if a transition fired
     */
    public boolean fire(String event) {
        // Get all transitions that can fire given the event their conditions
        List<Transition> transitions = stateChart.getTransitions(getCurrentState());
        List<Transition> transitionCandidates = transitions.stream()
                .filter(t -> Objects.equals(t.getEvent(), event))
                .filter(t -> !t.hasCond() || checkCondition(t.getCond()))
                .collect(Collectors.toList());


        if (transitionCandidates.isEmpty()) {
            log.warn("no transition candidate found for state {} and event {}", getCurrentState(), event);
            return false;
        }

        // TODO slect transition randomly?
        Transition transition = transitionCandidates.get(0);

        Optional<State> optSourceState = stateChart.getState(getCurrentState());
        if (!optSourceState.isPresent()) {
            log.error("Can't fire transition: Source state {} is not available. Missing state in state chart?", getCurrentState());
            return false;
        }
        State sourceState = optSourceState.get();
        String targetStateId = transition.getTarget();
        log.info("state change {}->{}", getCurrentState(), targetStateId);


        // Update current state
        Optional<State> optTargetState = stateChart.getState(targetStateId);
        if (!optTargetState.isPresent()) {
            log.error("Can't fire transition: Target state {} is not available. Missing state in state chart?", targetStateId);
            return false;
        }
        objState = objState.setState(targetStateId);

        // Trigger on-exit functions if available
        sourceState.getOnExits().forEach(oe -> oe.getScripts().forEach(s -> {
            if (!objState.functions.containsKey(s)) {
                log.warn("No script={} found for on-exit during transission {}-{}|{}|->{}",
                        s, sourceState.getId(), transition.getEvent(), transition.getCond(), targetStateId);
                return;
            }
            objState.functions.get(s).run();
        }));

        // Trigger on-transitions functions if available
        transition.getScripts().forEach(s -> {
            if (!objState.functions.containsKey(s)) {
                log.warn("No script={} found for transition during transission {}-{}|{}|->{}",
                        s, sourceState.getId(), transition.getEvent(), transition.getCond(), targetStateId);
                return;
            }
            objState.functions.get(s).run();
        });

        // Trigger on-entry functions if available
        State targetState = optTargetState.get();
        targetState.getOnEntries().forEach(oe -> oe.getScripts().forEach(s -> {
            if (!objState.functions.containsKey(s)) {
                log.warn("No script={} found for on-entry during transission {}-{}|{}|->{}",
                        s, sourceState.getId(), transition.getEvent(), transition.getCond(), targetStateId);
                return;
            }
            objState.functions.get(s).run();
        }));

        return true;
    }

    public String getCurrentState() {
        return objState.currentState;
    }

    public Collection<String> getStates() {
        return stateChart.getStates().stream().map(s -> s.getId()).collect(Collectors.toList());
    }

    public boolean checkCondition(String id) {
        if (!objState.conditions.containsKey(id)) {
            log.warn("Can't check condition {}: Not available", id);
            return false;
        }

        boolean conditionFulfilled = objState.conditions.get(id).getAsBoolean();
        return conditionFulfilled;
    }

    public void addCondition(String id, BooleanSupplier condition) {
        if (objState.conditions.containsKey(id)) {
            log.info("Overwriting condition for {}", id);
        } else {
            log.info("Adding new condition for {}", id);
        }
        objState = objState.addCondition(id, condition);
    }

    public void addFunction(String id, Runnable fnc) {
        if (objState.functions.containsKey(id)) {
            log.info("Overwriting function for {}", id);
        } else {
            log.info("Adding function for {}", id);
        }
        objState = objState.addFunction(id, fnc);
    }

    /**
     * Looks via reflection for functions of the form void -> void.
     * Those functions are registered as functions that can be triggered by transitions (on-entry / on-exit)
     *
     * @param provider
     */
    public void addFunctions(Object provider) {
        List<Method> methods = this.getAllMethods(provider.getClass());
        methods.stream()
                .filter(m -> m.getReturnType() == Void.class || m.getReturnType() == void.class)
                .filter(m -> m.getParameterCount() == 0)
                .forEach(m -> addFunction(m.getName(), () -> {
                    try {
                        m.invoke(provider);
                    } catch (Exception e) {
                        log.error("could not execute function {}", m.getName(), e);
                    }
                }));
    }

    /**
     * @param clazz
     * @return All methods of the given class including the methods of the parent class
     */
    public static List<Method> getAllMethods(Class clazz) {
        List<Method> methods = new ArrayList<>();
        while (clazz != null) {
            methods.addAll(List.of(clazz.getDeclaredMethods()));
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    /**
     * Looks via reflection for functions of the form void -> bool.
     * Those functions are registered as functions that can be triggered by transitions to check conditions
     *
     * @param provider
     */
    public void addConditions(Object provider) {
        List<Method> methods = this.getAllMethods(provider.getClass());
        methods.stream()
                .filter(m -> m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)
                .filter(m -> m.getParameterCount() == 0)
                .forEach(m -> addCondition(m.getName(), () -> {
                    try {
                        Object result = m.invoke(provider);
                        return (Boolean) result;
                    } catch (Exception e) {
                        log.error("could not execute function {}", m.getName(), e);
                        return false;
                    }
                }));
    }

    public void reset() {
        objState = objState.setState(stateChart.getInitialState());
    }

    public String getInitialState() {
        return stateChart.getInitialState();
    }

    public StateChart getStateChart() {
        return stateChart;
    }
}
