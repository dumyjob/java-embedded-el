package com.markbolo.springelsupport.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnnotationElDirectVariablesReference extends AnnotationElVariablesReference {

    private final String elExpressionFragment;
    private final PsiMethod annotationOwner;

    public AnnotationElDirectVariablesReference(@NotNull final PsiElement element,
                                                final TextRange textRange,
                                                final String value,
                                                final PsiMethod owner) {
        super(element,textRange);
        this.elExpressionFragment = value;
        this.annotationOwner = owner;
    }



    @Override
    public @Nullable PsiElement resolve() {
        // el表达式中的第一个直接引用方法上的参数
        PsiParameterList psiParameterList =  annotationOwner.getParameterList();
        for(PsiParameter psiParameter : psiParameterList.getParameters()){
            if(elExpressionFragment.equals(psiParameter.getName())){
                // 如果el表达式指向到方法的参数
                return psiParameter;
            }
        }

        return null;
    }

    @Override
    PsiClass resolveClass() {
        final PsiParameter psiParameter = (PsiParameter) resolve();
        if(psiParameter == null ) {
            return null;
        }
        PsiType psiType = psiParameter.getType(); // 获取参数的类型
        if (psiType instanceof PsiClassType psiClassType) {
            return psiClassType.resolve(); // 对应的PsiClass对象
        }
        return null;
    }

    @Override
    public boolean isReferenceTo(@NotNull final PsiElement element) {
        return super.isReferenceTo(element);
    }
}
