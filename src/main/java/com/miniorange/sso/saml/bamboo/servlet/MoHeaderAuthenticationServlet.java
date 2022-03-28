package com.miniorange.sso.saml.bamboo.servlet;

import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.seraph.auth.Authenticator;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLManager;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;

public class MoHeaderAuthenticationServlet extends HttpServlet {
    private static Log LOGGER = LogFactory.getLog(MoHeaderAuthenticationServlet.class);

    private MoSAMLSettings settings;
    private BambooUserManager bambooUserManager;
    private BambooPermissionManager bambooPermissionManager;
    private MoSAMLManager samlManager;
    private MoPluginHandler moPluginHandler;
    private final TemplateRenderer renderer;

    public MoHeaderAuthenticationServlet(MoSAMLSettings settings, BambooPermissionManager bambooPermissionManager,
                                         BambooUserManager bambooUserManager, TemplateRenderer renderer,
                                         MoPluginHandler moPluginHandler, MoSAMLManager samlManager) {
        super();
        this.settings = settings;
        this.bambooPermissionManager = bambooPermissionManager;
        this.bambooUserManager = bambooUserManager;
        this.samlManager = samlManager;
        this.moPluginHandler = moPluginHandler;
        this.renderer = renderer;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.debug("MoHeaderAuthentication: doGet called");
        handleHeaderBasedAuthentication(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.debug("MoHeaderAuthentication: doPost called");
        handleHeaderBasedAuthentication(request, response);
    }

    private void handleHeaderBasedAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Boolean isAuthenticated = Boolean.FALSE;
        String redirectUrl;
        try {
            if(settings.getHeaderAuthenticationSettings()){
                if ((!settings.isEvaluationOrSubscriptionLicense())
                        || (settings.isEvaluationOrSubscriptionLicense() && settings.isValidLicense())){
                    isAuthenticated = validateHeaderAndLogin(request, response);
                } else {
                    LOGGER.debug("Invalid license");
                }
            } else{
                LOGGER.info("Header based authentication is disabled");
            }
        } catch (Exception e) {
            LOGGER.error("An exception occurred in header based authentication. Redirecting to the login page. "+e);
        }
        redirectUrl = settings.getSpBaseUrl();
        if(isAuthenticated && !StringUtils.isBlank(request.getHeader(settings.getHeaderAuthenticationRelayStateAttribute()))){
            redirectUrl = request.getHeader(settings.getHeaderAuthenticationRelayStateAttribute());
        }
        LOGGER.debug("Redirecting user to: " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private Boolean validateHeaderAndLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String username = request.getHeader(settings.getHeaderAuthenticationAttribute());
        Authenticator authenticator = SecurityConfigFactory.getInstance().getAuthenticator();
        if(bambooUserManager.getBambooUser(username) == null) {
            LOGGER.error("User does not exist. Redirecting to the login page");
            return Boolean.FALSE;
        }
        LOGGER.debug("creating session for user: " + username);
        BambooUser bambooUser= bambooUserManager.getBambooUser(username);
        authoriseUserAndEstablishSession((DefaultAuthenticator) authenticator, bambooUser, request, response);
        LOGGER.info("User session established");
        return Boolean.TRUE;
    }

    private void authoriseUserAndEstablishSession(DefaultAuthenticator authenticator, Object userObject,
                                                    HttpServletRequest request, HttpServletResponse response)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Principal principal = (Principal) userObject;
        Method authUserMethod = DefaultAuthenticator.class.getDeclaredMethod("authoriseUserAndEstablishSession",
                HttpServletRequest.class, HttpServletResponse.class, Principal.class);
        authUserMethod.setAccessible(true);
        authUserMethod.invoke(authenticator, request, response, principal);
    }
}
