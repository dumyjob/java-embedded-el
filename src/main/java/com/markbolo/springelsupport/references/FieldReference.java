package com.markbolo.springelsupport.references;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class FieldReference extends PsiPolyVariantReferenceBase<PsiElement> {


    private PsiLiteralExpression expression;

    public FieldReference(@NotNull PsiElement psiElement, PsiLiteralExpression psiLiteralExpression) {
        super(psiElement);
        this.expression = psiLiteralExpression;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        return new ResolveResult[]{new PsiElementResolveResult(expression)};
    }
}
