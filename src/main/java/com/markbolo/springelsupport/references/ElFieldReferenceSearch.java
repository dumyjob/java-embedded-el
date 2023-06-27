package com.markbolo.springelsupport.references;

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
    public boolean execute(ReferencesSearch.@NotNull SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
        final PsiElement elementToSearch = queryParameters.getElementToSearch();
        if (elementToSearch instanceof PsiField field) {
            final PsiClass containingClass  = field.getContainingClass();
            if (containingClass != null) {


                findUsageBySearcher(consumer, elementToSearch, field);

                // findUsageByClass(consumer, field, containingClass);
            }
        }

        return true;
    }

    private static void findUsageBySearcher(@NotNull Processor<? super PsiReference> consumer, PsiElement elementToSearch, PsiField field) {
        PsiSearchHelper psiSearchHelper = PsiSearchHelper.getInstance(elementToSearch.getProject());
        psiSearchHelper.processElementsWithWord(new TextOccurenceProcessor() {
            @Override
            public boolean execute(@NotNull PsiElement findElement, int offsetInElement) {

                if(findElement instanceof PsiLiteralExpression psiLiteralExpression) {
                    return  consumer.process(new FieldReference(elementToSearch, psiLiteralExpression));

                }

                return false;
            }
        }, GlobalSearchScope.allScope(field.getProject()), field.getName(), UsageSearchContext.IN_STRINGS, true);
    }

    private static void findUsageByClass(@NotNull Processor<? super PsiReference> consumer, PsiField field, PsiClass containingClass) {
        // 尝试使用QueryExecutor做findUsage , 使用referenceSearch是可行的
        ReferencesSearch.search(containingClass, GlobalSearchScope.allScope(field.getProject()))
                .forEach(new Processor<PsiReference>() {
                    @Override
                    public boolean process(PsiReference psiReference) {
                        return consumer.process(psiReference);
                    }
                });
    }


}
