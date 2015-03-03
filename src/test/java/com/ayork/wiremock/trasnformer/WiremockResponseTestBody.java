package com.ayork.wiremock.trasnformer;

public class WiremockResponseTestBody {
    
    private String requestAbsoluteUrl;
    private String requestBody;
    private String requestMethod;
    private String requestHeaderHost;
    private String requestHeaderUserAgent;
    private String requestHeaderAcceptAccept;
    private String requestHeaderAcceptLanguage;
    private String requestHeaderAcceptEncoding;
    private String requestHeaderConnection;
    private String requestUserAgent;
    private String customProp;
    private String customProp2;
    
    public String getRequestAbsoluteUrl() {
        return requestAbsoluteUrl;
    }
    public void setRequestAbsoluteUrl(final String requestAbsoluteUrl) {
        this.requestAbsoluteUrl = requestAbsoluteUrl;
    }
    public String getRequestBody() {
        return requestBody;
    }
    public void setRequestBody(final String requestBody) {
        this.requestBody = requestBody;
    }
    public String getRequestMethod() {
        return requestMethod;
    }
    public void setRequestMethod(final String requestMethod) {
        this.requestMethod = requestMethod;
    }
    public String getRequestHeaderHost() {
        return requestHeaderHost;
    }
    public void setRequestHeaderHost(final String requestHeaderHost) {
        this.requestHeaderHost = requestHeaderHost;
    }
    public String getRequestHeaderUserAgent() {
        return requestHeaderUserAgent;
    }
    public void setRequestHeaderUserAgent(final String requestHeaderUserAgent) {
        this.requestHeaderUserAgent = requestHeaderUserAgent;
    }
    public String getRequestHeaderAcceptAccept() {
        return requestHeaderAcceptAccept;
    }
    public void setRequestHeaderAcceptAccept(final String requestHeaderAcceptAccept) {
        this.requestHeaderAcceptAccept = requestHeaderAcceptAccept;
    }
    public String getRequestHeaderAcceptLanguage() {
        return requestHeaderAcceptLanguage;
    }
    public void setRequestHeaderAcceptLanguage(final String requestHeaderAcceptLanguage) {
        this.requestHeaderAcceptLanguage = requestHeaderAcceptLanguage;
    }
    public String getRequestHeaderAcceptEncoding() {
        return requestHeaderAcceptEncoding;
    }
    public void setRequestHeaderAcceptEncoding(final String requestHeaderAcceptEncoding) {
        this.requestHeaderAcceptEncoding = requestHeaderAcceptEncoding;
    }
    public String getRequestHeaderConnection() {
        return requestHeaderConnection;
    }
    public void setRequestHeaderConnection(final String requestHeaderConnection) {
        this.requestHeaderConnection = requestHeaderConnection;
    }
    public String getRequestUserAgent() {
        return requestUserAgent;
    }
    public void setRequestUserAgent(final String requestUserAgent) {
        this.requestUserAgent = requestUserAgent;
    }
    public String getCustomProp() {
        return customProp;
    }
    public void setCustomProp(final String customProp) {
        this.customProp = customProp;
    }
    public String getCustomProp2() {
        return customProp2;
    }
    public void setCustomProp2(final String customProp2) {
        this.customProp2 = customProp2;
    }

}