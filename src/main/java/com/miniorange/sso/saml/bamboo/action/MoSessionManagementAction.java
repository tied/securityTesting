package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.plugin.PluginException;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

public class MoSessionManagementAction extends BambooActionSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoSupportedIdpsAction.class);

    private MoPluginHandler moPluginHandler;
    private MoSAMLSettings settings;
    private UserManager userManager;
    private LoginUriProvider loginUriProvider;
    private String xsrfToken;
    private Boolean rememberMeSubmitted;
    private Boolean enableRememberMeCookie;

    public MoSessionManagementAction(MoPluginHandler moPluginHandler, MoSAMLSettings settings, UserManager userManager,
                                     LoginUriProvider loginUriProvider) {
        this.moPluginHandler = moPluginHandler;
        this.settings = settings;
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
    }

    public void validate() {
        LOGGER.info("Session Management Action doValidate called");

    }

    public String execute() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        xsrfToken = XsrfTokenUtils.getXsrfToken(request);

        final UserProfile user = userManager.getRemoteUser();

        if (user != null && userManager.isAdmin(user.getUserKey())) {
            try {
                LOGGER.debug("Session Management doExecute called");
                if (!settings.isValidLicense()) {
                    LOGGER.error("No valid license found.");
                    return "upm";
                }

                if (BooleanUtils.toBoolean(this.rememberMeSubmitted)) {
                    settings.setRememberMeCookieEnabled(BooleanUtils.toBoolean(this.enableRememberMeCookie));
                    addActionMessage(getText("samlsso.success.config"));
                }
                initialiseConfig();
                return "success";
            } catch (PluginException e) {
                LOGGER.error("An plugin exception occurred while initializing service provider configuration", e);
                addActionError(e.getMessage());
                return "input";
            } catch (Exception e) {
                LOGGER.error("An error occurred while initializing service provider configuration", e);
                addActionError("An error occured while saving your details. Please check logs for more info.");
                return "input";
            }
        }else {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return null;
        }
    }

    public void initialiseConfig(){
        this.enableRememberMeCookie = BooleanUtils.toBoolean(settings.getRememberMeCookieEnabled());
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    public MoPluginHandler getMoPluginHandler() {
        return moPluginHandler;
    }

    public void setMoPluginHandler(MoPluginHandler moPluginHandler) {
        this.moPluginHandler = moPluginHandler;
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public Boolean getRememberMeSubmitted() {
        return rememberMeSubmitted;
    }

    public void setRememberMeSubmitted(Boolean rememberMeSubmitted) {
        this.rememberMeSubmitted = rememberMeSubmitted;
    }

    public Boolean getEnableRememberMeCookie() {
        return enableRememberMeCookie;
    }

    public void setEnableRememberMeCookie(Boolean enableRememberMeCookie) {
        this.enableRememberMeCookie = enableRememberMeCookie;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }
}
