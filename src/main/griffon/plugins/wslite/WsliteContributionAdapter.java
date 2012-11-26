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

package griffon.plugins.wslite;

import griffon.util.CallableWithArgs;
import groovy.lang.Closure;

import java.util.Map;

/**
 * @author Andres Almiray
 */
public class WsliteContributionAdapter implements WsliteContributionHandler {
    private WsliteProvider provider = WsliteClientHolder.getInstance();

    public void setWsliteProvider(WsliteProvider provider) {
        this.provider = provider != null ? provider : WsliteClientHolder.getInstance();
    }

    public WsliteProvider getWsliteProvider() {
        return provider;
    }

    public Object withSoap(Map params, Closure closure) {
        return getWsliteProvider().withSoap(params, closure);
    }

    public Object withHttp(Map params, Closure closure) {
        return getWsliteProvider().withHttp(params, closure);
    }

    public Object withRest(Map params, Closure closure) {
        return getWsliteProvider().withRest(params, closure);
    }

    public <T> T withSoap(Map params, CallableWithArgs<T> callable) {
        return getWsliteProvider().withSoap(params, callable);
    }

    public <T> T withHttp(Map params, CallableWithArgs<T> callable) {
        return getWsliteProvider().withHttp(params, callable);
    }

    public <T> T withRest(Map params, CallableWithArgs<T> callable) {
        return getWsliteProvider().withRest(params, callable);
    }
}
