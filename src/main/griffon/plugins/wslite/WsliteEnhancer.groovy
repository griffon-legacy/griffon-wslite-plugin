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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
final class WsliteEnhancer {
    private static final Logger LOG = LoggerFactory.getLogger(WsliteEnhancer)

    private WsliteEnhancer() {}
    
    static void enhance(MetaClass mc, WsliteProvider provider = WsliteConnector.instance) {
        if(LOG.debugEnabled) LOG.debug("Enhancing $mc with $provider")
        mc.withRest = {Map params, Closure closure ->
            provider.withRest(params, closure)
        }
        mc.withRest = {Map params, CallableWithArgs callable ->
            provider.withRest(params, callable)
        }
        mc.withSoap = {Map params, Closure closure ->
            provider.withSoap(params, closure)
        }
        mc.withSoap = {Map params, CallableWithArgs callable ->
            provider.withSoap(params, callable)
        }
        mc.withHttp = {Map params, Closure closure ->
            provider.withHttp(params, closure)
        }
        mc.withHttp = {Map params, CallableWithArgs callable ->
            provider.withHttp(params, callable)
        }
    }
}