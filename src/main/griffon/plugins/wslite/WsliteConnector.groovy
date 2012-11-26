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

package griffon.plugins.wslite

import wslite.http.HTTPClient

/**
 * @author Andres Almiray
 */
@Singleton
class WsliteConnector {
    private static final HTTP_PROPERTIES = [
        'connectTimeout', 'readTimeout',
        'followRedirects', 'useCaches',
        'sslTrustAllCerts', 'sslTrustStoreFile',
        'sslTrustStorePassword', 'proxy',
        'httpConnectionFactory', 'authorization'
    ]

    public createClient(Class klass, Map params) {
        Map httpParams = [:]
        HTTP_PROPERTIES.each { name ->
            def value = params.remove(name)
            if (value != null) httpParams[name] = value
        }

        def client = klass.newInstance(params)

        if (!(client instanceof HTTPClient)) {
            httpParams.each { k, v ->
                client.httpClient[k] = v
            }
        }

        client
    }
}
