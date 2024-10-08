package com.pan.io.findusages.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.pan.io.findusages.config.FilterConfig;
import com.pan.io.findusages.service.SettingsFilterService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class FilterConfigForm extends DialogWrapper {
    private JTextField textField1;
    private JRadioButton RadioButton;
    private JRadioButton radioButton2;
    private JPanel mainPanel;
    private JRadioButton radioButton1;
    private JRadioButton rb22;
    private JCheckBox callView;
    private ButtonGroup buttonGroup2;
    private ButtonGroup buttonGroup1;
    private final Project project;

    public FilterConfigForm(@Nullable Project project) {
        super(project, false);
        this.project = project;
        init();
        initLoad(project);
    }


    private void initLoad(@Nullable Project project) {
        FilterConfig filterConfig = SettingsFilterService.getSettingsStorage(project);
        textField1.setText(filterConfig.getFilterAnnotation());
        // 是否只显示调用者
        if (filterConfig.isOnlyCaller()) {
            radioButton2.setSelected(true);
        } else {
            RadioButton.setSelected(true);
        }

        if (filterConfig.isShowCallTree()) {
            radioButton1.setSelected(true);
        } else {
            rb22.setSelected(true);
        }

        if (filterConfig.isShowCallLink()) {
            callView.setSelected(true);
        } else {
            callView.setSelected(false);
        }

    }

    FilterConfig getSettingsStorage() {
        return SettingsFilterService.getSettingsStorage(this.project);
    }


    @Override
    protected @Nullable JComponent createCenterPanel() {
        return mainPanel;
    }

    @Override
    protected void doOKAction() {
        // 保存配置
        FilterConfig filterConfig = getSettingsStorage();
        if (!Objects.equals(textField1.getText(), filterConfig.getFilterAnnotation())) {
            filterConfig.setFilterAnnotation(textField1.getText());
        }
        filterConfig.setOnlyCaller(radioButton2.isSelected());
        filterConfig.setShowCallTree(radioButton1.isSelected());
        filterConfig.setShowCallLink(callView.isSelected());
        super.doOKAction();
    }

}
