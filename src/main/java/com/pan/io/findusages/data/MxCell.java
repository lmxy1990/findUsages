package com.pan.io.findusages.data;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MxCell {

    private final PsiElement element;

    public MxCell(PsiElement element) {
        this.element = element;
    }

    public PsiElement getElement() {
        return element;
    }

    @Override
    public String toString() {
        if (element instanceof PsiMethod psiMethod) {
            PsiClass psiClass = psiMethod.getContainingClass();
            return psiClass != null ? psiClass.getName() + "." + psiMethod.getName() : psiMethod.getName();
        }

        return element.getText();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MxCell mxCell = (MxCell) o;

        return new EqualsBuilder().append(element, mxCell.element).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(element).toHashCode();
    }
}
