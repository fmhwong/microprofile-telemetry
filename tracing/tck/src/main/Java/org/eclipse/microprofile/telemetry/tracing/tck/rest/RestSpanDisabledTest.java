/*
 * Copyright (c) 2016-2022 Contributors to the Eclipse Foundation
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.eclipse.microprofile.telemetry.tracing.tck.rest;

import static java.net.HttpURLConnection.HTTP_OK;

import java.net.URL;

import javax.inject.Inject;

import org.eclipse.microprofile.telemetry.tracing.tck.exporter.InMemorySpanExporter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.restassured.RestAssured;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

@ExtendWith(ArquillianExtension.class)
class RestSpanDisabledTest {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsResource(new StringAsset("mp.telemetry.tracing.enabled=false"),
                        "META-INF/microprofile-config.properties");
    }

    @ArquillianResource
    URL url;
    @Inject
    InMemorySpanExporter spanExporter;

    @BeforeEach
    void setUp() {
        spanExporter.reset();
    }

    @Test
    void span() {
        RestAssured.given().get("/span").then().statusCode(HTTP_OK);
        spanExporter.getFinishedSpanItems(0);
    }

    @Test
    void spanName() {
        RestAssured.given().get("/span/1").then().statusCode(HTTP_OK);
        spanExporter.getFinishedSpanItems(0);
    }

    @Test
    void spanNameWithoutQueryString() {
        RestAssured.given().get("/span/1?id=1").then().statusCode(HTTP_OK);
        spanExporter.getFinishedSpanItems(0);
    }

    @Path("/")
    public static class SpanResource {
        @GET
        @Path("/span")
        public Response span() {
            return Response.ok().build();
        }

        @GET
        @Path("/span/{name}")
        public Response spanName(@PathParam(value = "name") String name) {
            return Response.ok().build();
        }
    }

    @ApplicationPath("/")
    public static class RestApplication extends Application {

    }
}
