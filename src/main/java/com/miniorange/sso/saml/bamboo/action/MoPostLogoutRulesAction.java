package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

public class MoPostLogoutRulesAction extends BambooActionSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoPostLogoutRulesAction.class);

    private Boolean postLogoutSettingsSubmitted;
    private String customLogoutURL;
    private Boolean enableLogoutTemplate;
    private String  logoutTemplate;
    private String xsrfToken;

    private PluginLicenseManager pluginLicenseManager;
    private MoSAMLSettings settings;
    private MoPluginHandler moPluginHandler;
    private UserManager userManager;
    private LoginUriProvider loginUriProvider;

    public MoPostLogoutRulesAction(MoSAMLSettings settings, MoPluginHandler moPluginHandler, UserManager userManager,
                                   LoginUriProvider loginUriProvider) {
        this.settings = settings;
        this.moPluginHandler = moPluginHandler;
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
    }

    public void validate() {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        logoutTemplate = StringUtils.equals(logoutTemplate,null) ? settings.getLogoutTemplate() : logoutTemplate;

        if (!BooleanUtils.toBoolean(postLogoutSettingsSubmitted)) {
            return;
        }

        if (super.hasActionErrors()) {
            xsrfToken = XsrfTokenUtils.getXsrfToken(request);
            initializeSAMLConfig();
        }
    }

    @Override
    public String execute() throws Exception {
        try {
            LOGGER.info("MoPostLogoutRulesAction doExecute Submitted: ");
            HttpServletRequest request = ServletActionContext.getRequest();
            HttpServletResponse response = ServletActionContext.getResponse();
            xsrfToken = XsrfTokenUtils.getXsrfToken(request);


            final UserProfile user = userManager.getRemoteUser();
            if (user != null && userManager.isAdmin(user.getUserKey())) {
                if (!settings.isValidLicense()) {
                    LOGGER.error("No valid license found");
                    return "upm";
                }

                if (BooleanUtils.toBoolean(this.postLogoutSettingsSubmitted)) {
                    validate();
                    moPluginHandler.savePostLogoutSettings(customLogoutURL, enableLogoutTemplate, logoutTemplate);
                    addActionMessage(getText("samlsso.success.config"));
                    LOGGER.debug("settings saved successfully");

                }
                initializeSAMLConfig();
                return "success";
            }else {
                response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
                return null;
            }
        } catch (MoPluginException e) {
            addActionError(e.getMessage());
            return "input";
        } catch (Exception e) {
            LOGGER.error("An error occurred while saving your details.",e);
            addActionError(e.getMessage());
            return "input";
        }
    }

    private void initializeSAMLConfig() {
        this.customLogoutURL=settings.getCustomLogoutURL();
        this.enableLogoutTemplate=settings.getEnableLogoutTemplate();
        this.logoutTemplate=settings.getLogoutTemplate();


    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    public Boolean getPostLogoutSettingsSubmitted() {
        return postLogoutSettingsSubmitted;
    }

    public void setPostLogoutSettingsSubmitted(Boolean postLogoutSettingsSubmitted) {
        this.postLogoutSettingsSubmitted = postLogoutSettingsSubmitted;
    }

    public String getCustomLogoutURL() {
        return customLogoutURL;
    }

    public void setCustomLogoutURL(String customLogoutURL) {
        this.customLogoutURL = customLogoutURL;
    }

    public Boolean getEnableLogoutTemplate() {
        return enableLogoutTemplate;
    }

    public void setEnableLogoutTemplate(Boolean enableLogoutTemplate) {
        this.enableLogoutTemplate = enableLogoutTemplate;
    }

    public String getLogoutTemplate() {
        return logoutTemplate;
    }

    public void setLogoutTemplate(String logoutTemplate) {
        this.logoutTemplate = logoutTemplate;
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public MoPluginHandler getMoPluginHandler() {
        return moPluginHandler;
    }

    public void setMoPluginHandler(MoPluginHandler moPluginHandler) {
        this.moPluginHandler = moPluginHandler;
    }

    public PluginLicenseManager getPluginLicenseManager() {
        return pluginLicenseManager;
    }

    public void setPluginLicenseManager(PluginLicenseManager pluginLicenseManager) {
        this.pluginLicenseManager = pluginLicenseManager;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

}
