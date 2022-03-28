package com.miniorange.sso.saml.bamboo.action;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.user.Group;
import com.atlassian.user.search.page.Pager;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.plugin.PluginException;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;

public class MoSignInSettingsAction extends BambooActionSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(MoSignInSettingsAction.class);

	private UserManager userManager;
	private LoginUriProvider loginUriProvider;
	private MoSAMLSettings settings;
	private BambooUserManager bambooUserManager;

	private Boolean enableBackdoor;
	private String loginButtonText;
	private Boolean disableDefaultLogin;
	private String relayState;
	private String relayStateRedirectionType;
	private String xsrfToken;
	private Boolean signinSettingsSubmitted;
	private Boolean enablelogoutTemplate;
	private String customLogoutURL;
	private String timeDelay;
	private String logoutTemplate;
	private Boolean enableErrorMsgTemplate;
	private String errorMsgTemplate;
	private Boolean enableSAMLSSO;
	private Boolean enableAutoRedirectDelay;
	private String numberOfLoginAttempts;
	private Boolean backdoorSubmitted;
	private String backdoorKey;
	private String backdoorValue;
	private Boolean restrictBackdoor;
	private String[] backdoorGroups;
	private List<String> backdoorGroupsList;
	private Boolean enableLoginTemplate;
	private String loginTemplate;
	private ArrayList existingGroups;
	private String lowerBuild;
	private Boolean enableRememberMeCookie;
	private Boolean enablePasswordChange;
	private Boolean headerAuthentication;
	private String headerAuthenticationAttribute;

	private Boolean pluginApiAccessRestriction;


	public MoSignInSettingsAction(UserManager userManager, LoginUriProvider loginUriProvider, MoSAMLSettings settings,
								  BambooUserManager bambooUserManager){
		super();
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.settings = settings;
		this.bambooUserManager = bambooUserManager;
	}

	public void validate() {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		if (!BooleanUtils.toBoolean(signinSettingsSubmitted)) {
			return;
		}

		if (StringUtils.isNotBlank(this.relayState)) {
			try {
				new URL(this.relayState);
			} catch (MalformedURLException e) {
				addActionError("Invalid Relay State URL");
			}
		}
		if (StringUtils.isNotBlank(this.customLogoutURL)) {
			try {
				new URL(this.customLogoutURL);
			} catch (MalformedURLException e) {
				addActionError("Invalid Custom Logout URL.");
			}
		}

		if (super.hasActionErrors()) {
			xsrfToken = XsrfTokenUtils.getXsrfToken(request);
			initializeSAMLConfig();
		}
	}

	private Boolean hasError(String backdoorKey, String backdoorValue) {
		if (StringUtils.isBlank(backdoorKey.trim())) {
			addActionError("Backdoor query parameter key is invalid. Please make sure it's not empty and doesn't contain spaces.");
			return true;
		}
		if (StringUtils.isBlank(backdoorValue.trim())) {
			addActionError("Backdoor query parameter value is invalid. Please make sure it's not empty and doesn't contain spaces.");
			return true;
		}
		if (StringUtils.isBlank(this.backdoorKey) || this.backdoorKey.split("\\s+").length > 1) {
			addActionError(getText("samlsso.error.config.backdoorkey.invalid"));
			return true;
		}
		if (StringUtils.isBlank(this.backdoorValue) || this.backdoorValue.split("\\s+").length > 1) {
			addActionError(getText("samlsso.error.config.backdoorvalue.invalid"));
			return true;
		}
		return false;
	}

	@Override
	public String execute() throws Exception {
		LOGGER.debug("Sign in Settings Action execute called");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);

		final UserProfile user = userManager.getRemoteUser();

		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				LOGGER.debug("Sign in Settings Action doExecute Submitted: " + this.signinSettingsSubmitted);

				if (!settings.isValidLicense()) {
					return "upm";
				}
				this.lowerBuild = "false";
				this.lowerBuild = getLowerBuild();
				LOGGER.debug("signinSettingsSubmitted: " + this.signinSettingsSubmitted);
				LOGGER.debug("backdoorSubmitted: " + this.backdoorSubmitted);
				backdoorGroupsList = new ArrayList<>();
				if (BooleanUtils.toBoolean(this.signinSettingsSubmitted)) {
					if (StringUtils.equalsIgnoreCase("true", lowerBuild)) {
						if (backdoorGroups != null) {
							LOGGER.debug("Backdoor groups-" + backdoorGroups.length);
							backdoorGroupsList = new ArrayList<String>(Arrays.asList(backdoorGroups));
						}
					} else {
						backdoorGroupsList = settings.convertArrayToList(StringUtils.split(backdoorGroups[0],","));
					}

					MoPluginHandler.saveSigninSettings(this.disableDefaultLogin, this.enableBackdoor,
							this.loginButtonText, this.relayState, this.relayStateRedirectionType, this.customLogoutURL, this.enablelogoutTemplate,
							this.timeDelay,this.logoutTemplate,this.enableErrorMsgTemplate,this.errorMsgTemplate,
							this.enableSAMLSSO, this.enableAutoRedirectDelay,this.numberOfLoginAttempts, this.restrictBackdoor, this.backdoorGroupsList,
							this.enableLoginTemplate, this.loginTemplate, this.enableRememberMeCookie, this.enablePasswordChange,
							this.headerAuthentication, this.headerAuthenticationAttribute, this.pluginApiAccessRestriction);
					addActionMessage(getText("samlsso.success.config"));
				}
				if (BooleanUtils.toBoolean(this.backdoorSubmitted)) {
					LOGGER.debug("backdoorKey: " + this.backdoorKey);
					LOGGER.debug("backdoorValue: " + this.backdoorValue);
					this.backdoorKey = MoSAMLUtils.sanitizeText(backdoorKey);
					this.backdoorValue = MoSAMLUtils.sanitizeText(backdoorValue);
					Boolean error = hasError(this.backdoorKey, this.backdoorValue);
					if (!error) {
						MoPluginHandler.saveBackdoorValues(this.backdoorKey, this.backdoorValue);
						addActionMessage(getText("samlsso.backdoor.success.config"));
					}
				}
				initializeSAMLConfig();
				return "success";
			} catch (PluginException e) {
				e.printStackTrace();
				addActionError(e.getMessage());
				return "input";
			} catch (Exception e) {
				e.printStackTrace();
				addActionError("An error occurred while saving your details. Please check logs for more info.");
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

	private void initializeSAMLConfig() {
		this.disableDefaultLogin = settings.getDefaultLoginDisabled();
		this.enableBackdoor = settings.getBackdoorEnabled();
		this.relayState = settings.getRelayState();
		this.relayStateRedirectionType = settings.getRelayStateRedirectionType();
		this.loginButtonText = settings.getLoginButtonText();
		this.enablelogoutTemplate = settings.getEnableLogoutTemplate();
		this.customLogoutURL = settings.getCustomLogoutURL();
		this.timeDelay = settings.getTimeDelay();
		this.logoutTemplate = settings.getLogoutTemplate();
		this.enableErrorMsgTemplate = settings.getEnableErrorMsgTemplate();
		this.errorMsgTemplate = settings.getErrorMsgTemplate();
		this.enableSAMLSSO=settings.getEnableSAMLSSO();
		this.enableAutoRedirectDelay = settings.getEnableAutoRedirectDelay();
		this.numberOfLoginAttempts=settings.getNumberOfLoginAttempts();
		this.backdoorKey = settings.getBackdoorKey();
		this.backdoorValue = settings.getBackdoorValue();
		this.restrictBackdoor = settings.getRestrictBackdoor();
		this.backdoorGroupsList = settings.getBackdoorGroups();
		this.enableLoginTemplate = settings.getEnableLoginTemplate();
		this.loginTemplate = settings.getLoginTemplate();
		this.enablePasswordChange = settings.getEnablePasswordChange();
		ArrayList<String> existingGroup = getExistingGroups();
		this.enableRememberMeCookie = settings.getRememberMeCookieEnabled();
		this.lowerBuild = getLowerBuild();
		this.headerAuthentication = settings.getHeaderAuthenticationSettings();
		this.headerAuthenticationAttribute = settings.getHeaderAuthenticationAttribute();
		this.pluginApiAccessRestriction = settings.getPluginApiAccessRestriction();
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

	public Boolean getEnableBackdoor() {
		return enableBackdoor;
	}

	public void setEnableBackdoor(Boolean enableBackdoor) {
		this.enableBackdoor = enableBackdoor;
	}

	public String getLoginButtonText() {
		return loginButtonText;
	}

	public void setLoginButtonText(String loginButtonText) {
		this.loginButtonText = loginButtonText;
	}

	public Boolean getDisableDefaultLogin() {
		return disableDefaultLogin;
	}

	public void setDisableDefaultLogin(Boolean disableDefaultLogin) {
		this.disableDefaultLogin = disableDefaultLogin;
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

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	public Boolean getSigninSettingsSubmitted() {
		return signinSettingsSubmitted;
	}

	public void setSigninSettingsSubmitted(Boolean signinSettingsSubmitted) {
		this.signinSettingsSubmitted = signinSettingsSubmitted;
	}

	public Boolean getEnablelogoutTemplate() {
		return enablelogoutTemplate;
	}

	public void setEnablelogoutTemplate(Boolean enablelogoutTemplate) {
		this.enablelogoutTemplate = enablelogoutTemplate;
	}

	public String getCustomLogoutURL() {
		return customLogoutURL;
	}


	public Boolean getEnablePasswordChange() {
		return enablePasswordChange;
	}

	public void setEnablePasswordChange(Boolean enablePasswordChange) {
		this.enablePasswordChange = enablePasswordChange;
	}
	public void setCustomLogoutURL(String customLogoutURL) {
		this.customLogoutURL = customLogoutURL;
	}

	public String getTimeDelay() {
		return timeDelay;
	}

	public void setTimeDelay(String timeDelay) {
		this.timeDelay = timeDelay;
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

	public String getErrorMsgTemplate() {
		return errorMsgTemplate;
	}

	public void setErrorMsgTemplate(String errorMsgTemplate) {
		this.errorMsgTemplate = errorMsgTemplate;
	}

	public Boolean getEnableSAMLSSO() {
		return enableSAMLSSO;
	}

	public void setEnableSAMLSSO(Boolean enableSAMLSSO) {
		this.enableSAMLSSO = enableSAMLSSO;
	}

	public Boolean getEnableAutoRedirectDelay() {
		return enableAutoRedirectDelay;
	}

	public void setEnableAutoRedirectDelay(Boolean enableAutoRedirectDelay) {
		this.enableAutoRedirectDelay = enableAutoRedirectDelay;
	}

	public String getNumberOfLoginAttempts() {
		return numberOfLoginAttempts;
	}

	public void setNumberOfLoginAttempts(String numberOfLoginAttempts) {
		this.numberOfLoginAttempts = numberOfLoginAttempts;
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

	public Boolean getBackdoorSubmitted() {
		return backdoorSubmitted;
	}

	public void setBackdoorSubmitted(Boolean backdoorSubmitted) {
		this.backdoorSubmitted = backdoorSubmitted;
	}

	public Boolean getRestrictBackdoor() {
		return restrictBackdoor;
	}

	public void setRestrictBackdoor(Boolean restrictBackdoor) {
		this.restrictBackdoor = restrictBackdoor;
	}

	public String[] getBackdoorGroups() {
		return backdoorGroups;
	}

	public void setBackdoorGroups(String[] backdoorGroups) {
		this.backdoorGroups = backdoorGroups;
	}

	public List<String> getBackdoorGroupsList() {
		return backdoorGroupsList;
	}

	public void setBackdoorGroupsList(List<String> backdoorGroupsList) {
		this.backdoorGroupsList = backdoorGroupsList;
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

	public Boolean getPluginApiAccessRestriction() {
		return pluginApiAccessRestriction;
	}

	public void setPluginApiAccessRestriction(Boolean pluginApiAccessRestriction) {
		this.pluginApiAccessRestriction = pluginApiAccessRestriction;
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

	public String getLowerBuild() {
		if (settings.getCurrentBuildNumber() < 60604) {
			LOGGER.debug("Current Bamboo version is lesser than 6.6.0");
			this.lowerBuild = "true";
		} else {
			this.lowerBuild = "false";
			LOGGER.debug("Current Bamboo version is greater than or equal to 6.6.0");
		}
		return  this.lowerBuild;
	}

	public void setLowerBuild(String lowerBuild) {
		this.lowerBuild = lowerBuild;
	}

	public Boolean getEnableRememberMeCookie() {
		return enableRememberMeCookie;
	}

	public void setEnableRememberMeCookie(Boolean enableRememberMeCookie) {
		this.enableRememberMeCookie = enableRememberMeCookie;
	}

	public Boolean getHeaderAuthentication() {
		return headerAuthentication;
	}

	public void setHeaderAuthentication(Boolean headerAuthentication) {
		this.headerAuthentication = headerAuthentication;
	}

	public String getHeaderAuthenticationAttribute() {
		return headerAuthenticationAttribute;
	}

	public void setHeaderAuthenticationAttribute(String headerAuthenticationAttribute) {
		this.headerAuthenticationAttribute = headerAuthenticationAttribute;
	}
}
