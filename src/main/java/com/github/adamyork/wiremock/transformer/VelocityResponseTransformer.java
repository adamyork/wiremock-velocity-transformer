package com.github.adamyork.wiremock.transformer;

import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

/**
 * Class is used in conjunction with wiremock either standalone or
 * library. Used for transforming the response body of a parameterized
 * velocity template.
 * @author yorka012
 *
 */
public class VelocityResponseTransformer extends ResponseTransformer {

    private static final String NAME = "com.github.radadam.wiremock.transformer.VelocityResponseTransformer";

    /**
     * The Velocity context that will hold our request header
     * data.
     */
    private Context context;
    
    /**
     * The template
     */
    private FileSource fileSource;

    /* (non-Javadoc)
     * @see com.github.tomakehurst.wiremock.extension.Extension#name()
     */
    @Override
    public String name() {
        return NAME;
    }

    /* (non-Javadoc)
     * @see com.github.tomakehurst.wiremock.extension.ResponseTransformer#transform(com.github.tomakehurst.wiremock.http.Request, com.github.tomakehurst.wiremock.http.ResponseDefinition, com.github.tomakehurst.wiremock.common.FileSource)
     */
    @Override
    public ResponseDefinition transform(final Request request,
                                        final ResponseDefinition response,
                                        final FileSource fileSource) {
        if (response.specifiesBodyFile() && templateDeclared(response)) {
            VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.init();
            final ToolManager toolManager = new ToolManager();
            toolManager.setVelocityEngine(velocityEngine);
            context = toolManager.createContext();
            this.fileSource = fileSource;
            addBodyToContext(request.getBodyAsString());
            addHeadersToContext(request.getHeaders());
            context.put("requestAbsoluteUrl", request.getAbsoluteUrl());
            context.put("requestUrl", request.getUrl());
            context.put("requestMethod", request.getMethod());
            transformResponse(response);
        }
        return response;
    }
    
    /**
     * @param response
     * @return Boolean If the filesource is a template.
     */
    private Boolean templateDeclared(final ResponseDefinition response) {
        Pattern extension = Pattern.compile(".vm$");
        Matcher matcher = extension.matcher(response.getBodyFileName());
        return matcher.find();
    }

    /**
     * Adds the request header information to the Velocity context.
     * @param headers
     */
    private void addHeadersToContext(final HttpHeaders headers) {
        for (HttpHeader header : headers.all()) {
            final String rawKey = header.key();
            final String transformedKey = rawKey.replaceAll("-", "");
            context.put("requestHeader".concat(transformedKey), header.values()
                    .toString());
        }
    }

    /**
     * Adds the request body to the context if one exists.
     * @param body
     */
    private void addBodyToContext(final String body) {
        if (!body.isEmpty() && body != null) {
            context.put("requestBody", body);
        }
    }

    /**
     * Renders the velocity template.
     * @param response
     */
    private void transformResponse(final ResponseDefinition response) {
        final String templatePath = fileSource.getPath().concat("/" + response.getBodyFileName());
        final Template template = Velocity.getTemplate(templatePath);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        final byte[] fileBytes = String.valueOf(writer.getBuffer()).getBytes();
        response.setBody(fileBytes);
        response.setBodyFileName(null);
    }

}