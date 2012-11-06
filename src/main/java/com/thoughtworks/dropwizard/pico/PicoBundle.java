package com.thoughtworks.dropwizard.pico;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;

import java.util.HashSet;
import java.util.Set;

public class PicoBundle implements ConfiguredBundle<Configuration> {
    private PicoConfiguration picoConfiguration;

    public PicoBundle(PicoConfiguration picoConfiguration) {

        this.picoConfiguration = picoConfiguration;
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        Set<Class<?>>  resources = new HashSet<Class<?>>();
        picoConfiguration.registerResources(resources);

        MutablePicoContainer applicationScope = new DefaultPicoContainer(new Caching());
        applicationScope.addComponent(configuration);
        picoConfiguration.registerApplicationScope(applicationScope);

        MutablePicoContainer resourceScope = new DefaultPicoContainer(applicationScope);
        picoConfiguration.registerResourceScope(resourceScope);


        for(Class<?> c : resources){
            environment.addResource(c);
        }

        ResourceConfig jerseyResourceConfig = environment.getJerseyResourceConfig();
        jerseyResourceConfig.getSingletons().add(new PicoComponentProviderFactory(resourceScope, resources));
        environment.setJerseyServletContainer(new ServletContainer(jerseyResourceConfig));
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }
}
