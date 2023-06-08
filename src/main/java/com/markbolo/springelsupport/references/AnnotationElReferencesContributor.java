package com.markbolo.springelsupport.references;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
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
            PsiAnnotation annotation = AnnotationUtils.getPsiAnnotation(psiLiteralExpression);
            // 判断annotation中的方法是否有@Language("SpEL")注解,如果有则表示value值需要被解析为el表达式,同时找到对应的declaration
            if (!AnnotationUtils.isAnnotatedOnMethod(annotation)
                    || !AnnotationUtils.isSpELLanguage(annotation, AnnotationUtils.getNameValuePair(psiLiteralExpression))) {
                // 非@Lanaguage标记的方法
                return PsiReference.EMPTY_ARRAY;
            }


            // 文本就是el表达式,需要跳转到方法参数定义 "#unifiedOrderBO.extraOrderId"
            final String elExpression = psiLiteralExpression.getText();
            // el表达式切割
            final String[] values = elExpression.split("\\.");

            List<PsiReference> references = new ArrayList<>();
            ElReference prev = null;
            for (String val : values) {
                final ElReference reference;
                // el表达式 #开头
                final String strVal = val.replace("#", "")
                        .replace("\"", "");
                int start = elExpression.indexOf(strVal);
                TextRange property = new TextRange(start, start + strVal.length());

                if (prev == null) {
                    reference = new ElMethodParamReference(element, property,
                            strVal, AnnotationUtils.getAnnotatedMethod(annotation));
                } else if (prev.referencedClass() != null) {
                    reference = new ElBeanFieldReference(element, property,
                            strVal, prev.referencedClass());
                } else {
                    reference = null;
                }
                references.add(reference);
                prev = reference;
            }

            return references.isEmpty() ? PsiReference.EMPTY_ARRAY : references.toArray(PsiReference[]::new);
        }


    }
}
