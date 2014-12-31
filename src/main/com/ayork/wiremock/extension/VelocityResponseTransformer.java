package com.ayork.wiremock.extension;

import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class VelocityResponseTransformer extends ResponseTransformer {

    private static final String NAME = "velocityResponseTransformer";

    private VelocityContext context;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public ResponseDefinition transform(final Request request,
            final ResponseDefinition response) {
        if (response.specifiesBodyFile() && templateDeclared(response)) {
            context = new VelocityContext();
            addBodyToContext(request.getBodyAsString());
            addHeadersToContext(request.getHeaders());
            context.put("requestAbsoluteUrl", request.getAbsoluteUrl());
            context.put("requestUrl", request.getUrl());
            context.put("requestMethod", request.getMethod());
            transformResponse(response);
        }
        return response;
    }
    
    private Boolean templateDeclared(final ResponseDefinition response) {
        Pattern extension = Pattern.compile(".vm$");
        Matcher matcher = extension.matcher(response.getBodyFileName());
        return matcher.find();
    }

    private void addHeadersToContext(final HttpHeaders headers) {
        for (HttpHeader header : headers.all()) {
            context.put("requestHeader".concat(header.key()), header.values()
                    .toString());
        }
    }

    private void addBodyToContext(final String body) {
        if (!body.isEmpty() && body != null) {
            context.put("requestBody", body);
        }
    }

    private void transformResponse(final ResponseDefinition response) {
        final String templatePath = fileSource.getPath().concat("/" + response.getBodyFileName());
        final Template template = Velocity.getTemplate(templatePath);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        final byte[] fileBytes = String.valueOf(writer.getBuffer()).getBytes();
        response.setBody(fileBytes);
    }

}