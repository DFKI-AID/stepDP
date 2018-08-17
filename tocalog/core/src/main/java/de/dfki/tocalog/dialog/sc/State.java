package de.dfki.tocalog.dialog.sc;

/**
 */
public abstract class State {
    private final String id;

    public State(String id) {
        this.id = id;
    }

    private State(Builder builder) {
        this.id = builder.id;
    }

    public String getId() {
        return id;
    }

    protected abstract void onEntry();

    protected abstract void onExit();

    public static Builder create(String id) {
        return new Builder(id);
    }

    public static class Builder {
        public interface OnEntry {
            void onEntry();
        }

        public interface OnExit {
            void onExit();
        }

        private final String id;
        private Runnable onEntryFnc;
        private Runnable onExitFnc;

        public Builder(String id) {
            this.id = id;
        }

        public State build() {
            return new State(this) {
                @Override
                protected void onEntry() {
                    if (onEntryFnc == null) {
                        return;
                    }
                    onEntryFnc.run();
                }

                @Override
                protected void onExit() {
                    if (onExitFnc == null) {
                        return;
                    }
                    onExitFnc.run();
                }
            };
        }

        public Builder onExit(Runnable onExit) {
            this.onExitFnc = onExit;
            return this;
        }

        public Builder onEntry(Runnable onEntry) {
            this.onEntryFnc = onEntry;
            return this;
        }
    }


}
