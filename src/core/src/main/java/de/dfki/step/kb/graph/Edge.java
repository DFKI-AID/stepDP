package de.dfki.step.kb.graph;

import java.util.UUID;

public class Edge {

    public UUID _edgeUUID;
    public UUID _childUUID;
    public UUID _parentUUID;
    public String _label;

    public Edge(UUID PreviousUUID, UUID NextUUID, String Edge_label)
    {
        this._childUUID = PreviousUUID;
        this._parentUUID = NextUUID;
        this._label = Edge_label;
        this._edgeUUID = UUID.randomUUID();

    }

}
