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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import wslite.http.HTTPClient
import wslite.rest.RESTClient
import wslite.soap.SOAPClient

import griffon.util.CallableWithArgs

import java.util.concurrent.ConcurrentHashMap

/**
 * @author Andres Almiray
 */
class WsliteClientHolder implements WsliteProvider {
    private static final Logger LOG = LoggerFactory.getLogger(WsliteClientHolder)
    private static final WsliteClientHolder INSTANCE

    static {
        INSTANCE = new WsliteClientHolder()
    }

    static WsliteClientHolder getInstance() {
        INSTANCE
    }

    private final Map<String, HTTPClient> HTTP = new ConcurrentHashMap<String, HTTPClient>()
    private final Map<String, RESTClient> REST = new ConcurrentHashMap<String, RESTClient>()
    private final Map<String, SOAPClient> SOAP = new ConcurrentHashMap<String, SOAPClient>()

    private WsliteClientHolder() {

    }

    Object withSoap(Map params, Closure closure) {
        return doWithClient(SOAP, SOAPClient, params, closure)
    }

    Object withHttp(Map params, Closure closure) {
        return doWithClient(HTTP, HTTPClient, params, closure)
    }

    Object withRest(Map params, Closure closure) {
        return doWithClient(REST, RESTClient, params, closure)
    }

    public <T> T withSoap(Map params, CallableWithArgs<T> callable) {
        return doWithClient(SOAP, SOAPClient, params, callable)
    }

    public <T> T withHttp(Map params, CallableWithArgs<T> callable) {
        return doWithClient(HTTP, HTTPClient, params, callable)
    }

    public <T> T withRest(Map params, CallableWithArgs<T> callable) {
        return doWithClient(REST, RESTClient, params, callable)
    }

    // ======================================================

    String[] getHttpClientIds() {
        List<String> ids = []
        ids.addAll(HTTP.keySet())
        ids.toArray(new String[ids.size()])
    }

    HTTPClient getHttpClient(String id) {
        HTTP[id]
    }

    void setHttpClient(String id, HTTPClient client) {
        HTTP[id] = client
    }

    String[] getRestClientIds() {
        List<String> ids = []
        ids.addAll(REST.keySet())
        ids.toArray(new String[ids.size()])
    }

    RESTClient getRestClient(String id) {
        REST[id]
    }

    void setRestClient(String id, RESTClient client) {
        REST[id] = client
    }

    String[] getSoapClientIds() {
        List<String> ids = []
        ids.addAll(SOAP.keySet())
        ids.toArray(new String[ids.size()])
    }

    SOAPClient getSoapClient(String id) {
        SOAP[id]
    }

    void setSoapClient(String id, SOAPClient client) {
        SOAP[id] = client
    }

    // ======================================================

    private Object doWithClient(Map clientStore, Class klass, Map params, Closure closure) {
        def id = params.id
        def client = fetchClient(clientStore, klass, params)

        if (closure) {
            closure.delegate = client
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            if (LOG.debugEnabled) LOG.debug("Executing statements with ${klass.simpleName} '$client'${id ? '(' + id + ')' : ''}")
            return closure()
        }
        return null
    }

    private <T> T doWithClient(Map clientStore, Class klass, Map params, CallableWithArgs<T> callable) {
        def id = params.id
        def client = fetchClient(clientStore, klass, params)

        if (callable) {
            callable.args = [client] as Object[]
            if (LOG.debugEnabled) LOG.debug("Executing statements with ${klass.simpleName} '$client'${id ? '(' + id + ')' : ''}")
            return callable.call()
        }
        return null
    }

    private fetchClient(Map clientStore, Class klass, Map params) {
        def client = clientStore[(params.id).toString()]
        if (client == null) {
            String id = params.id ? params.remove('id').toString() : '<EMPTY>'
            client = WsliteConnector.instance.createClient(klass, params)
            if (id != '<EMPTY>') clientStore[id] = client
        }
        client
    }
}
