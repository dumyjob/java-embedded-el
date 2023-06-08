package com.markbolo.springelsupport.references;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.TextOccurenceProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;


/**
 * class field find-usage to El-expression {#beanVariable.fieldName}
 */
public class ElFieldReferenceSearch implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {


    @Override
    public boolean execute(final ReferencesSearch.@NotNull SearchParameters queryParameters,
                           @NotNull final Processor<? super PsiReference> consumer) {

        final PsiElement elementToSearch = queryParameters.getElementToSearch();
        if (elementToSearch instanceof PsiField field) {
            final PsiClass containingClass = ReadAction.compute(field::getContainingClass);
            if (containingClass != null) {

                PsiSearchHelper psiSearchHelper = PsiSearchHelper.getInstance(elementToSearch.getProject());
                psiSearchHelper.processElementsWithWord(new TextOccurenceProcessor() {
                    @Override
                    public boolean execute(@NotNull PsiElement element, int offsetInElement) {

                        if(element instanceof PsiLiteralExpression psiLiteralExpression) {

                            boolean accepts = PlatformPatterns.psiElement(PsiLiteralExpression.class)
                                    .withAncestor(4, PlatformPatterns.psiElement(PsiAnnotation.class))
                                    .accepts(psiLiteralExpression);
                            if(!accepts){
                                return false;
                            }

                            PsiAnnotation annotation = AnnotationUtils.getPsiAnnotation(psiLiteralExpression);
                            // 判断annotation中的方法是否有@Language("SpEL")注解,如果有则表示value值需要被解析为el表达式,同时找到对应的declaration
                            if (!AnnotationUtils.isAnnotatedOnMethod(annotation)
                                    || !AnnotationUtils.isSpELLanguage(annotation, AnnotationUtils.getNameValuePair(psiLiteralExpression))) {
                                // 非@Lanaguage标记的方法
                                return false;
                            }

                            // 文本就是el表达式,需要跳转到方法参数定义 "#unifiedOrderBO.extraOrderId"
                            final String elExpression = psiLiteralExpression.getText();
                            // el表达式切割
                            final String[] values = elExpression.split("\\.");

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

                                prev = reference;
                                if(val.equals(field.getName())){
                                    return consumer.process(reference);
                                }
                            }
                        }


                       return true;
                    }
                }, GlobalSearchScope.allScope(field.getProject()), field.getName(), UsageSearchContext.ANY, true);
            }
        }

        return true;
    }
}
