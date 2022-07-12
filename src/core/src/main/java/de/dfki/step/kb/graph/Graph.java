package de.dfki.step.kb.graph;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.dfki.step.kb.IKBObject;


import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class Graph {
    public Map<IKBObject, ArrayList<Edge>> _child;
    public Map<IKBObject, ArrayList<Edge>> _parent;
    public Map<UUID,IKBObject> _uuidIkbChild;
    public Map<UUID,IKBObject> _uuidIkbParent;
    Boolean _directed = true;
    public Graph()
    {
        _child = new HashMap<IKBObject, ArrayList<Edge>>();
        _parent = new HashMap<IKBObject, ArrayList<Edge>>();
        _uuidIkbChild = new HashMap<UUID,IKBObject>();
        _uuidIkbParent = new HashMap<UUID,IKBObject>();
    }
    public void SetUndirected()
    {
        this._directed = false;
    }

    public UUID createEdge(IKBObject child_node,IKBObject parent_node, String label)
    {
        Edge Ed = new Edge(child_node.getUUID(), parent_node.getUUID(),label);
        _uuidIkbChild.put(Ed._childUUID, child_node);
        _uuidIkbParent.put(Ed._parentUUID, parent_node);
        addEdge(Ed);

        return Ed._edgeUUID;
    }

    public void addEdge(Edge Ed)
    {
        IKBObject child_node = _uuidIkbChild.get(Ed._childUUID);
        IKBObject parent_node = _uuidIkbParent.get(Ed._parentUUID);

        if (_child.get(child_node) == null)
        {
            ArrayList <Edge> list = new ArrayList<Edge>();
            list.add(Ed);
            _child.put(child_node, list);

        }
        else
        {
            _child.get(child_node).add(Ed);

        }
        if (_parent.get(parent_node) == null)
        {
            ArrayList <Edge> list = new ArrayList<Edge>();
            list.add(Ed);
            _parent.put(parent_node, list);
        }
        else
        {
            _parent.get(parent_node).add(Ed);
        }
    }
    public ArrayList<Edge> getAllEdges() {
        ArrayList<Edge> edges = new ArrayList<>();
        for (ArrayList<Edge> edgeList : _child.values()) {
            edges.addAll(edgeList);
        }
        return edges;
    }

    public  Edge findEdge(UUID ID)
    {
        for (ArrayList<Edge> edgeList : _child.values()) {
            for (Edge current_edge: edgeList)
            {
                if (current_edge._edgeUUID.equals(ID))
                {
                    return current_edge;
                }
            }
        }

        return null;
    }
    private void removeEdge(UUID ID, Map<IKBObject, ArrayList<Edge>> Map)
    {
            for (ArrayList<Edge> edgeList : Map.values())
            {
                for (Edge current_edge: edgeList)
                {
                    if (current_edge._edgeUUID.equals(ID))
                    {
                        edgeList.remove(current_edge);
                        return;
                    }
                }
            }
        return;
    }
    public void deleteEdge(UUID ID)
    {
        removeEdge(ID, _child);
        removeEdge(ID, _parent);
    }

    public ArrayList<IKBObject> getNodesBelow(IKBObject node)
    {
        if (!_directed)
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
                ArrayList<Edge> E = _parent.get(node);

                if (E == null)
                {
                    return null;
                }
                int num_edges = E.size();

                for (Edge current_edge : E)
                {
                    IKBObject child_node = _uuidIkbChild.get(current_edge._childUUID);
                    nodes.add(child_node);
                    node = child_node;
                    start_counter = 0;

                }
                if (_parent.get(node) == null)
                {
                    return nodes;
                }

            }
            return nodes;
        }


    }
    public List<IKBObject> findRelation(String Relation, IKBObject A)
    {
        List<Edge> E = _parent.get(A);
        List<IKBObject> nodes = new ArrayList<IKBObject>();;
        for (Edge current_edge : E)
        {
            if (current_edge._label.equals(Relation))
            {
                nodes.add(_uuidIkbChild.get(current_edge._childUUID));
            }
        }
        return nodes;
    }
    public void saveEdges(String path) throws IOException {
        ArrayList<Edge> edges = getAllEdges();
        Gson gson = new Gson();
        FileWriter writer = new FileWriter(path);
        gson.toJson(edges, writer);
        writer.flush();
        writer.close();

    }
    public void loadEdges(String path) throws FileNotFoundException {
        Gson gson = new Gson();

        BufferedReader br = new BufferedReader(new FileReader(path));
        Type userListType = new TypeToken<ArrayList<Edge>>(){}.getType();
        ArrayList<Edge> edges=  gson.fromJson(br, userListType);
        for (Edge current_edge : edges)
        {
            if (findEdge(current_edge._edgeUUID) == null)
            {
                this.addEdge(current_edge);
            }

        }
    }


}

