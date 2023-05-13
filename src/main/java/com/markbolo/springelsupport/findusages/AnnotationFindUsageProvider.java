package com.markbolo.springelsupport.findusages;

import com.intellij.lang.HelpID;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiParameterList;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnnotationFindUsageProvider implements FindUsagesProvider {

    @Override
    public boolean canFindUsagesFor(@NotNull final PsiElement psiElement) {
        // 方法参数或者类成员变量提供findUsages
        return psiElement instanceof PsiParameterList
                || psiElement instanceof PsiField;
    }

    @Override
    public @Nullable @NonNls String getHelpId(@NotNull final PsiElement psiElement) {
        return HelpID.FIND_OTHER_USAGES;
    }

    @Override
    public @Nls @NotNull String getType(@NotNull final PsiElement element) {
        return "SpEL";
    }

    @Override
    public @Nls @NotNull String getDescriptiveName(@NotNull final PsiElement element) {
        return "Spring EL expression used";
    }

    @Override
    public @Nls @NotNull String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
        return element.getText();
    }
}
