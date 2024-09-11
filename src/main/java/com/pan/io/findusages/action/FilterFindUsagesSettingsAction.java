package com.pan.io.findusages.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.pan.io.findusages.ui.FilterConfigForm;
import org.jetbrains.annotations.NotNull;

public class FilterFindUsagesSettingsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new FilterConfigForm(e.getProject()).show();
    }
}
