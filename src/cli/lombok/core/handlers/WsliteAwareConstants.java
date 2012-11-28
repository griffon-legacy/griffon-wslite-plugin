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

/**
 * @author Andres Almiray
 */
public interface WsliteAwareConstants {
    String WSLITE_PROVIDER_TYPE = "griffon.plugins.wslite.WsliteProvider";
    String WSLITE_CLIENT_HOLDER_TYPE = "griffon.plugins.wslite.WsliteClientHolder";
    String WSLITE_CONTRIBUTION_HANDLER_TYPE = "griffon.plugins.wslite.WsliteContributionHandler";
    String WSLITE_PROVIDER_FIELD_NAME = "this$wsliteProvider";
    String METHOD_GET_WSLITE_PROVIDER = "getWsliteProvider";
    String METHOD_SET_WSLITE_PROVIDER = "setWsliteProvider";
    String METHOD_WITH_SOAP = "withSoap";
    String METHOD_WITH_REST = "withRest";
    String METHOD_WITH_HTTP = "withHttp";
    String PROVIDER = "provider";

    MethodDescriptor[] METHODS = new MethodDescriptor[]{
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

    class MethodDescriptor {
        public String methodName;
        public String returnType;
        public String typeParameter;
        public String[][] arguments;
        public final String signature;

        public MethodDescriptor(String returnType, String methodName, String typeParameter, String[][] arguments) {
            this.returnType = returnType;
            this.methodName = methodName;
            this.typeParameter = typeParameter;
            this.arguments = arguments;
            this.signature = createMethodSignature();
        }

        private String createMethodSignature() {
            StringBuilder b = new StringBuilder();
            if (typeParameter != null) {
                b.append("<").append(typeParameter).append("> ");
            }
            b.append(returnType)
                .append(" ")
                .append(methodName)
                .append("(");
            int argCounter = 0;
            for (String[] arg : arguments) {
                String argName = "arg" + argCounter++;
                b.append(arg[0]);
                if (arg.length == 2) {
                    b.append("<").append(arg[1]).append(">");
                }
                b.append(" ").append(argName);
                if (argCounter != arguments.length) b.append(", ");
            }
            return b.toString();
        }
    }
}
