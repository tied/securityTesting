package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;

import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

public class MoCustomTemplateAction extends BambooActionSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoCustomCertificatesAction.class);

    private String xsrfToken;
    private String errorMsgTemplate;
    private String logoutTemplate;
    private Boolean enableErrorMsgTemplate;
    private Boolean customtemplatesSubmitted;

    private UserManager userManager;
    private MoSAMLSettings settings;
    private LoginUriProvider loginUriProvider;

    public MoCustomTemplateAction(UserManager userManager, MoSAMLSettings settings, LoginUriProvider loginUriProvider) {
        this.userManager = userManager;
        this.settings = settings;
        this.loginUriProvider = loginUriProvider;
    }

    @Override
    public String execute() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        xsrfToken = XsrfTokenUtils.getXsrfToken(request);

        final UserProfile user = userManager.getRemoteUser();

        if (user != null && userManager.isAdmin(user.getUserKey())) {
            if (BooleanUtils.toBoolean(customtemplatesSubmitted)) {
                MoPluginHandler.saveCustomTemplate(errorMsgTemplate, logoutTemplate, enableErrorMsgTemplate);
                addActionMessage(getText("samlsso.success.config"));
            }
            initializeSAMLConfig();
            return "success";
        }
        else {
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

    private void initializeSAMLConfig() {
        this.errorMsgTemplate = settings.getErrorMsgTemplate();
        this.logoutTemplate = settings.getLogoutTemplate();
        this.enableErrorMsgTemplate = settings.getEnableErrorMsgTemplate();
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    public String getErrorMsgTemplate() {
        return errorMsgTemplate;
    }

    public void setErrorMsgTemplate(String errorMsgTemplate) {
        this.errorMsgTemplate = errorMsgTemplate;
    }

    public String getLogoutTemplate() {
        return logoutTemplate;
    }

    public void setLogoutTemplate(String logoutTemplate) {
        this.logoutTemplate = logoutTemplate;
    }

    public Boolean getEnableErrorMsgTemplate() {
        return enableErrorMsgTemplate;
    }

    public void setEnableErrorMsgTemplate(Boolean enableErrorMsgTemplate) {
        this.enableErrorMsgTemplate = enableErrorMsgTemplate;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public LoginUriProvider getLoginUriProvider() {
        return loginUriProvider;
    }

    public void setLoginUriProvider(LoginUriProvider loginUriProvider) {
        this.loginUriProvider = loginUriProvider;
    }

    public Boolean getCustomtemplatesSubmitted() {
        return customtemplatesSubmitted;
    }

    public void setCustomtemplatesSubmitted(Boolean customtemplatesSubmitted) {
        this.customtemplatesSubmitted = customtemplatesSubmitted;
    }
}
