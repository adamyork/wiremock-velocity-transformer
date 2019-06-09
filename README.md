wiremock-velocity-transformer
===========================

transformer used to render velocity templates for stubbed responses.

## To use within a custom Wiremock Java implementation

- Include wiremock-velocity-transformer in your project
- Requires jdk 8+ 

Maven

````
<dependency>
  <groupId>com.github.adamyork</groupId>
  <artifactId>wiremock-velocity-transformer</artifactId>
  <version>2.0</version>
</dependency>
````

Gradle 

````
dependencies {
    compile group: "com.github.adamyork", name: "wiremock-velocity-transformer", version: "2.0"
}
````

- Follow the wiremock documentation for extending : [http://wiremock.org/extending-wiremock.html](http://wiremock.org/extending-wiremock.html)

- Register the velocity transformer with wiremock. For example :

````
new WireMockServer(wireMockConfig().extensions("com.github.adamyork.wiremock.transformer.VelocityResponseTransformer",));
````
or
````
new WireMockServer(wireMockConfig().extensions(VelocityResponseTransformer.class));
````
or 
````
new WireMockServer(wireMockConfig().extensions(new VelocityResponseTransformer()));
````

## To use in conjunction with Wiremock Standalone Jar

- Download the [standalone velocity transformer jar ](https://github.com/adamyork/wiremock-velocity-transformer/releases/download/2.0/wiremock-velocity-transformer-standalone-2.0.jar)

- Download the Wiremock standalone jar from :

[http://wiremock.org/docs/running-standalone/](http://wiremock.org/docs/running-standalone/)

- From the command line **NOTE : Change the versions of the jars to match the one's you have downloaded.**
Windows
````
java -cp "wiremock-standalone-2.32.2.jar;wiremock-velocity-transformer-standalone-2.0.jar" com.github.tomakehurst.wiremock.standalone.WireMockServerRunner --verbose --extensions com.github.adamyork.wiremock.transformer.VelocityResponseTransformer
````
Unix
````
java -cp "wiremock-standalone-2.32.2.jar:wiremock-velocity-transformer-standalone-2.0.jar" com.github.tomakehurst.wiremock.standalone.WireMockServerRunner --verbose --extensions com.github.adamyork.wiremock.transformer.VelocityResponseTransformer
````

## Notes on velocity templates

- all Generic Velocity tools are available to the Velocity context
- request bodies are available via the context
- query parameters via the context.
- if multiple query parameters are found with the same name, 
they will referenced by a postfix; and index start at 0. see example below

Below is an example of a template, assuming a uri of "/resource?startDate=2018-02-01&endDate=2018-02-28&product-code=10&product-code=j1j1j1"
````
{
    "requestAbsoluteUrl" : "$requestAbsoluteUrl",
    "requestMethod" : "$requestMethod",
    "requestHeaderHost" : "$requestHeaderHost",
    "requestHeaderUserAgent" : "$requestHeaderUserAgent",
    "requestHeaderAcceptAccept" : "$requestHeaderAccept",
    "requestHeaderAcceptLanguage" : "$requestHeaderAcceptLanguage",
    "requestHeaderAcceptEncoding" : "$requestHeaderAcceptEncoding",
    "requestHeaderConnection" : "$requestHeaderConnection",
    "requestBody" : $requestBody,
    "requestBodySomeKeyValue" : "$requestBody.someKey",
    #if($requestAbsoluteUrl == 'http://localhost:8089/my/resource')
    "customProp" : "customValue",
    "customProp2" : "customValue2",
    #else
    "customProp" : "customValue",
    #end
    "date" : "$date.getMonth()",
    "math" : "$math.floor(2.5)",
    "startDate" : "$startDate",
    "endDate" : "$endDate",
    "productCode0" : "$productcode0",
    "productCode1" : "$productcode1"
}
````

