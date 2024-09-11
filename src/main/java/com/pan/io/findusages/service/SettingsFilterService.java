package com.pan.io.findusages.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import com.pan.io.findusages.config.FilterConfig;

public interface SettingsFilterService extends PersistentStateComponent<FilterConfig> {

    /**
     * 获取实例
     *
     * @return {@link SettingsFilterService}
     */
    static SettingsFilterService getInstance(Project project) {
        return project.getService(SettingsFilterService.class);
    }

    /**
     * 获取设置存储
     *
     * @return {@link FilterConfig}
     */
    static FilterConfig getSettingsStorage(Project project) {
        return getInstance(project).getState();
    }

}
