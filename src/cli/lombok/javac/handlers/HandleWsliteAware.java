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

package lombok.javac.handlers;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import griffon.plugins.wslite.WsliteAware;
import lombok.core.AnnotationValues;
import lombok.core.handlers.WsliteAwareConstants;
import lombok.core.handlers.WsliteAwareHandler;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.ast.JavacType;

import static lombok.core.util.ErrorMessages.canBeUsedOnClassAndEnumOnly;
import static lombok.javac.handlers.JavacHandlerUtil.chainDotsString;
import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * @author Andres Almiray
 */
public class HandleWsliteAware extends JavacAnnotationHandler<WsliteAware> {
    private final JavacWsliteAwareHandler handler = new JavacWsliteAwareHandler();
    // private static final Logger LOG = LoggerFactory.getLogger(HandleWsliteAware.class);

    @Override
    public void handle(final AnnotationValues<WsliteAware> annotation, final JCTree.JCAnnotation source, final JavacNode annotationNode) {
        deleteAnnotationIfNeccessary(annotationNode, WsliteAware.class);

        JavacType type = JavacType.typeOf(annotationNode, source);
        if (type.isAnnotation() || type.isInterface()) {
            annotationNode.addError(canBeUsedOnClassAndEnumOnly(WsliteAware.class));
            return;
        }

        addInterface(WsliteAwareConstants.WSLITE_CONTRIBUTION_HANDLER_TYPE, type.node());
        handler.addWsliteProviderField(type);
        handler.addWsliteProviderAccessors(type);
        handler.addWsliteContributionMethods(type);
        type.editor().rebuild();
    }

    public void addInterface(String interfaceName, JavacNode node) {
        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) node.get();
        final ListBuffer<JCTree.JCExpression> implementing = ListBuffer.lb();
        implementing.appendList(classDecl.implementing);
        implementing.append(chainDotsString(node, interfaceName));
        classDecl.implementing = implementing.toList();
    }

    private static class JavacWsliteAwareHandler extends WsliteAwareHandler<JavacType> {
    }
}
