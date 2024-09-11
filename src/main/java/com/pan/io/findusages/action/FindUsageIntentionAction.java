package com.pan.io.findusages.action;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.pan.io.findusages.data.UsageCallerData;
import org.jetbrains.annotations.NotNull;

public class FindUsageIntentionAction extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        // 查找
        UsageCallerData usageCallerData = new UsageCallerData(element.getProject(), element);
        FilterFindUsageManager.doFindUsages(usageCallerData, element);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        // 必须是方法
        PsiElement psiMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        return psiMethod != null;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Find usage plus";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Find usage plus";
    }
}
