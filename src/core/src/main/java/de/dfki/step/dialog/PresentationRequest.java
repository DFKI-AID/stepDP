package de.dfki.step.dialog;

/**
 * @Deprecated replace with Token
 */
public class PresentationRequest {
    private final Object content;

    public PresentationRequest(Object content) {
        this.content = content;
    }

    public Object getContent() {
        return content;
    }
}
