package com.markbolo.springelsupport.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class ElMethodParamReference extends  ElReference {


    private final String elExpressionFragment;
    private final PsiMethod annotationOwner;

    public ElMethodParamReference(@NotNull final PsiElement element, final TextRange range,
                                  final String elExpressionFragment, final PsiMethod annotationOwner) {
        super(element, range);
        this.elExpressionFragment = elExpressionFragment;
        this.annotationOwner = annotationOwner;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(final boolean incompleteCode) {
        // el表达式中的第一个直接引用方法上的参数
        PsiParameterList psiParameterList =  annotationOwner.getParameterList();
        for(PsiParameter psiParameter : psiParameterList.getParameters()){
            if(elExpressionFragment.equals(psiParameter.getName())){
                // 如果el表达式指向到方法的参数
                return new ResolveResult[]{new PsiElementResolveResult(psiParameter)};
            }
        }

        return new ResolveResult[0];
    }

    @NotNull
    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }


    @Override
    public boolean isSoft() {
        return false;
    }

    @Override
    public PsiClass referencedClass() {
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
}
