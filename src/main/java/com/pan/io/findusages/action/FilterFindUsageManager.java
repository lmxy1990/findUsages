package com.pan.io.findusages.action;

import com.intellij.find.FindManager;
import com.intellij.find.FindSettings;
import com.intellij.find.findUsages.*;
import com.intellij.find.impl.FindManagerImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.content.Content;
import com.intellij.usageView.UsageViewContentManager;
import com.intellij.util.Query;
import com.pan.io.findusages.config.FilterConfig;
import com.pan.io.findusages.data.UsageCall;
import com.pan.io.findusages.data.UsageCallerData;
import com.pan.io.findusages.data.UsageCallerNode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class FilterFindUsageManager {


    public static void doFindUsages(UsageCallerData usageCallerData, @NotNull PsiElement element) {


        WriteAction.run(() -> filterFindUsagesData(usageCallerData, element));

        // 在主线程上更新UI
        ApplicationManager.getApplication().invokeLater(usageCallerData::showCallTree);
    }

    private static void filterFindUsagesData(UsageCallerData usageCallerData, @NotNull PsiElement element) {
        Project project = element.getProject();
        if (!(element instanceof PsiMethod psiMethod)) {
            return;
        }
        // 获取查找参数
        FindManagerImpl findManager = (FindManagerImpl) FindManager.getInstance(element.getProject());
        // clear
        FindUsagesManager findUsagesManager = findManager.getFindUsagesManager();
        findUsagesManager.clearFindingNextUsageInFile();

        // search option
        FindUsagesHandler handler = findUsagesManager.getFindUsagesHandler(element, FindUsagesHandlerFactory.OperationMode.USAGES_WITH_DEFAULT_OPTIONS);
        if (handler == null) {
            return;
        }
        AbstractFindUsagesDialog dialog = handler.getFindUsagesDialog(false, shouldOpenInNewTab(element.getProject()), mustOpenInNewTab(element.getProject()));
        FindUsagesOptions findUsagesOptions = dialog.calcFindUsagesOptions();

        // 找出第一级引用
        Query<PsiReference> search = ReferencesSearch.search(psiMethod, findUsagesOptions.searchScope, false);
        Collection<PsiReference> psiReferences = search.findAll();
        if (psiReferences.isEmpty()) {
            //终点，说明没有赋值的地方
            return;
        }

        String myAnnotation = getAnnotationList(psiMethod);
        UsageCall call = new UsageCall(psiMethod, psiMethod, psiMethod, myAnnotation, myAnnotation);
        // 调用树
        UsageCallerNode callerNode = new UsageCallerNode(call);
        usageCallerData.setCallerNode(callerNode);

        for (PsiReference psiReference : psiReferences) {
            // 被调用
            PsiElement psiElement = psiReference.getElement();

            // call
            PsiCall psiCall = PsiTreeUtil.getParentOfType(psiElement, PsiCall.class);
            if (psiCall == null) {
                continue;
            }
            handleCaller(project, psiCall, call, usageCallerData, myAnnotation, callerNode);
        }
    }

    private static boolean shouldOpenInNewTab(Project myProject) {
        return mustOpenInNewTab(myProject) || FindSettings.getInstance().isShowResultsInSeparateView();
    }

    private static boolean mustOpenInNewTab(Project myProject) {
        Content selectedContent = UsageViewContentManager.getInstance(myProject).getSelectedContent(true);
        return selectedContent != null && selectedContent.isPinned();
    }

    private static String getAnnotationList(PsiMethod psiMethod) {
        PsiAnnotation[] annotations = psiMethod.getAnnotations();
        StringBuilder annotationList = new StringBuilder();
        for (PsiAnnotation annotation : annotations) {
            annotationList.append(annotation.getQualifiedName()).append(",");
        }
        if (!annotationList.isEmpty() && annotationList.charAt(annotationList.length() - 1) == ',') {
            return annotationList.substring(0, annotationList.length() - 1);
        }
        return annotationList.toString();
    }

    private static void handleCaller(Project project, PsiCall psiCall, UsageCall parentCall, UsageCallerData usageCallerData, String nextAnnotations, UsageCallerNode callerNode) {
        if (usageCallerData.isContainCall(psiCall)) {
            return;
        }
        // 防止循环引用
        usageCallerData.addCall(parentCall);

        // callerMethod 转化为完整的method
        PsiMethod callerMethod = findMethod(psiCall);
        if (callerMethod == null) {
            return;
        }

        callerMethod = findClassMethod(callerMethod);

        // 处理当前节点
        String myAnnotation = getAnnotationList(callerMethod);
        nextAnnotations = StringUtils.isBlank(myAnnotation) ? nextAnnotations : (StringUtils.isBlank(nextAnnotations) ? myAnnotation : (nextAnnotations + "," + myAnnotation));

        UsageCall call = new UsageCall(parentCall.getCaller(), psiCall, callerMethod, myAnnotation, nextAnnotations);
        if (usageCallerData.getAllCallerNodes().contains(call)) {
            return;
        }
        // 全局记录调用者
        usageCallerData.addCall(call);
        // 调用树记录
        UsageCallerNode nextCallerNode = new UsageCallerNode(call);
        callerNode.addNextNode(nextCallerNode);

        // 递归查找调用
        Query<PsiReference> search = ReferencesSearch.search(callerMethod, GlobalSearchScope.projectScope(project), false);
        Collection<PsiReference> psiReferences = search.findAll();
        if (psiReferences.isEmpty()) {
            return;
        }
        for (PsiReference psiReference : psiReferences) {
            PsiElement psiElement = psiReference.getElement();
            PsiCall nextCall = PsiTreeUtil.getParentOfType(psiElement, PsiCall.class);
            if (nextCall == null) {
                continue;
            }
            handleCaller(project, nextCall, call, usageCallerData, nextAnnotations, nextCallerNode);
        }
    }

    public static PsiMethod findMethod(PsiCall psiCall) {
        // 向上找到上一层的调用
        PsiElement parent = psiCall.getParent();
        while (parent != null) {
            if (parent instanceof PsiMethod nextMethod) {
                return nextMethod;
                // 找到即跳出
            }
            if (parent instanceof PsiDirectory) {
                // 遍历到目录，则跳出循环
                break;
            }
            // 向上递归
            parent = parent.getParent();
        }
        return null;
    }


    public static PsiMethod findClassMethod(PsiMethod psiMethod) {
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass != null) {
            PsiMethod targetMethod = containingClass.findMethodBySignature(psiMethod, true);
            if (targetMethod != null) {
                return targetMethod;
            }
        }
        return psiMethod;
    }

    public static boolean isAvailable(String myAnnotation, String nextAnnotations, FilterConfig filterConfig) {
        if (StringUtils.isBlank(filterConfig.getFilterAnnotation())) {
            return true;
        }
        String filterAnnotation = filterConfig.getFilterAnnotation();
        String[] split = new String[]{filterAnnotation};
        if (filterAnnotation.contains(",")) {
            split = filterAnnotation.split(",");
        }
        if (filterConfig.isOnlyCaller()) {
            return Arrays.stream(split).anyMatch(myAnnotation::contains);
        } else {
            return Arrays.stream(split).anyMatch(nextAnnotations::contains);
        }
    }

}
