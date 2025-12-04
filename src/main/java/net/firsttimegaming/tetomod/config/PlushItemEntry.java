package net.firsttimegaming.tetomod.config;

public class PlushItemEntry {
    public String id;
    public int count;
    public int weight;

    public PlushItemEntry() {
        // needed for GSON
    }

    public PlushItemEntry(String id, int count, int weight) {
        this.id = id;
        this.count = count;
        this.weight = weight;
    }
}
