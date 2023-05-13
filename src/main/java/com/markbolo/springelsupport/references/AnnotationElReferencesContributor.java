package com.markbolo.springelsupport.references;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.ProcessingContext;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnnotationElReferencesContributor extends PsiReferenceContributor {

    private static final Logger LOGGER = Logger.getInstance(AnnotationElReferencesContributor.class);

    @Override
    public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
        LOGGER.info("register a reference provider ");
        // PlatformPatterns.psiElement(PsiLiteralExpression.class) done
        //  PlatformPatterns.psiElement() done
        // PsiJavaPatterns.psiMethod() x
        // PsiJavaPatterns.psiAnnotation() x
        // PsiMethodReferenceExpression x
        // 注解上的el表达式
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression.class)
                        .withAncestor(4, PlatformPatterns.psiElement(PsiAnnotation.class)),
                new AnnotationElReferenceProvider());
    }

    private static class AnnotationElReferenceProvider extends PsiReferenceProvider {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                               @NotNull ProcessingContext context) {

            PsiLiteralExpression psiLiteralExpression  = (PsiLiteralExpression) element;
            PsiAnnotation annotation = getPsiAnnotation(psiLiteralExpression);
            // 判断annotation中的方法是否有@Language("SpEL")注解,如果有则表示value值需要被解析为el表达式,同时找到对应的declaration
            List<PsiReference> references = new ArrayList<>();
            if (!isAnnotatedOnMethod(annotation)
                    || !isSpELLanguage(annotation, getNameValuePair(psiLiteralExpression))) {
                // 非@Lanaguage标记的方法
                return PsiReference.EMPTY_ARRAY;
            }


            // 文本就是el表达式,需要跳转到方法参数定义 "#unifiedOrderBO.extraOrderId"
            final String elExpression = psiLiteralExpression.getText();
            // el表达式切割
            final String[] values = elExpression.split("\\.");
            AnnotationElVariablesReference prev = null;
            for (String val : values) {
                final AnnotationElVariablesReference reference;
                // el表达式 #开头
                final String strVal = val.replace("#", "")
                        .replace("\"","");
                int start = elExpression.indexOf(strVal);
                TextRange property = new TextRange(start, start + strVal.length());
                if (prev == null) {
                    reference = new AnnotationElDirectVariablesReference(element, property,
                            strVal,  getAnnotatedMethod(annotation));
                } else if (prev.resolveClass() != null) {
                    reference = new AnnotationElIndirectVariablesReference(element, property,
                            strVal, prev.resolveClass());
                } else {
                    reference = null;
                }
                references.add(reference);
                prev = reference;
            }

            return references.isEmpty() ? PsiReference.EMPTY_ARRAY : references.toArray(PsiReference[]::new);
        }

        private static PsiNameValuePair getNameValuePair(final PsiLiteralExpression psiLiteralExpression) {
            final PsiElement parent = psiLiteralExpression.getParent();
            if (parent instanceof PsiNameValuePair psiNameValuePair) {
                return psiNameValuePair;
            } else {
                return (PsiNameValuePair) parent.getParent();
            }
        }

        private PsiAnnotation getPsiAnnotation(final PsiLiteralExpression psiLiteralExpression) {
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


        public boolean isAnnotatedOnMethod(final PsiAnnotation psiAnnotation){
            PsiAnnotationOwner owner = psiAnnotation.getOwner();
            if(!(owner instanceof PsiModifierList)){
                return false;
            }
            // 不是method上的注解
            return ((PsiModifierList) owner).getParent() instanceof PsiMethod;
        }

        public PsiMethod getAnnotatedMethod(final PsiAnnotation psiAnnotation){
           if(isAnnotatedOnMethod(psiAnnotation)){
               return (PsiMethod) psiAnnotation.getParent().getParent();
           }

           return null;
        }


        /**
         * 判断是否有 @Language("SpEL")
         *
         * @see com.intellij.psi.impl.PsiImplUtil#findAttributeValue 找到annotation注解定义
         */
        private boolean isSpELLanguage(final PsiAnnotation annotation, final PsiNameValuePair attribute) {
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
    }
}
