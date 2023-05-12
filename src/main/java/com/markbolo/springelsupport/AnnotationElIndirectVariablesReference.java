package com.markbolo.springelsupport;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

public class AnnotationElIndirectVariablesReference extends AnnotationElVariablesReference {

    private final String elExpressionFragment;
    private final PsiClass psiClass;



    public AnnotationElIndirectVariablesReference(final PsiElement psiElement, final TextRange textRange,
                                                  final String val, final PsiClass psiClass) {
        super(psiElement,textRange);
        this.elExpressionFragment = val;
        this.psiClass = psiClass;
    }


    @Override
    public @Nullable PsiElement resolve() {
        // el表达式中.后面的文本就指向到class中的某个成员变量
        final PsiField[] psiFields =  psiClass.getAllFields();
        for(PsiField psiField : psiFields){
            if(elExpressionFragment.equals(psiField.getName())){
                return psiField;
            }
        }

        return null;
    }

    @Override
    PsiClass resolveClass() {
        final PsiField psiField = (PsiField) resolve();
        if(psiField == null ) {
            return null;
        }
        PsiType psiType = psiField.getType(); // 获取参数的类型
        if (psiType instanceof PsiClassType psiClassType) {
            return psiClassType.resolve(); // 对应的PsiClass对象
        }
        return null;
    }
}
