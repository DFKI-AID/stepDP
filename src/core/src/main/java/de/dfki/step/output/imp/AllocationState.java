package de.dfki.step.output.imp;

/**
 * TODO: encapsulate enum inside the class
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

    public boolean canceled() {
        return state == State.CANCELED;
    }

    public boolean unknown() {
        return state == State.NONE;
    }

    public Exception getErrorCause() {
        return error;
    }

    private static final AllocationState None = new AllocationState(State.NONE);
    private static final AllocationState Init = new AllocationState(State.INIT);
    private static final AllocationState Presenting = new AllocationState(State.PRESENTING);
    private static final AllocationState Paused = new AllocationState(State.PAUSED);
    private static final AllocationState Cancel = new AllocationState(State.CANCEL);
    private static final AllocationState Canceled = new AllocationState(State.CANCELED);
    private static final AllocationState Success = new AllocationState(State.SUCCESS);
    private static final AllocationState Timeout = new AllocationState(State.TIMEOUT);

    public static AllocationState getNone() {
        return None;
    }

    public static AllocationState getInit() {
        return Init;
    }

    public static AllocationState getPresenting() {
        return Presenting;
    }

    public static AllocationState getPaused() {
        return Paused;
    }

    public static AllocationState getCancel() {
        return Cancel;
    }

    public static AllocationState getCanceled() {
        return Canceled;
    }

    public static AllocationState getSuccess() {
        return Success;
    }

    public static AllocationState getTimeout() {
        return Timeout;
    }

    public static AllocationState getError(Exception ex) {
        return new AllocationState(State.ERROR, ex);
    }

    public static AllocationState getError(String err) {
        return getError(new Exception(err));
    }

    @Override
    public String toString() {
        return "AllocationState{" +
                "state=" + state +
                (error == null ? "" : (", error=" + error)) +
                '}';
    }
}
