package com.markbolo.springelsupport.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import org.jetbrains.annotations.NotNull;

public abstract class ElReference extends PsiPolyVariantReferenceBase<PsiElement> {

    public ElReference(@NotNull final PsiElement element, final TextRange range) {
        super(element, range);
    }

   abstract PsiClass referencedClass();
}
