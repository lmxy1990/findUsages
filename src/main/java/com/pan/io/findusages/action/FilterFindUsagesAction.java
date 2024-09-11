package com.pan.io.findusages.action;

import com.intellij.find.FindBundle;
import com.intellij.find.FindSettings;
import com.intellij.find.actions.FindUsagesAction;
import com.intellij.find.actions.FindUsagesKt;
import com.intellij.find.actions.ResolverKt;
import com.intellij.find.actions.UsageVariantHandler;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.find.usages.api.SearchTarget;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;
import com.pan.io.findusages.data.UsageCallerData;
import com.pan.io.findusages.util.RefCallUtils;
import org.jetbrains.annotations.NotNull;

public class FilterFindUsagesAction extends FindUsagesAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = (Project) e.getData(CommonDataKeys.PROJECT);
        if (project != null) {
            PsiDocumentManager.getInstance(project).commitAllDocuments();
            final DataContext dataContext = e.getDataContext();
            //
            Object allTargets = RefCallUtils.invokeStaticMethod(ResolverKt.class, "allTargets", dataContext);
            RefCallUtils.invokeStaticMethod(ResolverKt.class, "findShowUsages", project, dataContext, allTargets, FindBundle.message("find.usages.ambiguous.title", new Object[0]), new UsageVariantHandler() {
                public void handleTarget(@NotNull SearchTarget target) {

                    SearchScope searchScope = FindUsagesOptions.findScopeByName(project, dataContext, FindSettings.getInstance().getDefaultScopeName());
                    FindUsagesKt.findUsages(FilterFindUsagesAction.this.toShowDialog(), project, searchScope, target);
                }

                public void handlePsi(@NotNull PsiElement element) {

                    FilterFindUsagesAction.this.startFindUsages(element);
                }
            });
        }
    }


    protected void startFindUsages(@NotNull PsiElement element) {
        // 查找
        UsageCallerData usageCallerData = new UsageCallerData(element.getProject(), element);
        FilterFindUsageManager.doFindUsages(usageCallerData, element);
    }



}
