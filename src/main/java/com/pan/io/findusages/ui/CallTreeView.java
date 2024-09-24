package com.pan.io.findusages.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class CallTreeView implements ToolWindowFactory {


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

    }


    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        ToolWindowFactory.super.init(toolWindow);
        toolWindow.setTitle("Filter Call Tree");
    }


}
