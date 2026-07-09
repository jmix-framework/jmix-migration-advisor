package com.company.synth.web.screens;

import com.haulmont.cuba.gui.screen.*;

@UiController("synth_Modern")
@UiDescriptor("modern-screen.xml")
public class Modern extends Screen {

    protected String kind(int x) {
        return switch (x) {
            case 1 -> "one";
            default -> "many";
        };
    }
}
