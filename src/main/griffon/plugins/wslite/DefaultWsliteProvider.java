/*
 * Copyright 2012-2013 the original author or authors.
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

package griffon.plugins.wslite;

import wslite.http.HTTPClient;
import wslite.rest.RESTClient;
import wslite.soap.SOAPClient;

import java.util.Map;

/**
 * @author Andres Almiray
 */
public class DefaultWsliteProvider extends AbstractWsliteProvider {
    private static final DefaultWsliteProvider INSTANCE;

    static {
        INSTANCE = new DefaultWsliteProvider();
    }

    public static DefaultWsliteProvider getInstance() {
        return INSTANCE;
    }

    private DefaultWsliteProvider() {}

    @Override
    protected HTTPClient getHttpClient(Map<String, Object> params) {
        return WsliteClientHolder.getInstance().fetchHttpClient(params);
    }

    @Override
    protected RESTClient getRestClient(Map<String, Object> params) {
        return WsliteClientHolder.getInstance().fetchRestClient(params);
    }

    @Override
    protected SOAPClient getSoapClient(Map<String, Object> params) {
        return WsliteClientHolder.getInstance().fetchSoapClient(params);
    }
}
