package com.github.radadam.wiremock.trasnformer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.radadam.wiremock.transformer.VelocityResponseTransformer;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VelocityResponseTransformerTest {
    
    private WireMockServer server;
    private WireMockTestClient client;
    
    @Before
    public void setUp() {
        final WireMockConfiguration config = new WireMockConfiguration();
        config.port(8089);
        config.extensions(new VelocityResponseTransformer());
        server = new WireMockServer(config);
        WireMock.configureFor("localhost", 8089);
        server.start();
        client = new WireMockTestClient(8089);
    }
    
    @After
    public void tearDown() {
        server.stop();
        server.shutdownServer();
    }
    
    @Test
    public void testDefaultHeadersArePresent() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBodyFile("response-test-body.vm")));
        WireMockResponse response = client.get("/my/resource",
                                                new TestHttpHeader("Accept","application/json"));
        System.out.println(response.content());
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class );
        assertThat(response.statusCode(),equalTo(200));
        assertEquals(body.getRequestAbsoluteUrl(),"http://localhost:8089/my/resource");
        assertEquals(body.getRequestBody(),"$requestBody");
        assertEquals(body.getRequestMethod(),"GET");
        assertEquals(body.getRequestHeaderHost(),"[localhost:8089]");
        assertEquals(body.getRequestHeaderConnection(),"[Keep-Alive]");
    }
    
    @Test
    public void testExtendedHeadersArePresent() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBodyFile("response-test-body.vm")));
        WireMockResponse response = client.get("/my/resource",
                                                new TestHttpHeader("Accept","application/json"),
                                                new TestHttpHeader("Accept-Language","en-US"),
                                                new TestHttpHeader("Accept-Encoding","UTF-8"),
                                                new TestHttpHeader("User-Agent","Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class );
        assertEquals(body.getRequestHeaderAcceptEncoding(),"[UTF-8]");
        assertEquals(body.getRequestHeaderAcceptLanguage(),"[en-US]");
        assertEquals(body.getRequestHeaderUserAgent(),"[Mozilla/5.0]");
    }
    
    @Test
    public void testExtendVelocitySyntaxSupport() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBodyFile("response-test-body.vm")));
        WireMockResponse response = client.get("/my/resource",
                                                new TestHttpHeader("Accept","application/json"),
                                                new TestHttpHeader("Accept-Language","en-US"),
                                                new TestHttpHeader("Accept-Encoding","UTF-8"),
                                                new TestHttpHeader("User-Agent","Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class );
        assertNotNull(body.getCustomProp2());
    }

}
