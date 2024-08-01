package io.jmix.migration.analysis.model;

public class LayoutItem {
    protected final String name;
    protected int quantity;

    public LayoutItem(String name) {
        this.name = name;
        this.quantity = 1;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void increment() {
        increment(1);
    }

    public void increment(int value) {
        this.quantity += value;
    }

    @Override
    public String toString() {
        return "LayoutItem{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
