package de.dfki.tocalog.dialog.sc;

import de.dfki.tocalog.dialog.Intent;

import java.util.Optional;

public class StateChartEvent {
    private Optional<Intent> intent;

    public StateChartEvent(Intent intent) {
        this.intent = Optional.of(intent);
    }

    public StateChartEvent() {
        this.intent = Optional.empty();
    }

    public Optional<Intent> getIntent() {
        return intent;
    }
}
