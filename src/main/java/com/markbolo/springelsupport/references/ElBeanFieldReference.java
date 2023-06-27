package com.markbolo.springelsupport.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * {#beanVar.field}
 */
public class ElBeanFieldReference  extends ElReference {

    private final String elExpressionFragment;
    private final PsiClass psiClass;

    public ElBeanFieldReference(@NotNull final PsiElement element, final TextRange range,
                                final String elExpressionFragment, final PsiClass psiClass) {
        super(element, range);
        this.elExpressionFragment = elExpressionFragment;
        this.psiClass = psiClass;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(final boolean incompleteCode) {
        // el表达式中.后面的文本就指向到class中的某个成员变量
        final PsiField psiField = psiClass.findFieldByName(elExpressionFragment, true);
        if (psiField == null) {
            return new ResolveResult[0];
        }

        return new ResolveResult[]{new PsiElementResolveResult(psiField)};
    }


    @NotNull
    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        //Find all the method with the registered method name on it's class.
        final PsiField psiField = psiClass.findFieldByName(elExpressionFragment, true);
        if (psiField != null) {
            psiField.setName(newElementName);
        }

        // ?? 干啥的
        ElementManipulators.getManipulator(getElement()).handleContentChange(getElement(), this.getRangeInElement(), newElementName);
        return getElement();
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @Override
    public PsiClass referencedClass() {
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

    @Override
    public boolean isReferenceTo(@NotNull final PsiElement element) {
        return true;
    }
}
