package com.markbolo.springelsupport.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;

public abstract class AnnotationElVariablesReference extends PsiReferenceBase<PsiElement> {

    public AnnotationElVariablesReference(@NotNull final PsiElement element, final TextRange rangeInElement, final boolean soft) {
        super(element, rangeInElement, soft);
    }

    public AnnotationElVariablesReference(@NotNull final PsiElement element, final TextRange rangeInElement) {
        super(element, rangeInElement);
    }

    public AnnotationElVariablesReference(@NotNull final PsiElement element, final boolean soft) {
        super(element, soft);
    }

    public AnnotationElVariablesReference(@NotNull final PsiElement element) {
        super(element);
    }

    abstract PsiClass resolveClass();


}
