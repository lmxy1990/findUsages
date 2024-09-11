package com.pan.io.findusages.config;

public class FilterConfig {

    // 需要过滤的注解
    public String filterAnnotation;
    // 是否只过滤调用方法，false 过滤整条调用连
    public boolean onlyCaller;


    public String getFilterAnnotation() {
        return filterAnnotation;
    }

    public void setFilterAnnotation(String filterAnnotation) {
        this.filterAnnotation = filterAnnotation;
    }

    public boolean isOnlyCaller() {
        return onlyCaller;
    }

    public void setOnlyCaller(boolean onlyCaller) {
        this.onlyCaller = onlyCaller;
    }

    public static FilterConfig defaultVal() {
        // 配置文件加载失败，直接创建配置
        FilterConfig storage = new FilterConfig();
        storage.filterAnnotation = "Transactional";
        storage.onlyCaller = false;
        return storage;
    }

}
