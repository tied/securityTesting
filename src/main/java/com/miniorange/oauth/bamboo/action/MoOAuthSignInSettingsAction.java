package com.miniorange.oauth.bamboo.action;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.user.Group;
import com.atlassian.user.search.page.Pager;
import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.bamboo.MoOAuthPluginHandler;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.bamboo.user.BambooUserManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.struts2.ServletActionContext;
import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.miniorange.oauth.utils.MoOAuthUtils;
import com.miniorange.oauth.bamboo.MoOAuthPluginHandler;


public class MoOAuthSignInSettingsAction extends BambooActionSupport {

	private static final Log LOGGER = LogFactory.getLog(MoOAuthSignInSettingsAction.class);
	
	private String loginButtonText;
	private Boolean signinSettingsSubmitted;
	private String relayState;
	private String allowedDomains;
	private String xsrfToken;
	private Boolean enableBackdoor;
	private Boolean disableDefaultLogin;
	private Boolean enableAutoRedirectDelay;
	private String autoRedirectDelayInterval;
	private String ssoButtonLocation;
	private Boolean enableLoginTemplate;
	private String loginTemplate;
	private Boolean enableLogoutTemplate;
	private String logoutTemplate;
	private String customLogoutURL;
	private Boolean enableErrorMsgTemplate;
	private String errorMsgTemplate;
	private Boolean pluginApiAccessRestriction;

	private Boolean backdoorSubmitted;
	private Boolean restrictBackdoor;
	private List<String> backdoorGroupsList;
	private String[] backdoorGroups;
	private String lowerBuild;
	private ArrayList existingGroups;

	private PluginLicenseManager pluginLicenseManager;
	private MoOAuthSettings settings;
	private UserManager userManager;
	private LoginUriProvider loginUriProvider;

	private String backdoorKey;
	private String backdoorValue;

	public MoOAuthSignInSettingsAction(MoOAuthSettings settings, 
			UserManager userManager, LoginUriProvider loginUriProvider, BambooUserManager bambooUserManager) {
		this.settings = settings;
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.bambooUserManager = bambooUserManager;
	}

	public void validate() {
		LOGGER.info("Sign In Settings Action validate called");
		HttpServletRequest request = ServletActionContext.getRequest();
		if (!BooleanUtils.toBoolean(this.signinSettingsSubmitted)) {
			return;
		}

		Boolean error = false;

		List<String> invalidValues = new ArrayList<>();

		UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

		if(StringUtils.isNotBlank(relayState) && !urlValidator.isValid(relayState))
			invalidValues.add("relaystate");

		for(String invalidValue : invalidValues){
			error = true;
			addActionError(getText("oauth.error.invalid."+invalidValue));
		}

		if (super.hasActionErrors() || error) {
			xsrfToken = XsrfTokenUtils.getXsrfToken(request);
			initializeOAuthConfig();
			return;
		}
		super.validate();
	}

	public String execute() throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		final UserProfile user = userManager.getRemoteUser();
		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				LOGGER.info("SignIn Settings Action doExecute Submitted: ");
				if (!settings.isLicenseValid()) {
					try {
						response.sendRedirect(settings.getManageAddOnURL());
						return null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				this.lowerBuild = "false";
				this.lowerBuild = getLowerBuild();
				LOGGER.debug("signinSettingsSubmitted: " + this.signinSettingsSubmitted);
				LOGGER.debug("backdoorSubmitted: " + this.backdoorSubmitted);
				backdoorGroupsList = new ArrayList<>();
				xsrfToken = XsrfTokenUtils.getXsrfToken(request);
				if (BooleanUtils.toBoolean(this.signinSettingsSubmitted)) {
					if (StringUtils.equalsIgnoreCase("true", lowerBuild)){
						if (backdoorGroups != null) {
							LOGGER.debug("Backdoor groups-" + backdoorGroups.length);
							backdoorGroupsList = new ArrayList<String>(Arrays.asList(backdoorGroups));
						}
					}else{
						backdoorGroupsList = settings.convertArrayToList(StringUtils.split(backdoorGroups[0],","));
					}
					MoOAuthPluginHandler.saveSigninSettings(this.loginButtonText, this.relayState,
							this.disableDefaultLogin ,this.enableBackdoor, this.enableAutoRedirectDelay,this.autoRedirectDelayInterval,
							this.allowedDomains,this.ssoButtonLocation,this.enableLoginTemplate, this.loginTemplate, this.enableLogoutTemplate, this.logoutTemplate,
							this.customLogoutURL, this.enableErrorMsgTemplate, this.errorMsgTemplate, this.pluginApiAccessRestriction,this.restrictBackdoor, this.backdoorGroupsList,this.backdoorKey, this.backdoorValue);
					addActionMessage(getText("oauth.success.config"));
				}
				initializeOAuthConfig();
				return "success";
			} catch (MoOAuthPluginException e) {
				e.printStackTrace();
				addActionError(e.getMessage());
				return "input";
			} catch (Exception e) {
				e.printStackTrace();
				addActionError("An error occurred while saving your details. Please check logs for more info.");
				return "input";
			}
		}else{
			response.sendRedirect(loginUriProvider.getLoginUri(MoOAuthPluginHandler.getUri(request)).toASCIIString());
			return null;
		}
	}

	private void initializeOAuthConfig() {
		LOGGER.info("initialize OAuth Config called");
		this.loginButtonText = settings.getLoginButtonText();
		this.relayState = settings.getRelayState();
		this.disableDefaultLogin = settings.getDefaultLoginDisabled();
		this.enableBackdoor = settings.getBackdoorEnabled();
		this.enableAutoRedirectDelay = settings.getEnableAutoRedirectDelay();
		this.autoRedirectDelayInterval=settings.getAutoRedirectDelayInterval();
		this.allowedDomains = settings.getAllowedDomains();
		this.ssoButtonLocation= settings.getSsoButtonLocation();
		this.enableLoginTemplate = settings.getEnableLoginTemplate();
		this.loginTemplate = settings.getLoginTemplate();
		this.enableLogoutTemplate = settings.getEnableLogoutTemplate();
		this.logoutTemplate = settings.getLogoutTemplate();
		this.customLogoutURL = settings.getCustomLogoutURL();
		this.enableErrorMsgTemplate = settings.getEnableErrorMsgTemplate();
		this.errorMsgTemplate = settings.getErrorMsgTemplate();
		this.pluginApiAccessRestriction = settings.getPluginApiAccessRestriction();
		this.restrictBackdoor = settings.getRestrictBackdoor();
		this.backdoorGroupsList = settings.getBackdoorGroups();
		this.lowerBuild =getLowerBuild();
		ArrayList<String> existingGroup = getExistingGroups();
		this.backdoorKey = settings.getBackdoorKey();
		this.backdoorValue = settings.getBackdoorValue();
	}

	public String getBaseUrl(){
		return settings.getBaseUrl();
	}

	public Boolean getPluginApiAccessRestriction() {
		return pluginApiAccessRestriction;
	}

	public void setPluginApiAccessRestriction(Boolean pluginApiAccessRestriction) {
		this.pluginApiAccessRestriction = pluginApiAccessRestriction;
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

	public String getCustomLogoutURL() {
		return customLogoutURL;
	}

	public void setCustomLogoutURL(String customLogoutURL) {
		this.customLogoutURL = customLogoutURL;
	}

	public Boolean getEnableLoginTemplate() {
		return enableLoginTemplate;
	}

	public void setEnableLoginTemplate(Boolean enableLoginTemplate) {
		this.enableLoginTemplate = enableLoginTemplate;
	}

	public String getLoginTemplate() {
		return this.loginTemplate;
	}

	public void setLoginTemplate(String loginTemplate) {
		this.loginTemplate = loginTemplate;
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


	public String getSsoButtonLocation() {
		return ssoButtonLocation;
	}

	public void setSsoButtonLocation(String relayState) {
		this.ssoButtonLocation = relayState;
	}
	public Boolean getEnableBackdoor() {
		return enableBackdoor;
	}

	public void setEnableBackdoor(Boolean enableBackdoor) {
		this.enableBackdoor = enableBackdoor;
	}

	public Boolean getDisableDefaultLogin() {
		return disableDefaultLogin;
	}

	public void setDisableDefaultLogin(Boolean disableDefaultLogin) {
		this.disableDefaultLogin = disableDefaultLogin;
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

	public String getAllowedDomains() {
		return allowedDomains;
	}

	public void setAllowedDomains(String allowedDomains) {
		this.allowedDomains = allowedDomains;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	public PluginLicenseManager getPluginLicenseManager() {
		return pluginLicenseManager;
	}

	public void setPluginLicenseManager(PluginLicenseManager pluginLicenseManager) {
		this.pluginLicenseManager = pluginLicenseManager;
	}

	public MoOAuthSettings getSettings() {
		return settings;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
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

	public String getLowerBuild() {
		this.lowerBuild = Boolean.toString(settings.isLowerBuild());
		return this.lowerBuild;
	}

	public void setLowerBuild(String lowerBuild) {
		this.lowerBuild = lowerBuild;
	}

	public ArrayList getExistingGroups() {
		try {
			Pager groupObjects = bambooUserManager.getGroups();
			this.existingGroups = new ArrayList<String>();
			Iterator<Group> itr = groupObjects.iterator();
			while (itr.hasNext()) {
				Group group = itr.next();
				if (!this.existingGroups.contains(group.getName().trim())) {
					this.existingGroups.add(group.getName().trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.existingGroups;
	}

	public void setExistingGroups(ArrayList existingGroups) {
		this.existingGroups = existingGroups;
	}
}
