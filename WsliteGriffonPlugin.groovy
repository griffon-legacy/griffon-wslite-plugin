/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
class WsliteGriffonPlugin {
    // the plugin version
    String version = '1.0.0'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.2.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [lombok: '0.4']
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-wslite-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Lightweight HTTP/REST/SOAP client support'

    String description = '''
The Wslite plugin enables the usage of [groovy-wslite][1] on a Griffon application.

Usage
-----

The plugin will inject the following dynamic methods:

 * `<R> R withRest(Map<String, Object> params, Closure<R> stmts)` - executes stmts using a RESTClient
 * `<R> R withHttp(Map<String, Object> params, Closure<R> stmts)` - executes stmts using an HTTPClient
 * `<R> R withSoap(Map<String, Object> params, Closure<R> stmts)` - executes stmts using a SOAPClient
 * `<R> R withRest(Map<String, Object> params, CallableWithArgs<R> stmts)` - executes stmts using a RESTClient
 * `<R> R withHttp(Map<String, Object> params, CallableWithArgs<R> stmts)` - executes stmts using an HTTPClient
 * `<R> R withSoap(Map<String, Object> params, CallableWithArgs<R> stmts)` - executes stmts using a SOAPClient

The following properties will be set on the implicit HTTPClient when using either `withRest` or `withSoap`:

 * connectTimeout
 * readTimeout
 * followRedirects
 * useCaches
 * sslTrustAllCerts
 * sslTrustStoreFile
 * sslTrustStorePassword
 * proxy
 * httpConnectionFactory
 * authorization

All dynamic methods will create a new client when invoked unless you define an `id:` attribute.
When this attribute is supplied the client will be stored in a cache managed by the `WsliteProvider` that
handled the call.

These methods are also accessible to any component through the singleton `griffon.plugins.wslite.WsliteEnhancer`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`WsliteEnhancer.enhance(metaClassInstance)`.

Configuration
-------------

### WsliteAware AST Transformation

The preferred way to mark a class for method injection is by annotating it with `@griffon.plugins.wslite.WsliteAware`.
This transformation injects the `griffon.plugins.wslite.WsliteContributionHandler` interface and default behavior that
fulfills the contract.

### Dynamic Method Injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.wslite.injectInto = ['controller', 'service']

Dynamic method injection wil skipped for classes implementing `griffon.plugins.wslite.WsliteContributionHandler`.

### Example

This example relies on [Grails][2] as the service provider. Follow these steps to configure the service on the Grails side:

1. Download a copy of [Grails][3] and install it.
2. Create a new Grails application. We'll pick 'exporter' as the application name.

        grails create-app exporter
    
3. Create a controller named `Calculator`

        grails create-controller calculator
    
4. Paste the following code in `grails-app/controllers/exporter/CalculatorController.groovy`

        package exporter
        import grails.converters.JSON
        class CalculatorController {
            def add() {
                double a = params.a.toDouble()
                double b = params.b.toDouble()
                render([result: (a + b)] as JSON)
            }
        }

5. Run the application

        grails run-app

Now we're ready to build the Griffon application

1. Create a new Griffon application. We'll pick `calculator` as the application name

        griffon create-app calculator

2. Install the wslite plugin

        griffon install-plugin wslite

3. Fix the view script to look like this

        package calculator
        application(title: 'Wslite Plugin Example',
          pack: true,
          locationByPlatform: true,
          iconImage: imageIcon('/griffon-icon-48x48.png').image,
          iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                       imageIcon('/griffon-icon-32x32.png').image,
                       imageIcon('/griffon-icon-16x16.png').image]) {
            gridLayout(cols: 2, rows: 4)
            label('Num1:')
            textField(columns: 20, text: bind(target: model, targetProperty: 'num1'))
            label('Num2:')
            textField(columns: 20, text: bind(target: model, targetProperty: 'num2'))
            label('Result:')
            label(text: bind{model.result})
            button('Calculate', enabled: bind{model.enabled}, actionPerformed: controller.calculate)
        }

4. Let's add required properties to the model

        package calculator
        @Bindable
        class CalculatorModel {
            String num1
            String num2
            String result
            boolean enabled = true
        }

5. Now for the controller code. Notice that there is minimal error handling in place. If the user
types something that is not a number the client will surely break, but the code is sufficient for now.

        package calculator
        import wslite.rest.ContentType
        class CalculatorController {
            def model

            def calculate = { evt = null ->
                String a = model.num1
                String b = model.num2
                execInsideUIAsync { model.enabled = false }
                try {
                    def result = withRest(url: 'http://localhost:8080/exporter/calculator', id: 'client') {
                        def response = get(path: '/add', query: [a: a, b: b], accept: ContentType.JSON)
                        response.json.result
                    }
                    execInsideUIAsync { model.result = result }
                } finally {
                    execInsideUIAsync { model.enabled = true }
                }
            }
        }
    
6. Run the application

        griffon run-app

Testing
-------

Dynamic methods will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `WsliteEnhancer.enhance(metaClassInstance, wsliteProviderInstance)` where 
`wsliteProviderInstance` is of type `griffon.plugins.wslite.WsliteProvider`. The contract for this interface looks like this

    public interface WsliteProvider {
        <R> R withSoap(Map<String, Object> params, Closure<R> closure);
        <R> R withHttp(Map<String, Object> params, Closure<R> closure);
        <R> R withRest(Map<String, Object> params, Closure<R> closure);
        <R> R withSoap(Map<String, Object> params, CallableWithArgs<R> callable);
        <R> R withHttp(Map<String, Object> params, CallableWithArgs<R> callable);
        <R> R withRest(Map<String, Object> params, CallableWithArgs<R> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyWsliteProvider implements WsliteProvider {
        public <R> R withSoap(Map<String, Object> params, Closure<R> closure) { null }
        public <R> R withHttp(Map<String, Object> params, Closure<R> closure) { null }
        public <R> R withRest(Map<String, Object> params, Closure<R> closure) { null }
        public <R> R withSoap(Map<String, Object> params, CallableWithArgs<R> callable) { null }
        public <R> R withHttp(Map<String, Object> params, CallableWithArgs<R> callable) { null }
        public <R> R withRest(Map<String, Object> params, CallableWithArgs<R> callable) { null }
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            WsliteEnhancer.enhance(service.metaClass, new MyWsliteProvider())
            // exercise service methods
        }
    }

On the other hand, if the service is annotated with `@griffon.plugins.wslite.WsliteAware` then usage of `WsliteEnhancer` should be
avoided at all costs. Simply set `wsliteProviderInstance` on the service instance directly, like so, first the service definition

    @griffon.plugins.wslite.WsliteAware
    class MyService {
        def serviceMethod() { ... }
    }

Next is the test

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            service.wsliteProvider = new MyWsliteProvider()
            // exercise service methods
        }
    }

Tool Support
------------

### DSL Descriptors

This plugin provides DSL descriptors for Intellij IDEA and Eclipse (provided you have the Groovy Eclipse plugin installed).
These descriptors are found inside the `griffon-wslite-compile-x.y.z.jar`, with locations

 * dsdl/wslite.dsld
 * gdsl/wslite.gdsl

### Lombok Support

Rewriting Java AST in a similar fashion to Groovy AST transformations is posisble thanks to the [lombok][4] plugin.

#### JavaC

Support for this compiler is provided out-of-the-box by the command line tools. There's no additional configuration required.

#### Eclipse

Follow these steps to setup Lombok in Eclipse

 1. Make sure the project has the required files to be opened as an Eclipse Project. Simply call the following command

         griffon integrate-with --eclipse

 2. Install the [eclipse-support][5] plugin and update the `.classpath` file

         griffon install-plugin eclipse-support
         griffon eclipse-update

 3. Open the project in Eclipse
 4. Locate the `lombok-x.y.z.jar` in the project libraries. Right click on it, run it as a Java application. Select
 `lombok.core.Main` as the class to launch. Follow the on-screen instructions. Make a not of the install path as you'll
 need it in the next step. Shutdown Eclipse.
 5. Go to the path where the `lombok.jar` was copied. This path is either found inside the Eclipse insatllatin directory or
 in your local settings. Copy the following file from the project's working directory

         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/wslite-<version>/dist/griffon-wslite-compile-<version>.jar .

 6. Edit the launch script for Eclipse and tweak the boothclasspath entry so that includes the file you just copied

         -Xbootclasspath/a:lombok.jar:griffon-wslite-compile-<version>.jar

 7. Launch Eclipse once more. Eclipse should be able to provide content assist for Java classes annotated with
 `@griffon.plugins.wslite.WsliteAware`.

#### NetBeans

Follow the instructions found in [Annotation Processors Support in the NetBeans IDE, Part I: Using Project Lombok][6].
You may need to specify `lombok.core.AnnotationProcessor` in the list of Annotation Processors.

NetBeans should be able to provide code suggestions on Java classes annotated with `@griffon.plugins.wslite.WsliteAware`.

#### Intellij IDEA

Follow these steps to setup Lombok in Intellij IDEA

 1. Download the latest stable release of [lombok-intellij-plugin][7] as a zip file.
 2. Open up the Preferences dialog in IntelliJ IDEA
 3. Go to the Plugins page. Click on the "Install plugin from disk..." button. Select the zip file you just downloaded.
 4. Shutdown IntelliJ IDEA; locate the directory where the lombok plugin was installed. This directory is usually found
 somewhere inside your personal settings. In OSx for example it would be located in
 `$USER_HOME/Library/Application Support/IntelliJIdea11/lombok-plugin`.
 5. Copy `griffon-wslite-compile-<version>.jar` to the `lib` directory

         $ pwd
           $USER_HOME/Library/Application Support/IntelliJIdea11/lombok-plugin
         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/wslite-<version>/dist/griffon-wslite-compile-<version>.jar lib

 6. Launch IntelliJ IDEA once more. Code completion should work now for Java classes annotated with `@griffon.plugins.wslite.WsliteAware`.

[1]: https://github.com/jwagenleitner/groovy-wslite
[2]: http://grails.org
[3]: http://grails.org/Download
[4]: /plugin/lombok
[5]: /plugin/eclipse-support
[6]: http://netbeans.org/kb/docs/java/annotations-lombok.html
[7]: http://code.google.com/p/lombok-intellij-plugin
'''
}
