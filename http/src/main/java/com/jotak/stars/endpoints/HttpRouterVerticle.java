package com.jotak.stars.endpoints;

import static com.jotak.stars.common.EventBusAddresses.EB_PUSH_STATS;

import com.jotak.stars.common.Cluster;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 * @author Joel Takvorian
 */
public class HttpRouterVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRouterVerticle.class);

    @Override public void start() throws Exception {
        Router router = Router.router(vertx);
        router.route("/eventbus/*").handler(SockJSHandler.create(vertx)
                .bridge(new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddress(EB_PUSH_STATS))));
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
        LOG.info("HTTP listening on 8080");
    }

    public static void main(String[] args) {
        Cluster.deploy(new HttpRouterVerticle(), LOG::error);
    }
}
