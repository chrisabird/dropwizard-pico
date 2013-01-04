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

public class PicoBundle<T extends Configuration> implements ConfiguredBundle<T> {
    private PicoConfiguration picoConfiguration;

    public PicoBundle(PicoConfiguration picoConfiguration) {

        this.picoConfiguration = picoConfiguration;
    }

    public void run(T configuration, Environment environment) throws Exception {
        Set<Class<?>>  resources = new HashSet<Class<?>>();
        picoConfiguration.registerResources(resources, configuration);

        MutablePicoContainer applicationScope = new DefaultPicoContainer(new Caching());
        applicationScope.addComponent(configuration);
        picoConfiguration.registerApplicationScope(applicationScope, configuration);

        MutablePicoContainer resourceScope = new DefaultPicoContainer(applicationScope);
        picoConfiguration.registerResourceScope(resourceScope, configuration);

        for(Class<?> c : resources){
            resourceScope.addComponent(c);
            environment.addResource(c);
        }

        ResourceConfig jerseyResourceConfig = environment.getJerseyResourceConfig();
        jerseyResourceConfig.getSingletons().add(new PicoComponentProviderFactory(resourceScope, resources));
        environment.setJerseyServletContainer(new ServletContainer(jerseyResourceConfig));
    }


    public void initialize(Bootstrap<?> bootstrap) {
    }
}
