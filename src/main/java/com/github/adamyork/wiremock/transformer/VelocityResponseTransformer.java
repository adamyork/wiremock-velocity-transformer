package com.github.adamyork.wiremock.transformer;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.config.ConfigurationUtils;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple;

import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class is used in conjunction with wiremock either standalone or
 * library. Used for transforming the response body of a parameterized
 * velocity template.
 *
 * @author yorka012
 */
public class VelocityResponseTransformer extends ResponseDefinitionTransformer {

    private static final String NAME = "com.github.adamyork.wiremock.transformer.VelocityResponseTransformer";

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
                                        final ResponseDefinition responseDefinition,
                                        final FileSource files,
                                        final Parameters parameters) {
        return Optional.of(templateDeclaredAndSpecifiesBodyFile(responseDefinition))
                .filter(bool -> bool)
                .map(bool -> {
                    this.fileSource = files;
                    final VelocityEngine velocityEngine = new VelocityEngine();
                    velocityEngine.setProperty("file.resource.loader.path", fileSource.getPath());
                    velocityEngine.init();
                    final ToolManager toolManager = new ToolManager();
                    toolManager.configure(ConfigurationUtils.GENERIC_DEFAULTS_PATH);
                    toolManager.setVelocityEngine(velocityEngine);
                    context = toolManager.createContext();
                    final Context contextWithBody = addBodyToContext(request.getBodyAsString(), context);
                    final Context contextWithBodyAndHeaders = addHeadersToContext(request.getHeaders(), contextWithBody);
                    final Context contextWithHeadersBodyAndParams = addParametersVariablesToContext(request.getUrl(),
                            contextWithBodyAndHeaders);
                    contextWithHeadersBodyAndParams.put("requestAbsoluteUrl", request.getAbsoluteUrl());
                    contextWithHeadersBodyAndParams.put("requestUrl", request.getUrl());
                    contextWithHeadersBodyAndParams.put("requestMethod", request.getMethod());
                    final String body = getRenderedBody(responseDefinition, contextWithHeadersBodyAndParams, velocityEngine);
                    return ResponseDefinitionBuilder.like(responseDefinition).but()
                            .withBody(body)
                            .build();
                })
                .orElse(responseDefinition);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Context addParametersVariablesToContext(final String url, final Context context) {
        return Unchecked.supplier(() -> {
            final List<NameValuePair> urlParamsAndValues = URLEncodedUtils.parse(new URI(url), Charset.defaultCharset());
            final Map<String, List<NameValuePair>> paramAndValueGroups = urlParamsAndValues.stream()
                    .collect(Collectors.groupingBy(NameValuePair::getName));
            paramAndValueGroups.values().stream()
                    .flatMap(group -> IntStream.range(0, group.size())
                            .mapToObj(i -> {
                                final String keyPostFix = Optional.of(group.size() > 1)
                                        .filter(bool -> bool)
                                        .map(bool -> Integer.toString(i))
                                        .orElse("");
                                final String key = group.get(i).getName()
                                        .concat(keyPostFix)
                                        .replace("-", "");
                                final String value = group.get(i).getValue();
                                return Tuple.tuple(key, value);
                            }))
                    .collect(Collectors.toList())
                    .forEach(tuple -> context.put(tuple.v1, tuple.v2));
            return context;
        }).get();
    }

    private Boolean templateDeclaredAndSpecifiesBodyFile(final ResponseDefinition response) {
        return response.specifiesBodyFile() && templateDeclared(response);
    }

    private Boolean templateDeclared(final ResponseDefinition response) {
        return response.getBodyFileName().endsWith(".vm");
    }

    private Context addHeadersToContext(final HttpHeaders headers, final Context context) {
        headers.all().forEach(httpHeader -> {
            final String rawKey = httpHeader.key();
            final String transformedKey = rawKey.replaceAll("-", "");
            context.put("requestHeader".concat(transformedKey), httpHeader.values().toString());
        });
        return context;
    }

    private Context addBodyToContext(final String body, final Context context) {
        return Optional.of(body.isEmpty())
                .filter(bool -> bool)
                .map(bool -> context)
                .orElseGet(() -> {
                    final JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
                    return Unchecked.supplier(() -> {
                        final JSONObject json = (JSONObject) parser.parse(body);
                        context.put("requestBody", json);
                        return context;
                    }).get();
                });
    }

    private String getRenderedBody(final ResponseDefinition response, final Context context, final VelocityEngine velocityEngine) {
        final Template template = velocityEngine.getTemplate(response.getBodyFileName());
        final StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return String.valueOf(writer.getBuffer());
    }
}