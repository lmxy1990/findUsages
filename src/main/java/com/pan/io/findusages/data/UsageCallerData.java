package com.pan.io.findusages.data;

import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCall;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.*;
import com.pan.io.findusages.action.FilterFindUsageManager;
import com.pan.io.findusages.config.FilterConfig;
import com.pan.io.findusages.service.SettingsFilterService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UsageCallerData {

    private final Project project;
    private final PsiElement element;
    private UsageCallerNode callerNode;
    private final List<UsageCall> allUsageCall;
    private final List<PsiCall> allPsiCall;


    public UsageCallerData(Project project, PsiElement element) {
        this.project = project;
        this.element = element;
        this.allUsageCall = new ArrayList<>();
        this.allPsiCall = new ArrayList<>();
    }


    public void addCall(UsageCall usage) {
        allUsageCall.add(usage);
    }

    public void addCall(PsiCall call) {
        allPsiCall.add(call);
    }

    public boolean isContainCall(PsiCall call) {
        return allPsiCall.contains(call);
    }

    public Project getProject() {
        return project;
    }

    public PsiElement getElement() {
        return element;
    }

    public void setCallerNode(UsageCallerNode callerNode) {
        this.callerNode = callerNode;
    }

    public List<UsageCall> getAllCallerNodes() {
        return allUsageCall;
    }

    public List<UsageCall> callerAllNodesWithFilter() {
        final FilterConfig filterConfig = SettingsFilterService.getSettingsStorage(project);
        if (StringUtils.isNotBlank(filterConfig.getFilterAnnotation())) {
            if (filterConfig.isOnlyCaller()) {
                return allUsageCall.stream()
                        .filter(e -> FilterFindUsageManager.isAvailable(e.getMyAnnotation(), e.getAnnotationList(), filterConfig))
                        .toList();
            }
            List<UsageCall> calls = new ArrayList<>();
            List<UsageCallerNode> callerNodeList = callerNode.getNextNodes();

            for (UsageCallerNode callerNode : callerNodeList) {
                List<UsageCall> callTree = new ArrayList<>();
                loopFindAddCall(calls, callerNode, callTree, filterConfig, false);
                // 这里可以打印出调用链路
            }
            UsageCall call = callerNode.getCall();
            // 添加自身
            if (!calls.contains(call) && FilterFindUsageManager.isAvailable(call.getMyAnnotation(), call.getAnnotationList(), filterConfig)) {
                calls.add(call);
            }
            return calls;
        }
        return allUsageCall;
    }


    public static boolean loopFindAddCall(List<UsageCall> allUsages, UsageCallerNode callerNode, List<UsageCall> callTree, FilterConfig filterConfig, boolean isFilter) {
        // 添加调用者
        callTree.add(callerNode.getCall());

        isFilter = isFilter || FilterFindUsageManager.isAvailable(callerNode.getCall().getMyAnnotation(), callerNode.getCall().getAnnotationList(), filterConfig);

        if (CollectionUtils.isEmpty(callerNode.getNextNodes())) {
            if (isFilter) {
                callTree.stream()
                        .filter(e -> !allUsages.contains(e))
                        .forEach(allUsages::add);
            }
            // 调用树终止
            return isFilter;
        }
        for (UsageCallerNode nextCallerNode : callerNode.getNextNodes()) {

            isFilter = isFilter || loopFindAddCall(allUsages, nextCallerNode, callTree, filterConfig, isFilter);
        }
        return isFilter;
    }

    public void showCallTree() {
        final FilterConfig filterConfig = SettingsFilterService.getSettingsStorage(project);
        List<UsageCall> callerList = callerAllNodesWithFilter();

        UsageViewManager usageViewManager = UsageViewManager.getInstance(project);

        UsageViewPresentation usageViewPresentation = new UsageViewPresentation();
        usageViewPresentation.setTargetsNodeText("Filter Find Usage");
        usageViewPresentation.setCodeUsagesString("Find Result Filter With [" + filterConfig.getFilterAnnotation() + " ]");
        usageViewPresentation.setTabText("Find Usage Plus");
        // 对象转换
        UsageTarget[] usageTargets = {new PsiElement2UsageTargetAdapter(element, true)};

        int count = callerList.size();
        PsiElement[] primaryElements = new PsiElement[count];
        UsageInfo[] usageInfo = new UsageInfo[count];
        // 组装界面数据
        for (int i = 0; i < callerList.size(); i++) {
            UsageCall call = callerList.get(i);
            // 获取调用者
            primaryElements[i] = call.getCaller();
            usageInfo[i] = new UsageInfo(call.getCaller());

        }
        // 显示
        if (usageViewManager != null) {
            // close
            UsageView usageView = usageViewManager.getSelectedUsageView();
            if (usageView != null) {
                usageView.close();
            }
        }
        if (primaryElements.length == 0) {
            UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, new Usage[]{}, usageViewPresentation);
        } else {
            Usage[] usages = UsageInfoToUsageConverter.convert(primaryElements, usageInfo);
            assert usageViewManager != null;
            usageViewManager.showUsages(usageTargets, usages, usageViewPresentation);
        }
    }


}
