package com.company.feat.web.screens.customer;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.screen.StandardEditor;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;

@UiController("feat_Customer.edit")
@UiDescriptor("customer-editor.xml")
public class CustomerEditor extends StandardEditor {

    protected UiComponents uiComponents;

    public void onInit() {
        Button extraButton = uiComponents.create(Button.class);
        this.configure(extraButton);
        this.stepOne();
        this.stepTwo();
        this.stepThree();
        this.stepFour();
        this.stepFive();
        this.stepSix();
    }

    protected void configure(Button button) {}
    protected void stepOne() {}
    protected void stepTwo() {}
    protected void stepThree() {}
    protected void stepFour() {}
    protected void stepFive() {}
    protected void stepSix() {}
}
