package com.miniorange.oauth.bamboo;

import java.io.IOException;
import java.util.*;

import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import com.atlassian.bamboo.util.BuildUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.user.Group;
import com.atlassian.user.search.page.Pager;
import com.atlassian.bamboo.user.BambooUserManager;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.Plugin;

import javax.servlet.http.HttpServletRequest;
import com.atlassian.bamboo.security.BambooPermissionManager;

public class MoOAuthSettings {

	private static Log LOGGER = LogFactory.getLog(MoOAuthSettings.class);

	private PluginSettings pluginSettings;
	private PluginSettingsFactory pluginSettingsFactory;
	private PluginLicenseManager pluginLicenseManager;
	private AdministrationConfigurationAccessor administrationConfigurationAccessor;
	private BambooUserManager bambooUserManager;
	private PluginAccessor pluginAccessor;
	private BambooPermissionManager bambooPermissionManager;
	public static String LOGIN_TEMPLATE = "";
	private static String LOGOUT_TEMPLATE = "";
	private static String ERRORMSG_TEMPLATE = "";

	static{
		try{
			final String ERRORMSG_TEMPLATE_PATH = "/templates/errorMsgTemplate.ftl";
			ERRORMSG_TEMPLATE = IOUtils.toString(MoOAuthSettings.class.getResourceAsStream(ERRORMSG_TEMPLATE_PATH), "UTF-8");
			final String LOGIN_TEMPLATE_PATH = "/templates/loginTemplate.ftl";
			final String LOGOUT_TEMPLATE_PATH = "/templates/logoutTemplate.ftl";
			LOGIN_TEMPLATE = IOUtils.toString(MoOAuthSettings.class.getResourceAsStream(LOGIN_TEMPLATE_PATH), "UTF-8");
			LOGOUT_TEMPLATE = IOUtils.toString(MoOAuthSettings.class.getResourceAsStream(LOGOUT_TEMPLATE_PATH), "UTF-8");
		} catch (IOException e) {
			LOGGER.error("An I/O error occurred while initializing the Settings.", e);
		}
	}

	public static Boolean isPluginConfigurationFileUploaded = Boolean.FALSE;

	public MoOAuthSettings(PluginLicenseManager pluginLicenseManager, PluginSettingsFactory pluginSettingsFactory, 
		AdministrationConfigurationAccessor administrationConfigurationAccessor,
		BambooUserManager bambooUserManager, PluginAccessor pluginAccessor,
		BambooPermissionManager bambooPermissionManager) {
		super();
		this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.pluginLicenseManager = pluginLicenseManager;
		this.administrationConfigurationAccessor = administrationConfigurationAccessor;
		this.bambooUserManager = bambooUserManager;
		this.pluginAccessor = pluginAccessor;
		this.bambooPermissionManager = bambooPermissionManager;
	}

	public String getCallBackUrl() {
		return getBaseUrl().concat("/plugins/servlet/oauth/callback");
	}
	public String getCreateUserUrl(){ return getBaseUrl().concat("/plugins/servlet/oauth/createuser");}
	
	public Boolean isLicenseDefine(){
		if (pluginLicenseManager.getLicense().isDefined()) {
			return true;
		}
		return false;
	}
	
	public Boolean isEvaluationOrSubscriptionLicense() {
		if (!isLicenseValid()) {
			PluginLicense pluginLicense = pluginLicenseManager.getLicense().get();
			if (pluginLicense.isEvaluation() || pluginLicense.isSubscription()) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	public String getBaseUrl() {
		String baseURL = administrationConfigurationAccessor.getAdministrationConfiguration().getBaseUrl();
		return baseURL;
	}

	public String getLoginPageUrl() {
		return getBaseUrl().concat("/userlogin!doDefault.action");
	}

	public String getAdminSessionUrl() {
		return getBaseUrl().concat("/authenticate");
	}

	public String getLogoutPageUrl() {
		return getBaseUrl().concat("?logout=true");
	}

	public String getDashboardUrl() {
		return getBaseUrl().concat("/");
	}

	public int getMaxUsers() {
		int t = 0;
		try {
			if (pluginLicenseManager.getLicense().isDefined()) {
				t = pluginLicenseManager.getLicense().get().getEdition().get();
			}
		} catch (Exception e) {
			return -1;

		}
		return t;
	}

	public Boolean isLicenseValid() {
   	 if (pluginLicenseManager.getLicense().isDefined()) {
            if((!(pluginLicenseManager.getLicense().get().isValid())) || (pluginLicenseManager.getLicense().get().getError().isDefined())){
            	
            	LOGGER.debug("Invalid License");
            	return Boolean.FALSE;
            }
        }
   	 return Boolean.TRUE;
	}

	public String getManageAddOnURL() {
		return getBaseUrl().concat("/plugins/servlet/upm");
	}
	
	public static String generateRandomAlphaNumericKey(int bytes) {
		String randomString = RandomStringUtils.random(bytes, true, true);
		return randomString;
	}

	public boolean isVerifyCredentialsInUse(HttpServletRequest request) {
		return MoOAuthHttpUtils.getCookie("verify_credentials_in_use",request) != null;
	}

	public boolean isTestIDPConfigurationInUse(HttpServletRequest request) {
		return MoOAuthHttpUtils.getCookie("test_configuration_in_use",request) != null;
	}

	public void setBambooUserManager(BambooUserManager bambooUserManager) {
		this.bambooUserManager = bambooUserManager;
	}

	public Integer getCurrentBuildNumber() {
		String buildNumber = BuildUtils.getCurrentBuildNumber();
		return Integer.parseInt(buildNumber);
	}

	public Boolean isLowerBuild(){
		if(getCurrentBuildNumber() < 60604){
			return true;
		}
		return false;
	}

	public String getImportExportPageUrl() {
		return getBaseUrl().concat("/plugins/servlet/bamboo-oauth/importexport.action");
	}

	//OAuth Provider Configurations
	public String getCustomAppName() {
		String appName = StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.CUSTOM_APP_NAME.getKey()), StringUtils.EMPTY);
		return appName;
	}

	public void setCustomAppName(String customAppName) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.CUSTOM_APP_NAME.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(customAppName, StringUtils.EMPTY)));
	}

	public Boolean getRememberMeCookieEnabled() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.REMEMBERME_COOKIE)), false);
	}

	public void setRememberMeCookieEnabled(Boolean isRememberMeCookieEnabled) {
		LOGGER.debug("is cookie enabled?:" + isRememberMeCookieEnabled);
		this.pluginSettings.put(MoOAuthPluginConstants.REMEMBERME_COOKIE,
				BooleanUtils.toString(isRememberMeCookieEnabled, "true", "false", "false"));
	}

	public String getAppName() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.DEFAULT_APP_NAME.getKey()), StringUtils.EMPTY);
	}
	public String getAppHostedOn(){
		return StringUtils.defaultIfBlank((String)this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.APP_HOSTED_ON.getKey()),
				MoOAuthPluginConstants.CLOUD_HOSTING);
	}
	public void setAppHostedOn(String appHostedOn){
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.APP_HOSTED_ON.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(appHostedOn, MoOAuthPluginConstants.CLOUD_HOSTING)));
	}

	public void setAppName(String appName) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.DEFAULT_APP_NAME.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(appName, StringUtils.EMPTY)));
	}

	public String getClientID() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.CLIENT_ID.getKey()), StringUtils.EMPTY);
	}

	public void setClientID(String clientID) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.CLIENT_ID.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(clientID, StringUtils.EMPTY)));
	}

	public String getClientSecret() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.CLIENT_SECRET.getKey()), StringUtils.EMPTY);
	}

	public void setClientSecret(String clientSecret) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.CLIENT_SECRET.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(clientSecret, StringUtils.EMPTY)));
	}
    
    public void setCustomizableCallbackURL(String customizableCallbackURL) {
        this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.CUSTOMIZABLE_CALLBACK_URL.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(customizableCallbackURL, StringUtils.EMPTY)));
	}
    
    public String getCustomizableCallbackURL() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.CUSTOMIZABLE_CALLBACK_URL.getKey()), StringUtils.EMPTY);
	}

	public String getScope() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.SCOPE.getKey()), StringUtils.EMPTY);
	}

	public void setScope(String scope) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.SCOPE.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(scope, StringUtils.EMPTY)));
	}

	public String getAuthorizeEndpoint() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.AUTHORIZE_END_POINT.getKey()), StringUtils.EMPTY);
	}

	public void setAuthorizeEndpoint(String authorizeEndpoint) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.AUTHORIZE_END_POINT.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(authorizeEndpoint, StringUtils.EMPTY)));
	}

	public String getAccessTokenEndpoint() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.ACCESS_TOKEN_END_POINT.getKey()), StringUtils.EMPTY);
	}

	public void setAccessTokenEndpoint(String accessTokenEndpoint) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.ACCESS_TOKEN_END_POINT.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(accessTokenEndpoint, StringUtils.EMPTY)));
	}

	public String getUserInfoEndpoint() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.USER_INFO_END_POINT.getKey()), StringUtils.EMPTY);
	}

	public void setUserInfoEndpoint(String userInfoEndpoint) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.USER_INFO_END_POINT.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(userInfoEndpoint, StringUtils.EMPTY)));
	}

	public String getFetchGroupsEndpoint() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.FETCH_GROUPS_ENDPOINT.getKey()), StringUtils.EMPTY);
	}

    public void setOauth_request_parameters(HashMap<String, String> oauth_request_parameters) {
        this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.OAUTH_REQUEST_PARAMETERS.getKey(), oauth_request_parameters);
    }

    public HashMap<String, String> getOauth_request_parameters() {
        Object oauth_request_parameters = this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.OAUTH_REQUEST_PARAMETERS.getKey());
        if (oauth_request_parameters != null) {
            return (HashMap) oauth_request_parameters;
        }
        return new HashMap<String, String>();
    }

	public void setFetchGroupsEndpoint(String groupApi) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.FETCH_GROUPS_ENDPOINT.getKey(), StringUtils.trimToEmpty(StringUtils.defaultIfBlank(groupApi, StringUtils.EMPTY)));
	}

	public void setDomainName(String domainName) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.DOMAIN_NAME.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(domainName, StringUtils.EMPTY)));
	}

	public String getDomainName() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.DOMAIN_NAME.getKey()),
				StringUtils.EMPTY);
	}

	public void setHostName(String hostName) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.HOST_NAME.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(hostName, StringUtils.EMPTY)));
	}

	public String getHostName() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.HOST_NAME.getKey()),
				StringUtils.EMPTY);
	}

	public void setRealmName(String realmName) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.REALM_NAME.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(realmName, StringUtils.EMPTY)));
	}

	public String getRealmName() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.REALM_NAME.getKey()),
				StringUtils.EMPTY);
	}

	public Boolean getUseStateParameter() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get
				(MoOAuthPluginConstants.OAuthConfiguration.USE_STATE_PARAMETER.getKey())), false);
	}

	public void setUseStateParameter(Boolean useStateParameter) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.USE_STATE_PARAMETER.getKey(), BooleanUtils.toString(useStateParameter,
				"true", "false", "false"));
	}

	public String getCheckIssuerFor() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.CHECK_ISSUER_FOR.getKey()),
				"Default");
	}

	public void setCheckIssuerFor(String checkIssuerFor) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.CHECK_ISSUER_FOR.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(checkIssuerFor, "Default")));
	}

	public String getCustomIssuerValue() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.CUSTOM_ISSUER_VALUE.getKey()),
				StringUtils.EMPTY);
	}

	public void setCustomIssuerValue(String customIssuerValue) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.CUSTOM_ISSUER_VALUE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(customIssuerValue, StringUtils.EMPTY)));
	}

	public Boolean getEnableCheckIssuerFor() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils
				.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.ENABLE_CHECK_ISSUER_FOR.getKey())), false);
	}

	public void setEnableCheckIssuerFor(Boolean enableCheckIssuerFor) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.ENABLE_CHECK_ISSUER_FOR.getKey(),
				BooleanUtils.toString(enableCheckIssuerFor, "true", "false", "false"));
	}

	public String getGoogleRefreshToken() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GOOGLE_REFRESH_TOKEN),
				StringUtils.EMPTY);
	}

	public void setGoogleRefreshToken(String refreshToken) {
		this.pluginSettings.put(MoOAuthPluginConstants.GOOGLE_REFRESH_TOKEN,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(refreshToken, StringUtils.EMPTY)));
	}

	public Boolean getResetSettings() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.RESET_SETTINGS)), false);
	}

	public void setResetSettings(Boolean resetSettings) {
		this.pluginSettings.put(MoOAuthPluginConstants.RESET_SETTINGS,
				BooleanUtils.toString(resetSettings, "true", "false", "false"));
	}

	public Boolean getIsTestIDPConfigurationClicked() {
		String isTestIDPConfigurationClicked = (String) this.pluginSettings.get(MoOAuthPluginConstants.IS_TEST_CONFIGURATION_CLICKED);
		if (BooleanUtils.toBoolean(isTestIDPConfigurationClicked)) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	public void setIsTestIDPConfigurationClicked(Boolean isTestIDPConfigurationClicked) {
		this.pluginSettings.put(MoOAuthPluginConstants.IS_TEST_CONFIGURATION_CLICKED, BooleanUtils.toString(isTestIDPConfigurationClicked,
				"true", "false", "false"));
	}

	public Boolean getIsVerifyCredentialsClicked() {
		String isVerifyCredentialsClicked = (String) this.pluginSettings.get(MoOAuthPluginConstants.IS_VERIFY_CREDENTIALS_CLICKED);
		if (BooleanUtils.toBoolean(isVerifyCredentialsClicked)) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	public void setIsVerifyCredentialsClicked(Boolean isVerifyCredentialsClicked) {
		this.pluginSettings.put(MoOAuthPluginConstants.IS_VERIFY_CREDENTIALS_CLICKED, BooleanUtils.toString(isVerifyCredentialsClicked,
				"true", "false", "false"));
	}

	public Boolean getIsCredentialsVerified() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils
				.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.IS_CREDENTIALS_VERIFIED)), false);
	}

	public void setIsCredentialsVerified(Boolean isCredentialsVerified) {
		this.pluginSettings.put(MoOAuthPluginConstants.IS_CREDENTIALS_VERIFIED,
				BooleanUtils.toString(isCredentialsVerified, "true", "false", "false"));
	}

	//Attribute Mapping
	public Boolean getKeepExistingUserAttributes() {
		if(this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.DISABLE_ATTRIBUTE_MAPPING.getKey())==null) return true;
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils
				.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.DISABLE_ATTRIBUTE_MAPPING.getKey())), true);
	}

	public void setKeepExistingUserAttributes(Boolean keepExistingUserAttributes) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.DISABLE_ATTRIBUTE_MAPPING.getKey(),
				BooleanUtils.toString(keepExistingUserAttributes, "true", "false", "false"));
	}

	public void setLoginUserAttribute(String loginUserAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.LOGIN_USER_BY_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(loginUserAttribute, "username")));
	}

	public String getLoginUserAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.LOGIN_USER_BY_ATTRIBUTE.getKey()), "username");
	}

	public String getUsernameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.USERNAME_ATTRIBUTE.getKey()),
				StringUtils.EMPTY);
	}

	public void setUsernameAttribute(String usernameAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.USERNAME_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(usernameAttribute, StringUtils.EMPTY)));
	}

	public Boolean getRegexPatternEnabled() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.ENABLE_REGEX_PATTERN.getKey())),
				false);
	}

	public void setRegexPatternEnabled(Boolean regexPatternEnabled) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.ENABLE_REGEX_PATTERN.getKey(),
				BooleanUtils.toString(regexPatternEnabled, "true", "false", "false"));
	}

	public String getRegexPattern() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.REGEX_PATTERN.getKey()), "");
	}

	public void setRegexPattern(String regexPattern) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.REGEX_PATTERN.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(regexPattern, "")));
	}

	public String getEmailAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.EMAIL_ATTRIBUTE.getKey()),
				StringUtils.EMPTY);
	}

	public void setEmailAttribute(String emailAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.EMAIL_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(emailAttribute, StringUtils.EMPTY)));
	}

	public String getFullNameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.FULL_NAME_ATTRIBUTE.getKey()), StringUtils.EMPTY);
	}

	public void setFullNameAttribute(String fullNameAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.FULL_NAME_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(fullNameAttribute, StringUtils.EMPTY)));
	}

	public Boolean getUseSeparateNameAttributes() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.USE_SEPARATE_NAME_ATTRIBUTE.getKey())),
				false);
	}

	public void setUseSeparateNameAttributes(Boolean useSeparateNameAttributes) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.USE_SEPARATE_NAME_ATTRIBUTE.getKey(),
				BooleanUtils.toString(useSeparateNameAttributes, "true", "false", "false"));
	}

	public String getFirstNameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.FIRST_NAME_ATTRIBUTE.getKey()), StringUtils.EMPTY);
	}

	public void setFirstNameAttribute(String firstNameAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.FIRST_NAME_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(firstNameAttribute, StringUtils.EMPTY)));
	}

	public String getLastNameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.LAST_NAME_ATTRIBUTE.getKey()), StringUtils.EMPTY);
	}

	public void setLastNameAttribute(String lastNameAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.LAST_NAME_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(lastNameAttribute, StringUtils.EMPTY)));
	}

	//Group Mapping
	public void setRestrictUserCreation(Boolean restrictUserCreation) {
		this.pluginSettings
				.put(MoOAuthPluginConstants.GroupMapping.DISABLE_USER_CREATION.getKey(), BooleanUtils.toString(restrictUserCreation,
						"true", "false", "false"));

	}

	public Boolean getRestrictUserCreation() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get
				(MoOAuthPluginConstants.GroupMapping.DISABLE_USER_CREATION.getKey())), false);

	}

	public String getDefaultGroup() {
//		LOGGER.info("OAuth Settings getDefaultGroup Called..");
		String defaultGroup = (String)this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.DEFAULT_GROUP.getKey());
		if(StringUtils.isBlank(defaultGroup)) {
			defaultGroup = StringUtils.EMPTY;
			List<String> existingGroups = new ArrayList();

			Pager groupObjects = bambooUserManager.getGroups();
			Iterator<Group> itr = groupObjects.iterator();
			while (itr.hasNext()) {
				Group group = itr.next();
				existingGroups.add(group.getName());
			}

			if (existingGroups.contains(bambooPermissionManager.getDefaultUsersGroup())) {
				defaultGroup = bambooPermissionManager.getDefaultUsersGroup();
			} else {
				if (existingGroups.size() > 1)
					defaultGroup = existingGroups.get(1);
				else
					defaultGroup = existingGroups.get(0);
			}

			LOGGER.debug("Default Group is " + defaultGroup);
		}
		return StringUtils.defaultString(defaultGroup,StringUtils.EMPTY);
	}

	public void setDefaultGroup(String defaultGroup) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.DEFAULT_GROUP.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(defaultGroup, "bamboo-admin")));
	}

	public List<String> getDefaultGroupsList() {
		LOGGER.info("OAuth Settings getDefaultGroupsList Called..");
		List<String> defaultGroups = (List<String>)this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.DEFAULT_GROUPS.getKey());
		if((defaultGroups == null) || (defaultGroups.isEmpty())) {
			defaultGroups = new ArrayList<String>();
			ArrayList<Group> existingGroups = new ArrayList<Group>();

			Pager groupObjects = bambooUserManager.getGroups();
			Iterator<Group> itr = groupObjects.iterator();
			while (itr.hasNext()) {
				Group group = itr.next();
				existingGroups.add(group);
				if (StringUtils.containsIgnoreCase(group.getName(), "bamboo"))
					defaultGroups.add(group.getName());
			}
			if (defaultGroups.size() == 0) {
				if (existingGroups.size() > 1)
					defaultGroups.add(existingGroups.get(1).getName());
				else
					defaultGroups.add(existingGroups.get(0).getName());
			}

			LOGGER.debug("Default Groups are " + defaultGroups.toString());
		}
		return defaultGroups;
	}

	public void setDefaultGroupsList(List<String> defaultGroups) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.DEFAULT_GROUPS.getKey(), defaultGroups);
	}

	public void setEnableDefaultGroupsFor(String enableDefaultGroupsFor) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ASSIGN_DEFAULT_GROUP_TO.getKey(), StringUtils
				.defaultIfEmpty(enableDefaultGroupsFor, MoOAuthPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS));
	}

	public String getEnableDefaultGroupsFor() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ASSIGN_DEFAULT_GROUP_TO.getKey()),
				MoOAuthPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS);
	}

	public void setOnTheFlyGroupMapping(Boolean createUsersIfRoleMapped) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_GROUP_MAPPING.getKey(),
				BooleanUtils.toString(createUsersIfRoleMapped, "true", "false", "false"));
	}

	public Boolean getOnTheFlyGroupMapping() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_GROUP_MAPPING.getKey())),
				false);
	}

	public Boolean getKeepExistingUserRoles() {
		if(this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.DISABLE_GROUP_MAPPING.getKey()) == null) return true;
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get
				(MoOAuthPluginConstants.GroupMapping.DISABLE_GROUP_MAPPING.getKey())), true);
	}

	public void setKeepExistingUserRoles(Boolean keepExistingUserRoles) {
		this.pluginSettings
				.put(MoOAuthPluginConstants.GroupMapping.DISABLE_GROUP_MAPPING.getKey(), BooleanUtils.toString(keepExistingUserRoles,
						"true", "false", "false"));
	}

	public String getRoleAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping
				.GROUP_ATTRIBUTE.getKey()), StringUtils.EMPTY);
	}

	public void setRoleAttribute(String roleAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.GROUP_ATTRIBUTE.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(roleAttribute, StringUtils.EMPTY)));
	}

	public String getOnTheFlyFilterIDPGroupsOption() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping
				.ON_THE_FLY_FILTER_IDP_GROUPS_OPTION.getKey()), MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER);
	}

	public void setOnTheFlyFilterIDPGroupsOption(String onTheFlyFilterIDPGroupsOption) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_FILTER_IDP_GROUPS_OPTION.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(onTheFlyFilterIDPGroupsOption, MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER)));
	}

	public String getOnTheFlyFilterIDPGroupsKey() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping
				.ON_THE_FLY_FILTER_IDP_GROUPS_KEY.getKey()), StringUtils.EMPTY);
	}

	public void setOnTheFlyFilterIDPGroupsKey(String onTheFlyFilterIDPGroupsKey) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_FILTER_IDP_GROUPS_KEY.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(onTheFlyFilterIDPGroupsKey, StringUtils.EMPTY)));
	}

	public Boolean getCreateUsersIfRoleMapped() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get
				(MoOAuthPluginConstants.GroupMapping.CREATE_USER_IF_ROLE_MAPPED.getKey())), false);
	}

	public void setCreateUsersIfRoleMapped(Boolean createUsersIfRoleMapped) {
		this.pluginSettings
				.put(MoOAuthPluginConstants.GroupMapping.CREATE_USER_IF_ROLE_MAPPED.getKey(), BooleanUtils.toString(createUsersIfRoleMapped,
						"true", "false", "false"));
	}

	public void setRoleMapping(HashMap<String, String> roleMapping) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ROLE_MAPPING.getKey(), roleMapping);
	}

	public HashMap<String, String> getRoleMapping() {
		Object roleMapping = this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ROLE_MAPPING.getKey());
		if (roleMapping != null) {
			return (HashMap) roleMapping;
		}
		return new HashMap<>();
	}

	public void setOnTheFlyCreateNewGroups(Boolean onTheFlyCreateNewGroups) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_CREATE_NEW_GROUPS.getKey(),
				BooleanUtils.toString(onTheFlyCreateNewGroups, "true", "false", "false"));
	}

	public Boolean getOnTheFlyCreateNewGroups() {
		String onTheFlyCreateNewGroups = (String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_CREATE_NEW_GROUPS.getKey());
		if (StringUtils.isBlank(onTheFlyCreateNewGroups)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(onTheFlyCreateNewGroups);
		}
	}

	public void setOnTheFlyAssignNewGroupsOnly(Boolean onTheFlyAssignNewGroupsOnly) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_KEEP_EXISTING_USERS_GROUPS.getKey(),
				BooleanUtils.toString(onTheFlyAssignNewGroupsOnly, "true", "false", "false"));
	}

	public Boolean getOnTheFlyAssignNewGroupsOnly() {
		String onTheFlyAssignNewGroupsOnly = (String) this.pluginSettings
				.get(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_KEEP_EXISTING_USERS_GROUPS.getKey());
		if (StringUtils.isBlank(onTheFlyAssignNewGroupsOnly)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(onTheFlyAssignNewGroupsOnly);
		}
	}

	public Object getOnTheFlyDoNotRemoveGroups() {
		if (this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_EXCLUDE_GROUPS.getKey()) != null) {
			return this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_EXCLUDE_GROUPS.getKey());
		}
		return new ArrayList<String>();
	}

	public void setOnTheFlyDoNotRemoveGroups(List<String> groups) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_EXCLUDE_GROUPS.getKey(), groups);
	}

	//Sign In Settings
	public Boolean getDefaultLoginDisabled() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.DISABLE_DEFAULT_LOGIN.getKey()));
	}

	public void setDefaultLoginDisabled(Boolean defaultLoginDisabled) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.DISABLE_DEFAULT_LOGIN.getKey(), BooleanUtils.toString(defaultLoginDisabled,
				"true", "false", "false"));
	}

	public String getLoginButtonText() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.LOGIN_BUTTON_TEXT.getKey()), "Use OAuth Login");
	}

	public void setLoginButtonText(String loginButtonText) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.LOGIN_BUTTON_TEXT.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(loginButtonText, "Use OAuth Login")));
	}

	public String getSsoButtonLocation() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.SSO_BUTTON_LOCATION.getKey()),
				"After Login Button");
	}

	public void setSsoButtonLocation(String ssoButtonLocation) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.SSO_BUTTON_LOCATION.getKey(), StringUtils.trimToEmpty(StringUtils.defaultIfBlank(ssoButtonLocation, "After Login Button")));
	}

	public void setRelayState(String relayState) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.RELAY_STATE.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(relayState, StringUtils.EMPTY)));
	}

	public String getRelayState( ) {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.RELAY_STATE.getKey()),StringUtils.EMPTY);
	}

	public Boolean getBackdoorEnabled() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ENABLE_BACKDOOR.getKey()));
	}

	public void setBackdoorEnabled(Boolean backdoorEnabled) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ENABLE_BACKDOOR.getKey(), BooleanUtils.toString(backdoorEnabled,
				"true", "false", "false"));
	}
	public Boolean getRestrictBackdoor() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.RESTRICT_BACKDOOR.getKey()));
	}

	public void setRestrictBackdoor(Boolean restrictBackdoor) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.RESTRICT_BACKDOOR.getKey(),
				BooleanUtils.toString(restrictBackdoor, "true", "false", "false"));
	}
	public List<String> getBackdoorGroups() {
		List<String> backdoorGroups = (List<String>) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.BACKDOOR_GROUPS.getKey());
		if (backdoorGroups == null) {
			backdoorGroups = new ArrayList<>();
			backdoorGroups.addAll(bambooPermissionManager.getAdminGroups());
		} else if (backdoorGroups.size() == 0) {
			backdoorGroups.addAll(bambooPermissionManager.getAdminGroups());
		}

		return backdoorGroups;
	}

	public void setBackdoorGroups(List<String> backdoorGroups) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.BACKDOOR_GROUPS.getKey(), backdoorGroups);
	}

	public List<String> convertArrayToList(String[] agentGroups) {
		List<String> agentGroupsList = new ArrayList<>();
		if (agentGroups != null) {
			for (String agentGroup : agentGroups) {
				agentGroupsList.add(agentGroup);
			}
		}
		return agentGroupsList;
	}
	public void setBackdoorKey(String backdoorKey) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.BACKDOOR_KEY.getKey(), StringUtils.defaultIfBlank(backdoorKey, "oauth_sso"));
	}

	public String getBackdoorKey() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.BACKDOOR_KEY.getKey()), "oauth_sso");
	}

	public void setBackdoorValue(String backdoorValue) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.BACKDOOR_VALUE.getKey(), StringUtils.defaultIfBlank(backdoorValue, "false"));
	}

	public String getBackdoorValue() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.BACKDOOR_VALUE.getKey()), "false");
	}

	public Boolean getEnableAutoRedirectDelay() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ENABLE_AUTO_REDIRECT_DELAY.getKey())),
				false);
	}

	public void setEnableAutoRedirectDelay(Boolean enableAutoRedirectDelay) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ENABLE_AUTO_REDIRECT_DELAY.getKey(),
				BooleanUtils.toString(enableAutoRedirectDelay, "true", "false", "false"));
	}

	public String getAutoRedirectDelayInterval() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.AUTO_REDIRECT_DELAY_INTERVAL.getKey()),
				"5");
	}

	public void setAutoRedirectDelayInterval(String autoRedirectDelayInterval) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.AUTO_REDIRECT_DELAY_INTERVAL.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(autoRedirectDelayInterval, StringUtils.EMPTY)));
	}

	public Boolean getEnableLoginTemplate() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ENABLE_LOGIN_TEMPLATE.getKey())),
				false);
	}

	public void setEnableLoginTemplate(Boolean enableLoginTemplate) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ENABLE_LOGIN_TEMPLATE.getKey(),
				BooleanUtils.toString(enableLoginTemplate, "true", "false", "false"));
	}
	
	public String getLoginTemplate() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.LOGIN_TEMPLATE.getKey()), LOGIN_TEMPLATE);
	}

	public void setLoginTemplate(String loginTemplate) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.LOGIN_TEMPLATE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(loginTemplate, LOGIN_TEMPLATE)));
	}

	public void setEnableLogoutTemplate(Boolean enableLogoutTemplate){
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ENABLE_LOGOUT_TEMPLATE.getKey(),
				BooleanUtils.toString(enableLogoutTemplate, "true","false", "false"));
	}

	public Boolean getEnableLogoutTemplate(){
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ENABLE_LOGOUT_TEMPLATE.getKey())),
				false);
	}

	public void setPluginApiAccessRestriction(Boolean pluginApiAccessRestriction){
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.PLUGIN_API_ACCESS_RESTRICTION.getKey(),
				BooleanUtils.toString(pluginApiAccessRestriction, "true", "false", "false"));
	}

	public Boolean getPluginApiAccessRestriction(){
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String)this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.PLUGIN_API_ACCESS_RESTRICTION.getKey())),
				false);
	}

	public void setEnableErrorMsgTemplate(Boolean enableErrorMsgTemplate){
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ENABLE_ERROR_MSG_TEMPLATE.getKey(),
				BooleanUtils.toString(enableErrorMsgTemplate, "true","false", "false"));
	}

	public Boolean getEnableErrorMsgTemplate(){
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ENABLE_ERROR_MSG_TEMPLATE.getKey())),
				false);
	}

	public String getAllowedDomains() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ALLOWED_DOMAINS.getKey()), StringUtils.EMPTY);
	}

	public void setAllowedDomains(String allowedDomains) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ALLOWED_DOMAINS.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(allowedDomains, StringUtils.EMPTY)));
	}

	public void setLogoutTemplate(String logoutTemplate){
		String encodedLogoutTemplate = Base64.getEncoder().encodeToString(logoutTemplate.getBytes());
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.LOGOUT_TEMPLATE.getKey(), StringUtils.trimToEmpty(StringUtils
		.defaultIfBlank(encodedLogoutTemplate, StringUtils.EMPTY)));
	}

	public String getLogoutTemplate(){
		String encodedLogoutTemplate = (String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.LOGOUT_TEMPLATE.getKey());
		if(StringUtils.isNotBlank(encodedLogoutTemplate)){
			encodedLogoutTemplate = new String(Base64.getDecoder().decode(encodedLogoutTemplate));
		}
		return StringUtils.defaultIfBlank(encodedLogoutTemplate, LOGOUT_TEMPLATE);
	}

	public void setCustomLogoutURL(String customLogoutURL){
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.CUSTOM_LOGOUT_URL.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(customLogoutURL, StringUtils.EMPTY)));
	}

	public String getCustomLogoutURL(){
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.CUSTOM_LOGOUT_URL.getKey()), StringUtils.EMPTY);
	}

	public void setErrorMsgTemplate(String errorMsgTemplate){
		String encodedErrorMsgTemplate = Base64.getEncoder().encodeToString(errorMsgTemplate.getBytes());
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ERROR_MSG_TEMPLATE.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(encodedErrorMsgTemplate, StringUtils.EMPTY)));
	}

	public String getErrorMsgTemplate(){
		String encodedLogoutTemplate = (String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ERROR_MSG_TEMPLATE.getKey());
		if(StringUtils.isNotBlank(encodedLogoutTemplate)){
			encodedLogoutTemplate = new String(Base64.getDecoder().decode(encodedLogoutTemplate));
		}
		return StringUtils.defaultIfBlank(encodedLogoutTemplate, ERRORMSG_TEMPLATE);
	}

	public void setAccessToken(String accessToken) {
		this.pluginSettings.put(MoOAuthPluginConstants.ACCESS_TOKEN, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(accessToken, StringUtils.EMPTY)));
	}

	public void setConfigurationStatus(String status){
		this.pluginSettings.put(MoOAuthPluginConstants.CONFIGURATION_STATUS, StringUtils.defaultIfBlank(status, StringUtils.EMPTY));
	}

	public String getConfigurationStatus(){
		return (String)this.pluginSettings.get(MoOAuthPluginConstants.CONFIGURATION_STATUS);
	}

	public String getAccessToken() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.ACCESS_TOKEN), StringUtils.EMPTY);
	}

	public String getRefreshToken() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.REFRESH_TOKEN), StringUtils.EMPTY);
	}

	public void setRefreshToken(String refreshToken) {
		this.pluginSettings.put(MoOAuthPluginConstants.REFRESH_TOKEN, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(refreshToken, StringUtils.EMPTY)));
	}

	public void setReturnUrl(String returnUrl) {
		this.pluginSettings.put(MoOAuthPluginConstants.RETURN_URL, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(returnUrl, StringUtils.EMPTY)));
	}

	public String getReturnUrl() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.RETURN_URL), StringUtils.EMPTY);
	}

	public void setTenantID(String tenantID) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.TENANT_ID.getKey(), StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(tenantID, StringUtils.EMPTY)));
	}

	public String getTenantID() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.TENANT_ID.getKey()), StringUtils.EMPTY);
	}

	public void setUserInfoMap(HashMap<String, String> userInfoMap) {
		this.pluginSettings.put(MoOAuthPluginConstants.USER_INFO_MAP, userInfoMap);

	}

	public HashMap<String, String> getUserInfoMap() {
		Object userInfoMap = this.pluginSettings.get(MoOAuthPluginConstants.USER_INFO_MAP);
		if (userInfoMap != null) {
			return (HashMap) userInfoMap;
		}
		return new HashMap<>();
	}

	public String getSingleLogoutURL() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SINGLE_LOGOUT_URL),
				StringUtils.EMPTY);
	}

	public void setSingleLogoutURL(String singleLogoutURL) {
		this.pluginSettings.put(MoOAuthPluginConstants.SINGLE_LOGOUT_URL,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(singleLogoutURL, StringUtils.EMPTY)));
	}

	public String getPublicKey() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.PUBLIC_KEY.getKey()),
				StringUtils.EMPTY);
	}

	public void setPublicKey(String publicKey) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.PUBLIC_KEY.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(publicKey, StringUtils.EMPTY)));
	}
	
	public Boolean getNonceCheck() { 
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get 
				(MoOAuthPluginConstants.OAuthConfiguration.NONCE_VALIDATION.getKey())), false); 
	} 
 
	public void setNonceCheck(Boolean nonceCheck) { 
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.NONCE_VALIDATION.getKey(), BooleanUtils.toString(nonceCheck, 
				"true", "false", "false")); 
	}

	public Boolean getACRValueCheck() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get
				(MoOAuthPluginConstants.OAuthConfiguration.ACR_VALUE_CHECK.getKey())), false);
	}

	public void setACRValueCheck(Boolean acrValueCheck) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.ACR_VALUE_CHECK.getKey(), BooleanUtils.toString(acrValueCheck	,
				"true", "false", "false"));
	}
	
	public AdministrationConfigurationAccessor getAdministrationConfigurationAccessor() {
		return administrationConfigurationAccessor;
	}

	public void setAdministrationConfigurationAccessor(
			AdministrationConfigurationAccessor administrationConfigurationAccessor) {
		this.administrationConfigurationAccessor = administrationConfigurationAccessor;
	}

	public PluginLicenseManager getPluginLicenseManager() {
		return pluginLicenseManager;
	}

	public void setPluginLicenseManager(PluginLicenseManager pluginLicenseManager) {
		this.pluginLicenseManager = pluginLicenseManager;
	}

	public void setPluginSettingsFactory(PluginSettingsFactory pluginSettingsFactory) {
		this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
	}

	public PluginSettings getPluginSettings() {
		return pluginSettings;
	}

	public void setPluginSettings(PluginSettings pluginSettings) {
		this.pluginSettings = pluginSettings;
	}

    public PluginAccessor getPluginAccessor() {
        return pluginAccessor;
    }

    public void setPluginAccessor(PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
    }

	public BambooPermissionManager getBambooPermissionManager() {
		return bambooPermissionManager;
	}

	public void setBambooPermissionManager(BambooPermissionManager bambooPermissionManager) {
		this.bambooPermissionManager = bambooPermissionManager;
	}


	public String getPluginVersion(){
		String pluginKey = pluginLicenseManager.getPluginKey();
		Plugin plugin = pluginAccessor.getPlugin(pluginKey);
		return plugin.getPluginInformation().getVersion();
	}

	public String getPluginName(){
		String pluginKey = pluginLicenseManager.getPluginKey();
		Plugin plugin = pluginAccessor.getPlugin(pluginKey);
		return plugin.getName();
	}

	public String getServerVersion(){
		return BuildUtils.getCurrentVersion();
	}

	public void deleteKey(String constantKey) {
		this.pluginSettings.remove(constantKey);
	}
	
	public void clearPluginSettings() {
		this.pluginSettings.remove(MoOAuthPluginConstants.USER_INFO_MAP);
		this.pluginSettings.remove(MoOAuthPluginConstants.IS_TEST_CONFIGURATION_CLICKED);
		this.pluginSettings.remove(MoOAuthPluginConstants.IS_VERIFY_CREDENTIALS_CLICKED);
		this.pluginSettings.remove(MoOAuthPluginConstants.REFRESH_TOKEN);
		this.pluginSettings.remove(MoOAuthPluginConstants.ACCESS_TOKEN);
		this.pluginSettings.remove(MoOAuthPluginConstants.IS_CREDENTIALS_VERIFIED);
		this.pluginSettings.remove(MoOAuthPluginConstants.RETURN_URL);
		this.pluginSettings.remove(MoOAuthPluginConstants.GOOGLE_REFRESH_TOKEN);

		resetOAuthConfig();
		resetAttributeMapping();
		resetGroupMapping();
		resetSignOnSettings();
	}

	public void resetOAuthConfig() {
		LOGGER.debug("resetOAuthConfig called ");
		for(MoOAuthPluginConstants.OAuthConfiguration constant : MoOAuthPluginConstants.OAuthConfiguration.values()) {
			resetConfig(constant.getKey());
		}
	}
	public void resetAttributeMapping() {
		LOGGER.debug("resetAttributeMapping called ");
		for(MoOAuthPluginConstants.AttributeMapping constant : MoOAuthPluginConstants.AttributeMapping.values()) {
			resetConfig(constant.getKey());
		}
	}
	public void resetGroupMapping() {
		LOGGER.debug("resetGroupMapping called ");
		for(MoOAuthPluginConstants.GroupMapping constant : MoOAuthPluginConstants.GroupMapping.values()) {
			resetConfig(constant.getKey());
		}
	}
	public void resetSignOnSettings() {
		LOGGER.debug("resetSignOnSettings called ");
		for(MoOAuthPluginConstants.SignInSettings constant : MoOAuthPluginConstants.SignInSettings.values()) {
			resetConfig(constant.getKey());
		}
	}
	public void resetConfig(String constantKey){
		this.pluginSettings.remove(constantKey);
	}

	//group regex
	public Boolean getGroupRegexPatternEnabled() {
		return BooleanUtils.toBoolean(
				StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.GROUP_REGEX_PATTERN_ENABLED.getKey()),"false"));
	}

	public void setGroupRegexPatternEnabled(Boolean groupRegexPatternEnabled) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.GROUP_REGEX_PATTERN_ENABLED.getKey(),
				BooleanUtils.toString(groupRegexPatternEnabled, "true", "false", "false"));
	}

	public String getRegexPatternForGroup() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.REGEX_PATTERN_FOR_GROUP.getKey()), "");
	}

	public void setRegexPatternForGroup(String regexPatternForGroup) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.REGEX_PATTERN_FOR_GROUP.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(regexPatternForGroup, "")));
	}
	public String getRegexGroups() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.REGEX_GROUPS.getKey()), "");
	}

	public void setRegexGroups(String regexGroups) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.REGEX_GROUPS.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(regexGroups, "")));
	}
	public String getTestRegex() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.TEST_REGEX.getKey()), "");
	}

	public void setTestRegex(String testRegex) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.TEST_REGEX.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(testRegex, "")));
	}

}
