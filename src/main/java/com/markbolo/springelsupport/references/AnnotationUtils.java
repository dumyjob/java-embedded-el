package com.markbolo.springelsupport.references;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import org.intellij.lang.annotations.Language;

public class AnnotationUtils {

    private AnnotationUtils() {
    }

    /**
     * 判断是否有 @Language("SpEL")
     *
     * @see com.intellij.psi.impl.PsiImplUtil#findAttributeValue 找到annotation注解定义
     */
    public static boolean isSpELLanguage(final PsiAnnotation annotation, final PsiNameValuePair attribute) {
        final PsiJavaCodeReferenceElement referenceElement = annotation.getNameReferenceElement();
        if (referenceElement == null) {
            return false;
        }

        // PsiAnnotation引用的annotation注解定义
        PsiElement resolved = referenceElement.resolve();
        if (resolved == null) {
            return false;
        }
        PsiMethod[] methods = ((PsiClass) resolved).findMethodsByName(attribute.getAttributeName(), false);
        for (PsiMethod method : methods) {
            if (PsiUtil.isAnnotationMethod(method)) {
                PsiAnnotation languageAnnotation = AnnotationUtil.findAnnotation(method, Language.class.getName());
                if (languageAnnotation != null) {
                    return true;
                }
            }
        }
        return false;
    }

    static PsiAnnotation getPsiAnnotation(final PsiLiteralExpression psiLiteralExpression) {
        // 如果是数组的value， 会比单个value的多一级 PsiNameValuePair -> {PsiArrayInitializerMemberValue} ->  PsiLiteralExpression
        final PsiElement parent = psiLiteralExpression.getParent();
        if (parent instanceof PsiNameValuePair) {
            // 单个
            return (PsiAnnotation) parent
                    .getParent()
                    .getParent();
        } else {
            // 数组value
            return (PsiAnnotation) parent
                    .getParent()
                    .getParent()
                    .getParent();
        }
    }

    public static boolean isAnnotatedOnMethod(final PsiAnnotation psiAnnotation){
        PsiAnnotationOwner owner = psiAnnotation.getOwner();
        if(!(owner instanceof PsiModifierList)){
            return false;
        }
        // 不是method上的注解
        return ((PsiModifierList) owner).getParent() instanceof PsiMethod;
    }

    public static PsiMethod getAnnotatedMethod(final PsiAnnotation psiAnnotation){
       if(isAnnotatedOnMethod(psiAnnotation)){
           return (PsiMethod) psiAnnotation.getParent().getParent();
       }

       return null;
    }

    static PsiNameValuePair getNameValuePair(final PsiLiteralExpression psiLiteralExpression) {
        final PsiElement parent = psiLiteralExpression.getParent();
        if (parent instanceof PsiNameValuePair psiNameValuePair) {
            return psiNameValuePair;
        } else {
            return (PsiNameValuePair) parent.getParent();
        }
    }
}
