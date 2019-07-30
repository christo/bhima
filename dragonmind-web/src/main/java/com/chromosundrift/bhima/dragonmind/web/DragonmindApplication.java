package com.chromosundrift.bhima.dragonmind.web;

import com.chromosundrift.bhima.api.Dragon;
import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.Wiring;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Arrays.asList;

public class DragonmindApplication extends Application<WebConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DragonmindApplication.class);
    private Dragon dragon;

    public DragonmindApplication(Dragon dragon) {
        this.dragon = dragon;
    }

    @Override
    public String getName() {
        return "web-configuration";
    }

    @Override
    public void initialize(Bootstrap<WebConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/static/", "/", "index.html"));
    }

    @Override
    public void run(WebConfiguration configuration,
                    Environment environment) {

        final TemplateHealthCheck healthCheck =
                new TemplateHealthCheck(configuration.getTemplate());
        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(new BhimaResource(dragon));
    }

    public void stop() {
        logger.info("stopping DragonmindApplication");
    }


    public static void main(String[] args) throws Exception {
        logger.info("starting DragonmindApplication in standalone mode");
        new DragonmindApplication(new DummyDragon()).run(args);
    }


    private static class DummyDragon implements Dragon {
        @Override
        public String getStatus() {
            return "dummy";
        }

        @Override
        public Config getConfig() {
            return null;
        }

        @Override
        public Wiring getWiring() {
            return null;
        }

        @Override
        public ProgramInfo getCurrentProgram() {
            return ProgramInfo.NULL_PROGRAM_INFO;
        }

        @Override
        public List<ProgramInfo> getPrograms() {
            return asList(getCurrentProgram());
        }

        @Override
        public ProgramInfo runProgram(String id) {
            logger.warn("Dummy Dragon can't run programs");
            return getCurrentProgram();
        }
    }
}
