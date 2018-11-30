package com.chromosundrift.bhima.dragonmind.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class DragonmindServer {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DragonmindServer.class);

    private static Server server;

    public void start(int port) {
        server = new Server(port);

        ServletContextHandler ctx =
                new ServletContextHandler(ServletContextHandler.NO_SESSIONS);


        ctx.setWelcomeFiles(new String[]{"index.html", "index.htm", "index.jsp"});

        ServletHolder holderPwd = new ServletHolder("default", DefaultServlet.class);
        holderPwd.setInitParameter("dirAllowed", "true");
        ctx.addServlet(holderPwd, "/");

        ctx.setContextPath("/");
        server.setHandler(ctx);

        ServletHolder serHol = ctx.addServlet(ServletContainer.class, "/rest/*");
        serHol.setInitOrder(1);
        serHol.setInitParameter("jersey.config.server.provider.packages",
                "com.chromosundrift.");

        ServletHandler servletHandler = new ServletHandler();

        StatisticsHandler statsHandler = new StatisticsHandler();
        statsHandler.setHandler(servletHandler);

        server.setHandler(statsHandler);
        server.setStopTimeout(3000L);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        logger.info("Shutting down Dragonmind Server");
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
