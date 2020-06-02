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
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import wiremock.org.apache.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;

@Slf4j
public class ImportOpenApiExtension implements AdminApiExtension {

    private final String IMPORT_OPENAPI_PATH = "/mappings/import/openapi/";

    private final OpenApiMockServer mockServer;

    ImportOpenApiExtension(final OpenApiMockServer mockServer) {
        this.mockServer = mockServer;
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
                    mockServer.createMocks(parseResult.getOpenAPI());
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
}
