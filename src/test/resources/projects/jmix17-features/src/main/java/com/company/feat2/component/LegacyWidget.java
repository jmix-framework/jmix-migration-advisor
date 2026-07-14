package com.company.feat2.component;

import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;

public class LegacyWidget extends CssLayout {

    public LegacyWidget() {
        addComponent(new Button("Click"));
    }
}
