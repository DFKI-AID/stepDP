package de.dfki.step.kb;

import de.dfki.step.blackboard.Board;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.graph.Graph;
import de.dfki.step.kb.semantic.Type;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GraphTest {
    private Board board = new Board();
    private KnowledgeBase kb = new KnowledgeBase(board);
    @Test
    public void GraphTest() throws Exception {

        Type T = new Type("default", kb);
        IKBObject plant =  kb.createInstance("plant", T);
        IKBObject tree =  kb.createInstance("tree", T);
        IKBObject apple_tree =  kb.createInstance("apple tree", T);
        IKBObject apple =  kb.createInstance("apple", T);


        Graph G = new Graph();
        UUID E1 = G.createEdge(tree, plant, "is");
        UUID E2 = G.createEdge(apple_tree, tree, "is");
        UUID E3 = G.createEdge(apple, apple_tree, "grows on");
        G.saveEdges("edges.json");
        G.deleteEdge(E2);
        List<String> names = Arrays.asList("tree");
        ArrayList<IKBObject> nodes = G.getNodesBelow(plant);
        int index = 0;
        for (IKBObject node:  nodes)
        {
            Assert.assertTrue(node.getName().equals(names.get(index)));
            index++;

        }


        List<IKBObject> nodes_neighbours = G.findRelation("grows on", apple_tree);
        Assert.assertTrue(nodes_neighbours.get(0).getName().equals("apple"));

        G.loadEdges("edges.json");

        names = Arrays.asList("tree", "apple tree", "apple");
        nodes = G.getNodesBelow(plant);
        index = 0;
        for (IKBObject node:  nodes)
        {
            Assert.assertTrue(node.getName().equals(names.get(index)));
            index++;

        }
    }
}
