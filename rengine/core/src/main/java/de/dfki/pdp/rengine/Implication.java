package de.dfki.pdp.rengine;

/**
 *
 */
public class Implication {
    private final Token data;
    private final Runnable implication;

    public Implication(Token data, Runnable implication) {
        this.data = data;
        this.implication = implication;
    }

    public Token getData() {
        return data;
    }

    public Runnable getImplication() {
        return implication;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public void execute() {
        this.implication.run();
    }

    public static Builder builder(Runnable implication) {
        return new Builder(implication);
    }

    public static Implication empty() {
        return EMPTY;
    }

    public final static Implication EMPTY = new Implication(new Token(), () -> {});

    public static class Builder {
        private final Runnable implication;
        private final Token.Builder builder = Token.builder();

        public Builder(Runnable implication) {
            this.implication = implication;
        }

        public Builder add(String key, Object value) {
            builder.add(key, value);
            return this;
        }

        public Implication build() {
            return new Implication(builder.build(), implication);
        }
    }
}
