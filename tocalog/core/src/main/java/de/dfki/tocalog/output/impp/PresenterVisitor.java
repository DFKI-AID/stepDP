package de.dfki.tocalog.output.impp;

/**
 */
public class PresenterVisitor implements OutputNode.Visitor {
    private OutputNode.InnerNode.Builder currentBuilder;
    private PresentableVisitor presentableVisitor = new PresentableVisitor();
    private CopyVisitor copyVisitor = new CopyVisitor();

    public void visitInnerNode(OutputNode.InnerNode node) {
        OutputNode.InnerNode.Builder builder = currentBuilder;
        currentBuilder = OutputNode.buildNode(node.getSemantic());
        node.getId().ifPresent(id -> currentBuilder.setId(id));

        switch (node.getSemantic()) {
            case redundant:
                for(OutputNode childNode : node.getChildNodes()) {
                    if(!presentableVisitor.isPresentable(childNode)) {
                        continue;
                    }
                    this.visit(childNode);
                }
                break;
            case complementary:
            case concurrent:
                for(OutputNode childNode : node.getChildNodes()) {
                    childNode.accept(this);
                }
                break;
            case alternative:
                for(OutputNode childNode : node.getChildNodes()) {
                    if(!presentableVisitor.isPresentable(childNode)) {
                        continue;
                    }
                    this.visit(childNode);
                    break;
                }
                break;
            case sequential:
                if(node.getChildNodes().isEmpty()) {
                    break;
                }
                node.getChildNodes().get(0).accept(this);
                break;
        }

        if(builder != null) {
            builder.addNode(currentBuilder.build());
            currentBuilder = builder;
        }
    }

    public void visitLeaf(OutputNode.Leaf leaf) {
        //TODO does not work leaf only trees

        OutputNode.Leaf leafCopy = leaf.copy();
        currentBuilder.addNode(leafCopy);
    }

    public void visit(OutputNode node) {
//        copyVisitor.visit(node);
//        result = copyVisitor.copy();
        node.accept(this);
    }

    public OutputNode getResult() {
        return currentBuilder.build();
    }
}
