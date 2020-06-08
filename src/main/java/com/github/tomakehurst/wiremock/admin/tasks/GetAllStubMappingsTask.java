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
package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.LimitAndOffsetPaginator;
import com.github.tomakehurst.wiremock.admin.model.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class GetAllStubMappingsTask implements AdminTask {

    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        try {
            return getResponseByContext(admin, request);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ResponseDefinition getResponseByContext(Admin admin, Request request) throws URISyntaxException {
        final List<NameValuePair> queryParams = new URIBuilder("http://" + request.getUrl()).getQueryParams();
        Optional<String> context = queryParams.stream().filter(param -> param.getName().equalsIgnoreCase("context")).map(NameValuePair::getValue).findFirst();
        List<StubMapping> stubs;
        ListStubMappingsResult result = new ListStubMappingsResult(LimitAndOffsetPaginator.fromRequest(admin.listAllStubMappings().getMappings(), request));
        if (context.isPresent()) {
            stubs = (List<StubMapping>) additionalFilter(result.getMappings().stream()
                    .filter(item ->
                            Objects.nonNull(item.getMetadata().get("context")) &&
                                    item.getMetadata().get("context").toString().equalsIgnoreCase(context.get()))
            ).collect(Collectors.toList());
        } else {
            stubs = (List<StubMapping>) additionalFilter(result.getMappings().stream()
                    .filter(item -> Objects.isNull(item.getMetadata().get("context"))))
                    .collect(Collectors.toList());

        }
        log.info("Extracted stubs are " + stubs);
        ListStubMappingsResult newResult = new ListStubMappingsResult(stubs, result.getMeta());
        return ResponseDefinitionBuilder.jsonResponse(newResult);

    }

    private Stream additionalFilter(Stream<StubMapping> stream) {
        return stream.filter(item -> !item.getRequest().getMethod().getName().equalsIgnoreCase(RequestMethod.OPTIONS.getName()))
                .filter(item -> Objects.nonNull(item.getName()))
                .filter(item -> Objects.nonNull(item.getMetadata()))
                .filter(item -> item.getMetadata().containsKey("specification"))
                .filter(item -> StringUtils.isNotEmpty(item.getMetadata().getString("specification")))
                // Exclude stubs of UI
                .filter(item -> !item.getMetadata().getString("specification").equalsIgnoreCase("ui"));
    }

}
