package de.dfki.step.kb.graph;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.dfki.step.kb.IKBObject;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class Graph {
    public Database data;
    Boolean _directed = true;
    public Graph()
    {
        this.data = new Database();
    }
    public void SetUndirected()
    {
        this._directed = false;
    }

    public boolean createNode(IKBObject node)
    {
        if (this.data.nodes.contains(node.getName()))
        {
            System.out.println("Node already present");
            return false;
        }
        else {
            data.nodes.add(node.getName());
            return true;
        }
    }

    public boolean deleteNode(IKBObject node) {
        if (this.data.nodes.contains(node.getName()))
        {
            data.nodes.remove(node.getName());
            return true;
        }
        else {
            System.out.println("Node not present");
            return false;

        }

    }

    public boolean createEdge(IKBObject child_node,IKBObject parent_node, String edge_label)
    {
        if (this.data.nodes.contains(child_node.getName()) && this.data.nodes.contains(parent_node.getName()))
        {
            for (int i =0; i <this.data.edges.size(); i++)
            {
                String parent = this.data.edges.get(i).split(",")[0];
                String child = this.data.edges.get(i).split(",")[1];
                if (child == child_node.getName() && parent == parent_node.getName() && this.data.edges_labels.get(i) == edge_label)
                {
                    System.out.println("Edge already present");
                    return false;
                }
            }
            this.data.edges.add(parent_node.getName().trim() + "," + child_node.getName().trim());
            this.data.edges_labels.add(edge_label);
            return true;

        }
        System.out.println("Node not found in graph");
        return false;
    }

    public ArrayList<String> getAllEdges() {
        return this.data.edges;
    }

    public boolean deleteEdge(IKBObject child_node,IKBObject parent_node, String edge_label)
    {
        for (int i = 0; i < this.data.edges.size(); i++) {
            String parent = this.data.edges.get(i).split(",")[0];
            String child = this.data.edges.get(i).split(",")[1];

            if (child.equals( child_node.getName()) && parent.equals(parent_node.getName()) && this.data.edges_labels.get(i).equals( edge_label)) {
                this.data.edges.remove(this.data.edges.get(i));
                this.data.edges_labels.remove(this.data.edges_labels.get(i));
                return true;
            }
        }
        return false;

    }

    public Collection<String> getNodesBelow(IKBObject node)
    {
        if (!_directed)
        {
            return null;
        }
        else
        {
            Set<String> visited = new HashSet<>();
            List<String> result = new ArrayList<>();
            dfs(node.getName(), this.data.edges, visited, result,1);
            return result;
        }
    }
    public Collection<String> getNodesAbove(IKBObject node)
    {
        if (!_directed)
        {
            return null;
        }
        else
        {
            Set<String> visited = new HashSet<>();
            List<String> result = new ArrayList<>();
            dfs(node.getName(), this.data.edges, visited, result, 0);
            return result;
        }

    }
    private static void dfs(String node, ArrayList<String> edges, Set<String> visited, List<String> result,int node_below) {
        visited.add(node);
        for (String edge : edges) {
            String[] parts = edge.split(",");
            if (parts[1 - node_below].equals(node) && !visited.contains(parts[node_below])) {
                result.add(parts[node_below]);
                dfs(parts[node_below], edges, visited, result, node_below);
            }
        }
    }


    public ArrayList<String> findRelation(String Relation, IKBObject A)
    {
    ArrayList<String> nodes = new ArrayList<>();
    for (int i = 0; i < this.data.edges.size(); i++) {
        String parent = this.data.edges.get(i).split(",")[0];
        String child = this.data.edges.get(i).split(",")[1];

        if ((A.getName().equals(parent)) && (this.data.edges_labels.get(i).equals(Relation)))
            nodes.add(child);
        else if ((A.getName().equals(child)) && (this.data.edges_labels.get(i).equals(Relation)))
            nodes.add(child);
        }
    return nodes;
    }
    public void saveData(String path) throws IOException {
        Gson gson = new Gson();
        FileWriter writer = new FileWriter(path);
        gson.toJson(this.data, writer);
        writer.flush();
        writer.close();

    }
    public void loadData(String path) throws FileNotFoundException {
        Gson gson = new Gson();
        BufferedReader br = new BufferedReader(new FileReader(path));
        Type userListType = new TypeToken<Database>(){}.getType();
        this.data =  gson.fromJson(br, userListType);
    }

}

