wiremock-velocity-extension
===========================

extension for rendering velocity templates for stubbed responses.

-To Use from CLI
````java
java -cp "tmp/wiremock-velocity-extension-0.1.jar:tmp/wiremock-1.52-standalone.jar" com.github.tomakehurst.wiremock.standalone.WireMockServerRunner --verbose --root-dir tmp/default/ --extensions com.ayork.wiremock.extension.VelocityResponseTransformer
````
or
````java
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8085 -cp "tmp/wiremock-velocity-extension-0.1.jar:tmp/wiremock-1.52-standalone.jar" com.github.tomakehurst.wiremock.standalone.WireMockServerRunner --verbose --root-dir tmp/default/ --extensions com.ayork.wiremock.extension.VelocityResponseTransformer
````