package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.MoPluginException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

public class MoHeaderBasedAuthAction extends BambooActionSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoHeaderBasedAuthAction.class);
    private MoSAMLSettings settings;
    private MoPluginHandler pluginHandler;
    private UserManager userManager;
    private LoginUriProvider loginUriProvider;
    private Boolean headerbasedauthSubmitted;
    private Boolean enableHeaderAuthentication;
    private String headerAuthenticationAttribute;
    private I18nResolver i18nResolver;
    private String xsrfToken;

    public MoHeaderBasedAuthAction(MoSAMLSettings settings, MoPluginHandler pluginHandler, UserManager userManager,
                                   LoginUriProvider loginUriProvider) {
        this.settings = settings;
        this.pluginHandler = pluginHandler;
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
    }
    public void validate() {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        LOGGER.info("MoPostLogoutRulesAction  doValidate called");
        boolean error = false;

        if(enableHeaderAuthentication != null) {
            if(StringUtils.isBlank(headerAuthenticationAttribute)) {
                addActionError(getText("samlsso.error.config.headerAuthenticationAttribute.empty"));
                error = true;
            }
        }
        if (!BooleanUtils.toBoolean(headerbasedauthSubmitted)) {
            return;
        }

        if (super.hasActionErrors()) {
            xsrfToken = XsrfTokenUtils.getXsrfToken(request);
            initializeHeaderBasedAuth();
        }
        if (error) {
            initializeHeaderBasedAuth();
        }
    }

    @Override
    public String execute() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        xsrfToken = XsrfTokenUtils.getXsrfToken(request);

        final UserProfile user = userManager.getRemoteUser();

        if (user != null && userManager.isAdmin(user.getUserKey())) {
            try {
                LOGGER.info("MoHeaderBasedAuthAction doExecute Submitted: ");
                if (!settings.isValidLicense()) {
                    LOGGER.error("No valid license found");
                    return "upm";
                }
                if (BooleanUtils.toBoolean(this.headerbasedauthSubmitted)) {
                    pluginHandler.saveHeaderBasedAuthSettings(enableHeaderAuthentication, headerAuthenticationAttribute);
                    addActionMessage(getText("samlsso.headerbasedauth.save.success"));
                }
                initializeHeaderBasedAuth();
                return "success";
            } catch (MoPluginException e) {
                LOGGER.error("An error occurred while saving your details." + e);
                addActionError(e.getMessage());
                return "error";
            } catch (Exception e) {
                LOGGER.error("An error occurred while saving your Header Authentication details." + e);
                addActionError("An error occurred while saving your details. Please check logs for more info.");
                return "error";
            }
        } else {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return null;
        }
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    private void initializeHeaderBasedAuth(){
        this.enableHeaderAuthentication = settings.getHeaderAuthenticationSettings();
        this.headerAuthenticationAttribute = settings.getHeaderAuthenticationAttribute();
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public MoPluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public void setPluginHandler(MoPluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;
    }

    public String getHeaderAuthenticationAttribute() {
        return headerAuthenticationAttribute;
    }

    public void setHeaderAuthenticationAttribute(String headerAuthenticationAttribute) {
        this.headerAuthenticationAttribute = headerAuthenticationAttribute;
    }

    public Boolean getHeaderbasedauthSubmitted() {
        return headerbasedauthSubmitted;
    }

    public void setHeaderbasedauthSubmitted(Boolean headerbasedauthSubmitted) {
        this.headerbasedauthSubmitted = headerbasedauthSubmitted;
    }

    public Boolean getEnableHeaderAuthentication() {
        return enableHeaderAuthentication;
    }

    public void setEnableHeaderAuthentication(Boolean enableHeaderAuthentication) {
        this.enableHeaderAuthentication = enableHeaderAuthentication;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }
}
