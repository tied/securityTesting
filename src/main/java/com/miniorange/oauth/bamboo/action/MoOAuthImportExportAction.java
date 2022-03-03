package com.miniorange.oauth.bamboo.action;

import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.oauth.bamboo.MoOAuthPluginHandler;
import com.miniorange.oauth.bamboo.MoOAuthSettings;


    public class MoOAuthImportExportAction extends BambooActionSupport {

    private static final Log LOGGER = LogFactory.getLog(MoOAuthImportExportAction.class);

    private UserManager userManager;
    private LoginUriProvider loginUriProvider;
    private MoOAuthSettings settings;

    public Boolean isApplicationConfigured;
    private Boolean importFileSubmitted;
    private String xsrfToken;

    public MoOAuthImportExportAction(UserManager userManager, LoginUriProvider loginUriProvider, MoOAuthSettings settings
                               
                                ) {
        super();
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.settings = settings;

        if(settings.getClientID() == ""){
            isApplicationConfigured = false;
        } else{
            isApplicationConfigured = true;
        }
    }


    public Boolean doValidate() {
        LOGGER.info("MoOAuthImportExportAction: doValidate() called");
        return true;
    }

    @Override
    public String execute() throws Exception {
        LOGGER.info("MoOAuthImportExportAction execute called");

        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        xsrfToken = XsrfTokenUtils.getXsrfToken(request);

        final UserProfile user = userManager.getRemoteUser();

        if (user != null && userManager.isAdmin(user.getUserKey())) {
            try {
                String message = StringUtils.EMPTY;
                if (!settings.isLicenseValid()) {
                    LOGGER.error("Invalid License");
                    return "upm";
                }

                if (MoOAuthSettings.isPluginConfigurationFileUploaded) {
                    MoOAuthSettings.isPluginConfigurationFileUploaded = Boolean.FALSE;
                    if (StringUtils.isNotBlank(settings.getConfigurationStatus())) {
                        if (StringUtils.equalsIgnoreCase(settings.getConfigurationStatus(), "success")) {
                            addActionMessage(getText("oauth.success.config"));
                        } else {
                            addActionError(settings.getConfigurationStatus());
                        }
                    }
                }
                return "success";

            } catch (Exception e) {
                e.printStackTrace();
                return "input";
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

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public LoginUriProvider getLoginUriProvider() {
        return loginUriProvider;
    }

    public void setLoginUriProvider(LoginUriProvider loginUriProvider) {
        this.loginUriProvider = loginUriProvider;
    }

    public MoOAuthSettings getSettings() {
        return settings;
    }

    public void setSettings(MoOAuthSettings settings) {
        this.settings = settings;
    }

    public Boolean getImportFileSubmitted() {
        return importFileSubmitted;
    }

    public void setImportFileSubmitted(Boolean importFileSubmitted) {
        this.importFileSubmitted = importFileSubmitted;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }
}
