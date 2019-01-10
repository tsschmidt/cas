package org.apereo.cas.configuration.model.support.services.git;

import org.apereo.cas.configuration.model.support.services.json.JsonServiceRegistryProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;

/**
 * Properties used to configure a GitServiceRegistry.
 *
 * @author Travis Schmidt
 * @since 6.0.1
 */
@RequiresModule(name = "cas-server-support-git-service-registry")
@Getter
@Setter
public class GitServiceRegistryProperties extends JsonServiceRegistryProperties {

    private static final long serialVersionUID = 3165325670422728583L;

    /**
     * Location of remote git repository wether it be on accessible filesystem or in the cloud.
     */
    private final URI uri = null;

    /**
     * User name used to login into remote repository.
     */
    private final String username = null;

    /**
     * Password used to login into remote repository.
     */
    private final String password = null;

}
