package com.github.adamyork.wiremock.transformer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
                new TestHttpHeader("Accept", "application/json"));
        System.out.println(response.content());
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class);
        assertThat(response.statusCode(), equalTo(200));
        assertTrue(body.getRequestAbsoluteUrl().contains("http://localhost:8089/my/resource"));
        assertTrue(body.getRequestMethod().contains("GET"));
        assertTrue(body.getRequestHeaderHost().contains("localhost:8089"));
        assertTrue(body.getRequestHeaderConnection().contains("keep-alive"));
    }

    @Test
    public void testMappingWithBody() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("A response body instead of a bodyFileName")));
        WireMockResponse response = client.get("/my/resource",
                new TestHttpHeader("Accept", "application/json"));
        System.out.println(response.content());
        assertThat(response.statusCode(), equalTo(200));
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
                new TestHttpHeader("Accept", "application/json"),
                new TestHttpHeader("Accept-Language", "en-US"),
                new TestHttpHeader("Accept-Encoding", "UTF-8"),
                new TestHttpHeader("User-Agent", "Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class);
        assertEquals(body.getRequestHeaderAcceptEncoding(), "[UTF-8]");
        assertEquals(body.getRequestHeaderAcceptLanguage(), "[en-US]");
        assertEquals(body.getRequestHeaderUserAgent(), "[Mozilla/5.0]");
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
                new TestHttpHeader("Accept", "application/json"),
                new TestHttpHeader("Accept-Language", "en-US"),
                new TestHttpHeader("Accept-Encoding", "UTF-8"),
                new TestHttpHeader("User-Agent", "Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class);
        assertNotNull(body.getCustomProp2());
    }

    @Test
    public void testDateToolReturnsMonth() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBodyFile("response-test-body.vm")));
        WireMockResponse response = client.get("/my/resource",
                new TestHttpHeader("Accept", "application/json"),
                new TestHttpHeader("Accept-Language", "en-US"),
                new TestHttpHeader("Accept-Encoding", "UTF-8"),
                new TestHttpHeader("User-Agent", "Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class);
        final Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final String month = Integer.toString(cal.get(Calendar.MONTH));
        assertEquals(month, body.getDate());
    }

    @Test
    public void mathToolFloorsValue() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBodyFile("response-test-body.vm")));
        WireMockResponse response = client.get("/my/resource",
                new TestHttpHeader("Accept", "application/json"),
                new TestHttpHeader("Accept-Language", "en-US"),
                new TestHttpHeader("Accept-Encoding", "UTF-8"),
                new TestHttpHeader("User-Agent", "Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class);
        assertEquals("2", body.getMath());
    }

    @Test
    public void postBodyProcessedInTemplate() {
        stubFor(post(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBodyFile("response-test-body-for-post.vm")));
        // Create the request body as a MultiValueMap
        final Map<String, String> requestBody = new HashMap<>();
        requestBody.put("someKey", "someValue");
        final JSONObject jsonObject = new JSONObject(requestBody);
        final HttpEntity httpEntity = new StringEntity(jsonObject.toString(), ContentType.create("application/json", "utf-8"));
        WireMockResponse response = client.post("/my/resource",
                httpEntity,
                new TestHttpHeader("Accept", "application/json"),
                new TestHttpHeader("Accept-Language", "en-US"),
                new TestHttpHeader("Accept-Encoding", "UTF-8"),
                new TestHttpHeader("User-Agent", "Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class);
        assertNotNull(body.getRequestBody());
        assertTrue(body.getRequestBody() instanceof LinkedHashMap);
        final LinkedHashMap map = (LinkedHashMap) body.getRequestBody();
        assertEquals(map.get("someKey"), "someValue");
        assertNotNull(body.getRequestBodySomeKeyValue());
        assertEquals(body.getRequestBodySomeKeyValue(), "someValue");
    }

    @Test
    public void queryParametersAreProcessed() {
        stubFor(get(urlEqualTo("/my/resource?startDate=2018-02-01&endDate=2018-02-28&product-code=10&product-code=j1j1j1"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBodyFile("response-test-body-with-query-params.vm")));
        final WireMockResponse response = client.get("/my/resource?startDate=2018-02-01&endDate=2018-02-28&product-code=10&product-code=j1j1j1",
                new TestHttpHeader("Accept", "application/json"),
                new TestHttpHeader("Accept-Language", "en-US"),
                new TestHttpHeader("Accept-Encoding", "UTF-8"),
                new TestHttpHeader("User-Agent", "Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class);
        assertEquals(body.getStartDate(), "2018-02-01");
        assertEquals(body.getEndDate(), "2018-02-28");
        assertEquals(body.getProductCode0(), "10");
        assertEquals(body.getProductCode1(), "j1j1j1");
    }

    @Test
    public void templateProcessingIsNotAppliedWhenNotTemplate() {
        final VelocityResponseTransformer transformer = new VelocityResponseTransformer();
        final Request mockRequest = mock(Request.class);
        final ResponseDefinition mockResponseDefinition = mock(ResponseDefinition.class);
        when(mockResponseDefinition.getBodyFileName()).thenReturn(".json");
        transformer.transform(mockRequest, mockResponseDefinition, null, null);
        verify(mockRequest, never()).getBodyAsString();
    }

}
