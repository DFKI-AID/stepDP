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
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class GraphTest {
    private Board board = new Board();
    private KnowledgeBase kb = new KnowledgeBase(board);
    @Test
    public void GraphTestBelow() throws Exception {

        Type T = new Type("default", kb);
        IKBObject plant =  kb.createInstance("plant", T);
        IKBObject tree =  kb.createInstance("tree", T);
        IKBObject apple_tree =  kb.createInstance("apple tree", T);
        IKBObject apple =  kb.createInstance("apple", T);


        Graph G = new Graph();
        G.createNode(plant);
        G.createNode(tree);
        G.createNode(apple_tree);
        G.createNode(apple);

        G.createEdge(tree, plant, "is");
        G.createEdge(apple_tree, tree, "is");
        G.createEdge(apple, apple_tree, "grows on");
        G.saveData("data.json");
        G.deleteEdge(apple_tree, tree, "is");
        List<String> names = Arrays.asList("tree");
        Collection<String> nodes = G.getNodesBelow(plant);
        int index = 0;
        for (String node:  nodes)
        {
            Assert.assertTrue(node.equals(names.get(index)));
            index++;

        }


        ArrayList<String> nodes_neighbours = G.findRelation("grows on", apple_tree);
        Assert.assertTrue(nodes_neighbours.get(0).equals("apple"));

        G.loadData("data.json");

        names = Arrays.asList("tree", "apple tree", "apple");
        nodes = G.getNodesBelow(plant);
        index = 0;
        for (String node:  nodes)
        {
            Assert.assertTrue(node.equals(names.get(index)));
            index++;

        }
    }

    @Test
    public void GraphTestLoop() throws Exception {

        Type T = new Type("default", kb);
        IKBObject plant =  kb.createInstance("plant", T);
        IKBObject tree =  kb.createInstance("tree", T);
        IKBObject apple_tree =  kb.createInstance("apple tree", T);

        Graph G = new Graph();
        G.createNode(plant);
        G.createNode(tree);
        G.createNode(apple_tree);

        G.createEdge(tree, plant, "is");
        G.createEdge(plant, tree, null);
        G.createEdge(apple_tree, tree, "type of");
        G.createEdge(plant, apple_tree, "is");

        G.saveData("loop.json");
        List<String> names = Arrays.asList("tree", "apple tree");
        Collection<String> nodes = G.getNodesBelow(plant);
        int index = 0;
        for (String node:  nodes)
        {
            Assert.assertTrue(node.equals(names.get(index)));
            index++;

        }
    }

    @Test
    public void GraphTestAbove() throws Exception {
        Type T = new Type("default", kb);
        IKBObject plant =  kb.createInstance("plant", T);
        IKBObject tree =  kb.createInstance("tree", T);
        IKBObject apple_tree =  kb.createInstance("apple tree", T);
        IKBObject apple =  kb.createInstance("apple", T);


        Graph G = new Graph();
        G.createNode(plant);
        G.createNode(tree);
        G.createNode(apple_tree);
        G.createNode(apple);

        G.createEdge(tree, plant, "is");
        G.createEdge(apple_tree, tree, "is");
        G.createEdge(apple, apple_tree, "grows on");


        List<String> names = Arrays.asList("apple tree", "tree", "plant");
        Collection<String> nodes = G.getNodesAbove(apple);

        int index = 0;
        for (String node:  nodes)
        {
            Assert.assertTrue(node.equals(names.get(index)));
            index++;

        }
    }
    @Test
    public void pythonDataFile() throws Exception {
        Graph G = new Graph();
        Type T = new Type("default", kb);
        IKBObject keyboard =  kb.createInstance("keyboard", T);
        IKBObject monitor =  kb.createInstance("monitor", T);
        G.loadData("pythondb.json");
        Collection<String> nodes = G.getNodesAbove(monitor);
        List<String> names = Arrays.asList("keyboard", "mouse");
        int index = 0;
        for (String node:  nodes)
        {
            Assert.assertTrue(node.equals(names.get(index)));
            index++;

        }

    }
}
