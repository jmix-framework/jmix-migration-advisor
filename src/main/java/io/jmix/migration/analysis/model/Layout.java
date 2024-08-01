package io.jmix.migration.analysis.model;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Layout {
    protected final Map<String, LayoutItem> items;

    public Layout() {
        this.items = new HashMap<>();
    }

    @Nullable
    public LayoutItem getItem(String name) {
        return items.get(name);
    }

    public void putItem(String name) {
        LayoutItem item = items.get(name);
        if (item == null) {
            item = new LayoutItem(name);
            items.put(name, item);
        } else {
            item.increment();
        }
    }

    public List<LayoutItem> getAllItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public String toString() {
        return "Layout{" +
                "items=" + items +
                '}';
    }
}
