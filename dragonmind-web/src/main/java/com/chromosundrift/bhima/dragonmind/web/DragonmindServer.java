package com.chromosundrift.bhima.dragonmind.web;

import com.chromosundrift.bhima.api.Dragon;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class DragonmindServer {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DragonmindServer.class);

    private DragonmindApplication dragonmindApplication;

    public void start(Dragon dragon) {
        try {
            dragonmindApplication = new DragonmindApplication(dragon);
            dragonmindApplication.run("server", "web-configuration.yml");
        } catch (Exception e) {
            logger.error("Cannot start dragonmind server", e);
        }
    }

    public void stop() {
        logger.info("Shutting down Dragonmind Server");
        if (dragonmindApplication != null) {
            dragonmindApplication.stop();
        }
    }
}
