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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Mock Server based on Swagger specification
 */
@Slf4j
public class OpenApiMockServer {

    private static final String HELP = "help";
    private static final String PORT = "port";
    private static final String OPEN_API_DIR = "openapi-dir";

    /*
     * Contains instance of created wire mock server
     */
    private final WireMockServer wireMockServer;

    private final ImportOpenApiExtension openApiExtension;

    private static OpenApiMockServer openApiMockServer = null;

    public static OpenApiMockServer getInstance(final int port) {
        if (openApiMockServer == null) {
            openApiMockServer = new OpenApiMockServer(port);
        }

        return openApiMockServer;
    }

    public static void main(String[] args) throws IOException {

        OptionParser optionParser = new OptionParser();
        optionParser.accepts(PORT, "The port number for the server to listen on. 0 for dynamic port selection.").withOptionalArg().defaultsTo("8080");
        optionParser.accepts(OPEN_API_DIR, "Specifies path to Open API specifications that need to be loaded during startup").withOptionalArg();
        optionParser.accepts(HELP, "Print this message");

        OptionSet optionSet = optionParser.parse(args);

        if (optionSet.has(HELP)) {
            optionParser.printHelpOn(System.out);
        } else {

            // Get instance of server
            OpenApiMockServer server = getInstance(Integer.parseInt((String) optionSet.valueOf(PORT)));

            // bootstrap open api specifications
            if (optionSet.has(OPEN_API_DIR)) {
                server.loadSpecifications((String) optionSet.valueOf(OPEN_API_DIR));
            }
        }

    }

    /**
     * Creates instance of MockServer listening on speficied port
     */
    private OpenApiMockServer(int port) {
        log.info("Starting MockServer listening on port {}", port);
        openApiExtension = new ImportOpenApiExtension();
        wireMockServer = new WireMockServer(
                wireMockConfig()
                        .port(port)
                        .notifier(new Slf4jNotifier(true))
                        .extensions(ExpectedExtension.class)
                        .extensions(openApiExtension)
                        .extensions(new ResponseTemplateTransformer(true))
                        .usingFilesUnderClasspath("ui"));
        wireMockServer.start();
    }

    public void loadSpecifications(final String openApiFolder) {
        log.info("Create mocks from Open API specs in  {}", openApiFolder);

        File folder = new File(openApiFolder);
        if (folder.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(folder.listFiles())).forEach(apiDefinition -> {
                if (isYamlFile(apiDefinition)) {
                    createMocksFromFile(apiDefinition.toURI());
                }
            });
        }
    }

    private void createMocksFromFile(final URI swaggerLocation) {
        SwaggerParseResult parseResult = new OpenAPIParser().readLocation(swaggerLocation.toString(), null, null);

        if (parseResult.getOpenAPI() != null) {
            openApiExtension.createMocks(wireMockServer, parseResult.getOpenAPI());
        }
    }

    private boolean isYamlFile(File swaggerDefinition) {
        return (swaggerDefinition.isFile() && (swaggerDefinition.getName().endsWith("yaml")
                || swaggerDefinition.getName().endsWith("yml")));
    }

}

