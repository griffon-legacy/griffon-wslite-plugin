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

package lombok.core.handlers;

import lombok.ast.*;

import java.util.ArrayList;
import java.util.List;

import static lombok.ast.AST.*;

/**
 * @author Andres Almiray
 */
public abstract class WsliteAwareHandler<TYPE_TYPE extends IType<? extends IMethod<?, ?, ?, ?>, ?, ?, ?, ?, ?>> {
    public static final String WSLITE_PROVIDER_TYPE = "griffon.plugins.wslite.WsliteProvider";
    public static final String WSLITE_CLIENT_HOLDER_TYPE = "griffon.plugins.wslite.WsliteClientHolder";
    public static final String WSLITE_CONTRIBUTION_HANDLER_TYPE = "griffon.plugins.wslite.WsliteContributionHandler";
    private static final String WSLITE_PROVIDER_FIELD_NAME = "this$wsliteProvider";
    private static final String METHOD_GET_WSLITE_PROVIDER = "getWsliteProvider";
    private static final String METHOD_SET_WSLITE_PROVIDER = "setWsliteProvider";
    private static final String METHOD_WITH_SOAP = "withSoap";
    private static final String METHOD_WITH_REST = "withRest";
    private static final String METHOD_WITH_HTTP = "withHttp";
    private static final String PROVIDER = "provider";

    private static final MethodDescriptor[] METHODS = new MethodDescriptor[]{
        new MethodDescriptor("java.lang.Object", METHOD_WITH_HTTP, null, new String[][]{
            {"java.util.Map"}, {"groovy.lang.Closure"}}),
        new MethodDescriptor("T", METHOD_WITH_HTTP, "T", new String[][]{
            {"java.util.Map"}, {"griffon.util.CallableWithArgs", "T"}}),
        new MethodDescriptor("java.lang.Object", METHOD_WITH_REST, null, new String[][]{
            {"java.util.Map"}, {"groovy.lang.Closure"}}),
        new MethodDescriptor("T", METHOD_WITH_REST, "T", new String[][]{
            {"java.util.Map"}, {"griffon.util.CallableWithArgs", "T"}}),
        new MethodDescriptor("java.lang.Object", METHOD_WITH_SOAP, null, new String[][]{
            {"java.util.Map"}, {"groovy.lang.Closure"}}),
        new MethodDescriptor("T", METHOD_WITH_SOAP, "T", new String[][]{
            {"java.util.Map"}, {"griffon.util.CallableWithArgs", "T"}})
    };

    public void addWsliteProviderField(final TYPE_TYPE type) {
        type.editor().injectField(
            FieldDecl(Type(WSLITE_PROVIDER_TYPE), WSLITE_PROVIDER_FIELD_NAME)
                .makePrivate()
                .withInitialization(Call(Name(WSLITE_CLIENT_HOLDER_TYPE), "getInstance"))
        );
    }

    public void addWsliteProviderAccessors(final TYPE_TYPE type) {
        type.editor().injectMethod(
            MethodDecl(Type("void"), METHOD_SET_WSLITE_PROVIDER)
                .makePublic()
                .withArgument(Arg(Type(WSLITE_PROVIDER_TYPE), PROVIDER))
                .withStatement(
                    If(Equal(Name(PROVIDER), Null()))
                        .Then(Block()
                            .withStatement(Assign(Field(WSLITE_PROVIDER_FIELD_NAME), Call(Name(WSLITE_CLIENT_HOLDER_TYPE), "getInstance"))))
                        .Else(Block()
                            .withStatement(Assign(Field(WSLITE_PROVIDER_FIELD_NAME), Name(PROVIDER)))))
        );

        type.editor().injectMethod(
            MethodDecl(Type(WSLITE_PROVIDER_TYPE), METHOD_GET_WSLITE_PROVIDER)
                .makePublic()
                .withStatement(Return(Field(WSLITE_PROVIDER_FIELD_NAME)))
        );
    }

    public void addWsliteContributionMethods(final TYPE_TYPE type) {
        for (MethodDescriptor methodDesc : METHODS) {
            List<Argument> methodArgs = new ArrayList<Argument>();
            List<Expression<?>> callArgs = new ArrayList<Expression<?>>();
            int argCounter = 0;
            for (String[] arg : methodDesc.arguments) {
                String argName = "arg" + argCounter++;
                TypeRef argType = Type(arg[0]);
                if (arg.length == 2) argType.withTypeArgument(Type(arg[1]));
                methodArgs.add(Arg(argType, argName));
                callArgs.add(Name(argName));
            }
            final MethodDecl methodDecl = MethodDecl(Type(methodDesc.returnType), methodDesc.methodName)
                .makePublic()
                .withArguments(methodArgs)
                .withStatement(
                    Return(Call(Field(WSLITE_PROVIDER_FIELD_NAME), methodDesc.methodName)
                        .withArguments(callArgs))
                );
            if (methodDesc.typeParameter != null) methodDecl.withTypeParameter(TypeParam(methodDesc.typeParameter));
            type.editor().injectMethod(methodDecl);
        }
    }

    private static class MethodDescriptor {
        private String methodName;
        private String returnType;
        private String typeParameter;
        private String[][] arguments;

        private MethodDescriptor(String returnType, String methodName, String typeParameter, String[][] arguments) {
            this.returnType = returnType;
            this.methodName = methodName;
            this.typeParameter = typeParameter;
            this.arguments = arguments;
        }
    }
}
