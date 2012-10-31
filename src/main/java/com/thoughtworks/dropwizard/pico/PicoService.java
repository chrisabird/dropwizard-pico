package com.thoughtworks.dropwizard.pico;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.yammer.dropwizard.bundles.AssetsBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;

import java.util.HashSet;
import java.util.Set;

public abstract class PicoService<T extends com.yammer.dropwizard.config.Configuration>  extends com.yammer.dropwizard.Service<T> {
    private MutablePicoContainer applicationScope;
    private MutablePicoContainer resourceScope;
    private Set<Class<?>> resources;

    public PicoService(String name, PicoConfiguration picoConfiguration) {
        super(name);
        populateContainers(picoConfiguration);
        addBundle(new AssetsBundle());
    }

    private void populateContainers(PicoConfiguration picoConfiguration) {
        resources = new HashSet<Class<?>>();
        picoConfiguration.registerResources(resources);

        applicationScope = new DefaultPicoContainer(new Caching());
        picoConfiguration.registerApplicationScope(applicationScope);

        resourceScope = new DefaultPicoContainer(applicationScope);
        picoConfiguration.registerResourceScope(resourceScope);
    }

    protected void initialize(T appConfiguration, Environment environment) throws Exception {
        for(Class<?> c : resources){
            environment.addResource(c);
        }
    }

    public ServletContainer getJerseyContainer(DropwizardResourceConfig resourceConfig, T serviceConfig) {
        resourceConfig.getSingletons().add(new PicoComponentProviderFactory(resourceScope, resources));
        return new ServletContainer(resourceConfig);
    }
}
