package de.dfki.step.fusion;

/**
 * Get the time interval of inputs = [min, max] where min is the timestamp of the earliest input and max the latest
 */
public class TimestampVisitor implements FusionNode.Visitor {
    private Range range;
    public Range accept(FusionNode node) {
        range = new Range();
        node.accept(this);
        return range;
    }

    @Override
    public void visit(InputNode input) {

    }

    @Override
    public void visit(ParallelNode input) {

    }

    public static class Range {
        protected long start;
        protected long end;

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }
    }
}
