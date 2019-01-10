package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.GitServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;

import lombok.SneakyThrows;
import lombok.val;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration class for setting up a {@link GitServiceRegistry}.
 *
 * @author Travis Schmidt
 * @since 6.0.1
 */
@Configuration("gitServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 1)
@ConditionalOnProperty(prefix = "cas.serviceRegistry.git", name = "location")
public class GitServiceRegistryConfiguration {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("registeredServiceReplicationStrategy")
    private ObjectProvider<RegisteredServiceReplicationStrategy> registeredServiceReplicationStrategy;

    @Autowired
    @Qualifier("registeredServiceResourceNamingStrategy")
    private ObjectProvider<RegisteredServiceResourceNamingStrategy> resourceNamingStrategy;

    @Bean
    @SneakyThrows
    public ServiceRegistry gitServiceRegistry() {
        val registry = casProperties.getServiceRegistry();
        return new GitServiceRegistry(registry.getGit().getUri(), registry.getGit().getLocation(), eventPublisher,
                registeredServiceReplicationStrategy.getIfAvailable(), resourceNamingStrategy.getIfAvailable(),
                credentialsProvider());
    }

    @Bean
    @ConditionalOnMissingBean(name = "gitServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer gitServiceRegistryExecutionPlanConfigurer() {
        return new ServiceRegistryExecutionPlanConfigurer() {
            @Override
            public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
                plan.registerServiceRegistry(gitServiceRegistry());
            }
        };
    }

    @Bean
    public CredentialsProvider credentialsProvider() {
        return new UsernamePasswordCredentialsProvider(casProperties.getServiceRegistry().getGit().getUsername(),
                casProperties.getServiceRegistry().getGit().getPassword());
    }
}
