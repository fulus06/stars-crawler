package com.jotak.stars;

import com.jotak.stars.crawler.CrawlerVerticle;
import com.jotak.stars.endpoints.HttpRouterVerticle;
import com.jotak.stars.stats.StatsVerticle;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.hawkular.VertxHawkularOptions;

public class Main {

	public static void main(String[] args) {

		Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
				new VertxHawkularOptions()
						.setEnabled(true)
						.setTenant("stars-crawler")
						.setHost("metrics.192.168.42.63.xip.io")
						.setPort(443)
						.setHttpOptions(new HttpClientOptions().setSsl(true).setVerifyHost(false).setTrustAll(true))
						.setHttpHeaders(new JsonObject().put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJzdGFycy1jcmF3bGVyIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6InNjaC10b2tlbi00cjJ3eCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJzY2giLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiI0MjMyYzczNi1hMWM2LTExZTYtOWJmYy1iZTg1ZmZkZmIwY2UiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6c3RhcnMtY3Jhd2xlcjpzY2gifQ.Tz26ZeqnG2S14mMou_BlR5hnQQgWP4C6Tx2oWHI6tBd6359Pht0Zy6Kfcc5-sOv7ZWU1pN-hzn5rXhFFlp-niDTl0zKTcgYUv-ighmj5RrpPgNdZJTFrZDdmPITXRAkVebhBslU3kece47CJ6bz59O1HJbOLFaTa6sO1XCBMVZO-srKj1QZoS-8keKmhPLyvNUlFdwrLjv46fDvey1c6_Mb4DNWz2v8b_3-wnbE8pxKusDfbrQ0ON1DcQChM5PDxHHBnqVzxjx7aWc5GmcKt3KNuKgXSMtl-vsj3VF9YJh2woW3l2Kkj0RWxr6h9q15dyLwf68bV9QVZZ_TgxkUyEA"))
						.setPrefix(RunningHost.getHostId())
		));
		vertx.deployVerticle(new CrawlerVerticle());
        vertx.deployVerticle(new StatsVerticle());
		vertx.deployVerticle(new HttpRouterVerticle());
	}
}
