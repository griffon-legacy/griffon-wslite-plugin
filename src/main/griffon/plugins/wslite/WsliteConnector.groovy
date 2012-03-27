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

import griffon.util.CallableWithArgs
import wslite.http.HTTPClient
import wslite.soap.SOAPClient
import wslite.rest.RESTClient
import java.util.concurrent.ConcurrentHashMap

import java.lang.reflect.InvocationTargetException

/**
 * @author Andres Almiray
 */
@Singleton
class WsliteConnector implements WsliteProvider { 
    private final Map BUILDERS = new ConcurrentHashMap()
    
    Object withSoap(Map params, Closure closure) {
        return doWithClient(SOAPClient, params, closure)
    }
       
    Object withHttp(Map params, Closure closure) {
        return doWithClient(HTTPClient, params, closure)
    } 
       
    Object withRest(Map params, Closure closure) {
        return doWithClient(RESTClient, params, closure)
    }
    
    public <T> T withSoap(Map params, CallableWithArgs<T> callable) {
        return doWithClient(SOAPClient, params, callable)
    } 
  
    public <T> T withHttp(Map params, CallableWithArgs<T> callable) {
        return doWithClient(HTTPClient, params, callable)
    } 

    public <T> T withRest(Map params, CallableWithArgs<T> callable) {
        return doWithClient(RESTClient, params, callable)
    }

    // ======================================================

    private Object doWithClient(Class klass, Map params, Closure closure) {
        def client = configureClient(klass, params)

        if (closure) {
            closure.delegate = client
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            return closure()
        }
        return null
    }

    private <T> T doWithClient(Class klass, Map params, CallableWithArgs<T> callable) {
        def client = configureClient(klass, params)

        if (callable) {
            callable.args = [client] as Object[]
            return callable.run()
        }
        return null
    }

    private configureClient(Class klass, Map params) {
        def client = null
        if (params.id) {
            String id = params.remove('id').toString()
            client = BUILDERS[id]
            if(client == null) {
                client = makeClient(klass, params)
                BUILDERS[id] = client 
            }
        } else {
            client = makeClient(klass, params)
        }

        client
    }

    private static final HTTP_PROPERTIES = [
         'connectTimeout', 'readTimeout',
         'followRedirects', 'useCaches',
         'sslTrustAllCerts', 'sslTrustStoreFile',
         'sslTrustStorePassword', 'proxy',
         'httpConnectionFactory', 'authorization'
    ]

    private makeClient(Class klass, Map params) {
        Map httpParams = [:]
        HTTP_PROPERTIES.each { name ->
            def value = params.remove(name)
            if(value != null) httpParams[name] = value
        }
        
        def client = klass.newInstance(params)
        
        if(!(client instanceof HTTPClient)) {
            httpParams.each { k, v ->
                client.httpClient[k] = v
            }
        }
        
        client
    }
}
