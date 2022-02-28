package com.miniorange.oauth.confluence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import com.miniorange.oauth.utils.MoOAuthUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.spring.container.ContainerManager;
import com.google.gson.JsonObject;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;

public class MoOAuthPluginHandler {

	private static Log LOGGER = LogFactory.getLog(MoOAuthPluginHandler.class);
	private static MoOAuthSettings settings;


	public MoOAuthPluginHandler(MoOAuthSettings settings){
		this.settings=settings;
	}


	public static HashMap<String, String> getAuthorizationHeaders(Long customerId, String apiKey) {
		HashMap<String, String> headers = new HashMap<String, String>();
		Long timestamp = System.currentTimeMillis();
		String stringToHash = customerId + timestamp + apiKey;
		String hashValue = DigestUtils.sha512Hex(stringToHash);

		headers.put("Customer-Key", String.valueOf(customerId));
		headers.put("Timestamp", String.valueOf(timestamp));
		headers.put("Authorization", hashValue);
		return headers;
	}

	public void saveOAuthConfiguration(String customCallbackParameter, String appName, String customAppName, String clientID, String clientSecret, String scope,
									   String authorizeEndpoint, String accessTokenEndpoint, String userInfoEndpoint, String fetchGroupsEndpoint,
									   Boolean useStateParameter, Boolean acrValueCheck, Boolean nonceCheck, String publicKey, 
									   String jWKSEndpointURL, String validateSignatureMethod, Boolean enableCheckIssuerFor, 
									   String checkIssuerFor, String customIssuerValue, String singleLogoutUrl, String sendTokenAuthParameterIn,HashMap<String, String> oauth_request_parameters)
	{
		LOGGER.debug("saveOAuthConfiguration saving info, customCallbackParameter : "+ customCallbackParameter + ",customAppName: " + customAppName + ", clientID: " + clientID
				+ "clientSecret: " + clientSecret + ", authorizeEndpoint: " + authorizeEndpoint
				+ ", accessTokenEndpoint: " + accessTokenEndpoint + ", userInfoEndpoint: " + userInfoEndpoint
				+ ", fetchGroupsEndpoint: " + fetchGroupsEndpoint + ", useStateParameter = "+useStateParameter +", publicKey = "+publicKey
				+", jWKSEndpointURL:"+jWKSEndpointURL+", validateSignatureMethod:"+validateSignatureMethod+ ", enableCheckIssuerFor:"+enableCheckIssuerFor+", checkIssuerFor:"+checkIssuerFor+", customIssuerValue:"+customIssuerValue
				+" sendTokenAuthParameterIn : "+sendTokenAuthParameterIn + " acrValueCheck : "+ acrValueCheck + " nonceCheck : "+ nonceCheck);
		settings.setCustomCallbackParameter(customCallbackParameter);
		settings.setAppName(appName);
		settings.setCustomAppName(customAppName);
		settings.setClientID(clientID);
		settings.setClientSecret(clientSecret);
		settings.setScope(scope);
		settings.setAuthorizeEndpoint(authorizeEndpoint);
		settings.setAccessTokenEndpoint(accessTokenEndpoint);
		settings.setUserInfoEndpoint(userInfoEndpoint);
		settings.setFetchGroupsEndpoint(fetchGroupsEndpoint);
		settings.setUseStateParameter(useStateParameter);
		settings.setAcrValue(acrValueCheck);
		settings.setNonceCheck(nonceCheck);
		settings.setPublicKey(MoOAuthUtils.serializePublicCertificate(publicKey));
		settings.setjWKSEndpointURL(jWKSEndpointURL);
		settings.setValidateSignatureMethod(validateSignatureMethod);
		settings.setEnableCheckIssuerFor(enableCheckIssuerFor);
		settings.setCheckIssuerFor(checkIssuerFor);
		settings.setCustomIssuerValue(customIssuerValue);
		settings.setSingleLogoutURL(singleLogoutUrl);
		settings.setSendTokenAuthParameterIn(sendTokenAuthParameterIn);
		settings.setOauth_request_parameters(oauth_request_parameters);
	}

	public void saveOAuthConfiguration(String customCallbackParameter, String appName, String clientID, String clientSecret, String scope,
			String tenantID, String domainName, String hostName, String realmName, String publicKey, String jWKSEndpointURL, String validateSignatureMethod,
		    Boolean enableCheckIssuerFor, String checkIssuerFor, String customIssuerValue, Boolean useStateParameter, String singleLogoutUrl,HashMap<String, String> oauth_request_parameters, Boolean acrValueCheck, Boolean nonceCheck) {
		LOGGER.debug("saveOAuthConfiguration saving info, customCallbackParameter : "+ customCallbackParameter + ", appName: " + appName + ", clientID: " + clientID
				+ ", clientSecret: " + clientSecret +", enableCheckIssuerFor:"+enableCheckIssuerFor+", checkIssuerFor:"+checkIssuerFor+", customIssuerValue:"+customIssuerValue
				);
		settings.setCustomCallbackParameter(customCallbackParameter);
		settings.setAppName(appName);
		settings.setCustomAppName(appName);

		if (StringUtils.equals(appName, MoOAuthPluginConstants.GOOGLE)) {
			LOGGER.debug("Setting Google App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.GOOGLE_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.GOOGLE_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.GOOGLE_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.GOOGLE_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.FACEBOOK)) {
			LOGGER.debug("Setting Facebook App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.FACEBOOK_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.FACEBOOK_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.FACEBOOK_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.FACEBOOK_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.MICROSOFT)) {
			LOGGER.debug("Setting Microsoft App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.MICROSOFT_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.MICROSOFT_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.MICROSOFT_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.MICROSOFT_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.SLACK)) {
			LOGGER.debug("Setting Slack App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.SLACK_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.SLACK_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.SLACK_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.SLACK_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.DISCORD)) {
			LOGGER.debug("Setting Discord App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.DISCORD_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.DISCORD_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.DISCORD_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.DISCORD_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.GITLAB)) {
			LOGGER.debug("Setting Gitlab App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.GITLAB_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.GITLAB_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.GITLAB_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.GITLAB_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.MEETUP)) {
			LOGGER.debug("Setting Meetup App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.MEETUP_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.MEETUP_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.MEETUP_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.MEETUP_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.GITHUB)) {
			LOGGER.debug("Setting Github App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.GITHUB_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.GITHUB_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.GITHUB_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.GITHUB_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.AZURE)) {
			LOGGER.debug("Setting Azure App Endpoints...!");
			settings.setTenantID(tenantID);
			settings.setAccessTokenEndpoint(
					"https://login.microsoftonline.com/" + settings.getTenantID() + "/oauth2/token");
			settings.setAuthorizeEndpoint(
					"https://login.microsoftonline.com/" + settings.getTenantID() + "/oauth2/v2.0/authorize");
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.AZURE_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.AZURE_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.AZURE_B2C)) {
			LOGGER.debug("Setting Azure B2C App Endpoints...!");
			settings.setTenantID(tenantID);
			settings.setAccessTokenEndpoint(
					"https://login.microsoftonline.com/" + settings.getTenantID() + "/oauth2/token");
			settings.setAuthorizeEndpoint(
					"https://login.microsoftonline.com/" + settings.getTenantID() + "/oauth2/authorize");
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.AZURE_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint("");
			//settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.AZURE_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.AWS_COGNITO)) {
			LOGGER.debug("Setting AWS Cognito App Endpoints...!");
			settings.setDomainName(domainName);
			settings.setAuthorizeEndpoint(settings.getDomainName() + "/oauth2/authorize");
			settings.setAccessTokenEndpoint(settings.getDomainName() + "/oauth2/token");
			settings.setFetchGroupsEndpoint("");
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.ADFS)) {
			LOGGER.debug("Setting ADFS App Endpoints...!");
			settings.setDomainName(domainName);
			settings.setAuthorizeEndpoint(settings.getDomainName() + "/adfs/oauth2/authorize?");
			settings.setAccessTokenEndpoint(settings.getDomainName() + "/adfs/oauth2/token");
			settings.setFetchGroupsEndpoint("");
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.KEYCLOAK)) {
			LOGGER.debug("Setting KeyCloak App Endpoints...!");
			settings.setHostName(hostName);
			settings.setRealmName(realmName.trim());
			settings.setScope("openid");

			settings.setAccessTokenEndpoint(
					hostName + "/auth/realms/" + settings.getRealmName().replace(" ","%20") + MoOAuthPluginConstants.KEYCLOAK_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(
					hostName + "/auth/realms/" + settings.getRealmName() + MoOAuthPluginConstants.KEYCLOAK_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(
					hostName + "/auth/realms/" + settings.getRealmName().replace(" ","%20") + MoOAuthPluginConstants.KEYCLOAK_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.KEYCLOAK_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.OKTA)) {
			LOGGER.debug("Setting Okta App Endpoints...!");
			settings.setDomainName(domainName);
			settings.setAuthorizeEndpoint(settings.getDomainName() + "/oauth2/default/v1/authorize");
			settings.setAccessTokenEndpoint(settings.getDomainName() + "/oauth2/default/v1/token");
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.SALESFORCE)) {
			LOGGER.debug("Setting Salesforce App Endpoints...!");
			settings.setDomainName(domainName);
			settings.setAuthorizeEndpoint(settings.getDomainName() + "/services/oauth2/authorize");
			settings.setAccessTokenEndpoint(settings.getDomainName() + "/services/oauth2/token");
			settings.setUserInfoEndpoint(settings.getDomainName() + "/services/oauth2/userinfo");
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.miniOrange)) {
			LOGGER.debug("Setting miniOrange App Endpoints...!");
			settings.setDomainName(domainName);
			settings.setAuthorizeEndpoint(settings.getDomainName() + "/moas/idp/openidsso");
			settings.setAccessTokenEndpoint(settings.getDomainName() + "/moas/rest/oauth/token");
			settings.setUserInfoEndpoint(settings.getDomainName() + "/moas/rest/oauth/getuserinfo");
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.GLUU_SERVER)){
			LOGGER.debug("Setting Gluu App Endpoints...!");
			settings.setDomainName(domainName);
			settings.setAuthorizeEndpoint(settings.getDomainName() + "/oxauth/restv1/authorize");
			settings.setAccessTokenEndpoint(settings.getDomainName() + "/oxauth/restv1/token");
			settings.setUserInfoEndpoint(settings.getDomainName() + "/oxauth/restv1/userinfo");
		}
		settings.setClientID(clientID);
		settings.setClientSecret(clientSecret);
		if(StringUtils.equals(appName, MoOAuthPluginConstants.KEYCLOAK)){
			settings.setScope("openid");
		} else {
			settings.setScope(scope);
		}
		settings.setPublicKey(publicKey);
		settings.setjWKSEndpointURL(jWKSEndpointURL);
		settings.setValidateSignatureMethod(validateSignatureMethod);
		settings.setEnableCheckIssuerFor(enableCheckIssuerFor);
		settings.setCheckIssuerFor(checkIssuerFor);
		settings.setCustomIssuerValue(customIssuerValue);
		settings.setSingleLogoutURL(singleLogoutUrl);
		settings.setOauth_request_parameters(oauth_request_parameters);
		settings.setUseStateParameter(useStateParameter);
		settings.setAcrValue(acrValueCheck);
		settings.setNonceCheck(nonceCheck);
	}

	public void saveRoleMapping(String roleAttribute, Boolean keepExistingUserRoles,
								Boolean createUsersIfRoleMapped, HashMap<String, String> roleMapping, String defaultGroup,
								List<String> defaultGroups, Boolean restrictUserCreation, String enableDefaultGroupsFor,
								Boolean onTheFlyGroupCreation,Boolean groupRegexPatternEnabled, String regexPatternForGroup, String regexGroups,String testRegex, Boolean createNewGroups, Boolean onTheFlyAssignNewGroupsOnly,
								List<String> onTheFlyDoNotRemoveGroupsList, String onTheFlyFilterIDPGroupsOption, String onTheFlyFilterIDPGroupsKey) {
		LOGGER.debug("saveRoleMapping Saving Role Mapping: Default group " + defaultGroup + "Default groups" + defaultGroups +", Group Attribute: "
				+ roleAttribute + ", keepExistingUsersRoles: " + keepExistingUserRoles + ", createUsersIfRoleMapped: " + createUsersIfRoleMapped + ", " +
				"roleMapping: " + roleMapping + ", restrictUserCreation: " + restrictUserCreation +", enableDefaultGroupsFor:"+ enableDefaultGroupsFor +
				", enableGroupsFor :" + enableDefaultGroupsFor + ",OTF group : " + onTheFlyGroupCreation + ",createNewUsers : "+createNewGroups +
				",OTFnewGroupsOnly : " + onTheFlyAssignNewGroupsOnly + ",onTheFlyDoNotRemoveGroups : " + onTheFlyDoNotRemoveGroupsList +
				", onTheFlyFilterIDPGroupsOption : " + onTheFlyFilterIDPGroupsOption + ", onTheFlyFilterIDPGroupsKey : " + onTheFlyFilterIDPGroupsKey);
		settings.setRoleAttribute(roleAttribute);
		settings.setKeepExistingUserRoles(BooleanUtils.toBoolean(keepExistingUserRoles));
		settings.setCreateUsersIfRoleMapped(createUsersIfRoleMapped);
		settings.setRestrictUserCreation(restrictUserCreation);
		settings.setEnableDefaultGroupsFor(enableDefaultGroupsFor);
		settings.setRoleMapping(roleMapping);
		settings.setDefaultGroup(defaultGroup);
		settings.setDefaultGroups(defaultGroups);
		//settings.setGroupRegexPattern(groupRegexPattern);
		settings.setEnableDefaultGroupsFor(enableDefaultGroupsFor);
		settings.setOnTheFlyGroupMapping(onTheFlyGroupCreation);
		settings.setGroupRegexPatternEnabled(groupRegexPatternEnabled);
		settings.setRegexPatternForGroup(regexPatternForGroup);
		settings.setRegexGroups(regexGroups);
		settings.setTestRegex(testRegex);
		settings.setCreateNewGroups(createNewGroups);
		settings.setOnTheFlyAssignNewGroupsOnly(onTheFlyAssignNewGroupsOnly);
		settings.setOnTheFlyDoNotRemoveGroupsList(onTheFlyDoNotRemoveGroupsList);
		settings.setOnTheFlyFilterIDPGroupsOption(onTheFlyFilterIDPGroupsOption);
		settings.setOnTheFlyFilterIDPGroupsKey(onTheFlyFilterIDPGroupsKey);

	}

	public void saveAttributeMapping(String usernameAttribute, String emailAttribute,  String regexPattern, 
			Boolean regexPatternEnabled, String fullNameAttribute,
			Boolean useSeparateNameAttributes, String firstNameAttribute, String lastNameAttribute,
			Boolean keepExistingUserAttributes, String loginUserAttribute, HashMap<String, String> customAttributeMapping) {
		
		LOGGER.debug("saveAttributeMapping Saving Attribute Mapping: UserName Attribute: " + usernameAttribute
				+ " email Attribute: " + emailAttribute + " Full Name Attribute: " + fullNameAttribute
				+ "Is Separate Attributes: " + useSeparateNameAttributes + " First Name Attribute: "
				+ firstNameAttribute + " Last Name Attribute: " + lastNameAttribute + "Keep existing user attributes: "
				+ keepExistingUserAttributes + " Login User Attribute: " + loginUserAttribute 
				+ "regexPatternEnabled : "+ regexPatternEnabled + "regexPattern: "+regexPattern + " customAttributeMapping = "
				+ customAttributeMapping);
		settings.setUsernameAttribute(usernameAttribute);
		settings.setEmailAttribute(emailAttribute);
		settings.setRegexPatternEnabled((BooleanUtils.toBoolean(regexPatternEnabled)));
		if (BooleanUtils.toBoolean(regexPatternEnabled))
			settings.setRegexPattern(regexPattern);

		settings.setKeepExistingUserAttributes(BooleanUtils.toBoolean(keepExistingUserAttributes));
		settings.setFirstNameAttribute(firstNameAttribute);
		settings.setLastNameAttribute(lastNameAttribute);
		settings.setFullNameAttribute(fullNameAttribute);
		settings.setUseSeparateNameAttributes(BooleanUtils.toBoolean(useSeparateNameAttributes));
		settings.setLoginUserAttribute(loginUserAttribute);
		settings.setCustomAttributeMapping(customAttributeMapping);
	}

	public void saveSigninSettings(Boolean enableOAuthSSO,String loginButtonText, String relayState, String adminSessionOption, String allowedDomains,
			String customLogoutURL, Boolean enableLogoutTemplate, String logoutTemplate, Boolean disableDefaultLogin,Boolean disableAnonymousAccess, Boolean allowGuestLogin, String guestSessionTimeout,
			Boolean enableBackdoor, Boolean enableAutoRedirectDelay, String autoRedirectDelayInterval, String ssoButtonLocation, Boolean enableErrorMsgTemplate, String errorMsgTemplate,
			Boolean restrictBackdoor, List<String> backdoorGroupsList, Boolean autoActivateUser, Boolean isRememberMeCookieEnabled, Boolean enableLoginTemplate, String loginTemplate, Boolean pluginApiAccessRestriction
			, Boolean checkIDPSession, HashMap<String, String> customErrorMappingMap) {
		LOGGER.debug("saveAttributeMapping Saving SignIn Settings: LoginButtonText: " + loginButtonText);
		LOGGER.debug("enableAutoRedirectDelay : " + enableAutoRedirectDelay);

		settings.setEnableOAuthSSO(enableOAuthSSO);
		settings.setLoginButtonText(loginButtonText);
		settings.setRelayState(relayState);
		settings.setAdminSessionOption(adminSessionOption);
		settings.setAllowedDomains(allowedDomains);
		settings.setCustomLogoutURL(customLogoutURL);
		settings.setEnablelogoutTemplate(enableLogoutTemplate);
		settings.setLogoutTemplate(logoutTemplate);
		settings.setDefaultLoginDisabled(disableDefaultLogin);
		settings.setDisableAnonymousAccess(disableAnonymousAccess);
		settings.setGuestSessionTimeout(guestSessionTimeout);
		settings.setAllowGuestLogin(allowGuestLogin);
		settings.setBackdoorEnabled(enableBackdoor);
		settings.setEnableAutoRedirectDelay(BooleanUtils.toBoolean(enableAutoRedirectDelay));
		settings.setAutoRedirectDelayInterval(autoRedirectDelayInterval);
		settings.setSsoButtonLocation(ssoButtonLocation);
		settings.setErrorMsgTemplate(errorMsgTemplate);
		settings.setEnableErrorMsgTemplate(enableErrorMsgTemplate);
		settings.setRestrictBackdoor(restrictBackdoor);
		settings.setBackdoorGroups(backdoorGroupsList);
		settings.setAutoActivateUser(BooleanUtils.toBoolean(autoActivateUser));
		settings.setRememberMeCookieEnabled(isRememberMeCookieEnabled);
		settings.setCheckIDPSession(checkIDPSession);
		settings.setEnableLoginTemplate(enableLoginTemplate);
		settings.setLoginTemplate(loginTemplate);
		settings.setPluginApiAccessRestriction(pluginApiAccessRestriction);
		settings.setErrorMappingMap(customErrorMappingMap);
	}
	
	public void saveSessionManagement(HashMap<String, String> userSessionTimeout, String loginCookie) {
		LOGGER.info("Saving Session Management Configurations");
		LOGGER.debug("userSessionTimeout : " + userSessionTimeout);
		LOGGER.debug("loginCookie : "+ loginCookie);
		
		settings.setUserSessionTimeout(userSessionTimeout);
		settings.setLoginCookie(loginCookie);
		
	}

	/* saveDefaultConfiguration function is used to set the default values before importing*/

	public void saveDefaultConfiguration() {
		LOGGER.info("Saving Default Configurations");

		saveAttributeMapping(settings.getUsernameAttribute(), settings.getEmailAttribute(), settings.getRegexPattern(), settings.getRegexPatternEnabled(),
				settings.getFullNameAttribute(),settings.getUseSeparateNameAttributes(), settings.getFirstNameAttribute(), settings.getLastNameAttribute(),
				settings.getKeepExistingUserAttributes(), settings.getLoginUserAttribute(), settings.getCustomAttributeMapping());

		saveRoleMapping(settings.getRoleAttribute(), settings.getKeepExistingUserRoles(), settings.getCreateUsersIfRoleMapped(), settings.getRoleMapping(),
				settings.getDefaultGroup(), (List<String>) settings.getDefaultGroups(), settings.getRestrictUserCreation(), settings.getEnableDefaultGroupsFor(),
				settings.getOnTheFlyGroupMapping(),settings.getGroupRegexPatternEnabled(),settings.getRegexPatternForGroup(),settings.getRegexGroups(),settings.getTestRegex(),settings.getCreateNewGroups(), settings.getOnTheFlyAssignNewGroupsOnly(), (ArrayList<String>) settings.getOnTheFlyDoNotRemoveGroups(),
				settings.getOnTheFlyFilterIDPGroupsOption(), settings.getOnTheFlyFilterIDPGroupsKey());

		saveSigninSettings(settings.getEnableOAuthSSO(),settings.getLoginButtonText(),settings.getRelayState(),settings.getAdminSessionOption(),settings.getAllowedDomains(),
				settings.getCustomLogoutURL(),settings.getEnablelogoutTemplate(),settings.getLogoutTemplate(),settings.getDefaultLoginDisabled(),settings.getDisableAnonymousAccess(),
				settings.getAllowGuestLogin(),settings.getGuestSessionTimeout(),settings.getBackdoorEnabled(),settings.getEnableAutoRedirectDelay(),
				settings.getAutoRedirectDelayInterval(), settings.getSsoButtonLocation(),settings.getEnableErrorMsgTemplate(),
				settings.getErrorMsgTemplate(), settings.getRestrictBackdoor(), settings.getBackdoorGroups(), settings.getAutoActivateUser(), settings.getRememberMeCookieEnabled(),
				settings.getEnableLoginTemplate(), settings.getLoginTemplate(), settings.getPluginApiAccessRestriction(),settings.getCheckIDPSession(),new HashMap<String,String>());
	}
	
	public static Boolean isUserPresentInGroups(String username, List<String> groups) {
		LOGGER.debug("Testing for user " + username);
		try {
			UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
			ConfluenceUser user = userAccessor.getUserByName(username);

			if (user != null) {
				for (String group : groups) {
					if (userAccessor.getGroupNames(user).contains(group)) {
						return true;
					}
				}
				return false;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			LOGGER.error(e.getMessage());
			return false;
		}
		return false;
	}


	public void submitSupportQuery(String email, String phone, String query) {
		try {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("company", settings.getBaseUrl());
			jsonObject.addProperty("email", email);
			jsonObject.addProperty("phone", phone);
			jsonObject.addProperty("query", StringUtils.prependIfMissing(query, "[Confluence OAuth Client Plugin] "));
			String json = jsonObject.toString();
			String response = MoOAuthHttpUtils.sendPostRequest1(MoOAuthPluginConstants.SUPPORT_QUERY_URL, json,
					MoOAuthHttpUtils.CONTENT_TYPE_JSON,
					getAuthorizationHeaders(Long.valueOf(MoOAuthPluginConstants.DEFAULT_CUSTOMER_KEY),
							MoOAuthPluginConstants.DEFAULT_API_KEY));
			LOGGER.debug("Submit Query response: " + response);
		} catch (Exception e) {
			LOGGER.error("An error occured while saving your details.", e);
		}
	}
	public static String getGroupNameFromRegex(Matcher matcher, String replaceWith) {
		LOGGER.debug("forming String According To Groupname and replaceWith");
		StringBuffer groupname = new StringBuffer(StringUtils.EMPTY);
		try {
			String[] groupPositions = StringUtils.split(replaceWith, '$');
			for (String position : groupPositions) {
				LOGGER.debug("position "+position);
				LOGGER.debug("Group name "+matcher.group(Integer.parseInt(position.trim())));
				groupname.append(matcher.group(Integer.parseInt(position.trim())));
			}
		} catch (Exception e){
			LOGGER.error("An error occurred while applying regex on groups. Kindly check Regex configuration.",e);
		}
		return groupname.toString();
	}
	public static String getContextPath(){
		String baseUrl = settings.getBaseUrl();
		String contextPath = "";
		int startIndex = 3;
		List<String> baseURLComponents = Arrays.asList(baseUrl.split("/"));
		LOGGER.debug("BaseURL components : " + baseURLComponents.toString());

		for(int iterator = startIndex; iterator < baseURLComponents.size(); iterator++){
			contextPath +="/"+baseURLComponents.get(iterator);
		}
		if(contextPath == "") {
			contextPath = "/";
		}
		return contextPath;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public static boolean isGlobalAnonymousAccessEnabled(){
		PermissionManager permissionManager = (PermissionManager)ContainerManager.getComponent("permissionManager");
		return permissionManager.hasPermission(null, Permission.VIEW,PermissionManager.TARGET_APPLICATION);
	}
}
