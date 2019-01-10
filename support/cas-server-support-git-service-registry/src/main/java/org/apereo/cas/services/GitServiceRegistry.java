package org.apereo.cas.services;

import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Collection;

/**
 * Resources based service registry that is populated and updated through a remote Git repository.
 *
 * @author Travis Schmidt
 * @since 6.0.1
 */
@Slf4j
public class GitServiceRegistry implements ResourceBasedServiceRegistry {

    private final CredentialsProvider credentialsProvider;
    private JsonServiceRegistry registry;
    private Git repository;

    public GitServiceRegistry(final URI uri, final Resource configDirectory,
                              final ApplicationEventPublisher eventPublisher,
                              final RegisteredServiceReplicationStrategy replicationStrategy,
                              final RegisteredServiceResourceNamingStrategy namingStrategy,
                              final CredentialsProvider credentialsProvider) throws Exception {
        this.credentialsProvider = credentialsProvider;
        initRepository(uri, configDirectory.getFile());
        registry = new JsonServiceRegistry(configDirectory, false, eventPublisher, replicationStrategy, namingStrategy);
    }

    /**
     * Clones a git repo to load Json Service Registry.
     *
     * @param uri - the url
     * @param configDirectory - the path
     */
    @SneakyThrows
    private void initRepository(final URI uri, final File configDirectory) {
        if (!Files.exists(configDirectory.toPath())) {
            LOGGER.debug("Cloning remote repository [{}] to path [{}]", uri, configDirectory);
            repository = Git.cloneRepository()
                    .setURI(uri.toString())
                    .setDirectory(configDirectory)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
        } else {
            val builder = new FileRepositoryBuilder()
                    .setGitDir(configDirectory)
                    .setMustExist(true)
                    .findGitDir()
                    .readEnvironment();
            repository = new Git(builder.build());
            pull();
        }
    }

    /**
     * Scheduled method that will pull any changes from the remote database every 60 seconds and load changes if present.
     */
    @Scheduled(initialDelayString = "PT60S", fixedDelayString = "PT60S")
    @SneakyThrows
    public void pull() {
        val pull = this.repository.pull().setCredentialsProvider(credentialsProvider).call();
        if (pull.isSuccessful() && pull.getMergeResult().getMergedCommits().length > 0) {
            load();
        }
    }

    @Override
    public void update(final RegisteredService service) {
        registry.update(service);
    }

    @Override
    public Collection<RegisteredService> load() {
        return registry.load();
    }

    @Override
    public Collection<RegisteredService> load(final File file) {
        return registry.load(file);
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return registry.save(registeredService);
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        return registry.delete(registeredService);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return registry.findServiceById(id);
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        return registry.findServiceById(id);
    }

    @Override
    public String getName() {
        return registry.getName();
    }
}
