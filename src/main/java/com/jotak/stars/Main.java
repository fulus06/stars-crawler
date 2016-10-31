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
						.setHttpHeaders(new JsonObject().put("Authorization", "Bearer UARhU5O6mOls1NIqLtHWNRyCioSZPxlQi5jtcSeBhAc"))
						.setPrefix(RunningHost.getHostId())
		));
		vertx.deployVerticle(new CrawlerVerticle());
        vertx.deployVerticle(new StatsVerticle());
		vertx.deployVerticle(new HttpRouterVerticle());
	}
}
