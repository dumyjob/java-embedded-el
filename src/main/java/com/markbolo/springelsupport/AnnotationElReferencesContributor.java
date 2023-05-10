package com.markbolo.springelsupport;

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
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiAnnotation.class),
                new AnnotationElReferenceProvider());
    }

    private static class AnnotationElReferenceProvider extends PsiReferenceProvider {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                               @NotNull ProcessingContext context) {
            LOGGER.error("provider for annotation ");
            PsiAnnotation annotation = (PsiAnnotation) element;
            // 判断annotation中的方法是否有@Language("SpEL")注解,如果有则表示value值需要被解析为el表达式,同时找到对应的declaration
            PsiAnnotationOwner owner = annotation.getOwner();
            if(!(owner instanceof PsiMethod)){ // 不是method上的注解
                return PsiReference.EMPTY_ARRAY;
            }

            List<PsiReference> references = new ArrayList<>();
            for (PsiNameValuePair attribute : annotation.getParameterList().getAttributes()) {
                if (!isSpELLanguage(annotation, attribute) || attribute.getValue() == null) {
                    continue;
                }

                // attribute.value中的文本就是el表达式,需要跳转到方法参数定义
                final PsiAnnotationMemberValue annotationMemberValue = attribute.getValue();
                // el表达式切割
                final String[] values = annotationMemberValue.getText().split("\\.");
                int start = 0;
                AnnotationElVariablesReference prev = null;
                for (String val : values) {
                    TextRange property = new TextRange(start + 1, val.length() + 1);
                    final AnnotationElVariablesReference reference;
                    if (prev == null) {
                        reference = new AnnotationElDirectVariablesReference(annotationMemberValue, property,
                                val, (PsiMethod) owner);
                    } else if(prev.resolveClass() != null) {
                        reference = new AnnotationElIndirectVariablesReference(annotationMemberValue, property,
                                val, prev.resolveClass());
                    } else {
                        reference = null;
                    }
                    references.add(reference);
                    prev = reference;
                    start += val.length();
                }

            }


            return  references.isEmpty() ? PsiReference.EMPTY_ARRAY : references.toArray(PsiReference[]::new);
        }



        /**
         * 判断是否有 @Language("SpEL")
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
