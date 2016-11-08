/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jotak.stars.common;

import java.util.function.BiConsumer;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import io.vertx.ext.hawkular.VertxHawkularOptions;

/**
 * @author Joel Takvorian
 */
public final class Cluster {

    private Cluster() {
    }

    public static void deploy(final Verticle verticle, final BiConsumer<String, Throwable> errorHandler) {
        Vertx.clusteredVertx(new VertxOptions().setMetricsOptions(setupHawkular())
                        .setClusterManager(new InfinispanClusterManager())
                        .setClustered(true),
                handler -> {
                    if (handler.succeeded()) {
                        handler.result().deployVerticle(verticle);
                    } else {
                        errorHandler.accept("Failed to initialize clustered vertx", handler.cause());
                    }
                });
    }

    private static MetricsOptions setupHawkular() {
        return new VertxHawkularOptions()
                .setEnabled(true)
                .setTenant("stars-crawler")
                .setHost("metrics.192.168.42.63.xip.io")
                .setPort(443)
                .setHttpOptions(new HttpClientOptions().setSsl(true).setVerifyHost(false).setTrustAll(true))
                .setHttpHeaders(new JsonObject().put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJzdGFycy1jcmF3bGVyIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6InNjaC10b2tlbi00cjJ3eCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJzY2giLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiI0MjMyYzczNi1hMWM2LTExZTYtOWJmYy1iZTg1ZmZkZmIwY2UiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6c3RhcnMtY3Jhd2xlcjpzY2gifQ.Tz26ZeqnG2S14mMou_BlR5hnQQgWP4C6Tx2oWHI6tBd6359Pht0Zy6Kfcc5-sOv7ZWU1pN-hzn5rXhFFlp-niDTl0zKTcgYUv-ighmj5RrpPgNdZJTFrZDdmPITXRAkVebhBslU3kece47CJ6bz59O1HJbOLFaTa6sO1XCBMVZO-srKj1QZoS-8keKmhPLyvNUlFdwrLjv46fDvey1c6_Mb4DNWz2v8b_3-wnbE8pxKusDfbrQ0ON1DcQChM5PDxHHBnqVzxjx7aWc5GmcKt3KNuKgXSMtl-vsj3VF9YJh2woW3l2Kkj0RWxr6h9q15dyLwf68bV9QVZZ_TgxkUyEA"))
                .setPrefix(RunningHost.getHostId());
    }
}
