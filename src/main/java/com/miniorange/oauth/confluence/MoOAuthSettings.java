package com.miniorange.oauth.confluence;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.Plugin;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang3.RandomStringUtils;

import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.Contact;

public class MoOAuthSettings {

	private static Log LOGGER = LogFactory.getLog(MoOAuthSettings.class);
	private PluginSettings pluginSettings;
	private UserAccessor userAccessor;
	private PluginLicenseManager pluginLicenseManager;
	private PluginSettingsFactory pluginSettingsFactory;
	private PluginAccessor pluginAccessor;

	private static final String LOGOUT_TEMPLATE_PATH = "/template/com/miniorange/oauth/confluence/logoutTemplate.vm";
	private static final String ERRORMSG_TEMPLATE_PATH = "/template/com/miniorange/oauth/confluence/errorMessageTemplate.vm";
	private static final String LOGIN_TEMPLATE_PATH = "/template/com/miniorange/oauth/confluence/logintemplate.vm";
	public static String LOGOUT_TEMPLATE = "";
	public static String ERRORMSG_TEMPLATE = "";
	public static String LOGIN_TEMPLATE = "";
	public static HashMap<String, String> defaultErrorMappingMap;
	public static HashMap<String, String> defaultErrorMessagesMap;

	public static final String CUSTOMER_TOKEN_KEY = generateRandomAlphaNumericKey(16);

	static {
		try {
			LOGOUT_TEMPLATE = IOUtils.toString(MoOAuthSettings.class.getResourceAsStream(LOGOUT_TEMPLATE_PATH), "UTF-8");
			ERRORMSG_TEMPLATE = IOUtils.toString(MoOAuthSettings.class.getResourceAsStream(ERRORMSG_TEMPLATE_PATH),
					"UTF-8");
			LOGIN_TEMPLATE = IOUtils.toString(MoOAuthSettings.class.getResourceAsStream(LOGIN_TEMPLATE_PATH), "UTF-8");

			defaultErrorMappingMap = new HashMap<>();
			defaultErrorMappingMap.put("USER_DOMAIN_NOT_ALLOWED","Users from this domain are not allowed. Please contact your administrator for more details.");
			defaultErrorMappingMap.put("UNAUTHORIZED_USER_LOGIN","The user you were trying to authenticate is not authorized. Please contact your administrator for more details.");
			defaultErrorMappingMap.put("USER_NOT_FOUND","The user you were trying to authenticate does not exist in Confluence. Please contact your administrator for more details.");
			defaultErrorMappingMap.put("SSO_AUTHENTICATION_FAILED","SSO Authentication failed. Please contact your administrator for more details.");
			defaultErrorMessagesMap = new HashMap<>();
			defaultErrorMessagesMap.put("USER_DOMAIN_NOT_ALLOWED","Email domain not allowed");
			defaultErrorMessagesMap.put("UNAUTHORIZED_USER_LOGIN","The user you were trying to authenticate is not authorized.");
			defaultErrorMessagesMap.put("USER_NOT_FOUND","User not found in Confluence.");
			defaultErrorMessagesMap.put("SSO_AUTHENTICATION_FAILED","SSO Authentication Failed.");
		} catch (IOException e) {
			LOGGER.error("An I/O error occurred while initializing the OAuth Settings.", e);
		}
	}

	public MoOAuthSettings(PluginSettingsFactory pluginSettingsFactory, UserAccessor userAccessor, PluginLicenseManager pluginLicenseManager, PluginAccessor pluginAccessor){
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
		this.userAccessor = userAccessor;
		this.pluginLicenseManager = pluginLicenseManager;
		this.pluginAccessor = pluginAccessor;
	}



	public String getBaseUrl() {
		SettingsManager settingsManager = (SettingsManager) ContainerManager.getComponent("settingsManager");
		return settingsManager.getGlobalSettings().getBaseUrl();
	}

	public String getLoginPageUrl() {
		return getBaseUrl().concat("/login.action");
	}

	public String getAdminSessionUrl() {
		try {
			return getLoginPageUrl().concat("?oauth_sso=false&os_destination=")
					.concat(URLEncoder.encode(getBaseUrl().concat("/authenticate.action?oauth_sso=false"), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage());
		}
		return StringUtils.EMPTY;
	}

	public String getCallBackUrl() {
		return getBaseUrl().concat("/plugins/servlet/oauth/callback");
	}

	public String getAutoCallBackUrl(){
		return getBaseUrl().concat("/plugins/servlet/oauth/autocallback");
	}

	public String getAutoLoginURL(){
		return getBaseUrl().concat("/plugins/servlet/oauth/autoAuth");
	}

	public String getCustomLogoutTemplateUrl() {
		return getBaseUrl().concat("/plugins/servlet/oauth/logout");
	}

	public Boolean getIsTestIDPConfigurationClicked() {
		String isTestIDPConfigurationClicked = (String) this.pluginSettings
				.get(MoOAuthPluginConstants.IS_TEST_CONFIGURATION_CLICKED);
		if (BooleanUtils.toBoolean(isTestIDPConfigurationClicked)) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	public void setIsTestIDPConfigurationClicked(Boolean isTestIDPConfigurationClicked) {

		this.pluginSettings.put(MoOAuthPluginConstants.IS_TEST_CONFIGURATION_CLICKED,
				BooleanUtils.toString(isTestIDPConfigurationClicked, "true", "false", "false"));
	}

	public Boolean getIsVerifyCredentialsClicked() {
		String isVerifyCredentialsClicked = (String) this.pluginSettings
				.get(MoOAuthPluginConstants.IS_VERIFY_CREDENTIALS_CLICKED);
		if (BooleanUtils.toBoolean(isVerifyCredentialsClicked)) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	public void setIsVerifyCredentialsClicked(Boolean isVerifyCredentialsClicked) {

		this.pluginSettings.put(MoOAuthPluginConstants.IS_VERIFY_CREDENTIALS_CLICKED,
				BooleanUtils.toString(isVerifyCredentialsClicked, "true", "false", "false"));
	}

	public Boolean getIsCredentialsVerified() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils
				.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.IS_CREDENTIALS_VERIFIED)), false);
	}

	public void setIsCredentialsVerified(Boolean isCredentialsVerified) {
		this.pluginSettings.put(MoOAuthPluginConstants.IS_CREDENTIALS_VERIFIED,
				BooleanUtils.toString(isCredentialsVerified, "true", "false", "false"));
	}

	public String getRefreshToken() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.REFRESH_TOKEN),
				StringUtils.EMPTY);
	}

	public void setRefreshToken(String refreshToken) {
		this.pluginSettings.put(MoOAuthPluginConstants.REFRESH_TOKEN,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(refreshToken, StringUtils.EMPTY)));
	}

	public String getGoogleRefreshToken() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GOOGLE_REFRESH_TOKEN),
				StringUtils.EMPTY);
	}

	public void setGoogleRefreshToken(String refreshToken) {
		this.pluginSettings.put(MoOAuthPluginConstants.GOOGLE_REFRESH_TOKEN,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(refreshToken, StringUtils.EMPTY)));
	}

	public String getFetchGroupsEndpoint() {
		return StringUtils.defaultIfBlank(
				(String) this.pluginSettings.get(MoOAuthPluginConstants.FETCH_GROUPS_ENDPOINT), StringUtils.EMPTY);
	}

	public void setFetchGroupsEndpoint(String groupApi) {
		this.pluginSettings.put(MoOAuthPluginConstants.FETCH_GROUPS_ENDPOINT,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(groupApi, StringUtils.EMPTY)));
	}

	public Boolean getUseStateParameter() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.USE_STATE_PARAMETER)),
				false);
	}

	public void setUseStateParameter(Boolean useStateParameter) {
		this.pluginSettings.put(MoOAuthPluginConstants.USE_STATE_PARAMETER,
				BooleanUtils.toString(useStateParameter, "true", "false", "false"));
	}
	
	public Boolean getAcrValue() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get
				(MoOAuthPluginConstants.OAuthConfiguration.ACR_VALUE_CHECK.getKey())), false);
	}

	public void setAcrValue(Boolean acrValueCheck) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.ACR_VALUE_CHECK.getKey(), BooleanUtils.toString(acrValueCheck,
				"true", "false", "false"));
	}
	
	public Boolean getNonceCheck() { 
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get 
				(MoOAuthPluginConstants.OAuthConfiguration.NONCE_CHECK.getKey())), false); 
	} 
 
	public void setNonceCheck(Boolean nonceCheck) { 
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.NONCE_CHECK.getKey(), BooleanUtils.toString(nonceCheck, 
				"true", "false", "false")); 
	} 
	
	public void setCustomCallbackParameter(String customCallbackParameter) {  
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.CUSTOM_CALLBACK_PARAMETER.getKey(),  
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(customCallbackParameter, StringUtils.EMPTY)));  
	}  
  
	public String getCustomCallbackParameter() {  
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.CUSTOM_CALLBACK_PARAMETER.getKey()),  
				StringUtils.EMPTY);  
	}  

	public String getAppName() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.DEFAULT_APP_NAME.getKey()),
				StringUtils.EMPTY);
	}

	public void setAppName(String appName) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.DEFAULT_APP_NAME.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(appName, StringUtils.EMPTY)));
	}

	public void setAccessToken(String accessToken) {
		this.pluginSettings.put(MoOAuthPluginConstants.ACCESS_TOKEN,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(accessToken, StringUtils.EMPTY)));
	}

	public String getAccessToken() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.ACCESS_TOKEN),
				StringUtils.EMPTY);
	}

	public String getCustomAppName() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.CUSTOM_APP_NAME.getKey()), "");
	}

	public void setCustomAppName(String customAppName) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.CUSTOM_APP_NAME.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(customAppName, "")));
	}

	public String getSendTokenAuthParameterIn() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SEND_TOKEN_AUTHORIZATION_PARAMETERS_IN),
				"HttpHeader");
	}

	public void setSendTokenAuthParameterIn(String sendTokenAuthParameterIn) {
		this.pluginSettings.put(MoOAuthPluginConstants.SEND_TOKEN_AUTHORIZATION_PARAMETERS_IN,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(sendTokenAuthParameterIn, "HttpHeader")));
	}
	
	

	public String getClientID() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.CLIENT_ID.getKey()), "");
	}

	public void setClientID(String clientID) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.CLIENT_ID.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(clientID, "")));
	}

	public String getClientSecret() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.CLIENT_SECRET.getKey()), "");
	}

	public void setClientSecret(String clientSecret) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.CLIENT_SECRET.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(clientSecret, "")));
	}

	public void setTenantID(String tenantID) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.TENANT_ID.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(tenantID, StringUtils.EMPTY)));
	}

	public String getTenantID() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.TENANT_ID.getKey()),
				StringUtils.EMPTY);
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

	public String getAuthorizeEndpoint() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.AUTHORIZE_END_POINT.getKey()),
				"");
	}

	public void setAuthorizeEndpoint(String authorizeEndpoint) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.AUTHORIZE_END_POINT.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(authorizeEndpoint, "")));
	}

	public String getAccessTokenEndpoint() {
		return StringUtils
				.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.ACCESSTOKEN_END_POINT.getKey()), "");
	}

	public void setAccessTokenEndpoint(String accessTokenEndpoint) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.ACCESSTOKEN_END_POINT.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(accessTokenEndpoint, "")));
	}

	public String getUserInfoEndpoint() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.USERINFO_END_POINT.getKey()),
				"");
	}

	public void setUserInfoEndpoint(String userInfoEndpoint) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.USERINFO_END_POINT.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(userInfoEndpoint, "")));
	}

	public String getScope() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.SCOPE.getKey()), "");
	}

	public void setScope(String scope) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.SCOPE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(scope, "")));
	}

	public String getPublicKey() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.PUBLIC_KEY.getKey()),
				StringUtils.EMPTY);
	}

	public void setPublicKey(String publicKey) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.PUBLIC_KEY.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(publicKey, StringUtils.EMPTY)));
	}
	public void setjWKSEndpointURL(String jWKSEndpointURL) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.JWKS_END_POINT.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(jWKSEndpointURL, StringUtils.EMPTY)));
	}

	public void setValidateSignatureMethod(String validateSignatureMethod) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.VALIDATE_SIGNATURE_METHOD.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(validateSignatureMethod, StringUtils.EMPTY)));
	}

	public String getjWKSEndpointURL() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.JWKS_END_POINT.getKey()),
				StringUtils.EMPTY);
	}

	public String getValidateSignatureMethod() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.VALIDATE_SIGNATURE_METHOD.getKey()),
				StringUtils.EMPTY);
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

	public String getEmailAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.EMAIL_ATTRIBUTE.getKey()),
				StringUtils.EMPTY);
	}

	public void setEmailAttribute(String emailAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.EMAIL_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(emailAttribute, StringUtils.EMPTY)));
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

	public String getDefaultGroupForInitialConfiguration() {
		SettingsManager settingsManager = (SettingsManager) ContainerManager.getComponent("settingsManager");
		String defaultGroup = settingsManager.getGlobalSettings().getDefaultUsersGroup();
		return defaultGroup;
	}

	public String getDefaultGroup() {
		String defaultGroup = StringUtils
				.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.DEFAULT_GROUP.getKey()), "");
		if (StringUtils.isBlank(defaultGroup) || StringUtils.isEmpty(defaultGroup)) {
			defaultGroup = getDefaultGroupForInitialConfiguration();
			setDefaultGroup(defaultGroup);
		}
		return defaultGroup;
	}

	public void setDefaultGroup(String suppliedDefaultGroup) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.DEFAULT_GROUP.getKey(), StringUtils.trimToEmpty(suppliedDefaultGroup));
	}

	public Object getDefaultGroups() {
		if (this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.DEFAULT_GROUPS.getKey()) == null) {
			List<String> defaultGroups = new ArrayList<String>();
			defaultGroups.add(getDefaultGroupForInitialConfiguration());
			return defaultGroups;
		}
		return this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.DEFAULT_GROUPS.getKey());
	}

	public void setDefaultGroups(List<String> defaultGroups) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.DEFAULT_GROUPS.getKey(), defaultGroups);
	}

	public void setEnableDefaultGroupsFor(String enableDefaultGroupsFor) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ENABLE_DEFAULT_GROUPS_FOR.getKey(), StringUtils
				.defaultIfEmpty(enableDefaultGroupsFor, MoOAuthPluginConstants.GroupMapping.ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS.getKey()));
	}

	public String getEnableDefaultGroupsFor() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ENABLE_DEFAULT_GROUPS_FOR.getKey()),
				MoOAuthPluginConstants.GroupMapping.ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS.getKey());
	}



	public void setPluginSettingsFactory(PluginSettingsFactory pluginSettingsFactory) {
		this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
	}

	public void setLoginUserAttribute(String loginUserAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.LOGIN_USER_BY_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(loginUserAttribute, StringUtils.EMPTY)));
	}

	public String getLoginUserAttribute() {
		return StringUtils.defaultIfBlank(
				(String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.LOGIN_USER_BY_ATTRIBUTE.getKey()), StringUtils.EMPTY);
	}

	public String getUsernameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.USERNAME_ATTRIBUTE.getKey()),
				StringUtils.EMPTY);
	}

	public void setUsernameAttribute(String usernameAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.USERNAME_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(usernameAttribute, StringUtils.EMPTY)));
	}

	public Boolean getUseSeparateNameAttributes() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean(
				(String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.USE_SEPARATE_NAME_ATTRIBUTE.getKey())), false);
	}

	public void setUseSeparateNameAttributes(Boolean useSeparateNameAttributes) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.USE_SEPARATE_NAME_ATTRIBUTE.getKey(),
				BooleanUtils.toString(useSeparateNameAttributes, "true", "false", "false"));
	}

	public String getFirstNameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.FIRST_NAME_ATTRIBUTE.getKey()),
				StringUtils.EMPTY);
	}

	public void setFirstNameAttribute(String firstNameAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.FIRST_NAME_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(firstNameAttribute, StringUtils.EMPTY)));
	}

	public String getLastNameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.LAST_NAME_ATTRIBUTE.getKey()),
				StringUtils.EMPTY);
	}

	public void setLastNameAttribute(String lastNameAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.LAST_NAME_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(lastNameAttribute, StringUtils.EMPTY)));
	}

	public String getFullNameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.FULL_NAME_ATTRIBUTE.getKey()),
				"");
	}

	public void setFullNameAttribute(String fullNameAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.FULL_NAME_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(fullNameAttribute, StringUtils.EMPTY)));
	}

	public HashMap<String, String> getCustomAttributeMapping() {
		Object customAttributeMapping = this.pluginSettings.get(MoOAuthPluginConstants.AttributeMapping.CUSTOM_ATTRIBUTE_MAPPING.getKey());
		if (customAttributeMapping != null) {
			LOGGER.debug("customAttributeMapping is not empty : "+ customAttributeMapping.toString());
			HashMap<String, String> resCustomAttributeMapping = (HashMap<String, String>) customAttributeMapping;
			return resCustomAttributeMapping;
		}
		return new HashMap<String, String>();
	}

	public void setCustomAttributeMapping(HashMap<String, String> customAttributeMapping) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.CUSTOM_ATTRIBUTE_MAPPING.getKey(), customAttributeMapping);
	}
	public void setOauth_request_parameters(HashMap<String, String> oauth_request_parameters) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.OAUTH_REQUEST_PARAMETERS.getKey(), oauth_request_parameters);
	}
	
	public HashMap<String, String> getOauth_request_parameters() {
		Object oauth_request_parameters = this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.OAUTH_REQUEST_PARAMETERS.getKey());
		LOGGER.debug("oauth_request_parameters : "+ oauth_request_parameters);
		if (oauth_request_parameters!=null && StringUtils.isNotEmpty(oauth_request_parameters.toString())) {
			LOGGER.debug("oauth_request_parameters is not null..");
			HashMap<String, String> resoauth_request_parameters = (HashMap<String, String>) oauth_request_parameters;
			return resoauth_request_parameters;
		}
		return new HashMap<String, String>();
			
	}
	
	public ArrayList<String> getExtendedAttributes() {
		ArrayList<String> apps = new ArrayList<>();
		apps.add("Phone");
		apps.add("IM");
		apps.add("Website");
		apps.add("Position");
		apps.add("Department");
		apps.add("Location");
		return apps;
	}

	public Boolean getKeepExistingUserRoles() {
		String KeepExistingUserRoles = (String) this.pluginSettings
				.get(MoOAuthPluginConstants.GroupMapping.KEEP_EXISTING_USER_ROLES.getKey());
		if (StringUtils.isBlank(KeepExistingUserRoles)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(KeepExistingUserRoles);
		}

	}

	public void setKeepExistingUserRoles(Boolean keepExistingUserRoles) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.KEEP_EXISTING_USER_ROLES.getKey(),
				BooleanUtils.toString(keepExistingUserRoles, "true", "false", "false"));
	}

	public Boolean getKeepExistingUserAttributes() {
		String KeepExistingUserAttributes = (String) this.pluginSettings
				.get(MoOAuthPluginConstants.AttributeMapping.KEEP_EXISTING_USER_ATTRIBUTES.getKey());
		if (StringUtils.isBlank(KeepExistingUserAttributes)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(KeepExistingUserAttributes);
		}

	}

	public void setKeepExistingUserAttributes(Boolean keepExistingUserAttributes) {
		this.pluginSettings.put(MoOAuthPluginConstants.AttributeMapping.KEEP_EXISTING_USER_ATTRIBUTES.getKey(),
				BooleanUtils.toString(keepExistingUserAttributes, "true", "false", "false"));
	}

	public String getSingleLogoutURL() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.SINGLE_LOGOUT_URL.getKey()),
				StringUtils.EMPTY);
	}


	public void setSingleLogoutURL(String singleLogoutURL) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.SINGLE_LOGOUT_URL.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(singleLogoutURL, StringUtils.EMPTY)));
	}

	public Boolean getCreateUsersIfRoleMapped() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils
				.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.CREATE_USER_IF_ROLE_MAPPED.getKey())), false);
	}

	public void setCreateUsersIfRoleMapped(Boolean createUsersIfRoleMapped) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.CREATE_USER_IF_ROLE_MAPPED.getKey(),
				BooleanUtils.toString(createUsersIfRoleMapped, "true", "false", "false"));
	}

	public void setRestrictUserCreation(Boolean restrictUserCreation) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.RESTRICT_USER_CREATION.getKey(),
				BooleanUtils.toString(restrictUserCreation, "true", "false", "false"));

	}

	public Boolean getRestrictUserCreation() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.RESTRICT_USER_CREATION.getKey())),
				false);

	}

	public String getRoleAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ROLE_ATTRIBUTE.getKey()), "");
	}

	public void setRoleAttribute(String roleAttribute) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ROLE_ATTRIBUTE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(roleAttribute, "")));
	}

	public String getOnTheFlyFilterIDPGroupsOption(){
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(
				MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_FILTER_IDP_GROUPS_OPTION.getKey()),MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER);
	}

	public void setOnTheFlyFilterIDPGroupsOption(String onTheFlyFilterIDPGroupsOption){
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_FILTER_IDP_GROUPS_OPTION.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(onTheFlyFilterIDPGroupsOption, MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER)));
	}

	public String getOnTheFlyFilterIDPGroupsKey(){
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(
				MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_FILTER_IDP_GROUPS_KEY.getKey()),StringUtils.EMPTY);
	}

	public void setOnTheFlyFilterIDPGroupsKey(String onTheFlyFilterIDPGroupsKey){
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_FILTER_IDP_GROUPS_KEY.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(onTheFlyFilterIDPGroupsKey, StringUtils.EMPTY)));
	}

	public String getGroupRegexPattern() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.GROUP_REGEX_PATTERN.getKey()),
				MoOAuthPluginConstants.DEFAULT_GROUPS_REGEX_PATTERN);
	}

	public void setGroupRegexPattern(String regexPattern) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.GROUP_REGEX_PATTERN.getKey(), StringUtils.trimToEmpty(
				StringUtils.defaultIfBlank(regexPattern, MoOAuthPluginConstants.DEFAULT_GROUPS_REGEX_PATTERN)));
	}

	public HashMap<String, String> getRoleMapping() {
		Object roleMapping = this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ROLE_MAPPING.getKey());
		if (roleMapping != null) {
			return (HashMap) roleMapping;
		}
		return new HashMap<>();
	}

	public void setRoleMapping(HashMap<String, String> roleMapping) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ROLE_MAPPING.getKey(), roleMapping);
	}

	public void setReturnUrl(String returnUrl) {
		this.pluginSettings.put(MoOAuthPluginConstants.RETURN_URL,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(returnUrl, StringUtils.EMPTY)));
	}

	public String getReturnUrl() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.RETURN_URL),
				StringUtils.EMPTY);
	}

	public Boolean getEnableOAuthSSO() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils
				.toBooleanObject((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ENABLE_OAUTH_SSO.getKey())), true);
	}

	public void setEnableOAuthSSO(Boolean enableOAuthSSO) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ENABLE_OAUTH_SSO.getKey(),
				BooleanUtils.toString(enableOAuthSSO, "true", "false", "false"));
	}

	public String getLoginButtonText() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.LOGIN_BUTTON_TEXT.getKey()),
				"Use OAuth Login");
	}

	public void setLoginButtonText(String loginButtonText) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.LOGIN_BUTTON_TEXT.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(loginButtonText, "Use OAuth Login")));
	}

	public void setRelayState(String relayState) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.RELAY_STATE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(relayState, StringUtils.EMPTY)));
	}

	public String getRelayState() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.RELAY_STATE.getKey()),
				StringUtils.EMPTY);
	}

	public void setAllowedDomains(String allowedDomains) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ALLOWED_DOMAINS.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(allowedDomains, StringUtils.EMPTY)));
	}

	public String getAllowedDomains() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ALLOWED_DOMAINS.getKey()),
				StringUtils.EMPTY);
	}

	public String getCustomLogoutURL() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.CUSTOM_LOGOUT_URL.getKey()),
				StringUtils.EMPTY);
	}

	public void setCustomLogoutURL(String customLogoutURL) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.CUSTOM_LOGOUT_URL.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(customLogoutURL, StringUtils.EMPTY)));
	}

	public Boolean getEnablelogoutTemplate() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.ENABLE_LOGOUT_TEMPLATE)),
				false);
	}

	public void setEnablelogoutTemplate(Boolean enablelogoutTemplate) {
		this.pluginSettings.put(MoOAuthPluginConstants.ENABLE_LOGOUT_TEMPLATE,
				BooleanUtils.toString(enablelogoutTemplate, "true", "false", "false"));
	}

	public String getLogoutTemplate() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.LOGOUT_TEMPLATE),
				LOGOUT_TEMPLATE);
	}

	public void setLogoutTemplate(String logoutTemplate) {
		this.pluginSettings.put(MoOAuthPluginConstants.LOGOUT_TEMPLATE,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(logoutTemplate, LOGOUT_TEMPLATE)));
	}

	public Boolean getEnableLoginTemplate() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ENABLE_LOGIN_TEMPLATE.getKey())),
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

	public String getErrorMsgTemplate() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.ERROR_MSG_TEMPLATE),
				ERRORMSG_TEMPLATE);
	}

	public void setErrorMsgTemplate(String errorMsgTemplate) {
		this.pluginSettings.put(MoOAuthPluginConstants.ERROR_MSG_TEMPLATE,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(errorMsgTemplate, ERRORMSG_TEMPLATE)));
	}

	public Boolean getEnableErrorMsgTemplate() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.ENABLE_ERRORMSG_TEMPLATE)),
				false);
	}

	public void setEnableErrorMsgTemplate(Boolean enableErrorMsgTemplate) {

		this.pluginSettings.put(MoOAuthPluginConstants.ENABLE_ERRORMSG_TEMPLATE,
				BooleanUtils.toString(enableErrorMsgTemplate, "true", "false", "false"));
	}

	public Boolean getAutoActivateUser() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.AUTO_ACTIVATE_USER.getKey()));
	}

	public void setAutoActivateUser(Boolean autoActivateUser) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.AUTO_ACTIVATE_USER.getKey(),
				BooleanUtils.toString(autoActivateUser, "true", "false", "false"));
	}

	public Boolean getCheckIDPSession() {
		String appName = getAppName();
		if(StringUtils.equalsIgnoreCase(appName, "Azure AD") || StringUtils.equalsIgnoreCase(appName, "Azure B2C") || StringUtils.equalsIgnoreCase(appName, "Keycloak")) {
			return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils
					.toBooleanObject((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.CHECK_IDP_SESSION.getKey())), false);
		}
		else {
			return false;
		}
	}

	public void setCheckIDPSession(Boolean checkIDPSession) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.CHECK_IDP_SESSION.getKey(),
				BooleanUtils.toString(checkIDPSession, "true", "false", "false"));
	}


	public Boolean getBackdoorEnabled() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ENABLE_BACKDOOR.getKey()));
	}

	public void setBackdoorEnabled(Boolean backdoorEnabled) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ENABLE_BACKDOOR.getKey(),
				BooleanUtils.toString(backdoorEnabled, "true", "false", "false"));
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
			return new ArrayList<>();
		}

		return backdoorGroups;
	}

	public void setBackdoorGroups(List<String> backdoorGroups) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.BACKDOOR_GROUPS.getKey(), backdoorGroups);
	}

	/**
	 * Fetch the enableAutoRedirectDelay option saved by the admin from the database
	 * or file system
	 */
	public Boolean getEnableAutoRedirectDelay() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ENABLE_AUTO_REDIRECT_DELAY.getKey())),
				false);
	}

	/**
	 * Update the enableAutoRedirectDelay option saved by the admin in the database
	 * or file system
	 */
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


	public String getSsoButtonLocation() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.SSO_BUTTON_LOCATION.getKey()),
				"After Login Button");

	}

	public void setSsoButtonLocation(String ssoButtonLocation) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.SSO_BUTTON_LOCATION.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(ssoButtonLocation, "After Login Button")));

	}

	public Boolean getPluginApiAccessRestriction() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.PLUGIN_API_ACCESS_RESTRICTION.getKey()));
	}

	public void setPluginApiAccessRestriction(Boolean pluginApiAccessRestriction) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.PLUGIN_API_ACCESS_RESTRICTION.getKey(),
				BooleanUtils.toString(pluginApiAccessRestriction, "true", "false", "false"));
	}

	public Boolean getDefaultLoginDisabled() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.DISABLE_DEFAULT_LOGIN.getKey())),
				false);
	}

	public void setDefaultLoginDisabled(Boolean defaultLoginDisabled) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.DISABLE_DEFAULT_LOGIN.getKey(),
				BooleanUtils.toString(defaultLoginDisabled, "true", "false", "false"));
	}

	public Boolean getDisableAnonymousAccess() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.DISABLE_ANONYMOUS_ACCESS.getKey())),
				false);
	}

	public void setDisableAnonymousAccess(Boolean disableAnonymousAccess) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.DISABLE_ANONYMOUS_ACCESS.getKey(),
				BooleanUtils.toString(disableAnonymousAccess, "true", "false", "false"));
	}

	public Boolean getAllowGuestLogin() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ALLOW_GUEST_LOGIN.getKey())),
				false);
	}

	public void setAllowGuestLogin(Boolean allowGuestLogin) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ALLOW_GUEST_LOGIN.getKey(),
				BooleanUtils.toString(allowGuestLogin, "true", "false", "false"));
	}

	public String getGuestSessionTimeout() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.GUEST_SESSION_TIMEOUT.getKey()),
				"300");
	}

	public void setGuestSessionTimeout(String guestSessionTimeout) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.GUEST_SESSION_TIMEOUT.getKey(),guestSessionTimeout);
	}

	public static String generateRandomAlphaNumericKey(int bytes) {
		String randomString = RandomStringUtils.random(bytes, true, true);
		return randomString;
	}

	public String getCustomerTokenKey() {
		return StringUtils.defaultString((String) CUSTOMER_TOKEN_KEY);
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

	public String getContactEmail() {
		if (pluginLicenseManager.getLicense().isDefined()) {
			Iterator<Contact> itr = pluginLicenseManager.getLicense().get().getContacts().iterator();
			String email = null;
			if (itr.hasNext()) {
				email = itr.next().getEmail();
			}
			return email;
		}
		return StringUtils.EMPTY;
	}

	public Boolean isEvaluationOrSubscriptionLicense() {
		try {
			if (pluginLicenseManager.getLicense().isDefined()) {
				if (pluginLicenseManager.getLicense().get().isEvaluation()
						|| (pluginLicenseManager.getLicense().get().isSubscription())) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return false;
		}
	}

	public UserAccessor getUserAccessor() {
		return userAccessor;
	}

	public void setUserAccessor(UserAccessor userAccessor) {
		this.userAccessor = userAccessor;
	}

	public String getAdminSessionOption() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ADMIN_SESSION_OPTION.getKey()),
				StringUtils.EMPTY);
	}

	public void setAdminSessionOption(String adminSessionOption) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ADMIN_SESSION_OPTION.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(adminSessionOption, StringUtils.EMPTY)));

	}
	
	public HashMap<String, String> getUserSessionTimeout() {
		Object userSessionTimeout = this.pluginSettings.get(MoOAuthPluginConstants.SessionManagement.USER_SESSION_TIMEOUT.getKey());
		if (userSessionTimeout != null) {
			return (HashMap) userSessionTimeout;
		}
		return new HashMap<String, String>();
	}

	public void setUserSessionTimeout(HashMap<String, String> userSessionTimeout) {
		this.pluginSettings.put(MoOAuthPluginConstants.SessionManagement.USER_SESSION_TIMEOUT.getKey(), userSessionTimeout);
	}
	
	public String getLoginCookie() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.SessionManagement.LOGIN_COOKIE.getKey()),
				"JSESSIONID");
	}

	public void setLoginCookie(String loginCookie) {
		this.pluginSettings.put(MoOAuthPluginConstants.SessionManagement.LOGIN_COOKIE.getKey(),
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(loginCookie, "JSESSIONID")));
	}

	public Boolean isLicenseValid() {
		if (pluginLicenseManager.getLicense().isDefined()) {
			if ((!(pluginLicenseManager.getLicense().get().isValid()))
					|| (pluginLicenseManager.getLicense().get().getError().isDefined())) {
				LOGGER.debug("Invalid License");
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	public int getLicensedUsers() {
		int t = 0;
		try {
			t = userAccessor.countLicenseConsumingUsers();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return t;
	}

	public int getMaxUsers() {
		int t = 0;
		try {
			if (pluginLicenseManager.getLicense().isDefined()) {
				t = pluginLicenseManager.getLicense().get().getEdition().get();
			}
		} catch (Exception e) {
			LOGGER.info("Unlimited Number of Users");
			return -1;
		}
		return t;
	}

	public PluginLicenseManager getPluginLicenseManager() {
		return pluginLicenseManager;
	}

	public void setPluginLicenseManager(PluginLicenseManager pluginLicenseManager) {
		this.pluginLicenseManager = pluginLicenseManager;
	}

	public String getConfigureActionUrl() {
		return getBaseUrl().concat("/admin/plugins/confluence-oauth/configure.action");
	}

	public Boolean getResetSettings() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.RESET_SETTINGS)), false);
	}

	public void setResetSettings(Boolean resetSettings) {
		// TODO Auto-generated method stub
		this.pluginSettings.put(MoOAuthPluginConstants.RESET_SETTINGS,
				BooleanUtils.toString(resetSettings, "true", "false", "false"));

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
	public PluginSettings getPluginSettings() {
		return pluginSettings;
	}

	public void setPluginSettings(PluginSettings pluginSettings) {
		this.pluginSettings = pluginSettings;
	}

	public void deleteKey(String constantKey) {
		this.pluginSettings.remove(constantKey);
	}

	public String getUPMURL() {
		return getBaseUrl().concat("/plugins/servlet/upm");
	}

	public Boolean isLicenseDefine() {
		if (pluginLicenseManager.getLicense().isDefined()) {
			return true;
		}
		return false;
	}

	public Boolean getShowRestApiMsg() {
		String showRestApiMsg = (String) this.pluginSettings.get(MoOAuthPluginConstants.OAuthConfiguration.SHOW_REST_API_MSG.getKey());
		if (StringUtils.isBlank(showRestApiMsg)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(showRestApiMsg);
		}
	}

	public void setShowRestApiMsg(Boolean showRestApiMsg) {
		this.pluginSettings.put(MoOAuthPluginConstants.OAuthConfiguration.SHOW_REST_API_MSG.getKey(),
				BooleanUtils.toString(showRestApiMsg, "true", "false", "false"));
	}

	public void clearPluginSettings() {
		this.pluginSettings.remove(MoOAuthPluginConstants.IS_TEST_CONFIGURATION_CLICKED);
		this.pluginSettings.remove(MoOAuthPluginConstants.IS_VERIFY_CREDENTIALS_CLICKED);
		this.pluginSettings.remove(MoOAuthPluginConstants.IS_CREDENTIALS_VERIFIED);
		this.pluginSettings.remove(MoOAuthPluginConstants.REFRESH_TOKEN);
		this.pluginSettings.remove(MoOAuthPluginConstants.FETCH_GROUPS_ENDPOINT);
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.OAUTH_REQUEST_PARAMETERS.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.DEFAULT_APP_NAME.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.ACCESS_TOKEN);
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.CUSTOM_APP_NAME.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.CLIENT_ID.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.CLIENT_SECRET.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.TENANT_ID.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.DOMAIN_NAME.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.AUTHORIZE_END_POINT.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.ACCESSTOKEN_END_POINT.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.USERINFO_END_POINT.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.SCOPE.getKey());
        this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.HOST_NAME.getKey());
        this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.REALM_NAME.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.ACR_VALUE_CHECK.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.AttributeMapping.EMAIL_ATTRIBUTE.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.DEFAULT_GROUP.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.DEFAULT_GROUPS.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.AttributeMapping.LOGIN_USER_BY_ATTRIBUTE.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.AttributeMapping.USERNAME_ATTRIBUTE.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.AttributeMapping.USE_SEPARATE_NAME_ATTRIBUTE.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.AttributeMapping.FIRST_NAME_ATTRIBUTE.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.AttributeMapping.LAST_NAME_ATTRIBUTE.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.AttributeMapping.FULL_NAME_ATTRIBUTE.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.AttributeMapping.CUSTOM_ATTRIBUTE_MAPPING.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.KEEP_EXISTING_USER_ROLES.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.AttributeMapping.KEEP_EXISTING_USER_ATTRIBUTES.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.CREATE_USER_IF_ROLE_MAPPED.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.RESTRICT_USER_CREATION.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.ROLE_ATTRIBUTE.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.ROLE_MAPPING.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.RETURN_URL);
		this.pluginSettings.remove(MoOAuthPluginConstants.SignInSettings.ENABLE_OAUTH_SSO.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.SignInSettings.LOGIN_BUTTON_TEXT.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.SignInSettings.RELAY_STATE.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.SignInSettings.ALLOWED_DOMAINS.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.SignInSettings.CUSTOM_LOGOUT_URL.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.ENABLE_LOGOUT_TEMPLATE);
		this.pluginSettings.remove(MoOAuthPluginConstants.LOGOUT_TEMPLATE);
		this.pluginSettings.remove(MoOAuthPluginConstants.SignInSettings.ENABLE_BACKDOOR.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.SignInSettings.DISABLE_DEFAULT_LOGIN.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.SignInSettings.RESTRICT_BACKDOOR.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.SignInSettings.BACKDOOR_GROUPS.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.SignInSettings.ADMIN_SESSION_OPTION.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.USE_STATE_PARAMETER);
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.ENABLE_CHECK_ISSUER_FOR.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.CHECK_ISSUER_FOR.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.CUSTOM_ISSUER_VALUE.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.GROUP_REGEX_PATTERN.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.PUBLIC_KEY.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GOOGLE_REFRESH_TOKEN);
		this.pluginSettings.remove(MoOAuthPluginConstants.AttributeMapping.REGEX_PATTERN.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.AttributeMapping.ENABLE_REGEX_PATTERN.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.SignInSettings.ENABLE_AUTO_REDIRECT_DELAY.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.SINGLE_LOGOUT_URL.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.OAuthConfiguration.SHOW_REST_API_MSG.getKey());
		//Clear Error message Template Configuration
		this.pluginSettings.remove(MoOAuthPluginConstants.ENABLE_ERRORMSG_TEMPLATE);
		this.pluginSettings.remove(MoOAuthPluginConstants.ERROR_MSG_TEMPLATE);
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.ENABLE_DEFAULT_GROUPS_FOR.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_GROUP_MAPPING.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_DO_NOT_REMOVE_GROUPS.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.GroupMapping.CREATE_NEW_GROUPS.getKey());
		this.pluginSettings.remove(MoOAuthPluginConstants.REMEMBERME_COOKIE);

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

	/*public String getDefaultLogoutTemplate() {
		String logoutTemplate = "<html>" + "<head>" + "<title>Logout</title>"
				+ "$webResourceManager.requireResource('$pluginproperties.pluginkey:resources')"
				+ "<meta name='decorator' content='atl.general'>" + "</head>"
				+ "<body class='aui-layout aui-theme-default page-type-login' >" + "<div class='aui-page-panel'>"
				+ "<div class='aui-page-panel-inner'>" + "<section id=\"content\" role=\"main\">"
				+ "<header class=\"aui-page-header\" style=\"background-color:#f5f5f5 !important;\"><h1>You have logged out successfully</h1></header>"
				+ "<div class='aui-message info'>" + "<span class='aui-icon icon-info'></span>"
				+ "<p class='title'>You are now logged out. Any automatic login has also been stopped.</p>"
				+ "<p>Didn't mean to log out?<a href='$baseUrl'> Log in again.</a><p>" + "</div>" + "</section>"
				+ "</div>" + "</div>" + "</body>" + "</html>";
		return logoutTemplate;
	}*/
	public void setOnTheFlyGroupMapping(Boolean createUsersIfRoleMapped) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_GROUP_MAPPING.getKey(),
				BooleanUtils.toString(createUsersIfRoleMapped, "true", "false", "false"));
	}

	public Boolean getOnTheFlyGroupMapping() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_GROUP_MAPPING.getKey())),
				false);
	}

	public void setOnTheFlyAssignNewGroupsOnly(Boolean onTheFlyAssignNewGroupsOnly) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY.getKey(),
				BooleanUtils.toString(onTheFlyAssignNewGroupsOnly, "true", "false", "false"));
	}

	public Boolean getOnTheFlyAssignNewGroupsOnly() {

		String onTheFlyAssignNewGroupsOnly = (String) this.pluginSettings
				.get(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY.getKey());
		if (StringUtils.isBlank(onTheFlyAssignNewGroupsOnly)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(onTheFlyAssignNewGroupsOnly);
		}
	}

	public void setCreateNewGroups(Boolean createNewGroups) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.CREATE_NEW_GROUPS.getKey(),
				BooleanUtils.toString(createNewGroups, "true", "false", "false"));
	}

	public Boolean getCreateNewGroups() {
		String createNewGroups = (String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.CREATE_NEW_GROUPS.getKey());
		if (StringUtils.isBlank(createNewGroups)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(createNewGroups);
		}
	}

	public Boolean getGroupRegexPatternEnabled() {
		return BooleanUtils.toBoolean(
				StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.GROUP_REGEX_PATTERN_ENABLED.getKey()),"false"));
	}

	public void setGroupRegexPatternEnabled(Boolean groupRegexPatternEnabled) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.GROUP_REGEX_PATTERN_ENABLED.getKey(),
				BooleanUtils.toString(groupRegexPatternEnabled, "true", "false", "false"));
	}

	public String getRegexPatternForGroup() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.REGEX_PATTERN_FOR_GROUPS.getKey()), "");
	}

	public void setRegexPatternForGroup(String regexPatternForGroup) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.REGEX_PATTERN_FOR_GROUPS.getKey(),
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

	public void setOnTheFlyDoNotRemoveGroupsList(List<String> groups) {
		this.pluginSettings.put(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_DO_NOT_REMOVE_GROUPS.getKey(), groups);
	}

	public Object getOnTheFlyDoNotRemoveGroups() {
		if (this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_DO_NOT_REMOVE_GROUPS.getKey()) != null) {
			return this.pluginSettings.get(MoOAuthPluginConstants.GroupMapping.ON_THE_FLY_DO_NOT_REMOVE_GROUPS.getKey());
		}
		return new ArrayList<String>();
	}

	public void setRequestParamsMigrated(Boolean requestParamsMigrated) {
		this.pluginSettings.put(MoOAuthPluginConstants.REQUEST_PARAMS_MIGRATED,
				BooleanUtils.toString(requestParamsMigrated, "true", "false", "false"));
	}

	public Boolean getRequestParamsMigrated() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoOAuthPluginConstants.REQUEST_PARAMS_MIGRATED)),
				false);
	}

	public HashMap<String, String> getErrorMappingMap() {
		HashMap<String, String> errorMappingMap = (HashMap<String, String>) this.pluginSettings.get(MoOAuthPluginConstants.SignInSettings.ERROR_MESSAGE_MAPPING.getKey());
		if (errorMappingMap != null && errorMappingMap.size()!=0 ) {
			return errorMappingMap;
		}
		LOGGER.debug("Returning Default Error Mapping Map");
		return defaultErrorMappingMap;
	}

	public void setErrorMappingMap(HashMap<String, String> customErrorMappingMap) {
		this.pluginSettings.put(MoOAuthPluginConstants.SignInSettings.ERROR_MESSAGE_MAPPING.getKey(), customErrorMappingMap);
	}

	public HashMap<String, String> getDefaultErrorMessagesMap() {

		return defaultErrorMessagesMap;
	}



}
