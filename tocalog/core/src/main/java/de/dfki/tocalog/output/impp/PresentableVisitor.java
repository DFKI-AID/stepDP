package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.output.Output;

import java.util.*;
import java.util.function.Consumer;

/**
 */
public class PresentableVisitor implements OutputNode.Visitor {

    private Stack<Result> resultStack = new Stack();

    private static class Result {
        public OutputNode.Semantic currentSemantic;
        public boolean presentable = true;
        public List<Output> unassigned = new ArrayList<>();

    }

    @Override
    public void visitLeaf(OutputNode.Leaf leaf) {
        Result result = new Result();
        if (leaf.getServices().isEmpty()) {
            result.unassigned.add(leaf.getOutput());
        }
        result.presentable = !leaf.getServices().isEmpty();
        resultStack.push(result);
    }

    @Override
    public void visitInnerNode(OutputNode.InnerNode node) {
        Result result = new Result();
        result.currentSemantic = node.getSemantic();
        resultStack.push(result);

        Consumer<OutputNode> visitChild = (childNode) -> {
            childNode.accept(this);
            Result subResult = resultStack.pop();
            result.unassigned.addAll(subResult.unassigned);
        };

        Runnable visitAll = () -> {
            for (OutputNode childNode : node.getChildNodes()) {
                visitChild.accept(childNode);
            }
        };

        Runnable visitFirst = () -> {
            if(node.getChildNodes().isEmpty()) {
                return;
            }

            node.getChildNodes().get(0).accept(this);
        };


        switch (result.currentSemantic) {
            case redundant:
                visitAll.run();
                result.presentable &= result.unassigned.size() < node.getChildNodes().size();
                break;
            case complementary:
            case concurrent:
                visitAll.run();
                result.presentable &= result.unassigned.isEmpty();
                break;
            case alternative:
                result.presentable = false;
                for (OutputNode childNode : node.getChildNodes()) {
                    childNode.accept(this);
                    Result subResult = resultStack.pop();
                    if(subResult.presentable) {
                        result.presentable = true;
                        break;
                    }
                }
//                result.presentable &= result.unassigned.size() < node.getChildNodes().size();
                break;
            case sequential:
                visitFirst.run();
                result.presentable &= result.unassigned.isEmpty();
                break;
        }


    }

    public boolean isPresentable(OutputNode node) {
        resultStack.clear();
        node.accept(this);
        Result result = resultStack.pop();
        return result.presentable;
    }
}
