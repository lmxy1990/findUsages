package com.pan.io.findusages.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HelloDialog extends DialogWrapper {
    private JTextField helloTextField;

    public HelloDialog(@Nullable Project project, boolean canBeParent) {
        super(project, canBeParent);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return null;
    }
}
