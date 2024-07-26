package io.jmix.migration.model;

import java.util.ArrayList;
import java.util.List;

public class ScreenData {

    private final List<ScreenDataItem> items;

    public ScreenData() {
        this.items = new ArrayList<>();
    }

    public void addItem(ScreenDataItem item) {
        this.items.add(item);
    }

    public List<ScreenDataItem> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "ScreenData{" +
                "items=" + items +
                '}';
    }
}
