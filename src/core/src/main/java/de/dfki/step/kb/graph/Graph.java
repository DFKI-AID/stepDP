package de.dfki.step.kb.Graph_KB;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.dfki.step.kb.IKBObject;


import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class Graph {
    public Map<IKBObject, ArrayList<Edge>> Child;
    public Map<IKBObject, ArrayList<Edge>> Parent;
    public Map<UUID,IKBObject> UUID_IKB_Child;
    public Map<UUID,IKBObject> UUID_IKB_Parent;
    Boolean Directed = true;
    public Graph()
    {
        Child = new HashMap<IKBObject, ArrayList<Edge>>();
        Parent = new HashMap<IKBObject, ArrayList<Edge>>();
        UUID_IKB_Child = new HashMap<UUID,IKBObject>();
        UUID_IKB_Parent = new HashMap<UUID,IKBObject>();
    }
    public void SetUndirected()
    {
        this.Directed = false;
    }

    public UUID create_edge(IKBObject child_node,IKBObject parent_node, String label)
    {
        Edge Ed = new Edge(child_node.getUUID(), parent_node.getUUID(),label);
        UUID_IKB_Child.put(Ed.child_UUID, child_node);
        UUID_IKB_Parent.put(Ed.parent_UUID, parent_node);
        add_edge(Ed);

        return Ed.Edge_UUID;
    }

    public void add_edge(Edge Ed)
    {
        IKBObject child_node = UUID_IKB_Child.get(Ed.child_UUID);
        IKBObject parent_node = UUID_IKB_Parent.get(Ed.parent_UUID);

        if (Child.get(child_node) == null)
        {
            ArrayList <Edge> list = new ArrayList<Edge>();
            list.add(Ed);
            Child.put(child_node, list);

        }
        else
        {
            Child.get(child_node).add(Ed);

        }
        if (Parent.get(parent_node) == null)
        {
            ArrayList <Edge> list = new ArrayList<Edge>();
            list.add(Ed);
            Parent.put(parent_node, list);
        }
        else
        {
            Parent.get(parent_node).add(Ed);
        }
    }
    public ArrayList<Edge> getAllEdges() {
        ArrayList<Edge> edges = new ArrayList<>();
        for (ArrayList<Edge> edgeList : Child.values()) {
            edges.addAll(edgeList);
        }
        return edges;
    }

    public  Edge Find_Edge(UUID ID)
    {
        for (ArrayList<Edge> edgeList : Child.values()) {
            for (Edge current_edge: edgeList)
            {
                if (current_edge.Edge_UUID.equals(ID))
                {
                    return current_edge;
                }
            }
        }

        return null;
    }
    private void remove_edge(UUID ID, Map<IKBObject, ArrayList<Edge>> Map)
    {
            for (ArrayList<Edge> edgeList : Map.values())
            {
                for (Edge current_edge: edgeList)
                {
                    if (current_edge.Edge_UUID.equals(ID))
                    {
                        edgeList.remove(current_edge);
                        return;
                    }
                }
            }
        return;
    }
    public void delete_edge(UUID ID)
    {
        remove_edge(ID, Child);
        remove_edge(ID, Parent);
    }

    public ArrayList<IKBObject> GetNodesBelow(IKBObject node)
    {
        if (!Directed)
        {
            return null;
        }
        else
        {

            ArrayList<IKBObject> nodes = new ArrayList<IKBObject>();
            int start_counter = 0;
            while (start_counter==0)
            {
                start_counter = 1;
                ArrayList<Edge> E = Parent.get(node);

                if (E == null)
                {
                    return null;
                }
                int num_edges = E.size();

                for (Edge current_edge : E)
                {
                    IKBObject child_node = UUID_IKB_Child.get(current_edge.child_UUID);
                    nodes.add(child_node);
                    node = child_node;
                    start_counter = 0;

                }
                if (Parent.get(node) == null)
                {
                    return nodes;
                }

            }
            return nodes;
        }


    }
    public List<IKBObject> FindRelation(String Relation, IKBObject A)
    {
        List<Edge> E = Parent.get(A);
        List<IKBObject> nodes = new ArrayList<IKBObject>();;
        for (Edge current_edge : E)
        {
            if (current_edge.label.equals(Relation))
            {
                nodes.add(UUID_IKB_Child.get(current_edge.child_UUID));
            }
        }
        return nodes;
    }
    public void save_edges() throws IOException {
        ArrayList<Edge> edges = getAllEdges();
        Gson gson = new Gson();
        FileWriter writer = new FileWriter(new File("edges.json"));
        gson.toJson(edges, writer);
        writer.flush();
        writer.close();

    }
    public void load_edges(String path) throws FileNotFoundException {
        Gson gson = new Gson();

        BufferedReader br = new BufferedReader(new FileReader(path));
        Type userListType = new TypeToken<ArrayList<Edge>>(){}.getType();
        ArrayList<Edge> edges=  gson.fromJson(br, userListType);
        for (Edge current_edge : edges)
        {
            if (Find_Edge(current_edge.Edge_UUID) == null)
            {
                this.add_edge(current_edge);
            }

        }
    }


}

