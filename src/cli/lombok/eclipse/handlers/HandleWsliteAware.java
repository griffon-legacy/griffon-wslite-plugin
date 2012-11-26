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

package lombok.eclipse.handlers;

import griffon.plugins.wslite.WsliteAware;
import lombok.ListenerSupport;
import lombok.core.AnnotationValues;
import lombok.core.handlers.WsliteAwareHandler;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.ast.EclipseType;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

import java.util.Arrays;

import static lombok.ast.AST.Type;
import static lombok.core.util.ErrorMessages.canBeUsedOnClassAndEnumOnly;
import static lombok.eclipse.Eclipse.fromQualifiedName;
import static lombok.eclipse.Eclipse.poss;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * @author Andres Almiray
 */
public class HandleWsliteAware extends EclipseAnnotationHandler<WsliteAware> {
    private final EclipseWsliteAwareHandler handler = new EclipseWsliteAwareHandler();

    @Override
    public void handle(AnnotationValues<WsliteAware> annotation, Annotation source, EclipseNode annotationNode) {
        EclipseType type = EclipseType.typeOf(annotationNode, source);
        if (type.isAnnotation() || type.isInterface()) {
            annotationNode.addError(canBeUsedOnClassAndEnumOnly(ListenerSupport.class));
            return;
        }

        addInterface(EclipseWsliteAwareHandler.WSLITE_CONTRIBUTION_HANDLER_TYPE, type.get(), source);
        handler.addWsliteProviderField(type);
        handler.addWsliteProviderAccessors(type);
        handler.addWsliteContributionMethods(type);
        type.editor().rebuild();
    }

    private void addInterface(String interfaceName, TypeDeclaration type, Annotation annotation) {
        TypeReference[] interfaces = null;
        if (type.superInterfaces != null) {
            interfaces = new TypeReference[type.superInterfaces.length + 1];
            System.arraycopy(type.superInterfaces, 0, interfaces, 0, type.superInterfaces.length);
        } else {
            interfaces = new TypeReference[1];
        }
        final char[][] typeNameTokens = fromQualifiedName(interfaceName);
        interfaces[interfaces.length - 1] = new QualifiedTypeReference(typeNameTokens, poss(annotation, typeNameTokens.length));
        type.superInterfaces = interfaces;
    }

    private static class EclipseWsliteAwareHandler extends WsliteAwareHandler<EclipseType> {
    }
}
