package com.chromosundrift.bhima.dragonmind.web;

import com.chromosundrift.bhima.api.Dragon;
import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.api.Settings;
import com.chromosundrift.bhima.api.SystemInfo;
import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.Wiring;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Dropwizard web application base class for Dragonmind Web.
 */
public class DragonmindApplication extends Application<DragonWebConfig> {

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
    public void initialize(Bootstrap<DragonWebConfig> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
        bootstrap.addBundle(new AssetsBundle("/static/", "/", "index.html"));
    }

    @Override
    public void run(DragonWebConfig configuration,
                    Environment environment) {

        // TODO replace these with real ones:
        final TemplateHealthCheck healthCheck =
                new TemplateHealthCheck(configuration.getTemplate());
        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(new BhimaResource(dragon));

        // Enable CORS headers
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

    }

    public void stop() {
        logger.info("stopping DragonmindApplication");
    }


    public static void main(String[] args) throws Exception {
        logger.info("starting DragonmindApplication in standalone mode with dummy dragon");
        new DragonmindApplication(new DummyDragon()).run(args);
    }


    private static class DummyDragon implements Dragon {
        private SystemInfo systeminfo = new SystemInfo(0L, ProgramInfo.getNullProgramInfo());
        private Settings settings = new Settings(1d, true, false);

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
            return ProgramInfo.getNullProgramInfo();
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

        @Override
        public Map<String, Set<Integer>> getEffectiveWiring() {
            return null;
        }

        @Override
        public Settings getSettings() {
            return settings;
        }

        @Override
        public Settings setSettings(Settings settings) {
            this.settings = settings;
            return this.settings;
        }

        @Override
        public SystemInfo getSystemInfo() {
            return systeminfo;
        }
    }
}
