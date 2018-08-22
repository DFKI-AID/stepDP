package de.dfki.tocalog.output.impp;

/**
 */
public class AllocationState {
    public enum State {
        NONE,
        INIT,
        PRESENTING,
        PAUSED,
        CANCEL,
        CANCELED,
        SUCCESS,
        ERROR,
        TIMEOUT
    }

    private State state;
    private Exception error;

    public AllocationState(AllocationState other) {
        this.state = other.state;
        this.error = other.error;
    }

    public AllocationState(State state) {
        this.state = state;
    }

    public AllocationState(State state, Exception error) {
        this.state = state;
        this.error = error;
    }

    public State getState() {
        return state;
    }

    public boolean active() {
        return state == State.INIT || state == State.PRESENTING || state == State.PAUSED;
    }

    public boolean failed() {
        return state == State.ERROR || state == State.TIMEOUT;
    }

    public boolean finished() {
        return failed() || state == State.SUCCESS || state == State.CANCELED;
    }

    public static AllocationState NONE = new AllocationState(State.NONE);

    public static AllocationState Error(Exception error) {
        return new AllocationState(State.ERROR, error);
    }

    public boolean presenting() {
        return state == State.PRESENTING;
    }

    public boolean initializing() {
        return state == State.INIT;
    }

    public boolean successful() {
        return state == State.SUCCESS;
    }

    public boolean cancelling() {
        return state == State.CANCEL;
    }

    public boolean cancelled() {
        return state == State.CANCELED;
    }

    public boolean unknown() {
        return state == State.NONE;
    }
}
