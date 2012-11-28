/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lombok.intellij.processor.clazz;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import de.plushnikov.intellij.lombok.ErrorMessages;
import de.plushnikov.intellij.lombok.problem.ProblemBuilder;
import de.plushnikov.intellij.lombok.processor.clazz.AbstractLombokClassProcessor;
import de.plushnikov.intellij.lombok.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.lombok.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.lombok.psi.LombokPsiElementFactory;
import de.plushnikov.intellij.lombok.util.PsiMethodUtil;
import de.plushnikov.intellij.lombok.util.PsiPrimitiveTypeFactory;
import griffon.plugins.wslite.WsliteAware;
import lombok.core.handlers.WsliteAwareConstants;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Andres Almiray
 */
public class WsliteAwareProcessor extends AbstractLombokClassProcessor implements WsliteAwareConstants {
    private static final String WSLITE_PROVIDER_FIELD_INITIALIZER = WSLITE_CLIENT_HOLDER_TYPE + ".getInstance()";

    public WsliteAwareProcessor() {
        super(WsliteAware.class, PsiMethod.class);
    }

    @Override
    protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
        return validateAnnotationOnRigthType(psiClass, builder);
    }

    protected boolean validateAnnotationOnRigthType(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
        boolean result = true;
        if (psiClass.isAnnotationType() || psiClass.isInterface() || psiClass.isEnum()) {
            builder.addError(ErrorMessages.canBeUsedOnClassOnly(WsliteAware.class));
            result = false;
        }
        return result;
    }

    protected <Psi extends PsiElement> void processIntern(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<Psi> target) {
        PsiElementFactory psiElementFactory = psiElementFactory(psiClass);
        PsiManager manager = psiClass.getContainingFile().getManager();

        PsiType psiProviderType = psiElementFactory.createTypeFromText(WSLITE_PROVIDER_TYPE, psiClass);
        LombokLightFieldBuilder providerField = LombokPsiElementFactory.getInstance().createLightField(manager, WSLITE_PROVIDER_FIELD_NAME, psiProviderType)
            .withContainingClass(psiClass)
            .withModifier(PsiModifier.PRIVATE)
            .withNavigationElement(psiAnnotation);
        PsiExpression initializer = psiElementFactory.createExpressionFromText(String.format(WSLITE_PROVIDER_FIELD_INITIALIZER), psiClass);
        providerField.setInitializer(initializer);

        LombokLightMethodBuilder method = LombokPsiElementFactory.getInstance().createLightMethod(psiClass.getManager(), METHOD_GET_WSLITE_PROVIDER)
            .withMethodReturnType(psiProviderType)
            .withContainingClass(psiClass)
            .withNavigationElement(psiAnnotation);
        method.withModifier(PsiModifier.PUBLIC);
        target.add((Psi) method);

        method = LombokPsiElementFactory.getInstance().createLightMethod(psiClass.getManager(), METHOD_SET_WSLITE_PROVIDER)
            .withMethodReturnType(PsiPrimitiveTypeFactory.getInstance().getVoidType())
            .withContainingClass(psiClass)
            .withParameter(PROVIDER, psiProviderType)
            .withNavigationElement(psiAnnotation);
        method.withModifier(PsiModifier.PUBLIC);
        target.add((Psi) method);

        for (MethodDescriptor methodDesc : METHODS) {
            target.add((Psi) PsiMethodUtil.createMethod(psiClass, methodDesc.signature, psiAnnotation));
        }
    }

    private PsiElementFactory psiElementFactory(PsiClass psiClass) {
        Project project = psiClass.getProject();
        return JavaPsiFacade.getElementFactory(project);
    }
}
