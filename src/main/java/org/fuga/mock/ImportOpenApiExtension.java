/**
 * Copyright © 2020 FUGA (mark.schenk@fuga.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuga.mock;

import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import wiremock.org.apache.http.HttpStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.Metadata.metadata;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static wiremock.org.apache.commons.lang3.math.NumberUtils.isParsable;

@Slf4j
public class ImportOpenApiExtension implements AdminApiExtension {

    private static final String UNKNOWN = "unknown";
    private static final String EXPECTED_EXAMPLE = "expected-example";
    private static final String EXPECTED_RESPONSE = "expected-response";


    private static final String IMPORT_OPENAPI_PATH = "/mappings/import/openapi/";

    ImportOpenApiExtension() {
    }

    /**
     * To be overridden if the extension needs to expose new API resources under /__admin
     *
     * @param router
     *         the admin route builder
     */
    @Override
    public void contributeAdminApiRoutes(Router router) {
        // Define expected response/example
        router.add(POST, IMPORT_OPENAPI_PATH, (admin, request, pathParams) -> {

            try {
                ParseOptions options = new ParseOptions();
                options.setResolve(true);
                options.setFlatten(true);
                SwaggerParseResult parseResult = new OpenAPIParser().readContents(request.getBodyAsString(), null, options);

                if (parseResult.getOpenAPI() != null) {
                    log.info("Importing OpenAPI definition");
                    createMocks(admin, parseResult.getOpenAPI());
                    return responseDefinition()
                            .withStatus(HttpStatus.SC_OK)
                            .withBody("Imported OpenAPI definition successfully")
                            .build();
                } else {
                    return responseDefinition()
                            .withStatus(HttpStatus.SC_BAD_REQUEST)
                            .withBody("Failed to upload OpenAPI spec, cause " + parseResult.getMessages())
                            .build();

                }

            } catch(Exception error) {
                return responseDefinition()
                        .withStatus(HttpStatus.SC_BAD_REQUEST)
                        .withBody("Failed to upload OpenAPI spec, cause " + error.getMessage())
                        .build();
            }
        });

    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Stub the operations as specified within specification.
     *
     * @param specification
     *         Swagger specification
     */
    void createMocks(@NonNull Admin admin, @NonNull OpenAPI specification) {

        // Create for each defined server mocks
        specification.getServers().forEach(server -> {

            log.info("Processing API specs for {}", server.getUrl());
            try {
                String basePath = new URI(server.getUrl()).getPath();
                log.debug("Extracted basepath is {}", basePath);

                toStream(specification.getPaths()).forEach(paths -> {
                    final String path = ("/".equals(basePath) ? "" : basePath) + paths.getKey();
                    log.info("Creating mock(s) for path {}", path);
                    createMock(admin, createCategory(specification), path, paths.getValue());
                });
            } catch (URISyntaxException error) {
                log.error("Unable to retrieve basepath from url " + server.getUrl(), error);
            }
        });

    }

    private String createCategory(OpenAPI specification) {
        String title = UNKNOWN;
        String version = UNKNOWN;
        if (specification.getInfo() != null) {
            title = Optional.ofNullable(specification.getInfo().getTitle()).orElse(UNKNOWN);
            version = Optional.ofNullable(specification.getInfo().getVersion()).orElse(UNKNOWN);
        }
        return title + ":" +version;
    }

    private void createMock(final Admin admin, final String category, final String url, final PathItem path) {
        mockOperation(admin, category, PathItem.HttpMethod.GET, url, path.getGet());
        mockOperation(admin, category, PathItem.HttpMethod.PUT, url, path.getPut());
        mockOperation(admin, category, PathItem.HttpMethod.POST, url, path.getPost());
        mockOperation(admin, category, PathItem.HttpMethod.DELETE, url, path.getDelete());
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
                throw new UnsupportedOperationException("Unsupported HTTP Method");
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
        final MappingBuilder mock = createMockWithQueryParameters(method, url, operation);


        log.debug("Adding examples {}", exampleKey);
        if (!"default".equalsIgnoreCase(exampleKey)) {
            // In case not default response example expected-example header must match
            mock.withHeader(EXPECTED_EXAMPLE, equalToIgnoreCase(exampleKey));
        }

        if (responseStatus != 200) {
            // In case example not response for success expected-response header or defined expectation must match
            mock.andMatching(value -> {
                        String expectedResponseStatus = value.getHeader(EXPECTED_RESPONSE);

                        if (expectedResponseStatus == null) {
                            expectedResponseStatus = ExpectedExtension.getExpectedResponse(operation.getOperationId());
                        }
                        return MatchResult.of(isParsable(expectedResponseStatus) && responseStatus == Integer.parseInt(expectedResponseStatus));
                    }
            );
        }

        mock.willReturn(aResponse()
                .withStatus(responseStatus)
                .withHeader("Content-Type", mediaType)
                .withHeader("Cache-Control", "no-cache")
                .withBody(example));

        return mock;
    }

    /**
     * Creates default response for requests without mandatory parameters or
     * missing headers.
     */
    private Optional<MappingBuilder> createResponseBadRequest(PathItem.HttpMethod method, String url,
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

            return Optional.of(stub);
        }

        return Optional.empty();
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

    private void mockOperation(final Admin admin, final String category, final PathItem.HttpMethod method, final String url, final Operation operation) {
        if (operation != null) {
            // Create response for bad request (e,g, missing required query parameter)
            Optional<MappingBuilder> mock = createResponseBadRequest(method, url, operation);
            mock.ifPresent(mapping -> {
                mapping.withMetadata(metadata().attr("specification", category).build());
                admin.addStubMapping(mapping.build());
            });


            toStream(operation.getResponses()).forEach(responseDef -> {
                if (isParsable(responseDef.getKey())) {
                    final Integer responseStatus = Integer.valueOf(responseDef.getKey());
                    final ApiResponse response = responseDef.getValue();

                    // create mocks for each media type;
                    toStream(response.getContent()).forEach(mediaTypeDef -> {
                        final MediaType mediaType = mediaTypeDef.getValue();

                        toStream(mediaType.getExamples()).forEach(exampleDef -> {
                            // Will create response which will be returned if expected_example header is specified
                            MappingBuilder mapping = createResponseExample(method, url, operation, responseStatus, mediaTypeDef.getKey(), exampleDef.getKey(),exampleDef.getValue().getValue().toString());
                            mapping.withMetadata(metadata().attr("specification", category).build());
                            admin.addStubMapping(
                                    mapping.build());
                        });

                        Optional.ofNullable(mediaType.getExample()).ifPresent(example -> {
                            // Will create response that is always returned
                            MappingBuilder mapping = createResponseExample(method, url, operation, responseStatus, mediaTypeDef.getKey(), "default", example.toString());
                            mapping.withMetadata(metadata().attr("specification", category).build());
                            admin.addStubMapping(mapping.build());
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
