package io.jmix.migration.model;

import java.util.Map;

public class ScreensTotalInfo {
    private int legacyScreens;
    private int screens;
    private int fragments;
    private Map<String, Integer> uiComponents;
    private Map<String, Integer> facets;

    public ScreensTotalInfo() {
    }

    public int getLegacyScreens() {
        return legacyScreens;
    }

    public void setLegacyScreens(int legacyScreens) {
        this.legacyScreens = legacyScreens;
    }

    public int getScreens() {
        return screens;
    }

    public void setScreens(int screens) {
        this.screens = screens;
    }

    public int getFragments() {
        return fragments;
    }

    public void setFragments(int fragments) {
        this.fragments = fragments;
    }

    public Map<String, Integer> getUiComponents() {
        return uiComponents;
    }

    public void setUiComponents(Map<String, Integer> uiComponents) {
        this.uiComponents = uiComponents;
    }

    public Map<String, Integer> getFacets() {
        return facets;
    }

    public void setFacets(Map<String, Integer> facets) {
        this.facets = facets;
    }
}
