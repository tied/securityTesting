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
import org.apache.struts2.ServletActionContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class MoLooknFeelAction extends BambooActionSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoAddIDPConfigurationAction.class);


    private UserManager userManager;
    private LoginUriProvider loginUriProvider;
    private MoSAMLSettings settings;


    private String loginButtonText;
    private Boolean looknfeelSettingsSubmitted;
    private String status;
    private Boolean enableLoginTemplate;
    private String loginTemplate;
    private String errorMsgTemplate;
    private Boolean enableErrorMsgTemplate;
    private Boolean showLoginButtons;
    private String xsrfToken;


    public MoLooknFeelAction(UserManager userManager, LoginUriProvider loginUriProvider,
                             MoSAMLSettings settings) {
        super();
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.settings = settings;
    }


    public void doValidate() {

        loginTemplate = StringUtils.equals(loginTemplate,null) ? settings.getLoginTemplate() : loginTemplate;
        errorMsgTemplate = StringUtils.equals(errorMsgTemplate,null) ? settings.getErrorMsgTemplate() : errorMsgTemplate;
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

                if(BooleanUtils.toBoolean(this.looknfeelSettingsSubmitted)){
                    doValidate();
                    MoPluginHandler.saveLookAndFeelSettings( StringUtils.trim(this.loginButtonText),
                            this.errorMsgTemplate,  this.enableErrorMsgTemplate
                            ,this.enableLoginTemplate,this.loginTemplate,this.showLoginButtons);
                    addActionMessage(getText("samlsso.success.config"));
                    LOGGER.debug("settings saved successfully");
                }

                initializeSAMLConfig();
                return "success";
            } catch (MoPluginException e) {
                addActionError(e.getMessage());
                return "input";
            }catch(Exception e){
                LOGGER.error("An error occurred while saving your details.",e);
                addActionError("An error occurred while saving your details. Please check logs for more info.");
                return "input";
            }
        }else {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return null;
        }
    }

    private void initializeSAMLConfig() {
        this.loginButtonText = settings.getLoginButtonText();
        this.enableLoginTemplate = settings.getEnableLoginTemplate();
        this.loginTemplate = settings.getLoginTemplate();
        this.enableLoginTemplate = settings.getEnableLoginTemplate();
        this.errorMsgTemplate = settings.getErrorMsgTemplate();
        this.enableErrorMsgTemplate = settings.getEnableErrorMsgTemplate();
        this.showLoginButtons = settings.getShowLoginButtons();
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

    public String getLoginButtonText() {
        return loginButtonText;
    }

    public void setLoginButtonText(String loginButtonText) {
        this.loginButtonText = loginButtonText;
    }

    public Boolean getLooknfeelSettingsSubmitted() {
        return looknfeelSettingsSubmitted;
    }

    public void setLooknfeelSettingsSubmitted(Boolean looknfeelSettingsSubmitted) {
        this.looknfeelSettingsSubmitted = looknfeelSettingsSubmitted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getEnableLoginTemplate() {
        return enableLoginTemplate;
    }

    public void setEnableLoginTemplate(Boolean enableLoginTemplate) {
        this.enableLoginTemplate = enableLoginTemplate;
    }

    public String getLoginTemplate() {
        return loginTemplate;
    }

    public void setLoginTemplate(String loginTemplate) {
        this.loginTemplate = loginTemplate;
    }

    public String getErrorMsgTemplate() {
        return errorMsgTemplate;
    }

    public void setErrorMsgTemplate(String errorMsgTemplate) {
        this.errorMsgTemplate = errorMsgTemplate;
    }

    public Boolean getEnableErrorMsgTemplate() {
        return enableErrorMsgTemplate;
    }

    public void setEnableErrorMsgTemplate(Boolean enableErrorMsgTemplate) {
        this.enableErrorMsgTemplate = enableErrorMsgTemplate;
    }

    public Boolean getShowLoginButtons() {
        return showLoginButtons;
    }

    public void setShowLoginButtons(Boolean showLoginButtons) {
        this.showLoginButtons = showLoginButtons;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }
}
