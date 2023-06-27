package com.markbolo.springelsupport.references;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.TextOccurenceProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class FieldReferencesContributor extends PsiReferenceContributor {

    private static final Logger LOGGER = Logger.getInstance(FieldReferencesContributor.class);

    @Override
    public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
        // 功能行不通: register只能支持java PsiLiteralExpression和comments
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiIdentifier.class),
                new FieldReferenceProvider());
    }

    private static class FieldReferenceProvider extends PsiReferenceProvider {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                               @NotNull ProcessingContext context) {

            if (!(element instanceof PsiField field)) {
                return PsiReference.EMPTY_ARRAY;
            }

            List<PsiReference> references = new ArrayList<>();
            final PsiClass containingClass = field.getContainingClass();
            if (containingClass != null) {

                PsiSearchHelper psiSearchHelper = PsiSearchHelper.getInstance(element.getProject());
                psiSearchHelper.processElementsWithWord(new TextOccurenceProcessor() {
                    @Override
                    public boolean execute(@NotNull PsiElement findElement, int offsetInElement) {

                        if (findElement instanceof PsiLiteralExpression psiLiteralExpression) {

                            references.add(new FieldReference(element, psiLiteralExpression));
                        }

                        return false;
                    }
                }, GlobalSearchScope.allScope(field.getProject()), field.getName(), UsageSearchContext.IN_STRINGS, true);
            }

            return references.toArray(PsiReference[]::new);

        }
    }
}
