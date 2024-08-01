package io.jmix.migration.analysis.model;

public class ScreenDataItem {
    private final String name;
    private final String id;
    private ScreenDataItem parent;
    private String query;

    public ScreenDataItem(String name, String id) {
        this(name, id, null);
    }

    public ScreenDataItem(String name, String id, ScreenDataItem parent) {
        this.name = name;
        this.id = id;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public ScreenDataItem getParent() {
        return parent;
    }

    public void setParent(ScreenDataItem parent) {
        this.parent = parent;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "ScreenDataItem{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", parent=" + parent +
                '}';
    }
}
