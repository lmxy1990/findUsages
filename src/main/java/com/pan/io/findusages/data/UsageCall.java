package com.pan.io.findusages.data;

import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class UsageCall {
    // 被调用点
    private final PsiElement called;
    // 调用者
    private final PsiElement caller;
    private final PsiElement callMethod;
    // called annotation
    private final String myAnnotation;
    // called tree annotation
    private final String annotationList;


    public UsageCall(PsiElement called, PsiElement caller, PsiElement callMethod, String myAnnotation, String annotationList) {
        this.called = called;
        this.caller = caller;
        this.callMethod = callMethod;
        this.myAnnotation = myAnnotation;
        this.annotationList = annotationList;
    }

    public String getMyAnnotation() {
        return myAnnotation;
    }

    public String getAnnotationList() {
        return annotationList;
    }

    public PsiElement getCalled() {
        return called;
    }

    public PsiElement getCaller() {
        return caller;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UsageCall call = (UsageCall) o;

        return new EqualsBuilder().append(called, call.called).append(caller, call.caller).append(callMethod, call.callMethod).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(called).append(caller).append(callMethod).toHashCode();
    }


    @Override
    public String toString() {
        return caller.getText();
    }

    public PsiElement getCallMethod() {
        return callMethod;
    }
}
