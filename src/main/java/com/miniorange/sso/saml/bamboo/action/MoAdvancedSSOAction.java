package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

public class MoAdvancedSSOAction extends BambooActionSupport {

    private static Log LOGGER = LogFactory.getLog(MoAdvancedSSOAction.class);

    private UserManager userManager;
    private LoginUriProvider loginUriProvider;

    private String idpID;
    private String timeDelay;
    private MoSAMLSettings settings;
    private Boolean advancedSsoSubmitted;
    private Boolean refreshMetadata;
    private String refreshInterval;
    private Integer customRefreshInterval;
    private String customRefreshIntervalUnit;
    private String inputUrl;
    private String relayState;
    private String relayStateRedirectionType;
    private Boolean allowUserCreation;
    private Boolean enablePassiveSso;
    private Boolean forceAuthentication;
    private Boolean enableButtons;
    private String xsrfToken;

    public MoAdvancedSSOAction(UserManager userManager, LoginUriProvider loginUriProvider, MoSAMLSettings settings){
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.settings=settings;
    }

    public Boolean doValidate() {
        Boolean error = false;
        LOGGER.info("MoAddIDPConfigurationAction : doValidate called");
        if (BooleanUtils.toBoolean(this.advancedSsoSubmitted)) {
            try {
                if (Integer.parseInt(timeDelay) < 0) {
                    addActionError(getText("samlsso.timedelay.number.invalid"));
                    error = true;
                }
            } catch (NumberFormatException e) {
                addActionError( "Please enter positive integer for Validate IDP's SAML Response field.");
                error = true;
            }


            if (StringUtils.isNotBlank(this.relayState)) {
                try {
                    this.relayState = MoSAMLUtils.sanitizeText(this.relayState);
                    new URL(this.relayState);
                } catch (MalformedURLException e) {
                    LOGGER.error(e.getMessage());
                    addActionError("Invalid RelayState URL.");
                    error = true;
                }
            }

            if(BooleanUtils.toBoolean(refreshMetadata)) {
                LOGGER.info("Validating IDP input URL");
                try {
                    new URL(inputUrl);

                } catch (MalformedURLException e){
                    LOGGER.error("Invalid URL",e);
                    addActionError("Invalid IDP metadata URL. Please make sure it's not empty and doesn't contain spaces.");
                    error = true;
                }
            }
        }
        if (BooleanUtils.toBoolean(error)) {
            initializeSAMLConfig();
        }
        return error;
    }

    @Override
    public String execute() throws Exception {


        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        xsrfToken = XsrfTokenUtils.getXsrfToken(request);
        final UserProfile user = userManager.getRemoteUser();

        if (user != null && userManager.isAdmin(user.getUserKey())) {
            try {
                LOGGER.info("MO AdvancedSSO Action doExecute called ");
                if (!settings.isValidLicense()) {
                    LOGGER.error("No valid license found");
                    return "upm";
                }
                if (StringUtils.isBlank(this.idpID)) {
                    if (StringUtils.isNotBlank(request.getParameter("idpid"))) {
                        this.idpID = request.getParameter("idpid");
                    } else {
                        this.idpID = UUID.randomUUID().toString();
                    }
                }
                if (BooleanUtils.toBoolean(this.advancedSsoSubmitted)) {
                    LOGGER.info("Advance SSO submitted");
                    if (StringUtils.isBlank(idpID)) {
                        idpID = MoPluginConstants.DEFAULT_IDP_ID;
                    }
                    Boolean error = doValidate();
                    if (!error) {
                        LOGGER.info("Advance SSO submitted and No errors are there");
                        MoPluginHandler.saveAdvancedOption(this.idpID, allowUserCreation,this.forceAuthentication, this.inputUrl, this.timeDelay, refreshMetadata, refreshInterval,
                                customRefreshInterval, customRefreshIntervalUnit, this.relayState, this.relayStateRedirectionType, enablePassiveSso);
                        addActionMessage(getText("samlsso.success.config"));
                    }
                }
                initializeSAMLConfig();
                return "success";
            } catch (MoPluginException e) {
                LOGGER.error("MoPluginException = ", e);
                addActionError(e.getMessage());
                initializeSAMLConfig();
                return INPUT;
            } catch (Exception e) {
                LOGGER.error("Exception  ", e);
                addActionError("An error occurred while saving your details. Please check logs for more info.");
                initializeSAMLConfig();
                return INPUT;
            }
        }else {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return null;
        }
    }

    private void initializeSAMLConfig() {
        LOGGER.info("initilizeSAMLConfig Called");
        MoIDPConfig idpConfig = MoPluginHandler.constructIdpConfigObject(this.idpID);
        if(idpConfig == null){
            LOGGER.debug("Initiliazling new form");
            initializeNewForm();
            return;
        }
        this.allowUserCreation = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getAllowUserCreation(), true);
        this.forceAuthentication = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getForceAuthentication(), false);
        this.timeDelay = StringUtils.defaultString(idpConfig.getTimeDelay(), "01");
        this.refreshMetadata = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getRefreshMetadata(), false);
        this.inputUrl = StringUtils.defaultIfBlank(idpConfig.getInputUrl(), StringUtils.EMPTY);
        this.refreshInterval = StringUtils.defaultIfBlank(idpConfig.getRefreshInterval(), "hourly");
        this.customRefreshInterval = idpConfig.getCustomRefreshInterval()!=null?idpConfig.getCustomRefreshInterval():60;
        this.customRefreshIntervalUnit = StringUtils.defaultIfBlank(idpConfig.getCustomRefreshIntervalUnit(),
                "minutes");
        this.relayState=StringUtils.defaultIfBlank(idpConfig.getRelayState(),"");
        this.relayStateRedirectionType = StringUtils.defaultIfBlank(idpConfig.getRelayStateRedirectionType(), MoPluginConstants.FORCE_REDIRECT);
        this.enableButtons=true;
        this.enablePassiveSso = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getEnablePassiveSso(),false);

    }

    private void initializeNewForm(){
        LOGGER.info("initializeNewForm Called");

        this.allowUserCreation = true;
        this.enablePassiveSso=Boolean.FALSE;
        this.forceAuthentication=false;
        this.timeDelay = "01";
        this.refreshMetadata = Boolean.FALSE;
        this.inputUrl = StringUtils.EMPTY;
        this.refreshInterval = "hourly";
        this.customRefreshInterval = 60;
        this.customRefreshIntervalUnit = "minutes";
        this.relayState = StringUtils.EMPTY;
        this.relayStateRedirectionType = MoPluginConstants.FORCE_REDIRECT;
        this.enableButtons=false;
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

    public String getIdpID() {
        return idpID;
    }

    public void setIdpID(String idpID) {
        this.idpID = idpID;
    }

    public String getTimeDelay() {
        return timeDelay;
    }

    public void setTimeDelay(String timeDelay) {
        this.timeDelay = timeDelay;
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public Boolean getAdvancedSsoSubmitted() {
        return advancedSsoSubmitted;
    }

    public void setAdvancedSsoSubmitted(Boolean advancedSsoSubmitted) {
        this.advancedSsoSubmitted = advancedSsoSubmitted;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    public Boolean getRefreshMetadata() {
        return refreshMetadata;
    }

    public void setRefreshMetadata(Boolean refreshMetadata) {
        this.refreshMetadata = refreshMetadata;
    }

    public String getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(String refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public Integer getCustomRefreshInterval() {
        return customRefreshInterval;
    }

    public void setCustomRefreshInterval(Integer customRefreshInterval) {
        this.customRefreshInterval = customRefreshInterval;
    }

    public String getCustomRefreshIntervalUnit() {
        return customRefreshIntervalUnit;
    }

    public void setCustomRefreshIntervalUnit(String customRefreshIntervalUnit) {
        this.customRefreshIntervalUnit = customRefreshIntervalUnit;
    }
    public Boolean getAllowUserCreation() {
        return allowUserCreation;
    }

    public void setAllowUserCreation(Boolean allowUserCreation) {
        this.allowUserCreation = allowUserCreation;
    }

    public Boolean getEnablePassiveSso(){return enablePassiveSso;}

    public void setEnablePassiveSso(Boolean enablePassiveSso){this.enablePassiveSso=enablePassiveSso;}
    public Boolean getForceAuthentication() {
        return forceAuthentication;
    }

    public void setForceAuthentication(Boolean forceAuthentication) {
        this.forceAuthentication = forceAuthentication;
    }

    public String getInputUrl() {
        return inputUrl;
    }

    public void setInputUrl(String inputUrl) {
        this.inputUrl = inputUrl;
    }

    public String getRelayState() {
        return relayState;
    }

    public void setRelayState(String relayState) {
        this.relayState = relayState;
    }

    public String getRelayStateRedirectionType() {
        return relayStateRedirectionType;
    }

    public void setRelayStateRedirectionType(String relayStateRedirectionType) {
        this.relayStateRedirectionType = relayStateRedirectionType;
    }

    public Boolean getEnableButtons() {
        return enableButtons;
    }

    public void setEnableButtons(Boolean enableButtons) {
        this.enableButtons = enableButtons;
    }
}
