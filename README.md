wiremock-velocity-transformer
===========================

transformer used to render velocity templates for stubbed responses.

## To use within a custom Wiremock Java implementation

- Include wiremock-velocity-transformer in your project

- Follow the wiremock documentation for extending :

[http://wiremock.org/extending-wiremock.html](http://wiremock.org/extending-wiremock.html)

- Register the velocity transformer with wiremock. For example :

````java
new WireMockServer(wireMockConfig().extensions("com.ayork.wiremock.VelocityResponseTransformer",));

or

new WireMockServer(wireMockConfig().extensions(VelocityResponseTransformer.class));

or 

new WireMockServer(wireMockConfig().extensions(new VelocityResponseTransformer()));
````

## To use in conjuction with Wiremock Standalone Jar

- Download the standalone jar from :

[http://wiremock.org/running-standalone.html#running-standalone](http://wiremock.org/running-standalone.html#running-standalone)

- From the command line **NOTE : Change the versions of the jars to match the one's you have downloaded.**
````
java -cp "wiremock-velocity-transformer-standalone-1.0.jar:wiremock-1.53-standalone.jar" com.github.tomakehurst.wiremock.standalone.WireMockServerRunner --verbose --extensions com.ayork.wiremock.transformer.VelocityResponseTransformer
````