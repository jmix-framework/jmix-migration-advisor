package io.jmix.migration.model;

public class Facet {

    private final String name;

    public Facet(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Facet{" +
                "name='" + name + '\'' +
                '}';
    }
}
