package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;

/**
 * Multifactor Bypass Provider based on Authentication.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationMultifactorAuthenticationProviderBypass implements MultifactorAuthenticationProviderBypass {

    private final MultifactorAuthenticationProviderBypassProperties bypassProperties;

    @Override
    public boolean shouldExecute(final Authentication authentication,
                                 final RegisteredService registeredService,
                                 final MultifactorAuthenticationProvider provider,
                                 final HttpServletRequest request) {
        val principal = authentication.getPrincipal();
        val bypassByAuthn = locateMatchingAttributeBasedOnAuthenticationAttributes(bypassProperties, authentication);
        if (bypassByAuthn) {
            LOGGER.debug("Bypass rules for authentication for principal [{}] indicate the request may be ignored", principal.getId());
            setBypass(authentication, new DefaultMultifactorAuthenticatonBypassResult(provider.getId(), "AUTHENTICATION_PRINCIPAL"));
            return false;
        }

        val bypassByAuthnMethod = MultifactorAuthenticationUtils.locateMatchingAttributeValue(
                AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE,
                bypassProperties.getAuthenticationMethodName(),
                authentication.getAttributes(), false
        );
        if (bypassByAuthnMethod) {
            LOGGER.debug("Bypass rules for authentication method [{}] indicate the request may be ignored", bypassProperties.getAuthenticationMethodName());
            setBypass(authentication, new DefaultMultifactorAuthenticatonBypassResult(provider.getId(), "AUTHENTICATION_METHOD"));
            return false;
        }

        val bypassByHandlerName = MultifactorAuthenticationUtils.locateMatchingAttributeValue(
                AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS,
                bypassProperties.getAuthenticationHandlerName(),
                authentication.getAttributes(), false
        );
        if (bypassByHandlerName) {
            LOGGER.debug("Bypass rules for authentication handlers [{}] indicate the request may be ignored", bypassProperties.getAuthenticationHandlerName());
            setBypass(authentication, new DefaultMultifactorAuthenticatonBypassResult(provider.getId(), "AUTHENTICATION_HANDLER"));
            return false;
        }

        return true;
    }

    /**
     * Skip bypass and support event based on authentication attributes.
     *
     * @param bypass the bypass settings for the provider.
     * @param authn  the authn
     * @return the boolean
     */
    protected boolean locateMatchingAttributeBasedOnAuthenticationAttributes(
            final MultifactorAuthenticationProviderBypassProperties bypass, final Authentication authn) {
        return MultifactorAuthenticationUtils.locateMatchingAttributeValue(bypass.getAuthenticationAttributeName(),
                bypass.getAuthenticationAttributeValue(), authn.getAttributes(), false);
    }
}
