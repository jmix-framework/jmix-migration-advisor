package io.jmix.migration.model;

import java.util.List;

public class ScreenInfo {
    protected String descriptorFile;
    protected String controllerFile;
    protected String controllerClass;
    protected String screenId;

    protected ScreenData screenData;
    protected List<Facet> facets;
    protected Layout layout;

    protected ScreenControllerDetails controllerDetails;

    protected String extendedDescriptor;
    protected String extendedController;

    protected boolean legacy = false;
    protected boolean fragment = false;
    protected boolean registered = false;

    protected boolean descriptorProcessed;
    protected boolean controllerProcessed;

    public ScreenInfo() {
    }

    public String getDescriptorFile() {
        return descriptorFile;
    }

    public void setDescriptorFile(String descriptorFile) {
        this.descriptorFile = descriptorFile;
    }

    public String getControllerFile() {
        return controllerFile;
    }

    public void setControllerFile(String controllerFile) {
        this.controllerFile = controllerFile;
    }

    public String getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(String controllerClass) {
        this.controllerClass = controllerClass;
    }

    public String getScreenId() {
        return screenId;
    }

    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }

    public boolean isLegacy() {
        return legacy;
    }

    public void setLegacy(boolean legacy) {
        this.legacy = legacy;
    }

    public ScreenData getScreenData() {
        return screenData;
    }

    public void setScreenData(ScreenData screenData) {
        this.screenData = screenData;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public boolean isFragment() {
        return fragment;
    }

    public void setFragment(boolean fragment) {
        this.fragment = fragment;
    }

    public String getExtendedDescriptor() {
        return extendedDescriptor;
    }

    public void setExtendedDescriptor(String extendedDescriptor) {
        this.extendedDescriptor = extendedDescriptor;
    }

    public String getExtendedController() {
        return extendedController;
    }

    public void setExtendedController(String extendedController) {
        this.extendedController = extendedController;
    }

    public ScreenControllerDetails getControllerDetails() {
        return controllerDetails;
    }

    public void setControllerDetails(ScreenControllerDetails controllerDetails) {
        this.controllerDetails = controllerDetails;
    }

    public boolean isDescriptorProcessed() {
        return descriptorProcessed;
    }

    public void setDescriptorProcessed(boolean descriptorProcessed) {
        this.descriptorProcessed = descriptorProcessed;
    }

    public boolean isControllerProcessed() {
        return controllerProcessed;
    }

    public void setControllerProcessed(boolean controllerProcessed) {
        this.controllerProcessed = controllerProcessed;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
