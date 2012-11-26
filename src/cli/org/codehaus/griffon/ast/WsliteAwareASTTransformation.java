/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.griffon.ast;

import griffon.plugins.wslite.WsliteAware;
import griffon.plugins.wslite.WsliteClientHolder;
import griffon.plugins.wslite.WsliteContributionHandler;
import griffon.plugins.wslite.WsliteProvider;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.codehaus.griffon.ast.GriffonASTUtils.*;

/**
 * Handles generation of code for the {@code @WsliteAware} annotation.
 * <p/>
 *
 * @author Andres Almiray
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class WsliteAwareASTTransformation extends AbstractASTTransformation {
    private static final Logger LOG = LoggerFactory.getLogger(WsliteAwareASTTransformation.class);
    private static final ClassNode WSLITE_CONTRIBUTION_HANDLER_TYPE = makeClassSafe(WsliteContributionHandler.class);
    private static final ClassNode WSLITE_AWARE_TYPE = makeClassSafe(WsliteAware.class);
    private static final ClassNode WSLITE_PROVIDER_TYPE = makeClassSafe(WsliteProvider.class);
    private static final ClassNode WSLITE_CLIENT_HOLDER_TYPE = makeClassSafe(WsliteClientHolder.class);

    private static final String PROVIDER = "provider";
    private static final String METHOD_GET_WSLITE_PROVIDER = "getWsliteProvider";
    private static final String METHOD_SET_WSLITE_PROVIDER = "setWsliteProvider";
    private static final String METHOD_WITH_SOAP = "withSoap";
    private static final String METHOD_WITH_REST = "withRest";
    private static final String METHOD_WITH_HTTP = "withHttp";
    private static final String[] DELEGATING_METHODS = new String[]{
        METHOD_WITH_HTTP, METHOD_WITH_REST, METHOD_WITH_SOAP
    };

    static {
        Arrays.sort(DELEGATING_METHODS);
    }

    /**
     * Convenience method to see if an annotated node is {@code @WsliteAware}.
     *
     * @param node the node to check
     * @return true if the node is an event publisher
     */
    public static boolean hasWsliteAwareAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (WSLITE_AWARE_TYPE.equals(annotation.getClassNode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param nodes  the ast nodes
     * @param source the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        checkNodesForAnnotationAndType(nodes[0], nodes[1]);
        addWsliteContributionIfNeeded(source, (ClassNode) nodes[1]);
    }

    public static void addWsliteContributionIfNeeded(SourceUnit source, ClassNode classNode) {
        if (needsWsliteContribution(classNode, source)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Injecting " + WsliteContributionHandler.class.getName() + " into " + classNode.getName());
            }
            apply(classNode);
        }
    }

    protected static boolean needsWsliteContribution(ClassNode declaringClass, SourceUnit sourceUnit) {
        boolean found1 = false, found2 = false, found3 = false, found4 = false, found5 = false;
        ClassNode consideredClass = declaringClass;
        while (consideredClass != null) {
            for (MethodNode method : consideredClass.getMethods()) {
                // just check length, MOP will match it up
                found1 = method.getName().equals(METHOD_WITH_SOAP) && method.getParameters().length == 2;
                found2 = method.getName().equals(METHOD_WITH_REST) && method.getParameters().length == 2;
                found3 = method.getName().equals(METHOD_WITH_HTTP) && method.getParameters().length == 2;
                found4 = method.getName().equals(METHOD_SET_WSLITE_PROVIDER) && method.getParameters().length == 1;
                found5 = method.getName().equals(METHOD_GET_WSLITE_PROVIDER) && method.getParameters().length == 0;
                if (found1 && found2 && found3) {
                    return false;
                }
            }
            consideredClass = consideredClass.getSuperClass();
        }
        if (found1 || found2 || found3 || found4 || found5) {
            sourceUnit.getErrorCollector().addErrorAndContinue(
                new SimpleMessage("@WsliteAware cannot be processed on "
                    + declaringClass.getName()
                    + " because some but not all of methods from " + WsliteContributionHandler.class.getName() + " were declared in the current class or super classes.",
                    sourceUnit)
            );
            return false;
        }
        return true;
    }

    public static void apply(ClassNode declaringClass) {
        injectInterface(declaringClass, WSLITE_CONTRIBUTION_HANDLER_TYPE);

        // add field:
        // protected WsliteProvider this$wsliteProvider = WsliteClientHolder.instance
        FieldNode providerField = declaringClass.addField(
            "this$wsliteProvider",
            ACC_PRIVATE | ACC_SYNTHETIC,
            WSLITE_PROVIDER_TYPE,
            defaultWsliteProviderInstance());

        // add method:
        // WsliteProvider getWsliteProvider() {
        //     return this$wsliteProvider
        // }
        injectMethod(declaringClass, new MethodNode(
            METHOD_GET_WSLITE_PROVIDER,
            ACC_PUBLIC,
            WSLITE_PROVIDER_TYPE,
            Parameter.EMPTY_ARRAY,
            NO_EXCEPTIONS,
            returns(field(providerField))
        ));

        // add method:
        // void setWsliteProvider(WsliteProvider provider) {
        //     this$wsliteProvider = provider ?: WsliteClientHolder.instance
        // }
        injectMethod(declaringClass, new MethodNode(
            METHOD_SET_WSLITE_PROVIDER,
            ACC_PUBLIC,
            ClassHelper.VOID_TYPE,
            params(
                param(WSLITE_PROVIDER_TYPE, PROVIDER)),
            NO_EXCEPTIONS,
            block(
                ifs_no_return(
                    cmp(var(PROVIDER), ConstantExpression.NULL),
                    assigns(field(providerField), defaultWsliteProviderInstance()),
                    assigns(field(providerField), var(PROVIDER))
                )
            )
        ));

        for (MethodNode method : WSLITE_CONTRIBUTION_HANDLER_TYPE.getMethods()) {
            if (Arrays.binarySearch(DELEGATING_METHODS, method.getName()) < 0) continue;
            List<Expression> variables = new ArrayList<Expression>();
            Parameter[] parameters = new Parameter[method.getParameters().length];
            for (int i = 0; i < method.getParameters().length; i++) {
                Parameter p = method.getParameters()[i];
                parameters[i] = new Parameter(makeClassSafe(p.getType()), p.getName());
                parameters[i].getType().setGenericsTypes(p.getType().getGenericsTypes());
                variables.add(var(p.getName()));
            }
            ClassNode returnType = makeClassSafe(method.getReturnType());
            returnType.setGenericsTypes(method.getReturnType().getGenericsTypes());
            returnType.setGenericsPlaceHolder(method.getReturnType().isGenericsPlaceHolder());

            MethodNode newMethod = new MethodNode(
                method.getName(),
                ACC_PUBLIC,
                returnType,
                parameters,
                NO_EXCEPTIONS,
                returns(call(
                    field(providerField),
                    method.getName(),
                    new ArgumentListExpression(variables)))
            );
            newMethod.setGenericsTypes(method.getGenericsTypes());
            injectMethod(declaringClass, newMethod);
        }
    }

    private static Expression defaultWsliteProviderInstance() {
        return call(WSLITE_CLIENT_HOLDER_TYPE, "getInstance", NO_ARGS);
    }
}
