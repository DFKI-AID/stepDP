package de.dfki.step.kb.graph;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Edge {

    @SerializedName("uuid")
    public UUID _edgeUUID;
    @SerializedName("child")
    public UUID _childUUID;
    @SerializedName("parent")
    public UUID _parentUUID;
    @SerializedName("label")
    public String _label;

    public Edge(UUID childUUID, UUID parentUUID, String edgeLabel)
    {
        this._childUUID = childUUID;
        this._parentUUID = parentUUID;
        this._label = edgeLabel;
        this._edgeUUID = UUID.randomUUID();

    }

}
