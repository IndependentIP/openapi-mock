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
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import lombok.extern.slf4j.Slf4j;
import wiremock.org.apache.http.HttpStatus;

import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.http.RequestMethod.*;
import static wiremock.org.apache.commons.lang3.math.NumberUtils.isParsable;

/**
 * Extension of WireMock to define expected response for OpenApi
 */
@Slf4j
public class ExpectedExtension implements AdminApiExtension {

    private final String ENDPOINT_NAME = "name";
    private final String EXPECTED_PATH = "/expected/{" + ENDPOINT_NAME + "}";

    private static final ConcurrentHashMap<String, Integer> expected = new ConcurrentHashMap<>();

    static String getExpectedResponse(final String endpointName) {
        return String.valueOf(expected.get(endpointName));
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
        router.add(PUT, EXPECTED_PATH, (admin, request, pathParams) -> {
            String name = pathParams.get(ENDPOINT_NAME);

            QueryParameter expectedResponse = request.queryParameter("response");
            if (expectedResponse.isPresent()) {
                log.debug("Defining expected response for {}", name);
                if (isParsable(expectedResponse.firstValue())) {
                    expected.put(name, Integer.valueOf(expectedResponse.firstValue()));
                    return responseDefinition()
                            .withStatus(HttpStatus.SC_OK)
                            .withBody("Succefully added expected respose '" + expectedResponse.firstValue() + "' for " + name)
                            .build();
                } else {
                    return responseDefinition()
                            .withStatus(HttpStatus.SC_BAD_REQUEST)
                            .withBody("response must contain valid HTTP Status")
                            .build();
                }
            } else {
                return responseDefinition()
                        .withStatus(HttpStatus.SC_BAD_REQUEST)
                        .withBody("Request must contain parameter 'response'")
                        .build();
            }
        }
        );

        // Return expected response/example
        router.add(GET, EXPECTED_PATH, (admin, request, pathParams) -> {
            String name = pathParams.get(ENDPOINT_NAME);
            if (expected.containsKey(name)) {
                return responseDefinition()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody("expecting response with http status " + expected.get(name))
                        .build();
            } else {
                return responseDefinition()
                        .withStatus(HttpStatus.SC_NOT_FOUND)
                        .withBody("No expectation defined for '" + name + "'")
                        .build();
            }
        });

        // Remove expected response/example
        router.add(DELETE, EXPECTED_PATH, (admin, request, pathParams) -> {
            String name = pathParams.get(ENDPOINT_NAME);
            if (expected.containsKey(name)) {
                int expectedResponse = expected.remove(name);
                return responseDefinition()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody("removed expected response " + expectedResponse + " for " + name + ", now returning default response")
                        .build();
            } else {
                return responseDefinition()
                        .withStatus(HttpStatus.SC_NOT_FOUND)
                        .withBody("No expectation defined for '" + name + "'")
                        .build();
            }
        });

    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
