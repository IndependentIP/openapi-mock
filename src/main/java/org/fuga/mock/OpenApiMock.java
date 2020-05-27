package org.fuga.mock;

/*-
 * #%L
 * OpenAPI Mock
 * %%
 * Copyright (C) 2020 Fuga
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.extern.slf4j.Slf4j;
import wiremock.org.apache.http.HttpStatus;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static wiremock.org.apache.commons.lang3.math.NumberUtils.isParsable;

/**
 * Mock Server based on Swagger specification
 */
@Slf4j
public class OpenApiMock {

    public static final String EXPECTED_EXAMPLE = "expected-example";
    public static final String EXPECTED_RESPONSE = "expected-response";

    /*
     * Contains instance of created wire mock server
     */
    private final WireMockServer wireMockServer;

    public static void main(String[] args) {
        // TODO: properly parse input arguments
        new OpenApiMock("cfg", 8000);
    }

    /**
     * Default constructor for testing purposes only
     */
    OpenApiMock() {
        this(80);
    }

    /**
     * Creates instance of MockServer listening on speficied port
     */
    public OpenApiMock(int port) {
        log.info("Starting MockServer listening on port {}", port);
        wireMockServer = new WireMockServer(
                wireMockConfig()
                        .port(port)
                        .notifier(new Slf4jNotifier(true))
                        .extensions(ExpectedExtension.class));
        wireMockServer.start();
    }

    /**
     * Constructor Mock Server
     *
     * @param swaggerFolder
     *         Package that need to be scanned for annotated classes
     * @param port
     *         Port on which mock server will be reachable.
     */
    public OpenApiMock(String swaggerFolder, int port) {
        this(port);

        // Create Swagger stubs
        loadSpecifications(swaggerFolder);
    }

    /**
     * Gracefully shutdown the server.
     * <p>
     * This method assumes it is being called as the result of an incoming HTTP
     * request.
     */
    public void shutdown() {
        log.info("Shutdown mock server");
        wireMockServer.shutdown();
    }

    private void loadSpecifications(String swaggerFolder) {
        log.info("Create swagger model from yaml files in  {}", swaggerFolder);

        // Remove any previously defined stubs
        reset();

        File folder = new File(swaggerFolder);
        if (folder.isDirectory()) {
            Arrays.stream(folder.listFiles()).forEach(apiDefiniiton -> {
                if (isYamlFile(apiDefiniiton)) {
                    createMocks(apiDefiniiton.getAbsolutePath());
                }
            });
        }
    }

    private void createMocks(final String swaggerPath) {
        OpenAPI swaggerObject = new OpenAPIV3Parser().read(swaggerPath);

        if (swaggerObject != null) {
            createMocks(swaggerObject);
        }
    }

    private boolean isYamlFile(File swaggerDefinition) {
        return (swaggerDefinition.isFile() && (swaggerDefinition.getName().endsWith("yaml")
                || swaggerDefinition.getName().endsWith("yml")));
    }

    /**
     * Reset mock server removing all previously defined stubs
     */
    public void reset() {
        wireMockServer.resetMappings();
        wireMockServer.resetRequests();
        wireMockServer.resetScenarios();
    }

    /**
     * Stub the operations as specified within specification.
     *
     * @param specification
     *         Swagger specification
     */
    private void createMocks(OpenAPI specification) {

        // Create for each defined server mocks
        specification.getServers().forEach(server -> {

            log.info("Processing API specs for {}", server.getUrl());
            try {
                String basePath = new URI(server.getUrl()).getPath();
                log.debug("Extracted basepath is {}", basePath);
                toStream(specification.getPaths()).forEach(paths -> {
                    final String path = basePath + paths.getKey();
                    log.info("Creating mock(s) for path {}", path);
                    createMock(path, paths.getValue());
                });
            } catch (URISyntaxException error) {
                log.error("Unable to retrieve basepath from url " + server.getUrl(), error);
            }
        });

    }

    private void createMock(final String url, final PathItem path) {
        mockOperation(PathItem.HttpMethod.GET, url, path.getGet());
        mockOperation(PathItem.HttpMethod.PUT, url, path.getPut());
        mockOperation(PathItem.HttpMethod.POST, url, path.getPost());
        mockOperation(PathItem.HttpMethod.DELETE, url, path.getDelete());
    }

    /**
     * Creates stub for specified URL
     */
    private MappingBuilder createMock(final PathItem.HttpMethod method, final String url, final Operation operation) {
        // Replace path parameter place holders by regular expression.
        String wiremockCompatibleUrl = url.replaceAll("\\{.*\\}", ".*");

        // Make sure that url also matches requests including query
        // parameters
        wiremockCompatibleUrl = wiremockCompatibleUrl + "(\\?.*)?";

        MappingBuilder stub;
        switch (method) {
            case GET:
                stub = get(urlMatching(wiremockCompatibleUrl));
                break;

            case POST:
                stub = post(urlMatching(wiremockCompatibleUrl));
                break;

            case PUT:
                stub = put(urlMatching(wiremockCompatibleUrl));
                break;

            case DELETE:
                stub = delete(urlMatching(wiremockCompatibleUrl));
                break;

            default:
                log.warn("[{}]:{} is not supported yet", method, url);
                throw new Error("Unsupported HTTP Method");
        }

        stub.withName(operation.getOperationId());
        return stub;
    }

    /**
     * Creates mock for specified URL
     */
    private MappingBuilder createMockWithQueryParameters(final PathItem.HttpMethod method, final String url, final Operation operation) {
        final MappingBuilder mock = createMock(method, url, operation);

        // Add matchers for query parameters
        // TODO: Add matching of query parameters and/or headers
        toStream(operation.getParameters()).forEach(parameter -> {
            log.debug("Processing parameter {}", parameter.getIn());
            if (Optional.ofNullable(parameter.getRequired()).orElse(false)) {
                if (parameter.getIn().equalsIgnoreCase("query")) {
                    mock.withQueryParam(parameter.getName(),
                            matching(createRegularExpression()));
                }
            }
        });

        return mock;
    }

    private MappingBuilder createResponseExample(
            PathItem.HttpMethod method,
            String url, Operation operation,
            int responseStatus,
            String mediaType,
            String exampleKey,
            String example) {
        final MappingBuilder stub = createMockWithQueryParameters(method, url, operation);

        log.debug("Adding examples {}", exampleKey);
        if (!"default".equalsIgnoreCase(exampleKey)) {
            // In case not default response example expected-example header must match
            stub.andMatching(value -> MatchResult.of(exampleKey.equalsIgnoreCase(value.getHeader(EXPECTED_EXAMPLE)))
            );
        }

        if (responseStatus != 200) {
            // In case example not response for success expected-response header or defined expectation must match
            stub.andMatching(value -> {
                        String expectedResponseStatus = value.getHeader(EXPECTED_RESPONSE);

                        if (expectedResponseStatus == null) {
                            expectedResponseStatus = ExpectedExtension.getExpectedResponse(operation.getOperationId());
                        }
                        return MatchResult.of(isParsable(expectedResponseStatus) && responseStatus == Integer.parseInt(expectedResponseStatus));
                    }
            );
        }

        stub.willReturn(aResponse()
                .withStatus(responseStatus)
                .withHeader("Content-Type", mediaType)
                .withHeader("Cache-Control", "no-cache")
                .withBody(example));

        return stub;
    }

    /**
     * Creates default response for requests without mandatory parameters or
     * missing headers.
     */
    private void createResponseBadRequest(PathItem.HttpMethod method, String url,
                                          Operation operation) {
        if ((operation != null) && hasMandatoryQueryParameters(operation)) {

            log.info("Creating default response for bad request [{}]:{}",
                    method, url);
            MappingBuilder stub = createMock(method, url, operation);

            // Create response for request without mandatory query parameters
            stub.willReturn(
                    aResponse()
                            .withStatus(HttpStatus.SC_BAD_REQUEST)
                            .withHeader("Content-Type", "text/plain")
                            .withHeader("Cache-Control", "no-cache")
                            .withBody(
                                    "Invalid Request, missing mandatory parameter or header"))
                    .atPriority(Integer.MAX_VALUE);

            wireMockServer.stubFor(stub);
        }
    }

    /**
     * Returns whether the Operation has mandatory query parameters.
     */
    private boolean hasMandatoryQueryParameters(Operation operation) {
        if (operation.getParameters() != null) {
            for (Parameter parameter : operation.getParameters()) {
                if (parameter.getRequired() != null && parameter.getRequired()
                        && parameter.getIn().equalsIgnoreCase("query")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void mockOperation(final PathItem.HttpMethod method, final String url, final Operation operation) {
        if (operation != null) {
            // Create response for bad request (e,g, missing required query parameter)
            createResponseBadRequest(method, url, operation);

            toStream(operation.getResponses()).forEach(responseDef -> {
                if (isParsable(responseDef.getKey())) {
                    final Integer responseStatus = Integer.valueOf(responseDef.getKey());
                    final ApiResponse response = responseDef.getValue();

                    // create mocks for each media type;
                    toStream(response.getContent()).forEach(mediaTypeDef -> {
                        final MediaType mediaType = mediaTypeDef.getValue();

                        toStream(mediaType.getExamples()).forEach(exampleDef -> {
                            // Will create response which will be returned if expected_example header is specified
                            wireMockServer.stubFor(
                                    createResponseExample(method, url, operation, responseStatus, mediaTypeDef.getKey(), exampleDef.getKey(), exampleDef.getValue().toString()));
                        });

                        Optional.ofNullable(mediaType.getExample()).ifPresent(example -> {
                            // Will create response that is always returned
                            wireMockServer.stubFor(createResponseExample(method, url, operation, responseStatus, mediaTypeDef.getKey(), "default", example.toString()));
                        });
                    });
                } else {
                    // You can use the default response to describe responses collectively,
                    // this response is used for all HTTP codes that are not covered individually for this operation.
                    // This is currently NOT supported by the mocking server
                    log.warn("Unable to create stub for response '{}' in [{}]{}", new Object[]{responseDef.getKey(), method, url});
                }
            });

        }
    }

    /**
     * Create regular expression for matching any value
     */
    private String createRegularExpression() {
        return ".*";
    }

    private <V> Stream<V> toStream(Collection<V> collection) {
        return Optional.ofNullable(collection).stream().flatMap(Collection::stream);
    }

    private <K, V> Stream<Map.Entry<K, V>> toStream(Map<K, V> map) {
        return Optional.ofNullable(map).stream().flatMap(entry -> map.entrySet().stream());
    }

}
