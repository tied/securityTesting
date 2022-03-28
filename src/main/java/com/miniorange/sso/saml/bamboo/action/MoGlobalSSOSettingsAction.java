package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
//import com.opensymphony.webwork.ServletActionContext;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class MoGlobalSSOSettingsAction extends BambooActionSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoGlobalSSOSettingsAction.class);


    private UserManager userManager;
    private LoginUriProvider loginUriProvider;
    private MoSAMLSettings settings;

    private String xsrfToken;
    private Boolean enableSAMLSSO;
    private Boolean enablePasswordChange;
    private Boolean autoActivateUser;
    private Boolean globalSettingsSubmitted;
    private Boolean pluginApiAccessRestriction;
    private String resetAssertionIDListInterval;
    private Boolean restrictDuplicateAssertion;
    private int customResetInterval;
    private MoPluginHandler pluginHandler;


    public MoGlobalSSOSettingsAction(UserManager userManager, LoginUriProvider loginUriProvider,
                             MoSAMLSettings settings) {
        super();
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.settings = settings;
    }


    public void doValidate() {
        LOGGER.info("Look n Feel Action doValidate called");

    }

    @Override
    public String execute() throws Exception {

        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        xsrfToken = XsrfTokenUtils.getXsrfToken(request);

        final UserProfile user = userManager.getRemoteUser();

        if (user != null && userManager.isAdmin(user.getUserKey())) {

            try {
                if (!settings.isValidLicense()) {
                    LOGGER.error("No valid license found");
                    return "upm";
                }
                if (BooleanUtils.toBoolean(globalSettingsSubmitted)) {
                    pluginHandler.saveGlobalSSOSettings( enableSAMLSSO, enablePasswordChange, autoActivateUser,
                            pluginApiAccessRestriction,restrictDuplicateAssertion,resetAssertionIDListInterval,customResetInterval);
                    addActionMessage(getText("samlsso.success.config"));
                }
                initializeGlobalSSOSettings();
                return "success";
            } catch (MoPluginException e) {
                LOGGER.error(e.getMessage());
                addActionError(e.getMessage());
                return "input";
            }catch (Exception e) {
                LOGGER.error(e.getMessage());
                addActionError("An error occurred while saving your details. Please check logs for more info.");
                return "input";
            }
        }else {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return null;
        }
    }

    private void initializeGlobalSSOSettings() {
        this.enableSAMLSSO = settings.getEnableSAMLSSO();
        this.autoActivateUser = settings.getAutoActivateUser();
        this.enablePasswordChange = settings.getEnablePasswordChange();
        this.pluginApiAccessRestriction= settings.getPluginApiAccessRestriction();
        this.resetAssertionIDListInterval= settings.getResetAssertionIDListInterval();
        this.customResetInterval =settings.getCustomResetInterval();
        this.restrictDuplicateAssertion= settings.getRestrictDuplicateAssertion();
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    public HashMap<String, String> getIdpMap() {
        return settings.getIdpMap();
    }

    public ArrayList<String> getIdpList() {
        return settings.getIdPList();
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

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    public Boolean getEnableSAMLSSO() {
        return enableSAMLSSO;
    }

    public void setEnableSAMLSSO(Boolean enableSAMLSSO) {
        this.enableSAMLSSO = enableSAMLSSO;
    }

    public Boolean getEnablePasswordChange() {
        return enablePasswordChange;
    }

    public void setEnablePasswordChange(Boolean enablePasswordChange) {
        this.enablePasswordChange = enablePasswordChange;
    }

    public Boolean getGlobalSettingsSubmitted() {
        return globalSettingsSubmitted;
    }

    public void setGlobalSettingsSubmitted(Boolean globalSettingsSubmitted) {
        this.globalSettingsSubmitted = globalSettingsSubmitted;
    }

    public Boolean getPluginApiAccessRestriction() {
        return pluginApiAccessRestriction;
    }

    public void setPluginApiAccessRestriction(Boolean pluginApiAccessRestriction) {
        this.pluginApiAccessRestriction = pluginApiAccessRestriction;
    }

    public MoPluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public void setPluginHandler(MoPluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;
    }
}
