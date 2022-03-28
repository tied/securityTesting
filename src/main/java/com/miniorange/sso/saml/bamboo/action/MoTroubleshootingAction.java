package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoTroubleshootingAction extends BambooActionSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoAddIDPConfigurationAction.class);


    private UserManager userManager;
    private LoginUriProvider loginUriProvider;
    private MoSAMLSettings settings;

    private String xsrfToken;


    private MoPluginHandler pluginHandler;
    private MoIDPConfig idpConfig;
    private String testConfigResults;
    private String displaySamlResponse;
    private String displaySamlRequest;
    private Map<String, List<String>> attributesMap;
    private Boolean testConfigPerformed = Boolean.FALSE;
    private String idpID;



    public MoTroubleshootingAction(UserManager userManager, LoginUriProvider loginUriProvider,
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

                if (StringUtils.isNotBlank(request.getParameter("idpid"))) {
                    this.idpID = request.getParameter("idpid");
                }

                this.testConfigPerformed = settings.checkIfTestConfig(this.idpID);
                LOGGER.debug("Test Configured :"+testConfigPerformed);
                initializeTroubleshooting();
                return "success";
            } catch (MoPluginException e) {
                LOGGER.error("An error occurred while saving your details." + e);
                addActionError(e.getMessage());
                return "error";
            }catch (Exception e) {
                LOGGER.error("An error occurred while saving your details." + e);
                addActionError("An error occured while saving your details. Please check logs for more info.");
                return "error";
            }


        }else {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return null;
        }
    }

    private void initializeTroubleshooting() {
        idpConfig = pluginHandler.constructIdpConfigObject(this.idpID);
        this.testConfigPerformed = settings.checkIfTestConfig(this.idpID);
        try {
            if (this.testConfigPerformed && idpConfig != null) {
                this.attributesMap = MoSAMLUtils.toMap((JSONObject) settings.getIdpConfig(this.idpID).get("testConfig"));
                this.displaySamlResponse = (String)settings.getIdpConfig(this.idpID).get("samlResponse");
                this.displaySamlRequest = (String)settings.getIdpConfig(this.idpID).get("samlRequest");
            }else{
                this.attributesMap = new HashMap<>();
                this.displaySamlResponse = "";
                this.displaySamlRequest = "";
            }
        } catch (Exception e) {
            LOGGER.debug("error getting the test config details from settings.getIdpConfig(this.idpID)");
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

    public MoPluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public void setPluginHandler(MoPluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;
    }

    public MoIDPConfig getIdpConfig() {
        return idpConfig;
    }

    public void setIdpConfig(MoIDPConfig idpConfig) {
        this.idpConfig = idpConfig;
    }

    public String getTestConfigResults() {
        return testConfigResults;
    }

    public void setTestConfigResults(String testConfigResults) {
        this.testConfigResults = testConfigResults;
    }

    public String getDisplaySamlResponse() {
        return displaySamlResponse;
    }

    public void setDisplaySamlResponse(String displaySamlResponse) {
        this.displaySamlResponse = displaySamlResponse;
    }

    public String getDisplaySamlRequest() {
        return displaySamlRequest;
    }

    public void setDisplaySamlRequest(String displaySamlRequest) {
        this.displaySamlRequest = displaySamlRequest;
    }

    public Map<String, List<String>> getAttributesMap() {
        return attributesMap;
    }

    public void setAttributesMap(Map<String, List<String>> attributesMap) {
        this.attributesMap = attributesMap;
    }

    public Boolean getTestConfigPerformed() {
        return testConfigPerformed;
    }

    public void setTestConfigPerformed(Boolean testConfigPerformed) {
        this.testConfigPerformed = testConfigPerformed;
    }

    public String getIdpID() {
        return idpID;
    }

    public void setIdpID(String idpID) {
        this.idpID = idpID;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    public String getLogSettingsUrl(){
        return settings.getSpBaseUrl() + "/admin/configLog4j.action";
    }

    public String getTroubleshootingUrl(){
        return settings.getSpBaseUrl() + "/plugins/servlet/troubleshooting/view/";
    }
}
