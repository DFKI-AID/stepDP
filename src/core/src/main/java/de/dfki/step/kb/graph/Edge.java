package de.dfki.step.kb.Graph_KB;
import com.google.gson.Gson;
import de.dfki.step.kb.IKBObject;

import java.util.UUID;

public class Edge {

    public UUID Edge_UUID;
    public UUID child_UUID;
    public UUID parent_UUID;
    public String label;

    public Edge(UUID PreviousUUID, UUID NextUUID, String Edge_label)
    {
        this.child_UUID = PreviousUUID;
        this.parent_UUID = NextUUID;
        this.label = Edge_label;
        this.Edge_UUID = UUID.randomUUID();
    }

}
