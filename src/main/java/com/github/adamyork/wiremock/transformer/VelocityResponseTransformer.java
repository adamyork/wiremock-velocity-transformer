package com.github.adamyork.wiremock.transformer;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.shaded.org.json.simple.JSONObject;
import org.apache.velocity.tools.shaded.org.json.simple.parser.JSONParser;
import org.apache.velocity.tools.shaded.org.json.simple.parser.ParseException;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class is used in conjunction with wiremock either standalone or
 * library. Used for transforming the response body of a parameterized
 * velocity template.
 *
 * @author yorka012
 */
public class VelocityResponseTransformer extends ResponseDefinitionTransformer {

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

    @Override
    public ResponseDefinition transform(final Request request,
                                        final ResponseDefinition responseDefinition, final FileSource files,
                                        final Parameters parameters) {
        if(responseDefinition.specifiesBodyFile() && templateDeclared(responseDefinition)) {
            this.fileSource = files;
            final VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.init();
            final ToolManager toolManager = new ToolManager();
            toolManager.setVelocityEngine(velocityEngine);
            context = toolManager.createContext();
            addBodyToContext(request.getBodyAsString());
            addHeadersToContext(request.getHeaders());
            addParametersVariablesToContext(request.getUrl());
            context.put("requestAbsoluteUrl", request.getAbsoluteUrl());
            context.put("requestUrl", request.getUrl());
            context.put("requestMethod", request.getMethod());
            final String body = getRenderedBody(responseDefinition);
            return ResponseDefinitionBuilder.like(responseDefinition).but()
                    .withBody(body)
                    .build();
        } else {
            return responseDefinition;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * @param url the request url to be parsed
     */
    private void addParametersVariablesToContext(final String url) {
        try {
            final Map<String, Integer> uniqueMap = new HashMap<>();
            final List<NameValuePair> result = URLEncodedUtils.parse(new URI(url), Charset.defaultCharset());
            for (NameValuePair nvp : result) {
                int paramCount = 1;
                if(uniqueMap.get(nvp.getName()) != null) {
                    paramCount = uniqueMap.get(nvp.getName());
                }
                final String actualKey = nvp.getName().concat(Integer.toString(paramCount));
                context.put(actualKey.replace("-", ""), nvp.getValue());
                uniqueMap.put(nvp.getName(), ++paramCount);
            }
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param response the response definition
     * @return Boolean If the file source is a template.
     */
    private Boolean templateDeclared(final ResponseDefinition response) {
        Pattern extension = Pattern.compile(".vm$");
        Matcher matcher = extension.matcher(response.getBodyFileName());
        return matcher.find();
    }

    /**
     * Adds the request header information to the Velocity context.
     *
     * @param headers the request headers
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
     *
     * @param body the request body
     */
    private void addBodyToContext(final String body) {
        if(!body.isEmpty()) {
            final JSONParser parser = new JSONParser();
            try {
                final JSONObject json = (JSONObject) parser.parse(body);
                context.put("requestBody", json);
            } catch (final ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Renders the velocity template.
     *
     * @param response the response definition
     */
    private String getRenderedBody(final ResponseDefinition response) {
        final String templatePath = fileSource.getPath().concat("/" + response.getBodyFileName());
        final Template template = Velocity.getTemplate(templatePath);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        final String rendered = String.valueOf(writer.getBuffer());
        return rendered;
    }

}