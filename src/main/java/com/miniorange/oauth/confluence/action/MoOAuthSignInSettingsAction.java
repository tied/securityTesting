package com.miniorange.oauth.confluence.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.atlassian.xwork.SimpleXsrfTokenGenerator;
import com.atlassian.xwork.XsrfTokenGenerator;
import com.opensymphony.webwork.ServletActionContext;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;

import javax.servlet.http.HttpServletRequest;

public class MoOAuthSignInSettingsAction extends ConfluenceActionSupport {
	
private static final Logger LOGGER = LoggerFactory.getLogger(MoOAuthSignInSettingsAction.class);
	
	private Boolean enableOAuthSSO;
	private String loginButtonText;
	private String relayState;
	private String allowedDomains;
	private String adminSessionOption;
	private String customLogoutURL;
	private Boolean enableLogoutTemplate;
	private String logoutTemplate;
	private Boolean disableDefaultLogin;
	private Boolean enableBackdoor;
	private Boolean backdoorSubmitted;
	private String backdoorKey;
	private String backdoorValue;
	private Boolean enableAutoRedirectDelay;
	private String autoRedirectDelayInterval;
	private Boolean checkIDPSession;
	private Boolean isAutoLoginSuppProv;

	private String ssoButtonLocation;

	private Boolean signinSettingsSubmitted;
	private Boolean checkSingleLogoutURL;

	private String errorMsgTemplate;
	private Boolean enableErrorMsgTemplate;
	private Boolean enableRememberMeCookie;
	private Boolean restrictBackdoor;
	private List<String> backdoorGroupsList;
	private String[] backdoorGroups;
	private List<String> existingGroupsForBackdoorAccess;

	private Boolean disableAnonymousAccess;
	private Boolean isAnonymousAccessEnabled;
	private Boolean allowGuestLogin;
	private String guestSessionTimeout;
	private Boolean userCreation;
	private String loginTemplate;
	private Boolean enableLoginTemplate;
	private Boolean pluginApiAccessRestriction;

	private PluginLicenseManager pluginLicenseManager;
	private MoOAuthSettings settings;
	private MoOAuthPluginHandler pluginHandler;
	private Boolean autoActivateUser;
	private String xsrfToken;
	private HashMap<String, String> customErrorMappingMap;

	
	public MoOAuthSignInSettingsAction(MoOAuthSettings settings, MoOAuthPluginHandler pluginHandler) {
		this.settings = settings;
		this.pluginHandler = pluginHandler; 
	}
	
	public Boolean validation() {
		LOGGER.debug("Configure Action Validate");
		Boolean error = false;
		if (!BooleanUtils.toBoolean(this.signinSettingsSubmitted)) {
			error = true;
		}
		
		List<String> invalidValues = new ArrayList<>();

		if(StringUtils.startsWithIgnoreCase(relayState, "http") && !StringUtils.contains(relayState, settings.getBaseUrl())){
			invalidValues.add("relayStateURL");
		}

		if(StringUtils.isNotBlank(customLogoutURL)) {
			try {
				new URL(this.customLogoutURL);
			} catch (MalformedURLException e) {
				LOGGER.error(e.getMessage());
				invalidValues.add("customlogoutURL");
			}
		}
		
		for(String invalidValue : invalidValues){
			error = true;
			addActionError(getText("oauth.error.invalid."+invalidValue));
		}
		
		if (BooleanUtils.toBoolean(error)) {
			initializeOAuthConfig();
		}
		
		return error;
	}
	
	public String execute() throws Exception {
		try {
			LOGGER.debug("SignIn Settings Action doExecute Submitted: ");		
			if(!settings.isLicenseValid()){
				return "invalid";
			}

			HttpServletRequest request = ServletActionContext.getRequest();
			XsrfTokenGenerator tokenGenerator = new SimpleXsrfTokenGenerator();
			xsrfToken = tokenGenerator.generateToken(request);
			
			if (BooleanUtils.toBoolean(this.signinSettingsSubmitted)) {
				HashMap<String, String> customErrorMappingMap = new HashMap<>();

				for (int i = 0; i < MoOAuthSettings.defaultErrorMappingMap.size() ; i++) {
					String key = request.getParameter("errorMsgKey_" + i);
					String value = StringUtils.trimToEmpty(request.getParameter("errorMsgValue_" + i));

					if (StringUtils.isNotEmpty(key)){
						if(StringUtils.isEmpty(value)){
							value = MoOAuthSettings.defaultErrorMappingMap.get(key);
						}
						customErrorMappingMap.put(key.trim(), value.trim());

					}
				}

				Boolean error;
				error = validation();
                if(!error) {
                	backdoorGroupsList = settings.convertArrayToList(StringUtils.split(backdoorGroups[0],","));
					if (backdoorGroupsList.size() <= 0) {
						this.backdoorGroupsList.add("confluence-administrators");
					}
					
					if(!BooleanUtils.toBoolean(disableDefaultLogin)){
						this.enableBackdoor = Boolean.FALSE;
						this.enableAutoRedirectDelay = Boolean.FALSE;
					}
					
					LOGGER.debug("backdoorKey : "+ this.backdoorKey + " backdoorValue : "+ this.backdoorValue);

                    pluginHandler.saveSigninSettings( this.enableOAuthSSO, this.loginButtonText, this.relayState,  this.adminSessionOption,
							this.allowedDomains, this.customLogoutURL, this.enableLogoutTemplate, this.logoutTemplate, 
							this.disableDefaultLogin,this.disableAnonymousAccess,this.allowGuestLogin,this.guestSessionTimeout,
							this.enableBackdoor,  this.enableAutoRedirectDelay, autoRedirectDelayInterval, ssoButtonLocation,
           					this.enableErrorMsgTemplate, this.errorMsgTemplate, this.restrictBackdoor, this.backdoorGroupsList
							,this.autoActivateUser, this.enableRememberMeCookie, this.enableLoginTemplate, this.loginTemplate, this.pluginApiAccessRestriction
							,this.checkIDPSession,customErrorMappingMap);
					addActionMessage(getText("oauth.success.config"), "success", true, null);
				}
			}
			initializeOAuthConfig();
			return "success";
		} catch (MoOAuthPluginException e) {
			LOGGER.error(e.getMessage());
			addActionError(e.getMessage());
			return "input";
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			addActionError("An error occurred while saving your details. Please check logs for more info.");
			return "input";
		}
	}
	
	private void initializeOAuthConfig() {
		this.enableOAuthSSO = settings.getEnableOAuthSSO();
		this.loginButtonText = settings.getLoginButtonText();
		this.relayState = settings.getRelayState();
		this.adminSessionOption = settings.getAdminSessionOption();
		this.allowedDomains = settings.getAllowedDomains();
		this.customLogoutURL = settings.getCustomLogoutURL();
		this.enableLogoutTemplate = settings.getEnablelogoutTemplate();
		this.logoutTemplate = settings.getLogoutTemplate();
		this.disableDefaultLogin = settings.getDefaultLoginDisabled();
		this.allowGuestLogin=settings.getAllowGuestLogin();
		this.disableAnonymousAccess=settings.getDisableAnonymousAccess();
		this.guestSessionTimeout=settings.getGuestSessionTimeout();
		this.enableBackdoor = settings.getBackdoorEnabled();
		this.enableAutoRedirectDelay = settings.getEnableAutoRedirectDelay();
		this.autoRedirectDelayInterval=settings.getAutoRedirectDelayInterval();
		this.ssoButtonLocation = settings.getSsoButtonLocation();
		this.checkSingleLogoutURL = !settings.getSingleLogoutURL().isEmpty();
		this.enableErrorMsgTemplate = BooleanUtils.toBooleanDefaultIfNull(settings.getEnableErrorMsgTemplate(),Boolean.TRUE);
		this.errorMsgTemplate = settings.getErrorMsgTemplate();
		this.backdoorKey = settings.getBackdoorKey();
		this.backdoorValue = settings.getBackdoorValue();
		this.restrictBackdoor = settings.getRestrictBackdoor();
		this.backdoorGroupsList = settings.getBackdoorGroups();
		this.enableRememberMeCookie = settings.getRememberMeCookieEnabled();
		this.checkIDPSession = settings.getCheckIDPSession();
		String appName = settings.getAppName();
		this.customErrorMappingMap = settings.getErrorMappingMap();

		if(StringUtils.equalsIgnoreCase(appName, "Azure AD") || StringUtils.equalsIgnoreCase(appName, "Azure B2C") || StringUtils.equalsIgnoreCase(appName, "Keycloak")) {
			this.isAutoLoginSuppProv = true;
		}
		else {
			this.isAutoLoginSuppProv = false;
		}
		this.enableLoginTemplate = settings.getEnableLoginTemplate();
		this.loginTemplate = settings.getLoginTemplate();
		if (backdoorGroupsList.size() <= 0) {
			this.backdoorGroupsList.add("confluence-administrators");
		}
		this.pluginApiAccessRestriction= settings.getPluginApiAccessRestriction();
		LOGGER.debug("initializeOAuthConfig: backdoorKey : "+ this.backdoorKey + " backdoorValue : "+ this.backdoorValue);
		this.autoActivateUser = settings.getAutoActivateUser();
	}

	public MoOAuthSettings getSettings() {
		return settings;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public MoOAuthPluginHandler getPluginHandler() {
		return pluginHandler;
	}

	public void setPluginHandler(MoOAuthPluginHandler pluginHandler) {
		this.pluginHandler = pluginHandler;
	}

	public String getLoginButtonText() {
		return loginButtonText;
	}

	public void setLoginButtonText(String loginButtonText) {
		this.loginButtonText = loginButtonText;
	}

	public Boolean getSigninSettingsSubmitted() {
		return signinSettingsSubmitted;
	}

	public void setSigninSettingsSubmitted(Boolean signinSettingsSubmitted) {
		this.signinSettingsSubmitted = signinSettingsSubmitted;
	}

	public String getRelayState() {
		return relayState;
	}

	public void setRelayState(String relayState) {
		this.relayState = relayState;
	}

	public String getAllowedDomains() {
		return allowedDomains;
	}

	public void setAllowedDomains(String allowedDomains) {
		this.allowedDomains = allowedDomains;
	}

	public String getCustomLogoutURL() {
		return customLogoutURL;
	}

	public void setCustomLogoutURL(String customLogoutURL) {
		this.customLogoutURL = customLogoutURL;
	}

	public Boolean getEnablelogoutTemplate() {
		return enableLogoutTemplate;
	}

	public void setEnablelogoutTemplate(Boolean enableLogoutTemplate) {
		this.enableLogoutTemplate = enableLogoutTemplate;
	}

	public String getLogoutTemplate() {
		return logoutTemplate;
	}

	public void setLogoutTemplate(String logoutTemplate) {
		this.logoutTemplate = logoutTemplate;
	}

	public Boolean getEnableLogoutTemplate() {
		return enableLogoutTemplate;
	}

	public void setEnableLogoutTemplate(Boolean enableLogoutTemplate) {
		this.enableLogoutTemplate = enableLogoutTemplate;
	}

	public Boolean getDisableDefaultLogin() {
		return disableDefaultLogin;
	}

	public void setDisableDefaultLogin(Boolean disableDefaultLogin) {
		this.disableDefaultLogin = disableDefaultLogin;
	}

	public Boolean getEnableBackdoor() {
		return enableBackdoor;
	}

	public void setEnableBackdoor(Boolean enableBackdoor) {
		this.enableBackdoor = enableBackdoor;
	}
	
	public Boolean getEnableAutoRedirectDelay() {
		return enableAutoRedirectDelay;
	}

	public void setEnableAutoRedirectDelay(Boolean enableAutoRedirectDelay) {
		this.enableAutoRedirectDelay = enableAutoRedirectDelay;
	}
	
	public String getAutoRedirectDelayInterval() {
		return autoRedirectDelayInterval;
	}

	public void setAutoRedirectDelayInterval(String autoRedirectDelayInterval) {
		this.autoRedirectDelayInterval = autoRedirectDelayInterval;
	}

	public String getSsoButtonLocation() {
		return ssoButtonLocation;
	}

	public void setSsoButtonLocation(String ssoButtonLocation) {
		this.ssoButtonLocation = ssoButtonLocation;
	}
	
	public String getAdminSessionOption() {
		return adminSessionOption;
	}

	public void setAdminSessionOption(String adminSessionOption) {
		this.adminSessionOption = adminSessionOption;
	}

	public Boolean getCheckSingleLogoutURL() {
		return checkSingleLogoutURL;
	}

	public void setCheckSingleLogoutURL(Boolean checkSingleLogoutURL) {
		this.checkSingleLogoutURL = checkSingleLogoutURL;
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

	public Boolean getBackdoorSubmitted() {
		return backdoorSubmitted;
	}

	public void setBackdoorSubmitted(Boolean backdoorSubmitted) {
		this.backdoorSubmitted = backdoorSubmitted;
	}
	
	public String getBackdoorKey() {
		return backdoorKey;
	}

	public void setBackdoorKey(String backdoorKey) {
		this.backdoorKey = backdoorKey;
	}

	public String getBackdoorValue() {
		return backdoorValue;
	}

	public void setBackdoorValue(String backdoorValue) {
		this.backdoorValue = backdoorValue;
	}

	public Boolean getRestrictBackdoor() {
		return restrictBackdoor;
	}

	public void setRestrictBackdoor(Boolean restrictBackdoor) {
		this.restrictBackdoor = restrictBackdoor;
	}

	public List<String> getBackdoorGroupsList() {
		return backdoorGroupsList;
	}

	public void setBackdoorGroupsList(List<String> backdoorGroupsList) {
		this.backdoorGroupsList = backdoorGroupsList;
	}

	public String[] getBackdoorGroups() {
		return backdoorGroups;
	}

	public void setBackdoorGroups(String[] backdoorGroups) {
		this.backdoorGroups = backdoorGroups;
	}
	

	public Boolean getAutoActivateUser() {
		return autoActivateUser;
	}

	public void setAutoActivateUser(Boolean autoActivateUser) {
		this.autoActivateUser = autoActivateUser;
	}

	public Boolean getEnableOAuthSSO() {
		return enableOAuthSSO;
	}

	public void setEnableOAuthSSO(Boolean enableOAuthSSO) {
		this.enableOAuthSSO = enableOAuthSSO;
	}

	public Boolean getEnableRememberMeCookie() {
		return enableRememberMeCookie;
	}

	public void setEnableRememberMeCookie(Boolean enableRememberMeCookie) {
		this.enableRememberMeCookie = enableRememberMeCookie;
	}
	public Boolean getDisableAnonymousAccess() {
		return this.disableAnonymousAccess;
	}

	public void setDisableAnonymousAccess(Boolean disableAnonymousAccess) {
		this.disableAnonymousAccess = disableAnonymousAccess;
	}

	public void setIsAnonymousAccessEnabled(Boolean isAnonymousAccessEnabled) {
		this.isAnonymousAccessEnabled = isAnonymousAccessEnabled;
	}

	public Boolean getAllowGuestLogin() {
		return this.allowGuestLogin;
	}

	public void setAllowGuestLogin(Boolean allowGuestLogin) {
		this.allowGuestLogin = allowGuestLogin;
	}

	public String getGuestSessionTimeout() {
		return this.guestSessionTimeout;
	}

	public void setGuestSessionTimeout(String guestSessionTimeout) {
		this.guestSessionTimeout = guestSessionTimeout;
	}	

	public Boolean getIsAnonymousAccessEnabled() {
		return MoOAuthPluginHandler.isGlobalAnonymousAccessEnabled();
	}

	public Boolean getUserCreation() {
		return settings.getRestrictUserCreation();
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	public Boolean getCheckIDPSession() {return checkIDPSession;}

	public void setCheckIDPSession(Boolean checkIDPSession) {this.checkIDPSession = checkIDPSession;}

	public Boolean getIsAutoLoginSuppProv() {	return isAutoLoginSuppProv;}

	public void setIsAutoLoginSuppProv(Boolean autoLoginSuppProv) {isAutoLoginSuppProv = autoLoginSuppProv;}

	public String getLoginTemplate() {
		return loginTemplate;
	}

	public void setLoginTemplate(String loginTemplate) {
		this.loginTemplate = loginTemplate;
	}

	public Boolean getEnableLoginTemplate() {
		return enableLoginTemplate;
	}

	public void setEnableLoginTemplate(Boolean enableLoginTemplate) {
		this.enableLoginTemplate = enableLoginTemplate;
	}

	public Boolean getPluginApiAccessRestriction() {
		return pluginApiAccessRestriction;
	}

	public void setPluginApiAccessRestriction(Boolean pluginApiAccessRestriction) {
		this.pluginApiAccessRestriction = pluginApiAccessRestriction;
	}

	public HashMap<String, String> getCustomErrorMappingMap() {
		return customErrorMappingMap;
	}

	public void setCustomErrorMappingMap(HashMap<String, String> customErrorMappingMap) {
		this.customErrorMappingMap = customErrorMappingMap;
	}
}
