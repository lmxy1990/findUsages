package com.pan.io.findusages.service;


import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.pan.io.findusages.config.FilterConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "FilterFindSetting", storages = @Storage("filter-find-setting.xml"))
public class SettingsFilterServiceImpl implements SettingsFilterService{

    public SettingsFilterServiceImpl() {
    }

    private FilterConfig filterConfig = FilterConfig.defaultVal();

    @Override
    public @Nullable FilterConfig getState() {
        return filterConfig;
    }

    @Override
    public void loadState(@NotNull FilterConfig state) {

        this.filterConfig = state;
    }
}
