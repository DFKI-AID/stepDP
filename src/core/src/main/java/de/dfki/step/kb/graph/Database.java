package de.dfki.step.kb.graph;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.UUID;

public class Database {
    @SerializedName("nodes")
    public ArrayList<String> nodes;
    @SerializedName("edges")
    public ArrayList<String> edges;
    @SerializedName("edges_labels")
    public ArrayList<String> edges_labels;

    public Database()
    {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.edges_labels = new ArrayList<>();

    }

}
