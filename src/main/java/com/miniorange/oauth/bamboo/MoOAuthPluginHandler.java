package com.miniorange.oauth.bamboo;

import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.bamboo.user.BambooUser;
import com.miniorange.oauth.utils.MoOAuthUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.regex.Matcher;


public class MoOAuthPluginHandler {

	private static Log LOGGER = LogFactory.getLog(MoOAuthPluginHandler.class);

	private static MoOAuthSettings settings;
	private static BambooUserManager bambooUserManager;

	public MoOAuthPluginHandler(MoOAuthSettings settings, BambooUserManager bambooUserManager) {

		this.settings = settings;
		this.bambooUserManager = bambooUserManager;
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
	
	public static void saveOAuthConfiguration(String appName, String customAppName, String clientID, String clientSecret, String scope,
									   String authorizeEndpoint, String accessTokenEndpoint, String userInfoEndpoint,
									   String fetchGroupsEndpoint, Boolean useStateParameter, Boolean enableCheckIssuerFor,
									   String checkIssuerFor, String customIssuerValue, String singleLogoutURL, Boolean nonceCheck, String publicKey,Boolean acrValueCheck, HashMap<String, String> oauth_request_parameters)
	{
		LOGGER.info("Saving OAuthConfiguration");
		LOGGER.debug("customAppName: " + customAppName + ", clientID: " + clientID + ", clientSecret: " + clientSecret
						+ ", scope" + scope + ", authorizeEndpoint: " + authorizeEndpoint + ", accessTokenEndpoint: " +
						accessTokenEndpoint + ", userInfoEndpoint: " + userInfoEndpoint + ", fetchGroupsEndpoint: " +
						fetchGroupsEndpoint + ", useStateParameter: " + BooleanUtils.toBoolean(useStateParameter)
						+ ", enableCheckIssuerFor: " + BooleanUtils.toBoolean(enableCheckIssuerFor) + ", checkIssuerFor: "
						+ checkIssuerFor + ", customIssuerValue" + customIssuerValue + ", singleLogoutURL: " + singleLogoutURL +
						", publicKey "+ publicKey);
		settings.setAppName(appName);
		settings.setCustomAppName(customAppName);
		settings.setClientID(clientID);
		settings.setClientSecret(clientSecret);
		settings.setScope(scope);
		settings.setAuthorizeEndpoint(authorizeEndpoint);
		settings.setAccessTokenEndpoint(accessTokenEndpoint);
		settings.setUserInfoEndpoint(userInfoEndpoint);
		settings.setFetchGroupsEndpoint(fetchGroupsEndpoint);
		settings.setUseStateParameter(BooleanUtils.toBoolean(useStateParameter));
		settings.setEnableCheckIssuerFor(BooleanUtils.toBoolean(enableCheckIssuerFor));
		settings.setCheckIssuerFor(checkIssuerFor);
		settings.setCustomIssuerValue(customIssuerValue);
		settings.setSingleLogoutURL(singleLogoutURL);
		settings.setPublicKey(publicKey);
		settings.setNonceCheck(nonceCheck);
		settings.setACRValueCheck(acrValueCheck);
		settings.setOauth_request_parameters(oauth_request_parameters);
	}

	public static void saveOAuthConfiguration(String appName, String appHostedOn, String clientID, String clientSecret, String scope, String tenantID,
									   String hostName, String realmName, String domainName, Boolean enableCheckIssuerFor,
									   String checkIssuerFor, String customIssuerValue, String singleLogoutURL, Boolean useStateParameter, Boolean nonceCheck, String publicKey, Boolean acrValueCheck, HashMap<String, String> oauth_request_parameters)
	{
		LOGGER.debug("saveOAuthConfiguration saving info, appName: " + appName +", clientID: " + clientID
				+ "clientSecret: " + clientSecret + "scope: "+scope );
		settings.setAppName(appName);
		settings.setCustomAppName(MoOAuthUtils.sanitizeText(appName));
		domainName = MoOAuthUtils.sanitizeText(domainName);
		if(MoOAuthUtils.isOpenIdProtocol(appName)){
			settings.setPublicKey(publicKey);
			settings.setEnableCheckIssuerFor(BooleanUtils.toBoolean(enableCheckIssuerFor));
			settings.setCheckIssuerFor(checkIssuerFor);
			settings.setCustomIssuerValue(customIssuerValue);
		}
		if (StringUtils.equals(appName, MoOAuthPluginConstants.GOOGLE)) {
			LOGGER.info("Setting Google App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.GOOGLE_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.GOOGLE_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.GOOGLE_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.GOOGLE_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.FACEBOOK)) {
			LOGGER.info("Setting Facebook App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.FACEBOOK_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.FACEBOOK_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.FACEBOOK_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.FACEBOOK_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.GITHUB)) {
			LOGGER.debug("Setting Github App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.GITHUB_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.GITHUB_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.GITHUB_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.GITHUB_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.GITHUB_ENTERPRISE)) {
			LOGGER.debug("Setting Github Enterprise App Endpoints...!");
			//Endpoints are same as github
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.GITHUB_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.GITHUB_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.GITHUB_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.GITHUB_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.AZURE)) {
			LOGGER.info("Setting Azure AD App Endpoints...!");
			settings.setTenantID(tenantID);
			settings.setAccessTokenEndpoint(
					"https://login.microsoftonline.com/" + settings.getTenantID() + "/oauth2/token");
			settings.setAuthorizeEndpoint(
					"https://login.microsoftonline.com/" + settings.getTenantID() + "/oauth2/v2.0/authorize");
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.AZURE_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.AZURE_FETCH_GROUPS_ENDPOINT);
		}  else if (StringUtils.equals(appName, MoOAuthPluginConstants.AZURE_B2C)) {
			LOGGER.info("Setting AzureB2C App Endpoints...!");
			settings.setTenantID(tenantID);
			settings.setAccessTokenEndpoint(
					"https://login.microsoftonline.com/" + settings.getTenantID() + "/oauth2/token");
			settings.setAuthorizeEndpoint(
					"https://login.microsoftonline.com/" + settings.getTenantID() + "/oauth2/authorize");
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.AZURE_USER_INFO_ENDPOINT);
			//settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.AZURE_FETCH_GROUPS_ENDPOINT);
		}  else if (StringUtils.equals(appName, MoOAuthPluginConstants.SLACK)) {
			LOGGER.info("Setting Slack App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.SLACK_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.SLACK_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.SLACK_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.SLACK_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.DISCORD)) {
			LOGGER.info("Setting Discord App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.DISCORD_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.DISCORD_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.DISCORD_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.DISCORD_FETCH_GROUPS_ENDPOINT);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.GITLAB)) {
			settings.setAppHostedOn(appHostedOn);
			if (StringUtils.equals(appHostedOn, MoOAuthPluginConstants.SELF_HOSTING)){
				LOGGER.info("Setting Gitlab App Self Hosted Endpoints...!");
				settings.setDomainName(domainName);
				String baseURL = StringUtils.stripEnd(domainName, "/");
				String  GITLAB_AUTHORIZE_ENDPOINT = baseURL + "/oauth/authorize";
				String  GITLAB_ACCESS_TOKEN_ENDPOINT = baseURL + "/oauth/token";
				String  GITLAB_USER_INFO_ENDPOINT = baseURL + "/api/v4/user";
				String  GITLAB_FETCH_GROUPS_ENDPOINT = baseURL + "/api/v4/groups";

				LOGGER.debug("Gitlab Server Hosting URL : " + baseURL);
				LOGGER.debug("Authorize Endpoint : " + GITLAB_AUTHORIZE_ENDPOINT);
				LOGGER.debug("Access Token Endpoint : " + GITLAB_ACCESS_TOKEN_ENDPOINT);
				LOGGER.debug("User Info Endpoint : " + GITLAB_USER_INFO_ENDPOINT);
				LOGGER.debug("Groups Endpoint : " + GITLAB_FETCH_GROUPS_ENDPOINT);

				settings.setAuthorizeEndpoint(GITLAB_AUTHORIZE_ENDPOINT);
				settings.setAccessTokenEndpoint(GITLAB_ACCESS_TOKEN_ENDPOINT);
				settings.setUserInfoEndpoint(GITLAB_USER_INFO_ENDPOINT);
				settings.setFetchGroupsEndpoint(GITLAB_FETCH_GROUPS_ENDPOINT);
			}else{
				settings.setDomainName("");
				LOGGER.info("Setting Gitlab App Cloud Endpoints...!");
				settings.setAccessTokenEndpoint(MoOAuthPluginConstants.GITLAB_ACCESS_TOKEN_ENDPOINT);
				settings.setAuthorizeEndpoint(MoOAuthPluginConstants.GITLAB_AUTHORIZE_ENDPOINT);
				settings.setUserInfoEndpoint(MoOAuthPluginConstants.GITLAB_USER_INFO_ENDPOINT);
				settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.GITLAB_FETCH_GROUPS_ENDPOINT);
			}
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.MEETUP)) {
			LOGGER.info("Setting Meetup App Endpoints...!");
			settings.setAccessTokenEndpoint(MoOAuthPluginConstants.MEETUP_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(MoOAuthPluginConstants.MEETUP_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(MoOAuthPluginConstants.MEETUP_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.MEETUP_FETCH_GROUPS_ENDPOINT);
		}else if (StringUtils.equals(appName, MoOAuthPluginConstants.KEYCLOAK)) {
			LOGGER.info("Setting KeyCloak App Endpoints...!");
			settings.setHostName(hostName);
			settings.setRealmName(realmName.trim());
			settings.setAccessTokenEndpoint(
					hostName + "/auth/realms/" + settings.getRealmName().replace(" ","%20") + MoOAuthPluginConstants.KEYCLOAK_ACCESS_TOKEN_ENDPOINT);
			settings.setAuthorizeEndpoint(
					hostName + "/auth/realms/" + realmName + MoOAuthPluginConstants.KEYCLOAK_AUTHORIZE_ENDPOINT);
			settings.setUserInfoEndpoint(
					hostName + "/auth/realms/" + settings.getRealmName().replace(" ","%20") + MoOAuthPluginConstants.KEYCLOAK_USER_INFO_ENDPOINT);
			settings.setFetchGroupsEndpoint(MoOAuthPluginConstants.KEYCLOAK_FETCH_GROUPS_ENDPOINT);
		}else if (StringUtils.equals(appName, MoOAuthPluginConstants.AWS_COGNITO)) {
			LOGGER.info("Setting AWS Cognito App Endpoints...!");
			settings.setDomainName(domainName);
			settings.setAuthorizeEndpoint("https://" + settings.getDomainName() + "/oauth2/authorize");
			settings.setAccessTokenEndpoint("https://" + settings.getDomainName() + "/oauth2/token");
			settings.setEnableCheckIssuerFor(enableCheckIssuerFor);
			settings.setCheckIssuerFor(checkIssuerFor);
			settings.setCustomIssuerValue(customIssuerValue);
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.ADFS)) {
			LOGGER.info("Setting ADFS App Endpoints...!");
			settings.setDomainName(domainName);
			settings.setAuthorizeEndpoint("https://" + settings.getDomainName() + "/adfs/oauth2/authorize?");
			settings.setAccessTokenEndpoint("https://" + settings.getDomainName() + "/adfs/oauth2/token");
			settings.setEnableCheckIssuerFor(enableCheckIssuerFor);
			settings.setCheckIssuerFor(checkIssuerFor);
			settings.setCustomIssuerValue(customIssuerValue);
		}else if (StringUtils.equals(appName, MoOAuthPluginConstants.OKTA)) {
			LOGGER.debug("Setting Okta App Endpoints...!");
			settings.setDomainName(domainName);
			settings.setAuthorizeEndpoint("https://" + settings.getDomainName() + "/oauth2/default/v1/authorize");
			settings.setAccessTokenEndpoint("https://" + settings.getDomainName() + "/oauth2/default/v1/token");
		} else if (StringUtils.equals(appName, MoOAuthPluginConstants.SALESFORCE)) {
			LOGGER.debug("Setting Salesforce App Endpoints...!");
			settings.setDomainName(domainName);
			settings.setAuthorizeEndpoint("https://" + settings.getDomainName() + "/services/oauth2/authorize");
			settings.setAccessTokenEndpoint("https://" + settings.getDomainName() + "/services/oauth2/token");
			settings.setUserInfoEndpoint("https://" + settings.getDomainName() + "/services/oauth2/userinfo");
		} else if(StringUtils.equals(appName, MoOAuthPluginConstants.GLUU_SERVER)){
            LOGGER.debug("Setting Gluu Server App Endpoints...!");
            settings.setDomainName(domainName);
            settings.setAuthorizeEndpoint(settings.getDomainName() + MoOAuthPluginConstants.GLUU_AUTHORIZE_ENDPOINT);
            settings.setAccessTokenEndpoint(settings.getDomainName() + MoOAuthPluginConstants.GLUU_ACCESS_TOKEN_ENDPOINT);
            settings.setUserInfoEndpoint(settings.getDomainName() + MoOAuthPluginConstants.GLUU_USER_INFO_ENDPOINT);
        } else if(StringUtils.equals(appName, MoOAuthPluginConstants.miniOrange)){
			LOGGER.debug("Setting miniOrange Server App Endpoints...!");
			settings.setDomainName(domainName);
			LOGGER.debug("domain name::"+domainName);
			settings.setAuthorizeEndpoint("https://" + settings.getDomainName() + "/moas/idp/openidsso");
			settings.setAccessTokenEndpoint("https://" + settings.getDomainName() + "/moas/rest/oauth/token");
			settings.setUserInfoEndpoint("https://" + settings.getDomainName() + "/moas/rest/oauth/getuserinfo");
		}
		settings.setClientID(MoOAuthUtils.sanitizeText(clientID));
		settings.setClientSecret(MoOAuthUtils.sanitizeText(clientSecret));
        if(StringUtils.equals(appName, MoOAuthPluginConstants.KEYCLOAK)){
            settings.setScope("openid");
        }else{
            settings.setScope(MoOAuthUtils.sanitizeText(scope));
        }
		settings.setSingleLogoutURL(MoOAuthUtils.sanitizeText(singleLogoutURL));
		settings.setNonceCheck(nonceCheck);
		settings.setOauth_request_parameters(oauth_request_parameters);
		settings.setUseStateParameter(useStateParameter);
		settings.setACRValueCheck(acrValueCheck);
	}

	public static void saveSigninSettings(String loginButtonText, String relayState, Boolean disableDefaultLogin,
			Boolean enableBackdoor, Boolean enableAutoRedirectDelay,String autoRedirectDelayInterval, String allowedDomains,
										  String ssoButtonLocation,Boolean enableLoginTemplate, String loginTemplate,
										  Boolean enableLogoutTemplate, String logoutTemplate, String customLogoutURL,
										  Boolean enableErrorMsgTemplate, String errorMsgTemplate, Boolean pluginApiAccessRestriction,
										  Boolean restrictBackdoor, List<String> backdoorGroupsList,String backdoorKey, String backdoorValue) {
		LOGGER.info("Saving SignIn Settings");
		LOGGER.debug("LoginButtonText: " + loginButtonText + "relayState" + relayState + "disableDefaultLogin : "+
				BooleanUtils.toBoolean(disableDefaultLogin) + "enableBackdoor : "+ BooleanUtils.toBoolean(enableBackdoor)
				+ "enableAutoRedirectDelay : "+ BooleanUtils.toBoolean(enableAutoRedirectDelay) + "autoRedirectDelayInterval :"+ autoRedirectDelayInterval+
				"allowedDomains: " + allowedDomains + "ssoButtonLocation: " + ssoButtonLocation + "pluginApiAccessRestriction: " + BooleanUtils.toBoolean(pluginApiAccessRestriction)+
				"enableLoginTemplate:"+BooleanUtils.toBoolean(enableLoginTemplate)+"loginTemplate"+loginTemplate);
		settings.setLoginButtonText(MoOAuthUtils.sanitizeText(loginButtonText));
		settings.setRelayState(MoOAuthUtils.sanitizeText(relayState));
		settings.setDefaultLoginDisabled(BooleanUtils.toBoolean(disableDefaultLogin));
		settings.setBackdoorEnabled(BooleanUtils.toBoolean(enableBackdoor));
		settings.setEnableAutoRedirectDelay(BooleanUtils.toBoolean(enableAutoRedirectDelay));
		settings.setAutoRedirectDelayInterval(autoRedirectDelayInterval);
		settings.setAllowedDomains(allowedDomains);
		settings.setSsoButtonLocation(ssoButtonLocation);
		settings.setEnableLoginTemplate(enableLoginTemplate);
		settings.setLoginTemplate(loginTemplate);
		settings.setEnableLogoutTemplate(enableLogoutTemplate);
		settings.setLogoutTemplate(logoutTemplate);
		settings.setCustomLogoutURL(MoOAuthUtils.sanitizeText(customLogoutURL));
		settings.setEnableErrorMsgTemplate(enableErrorMsgTemplate);
		settings.setErrorMsgTemplate(errorMsgTemplate);
		settings.setPluginApiAccessRestriction(pluginApiAccessRestriction);
		settings.setRestrictBackdoor(restrictBackdoor);
		settings.setBackdoorGroups(backdoorGroupsList);
		settings.setBackdoorKey(backdoorKey);
		settings.setBackdoorValue(backdoorValue);
	}

	public static void saveRoleMapping(String roleAttribute, Boolean createUsersIfRoleMapped, Boolean keepExistingUserRoles,
								HashMap<String, String> roleMapping, String defaultGroup, List<String> defaultGroupsList,
								Boolean restrictUserCreation, String enableDefaultGroupsFor, Boolean onTheFlyGroupCreation,
								List<String> onTheFlyDoNotRemoveGroupsList, Boolean onTheFlyAssignNewGroupsOnly,
								Boolean onTheFlyCreateNewGroups, String onTheFlyFilterIDPGroupsOption, String onTheFlyFilterIDPGroupsKey,
									   Boolean groupRegexPatternEnabled,String regexPatternForGroup,String regexGroups,String testRegex) {
		LOGGER.info("Saving Group Mapping Configuration");
		LOGGER.debug("roleAttribute = " + roleAttribute + " createUsersIfRoleMapped = " + BooleanUtils.toBoolean(createUsersIfRoleMapped)
				+ " keepExistingUserRoles = " + BooleanUtils.toBoolean(keepExistingUserRoles) + " roleMapping = " + roleMapping
				+ " defaultGroup = " + defaultGroup + " defaultGroupsList = " + defaultGroupsList
				+ " restrictUserCreation = " + BooleanUtils.toBoolean(restrictUserCreation) +
				" enableDefaultGroupsFor = " + enableDefaultGroupsFor + " onTheFlyGroupCreation = "+
				BooleanUtils.toBoolean(onTheFlyGroupCreation) + " onTheFlyDoNotRemoveGroupsList = "
				+ onTheFlyDoNotRemoveGroupsList + " onTheFlyAssignNewGroupsOnly = "+ BooleanUtils.toBoolean(onTheFlyAssignNewGroupsOnly)
				+"onTheFlyCreateNewGroups = "+ BooleanUtils.toBoolean(onTheFlyCreateNewGroups) + ", onTheFlyFilterIDPGroupOption=" + onTheFlyFilterIDPGroupsOption
				+ ", onTheFlyFilterIDPGroupsKey=" + onTheFlyFilterIDPGroupsKey + " groupRegexPatternEnabled = " + groupRegexPatternEnabled + " regexPatternForGroup = " + regexPatternForGroup + " regexGroups " + regexGroups+ " testRegex " + testRegex);
		settings.setRoleAttribute(roleAttribute);
		settings.setCreateUsersIfRoleMapped(BooleanUtils.toBoolean(createUsersIfRoleMapped));
		settings.setKeepExistingUserRoles(BooleanUtils.toBoolean(keepExistingUserRoles));
		settings.setRoleMapping(roleMapping);
		settings.setDefaultGroup(defaultGroup);
		settings.setDefaultGroupsList(defaultGroupsList);
		settings.setRestrictUserCreation(BooleanUtils.toBoolean(restrictUserCreation));
		settings.setEnableDefaultGroupsFor(enableDefaultGroupsFor);
		settings.setOnTheFlyGroupMapping(BooleanUtils.toBoolean(onTheFlyGroupCreation));
		settings.setOnTheFlyDoNotRemoveGroups(onTheFlyDoNotRemoveGroupsList);
		settings.setOnTheFlyAssignNewGroupsOnly(BooleanUtils.toBoolean(onTheFlyAssignNewGroupsOnly));
		settings.setOnTheFlyCreateNewGroups(BooleanUtils.toBoolean(onTheFlyCreateNewGroups));
		settings.setOnTheFlyFilterIDPGroupsOption(onTheFlyFilterIDPGroupsOption);
		settings.setOnTheFlyFilterIDPGroupsKey(onTheFlyFilterIDPGroupsKey);

		//group regex
		settings.setGroupRegexPatternEnabled(groupRegexPatternEnabled);
		settings.setRegexPatternForGroup(regexPatternForGroup);
		settings.setRegexGroups(regexGroups);
		settings.setTestRegex(testRegex);
		
	}

	public static void saveAttributeMapping(String usernameAttribute, String emailAttribute, String regexPattern,
									 Boolean regexPatternEnabled, String  fullNameAttribute, Boolean useSeparateNameAttributes,
									 String firstNameAttribute, String lastNameAttribute, Boolean keepExistingUserAttributes,
									 String loginUserAttribute) {
		LOGGER.info("Saving Attribute Mapping");
		LOGGER.debug("UserName Attribute: " + usernameAttribute + " email Attribute: " + emailAttribute +
				" Full Name Attribute: " + fullNameAttribute + "Is Separate Attributes: " +
				BooleanUtils.toBoolean(useSeparateNameAttributes) + " First Name Attribute: " + firstNameAttribute
				+ " Last Name Attribute: " + lastNameAttribute + "Keep existing user attributes: " +
				BooleanUtils.toBoolean(keepExistingUserAttributes) + " loginUserAttribute " + loginUserAttribute);
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
	}

	/* saveDefaultConfiguration function is used to set the default values before importing*/
	public static void saveDefaultConfiguration() {
		LOGGER.info("Saving Default Configurations");
		saveOAuthConfiguration(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
				StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, Boolean.TRUE,
				Boolean.TRUE, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, Boolean.FALSE, StringUtils.EMPTY, Boolean.FALSE, new HashMap<>());

		saveOAuthConfiguration(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
				StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, Boolean.TRUE, StringUtils.EMPTY, StringUtils.EMPTY,
				StringUtils.EMPTY, Boolean.TRUE, Boolean.FALSE, StringUtils.EMPTY, Boolean.FALSE, new HashMap<>());

		saveAttributeMapping(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, Boolean.FALSE, StringUtils.EMPTY,
				Boolean.FALSE, StringUtils.EMPTY, StringUtils.EMPTY, Boolean.TRUE, "username");

		saveRoleMapping(StringUtils.EMPTY, Boolean.FALSE, Boolean.TRUE, new HashMap<>(), settings.getDefaultGroup(),
				Arrays.asList(settings.getDefaultGroup()), Boolean.FALSE, MoOAuthPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS,
					Boolean.FALSE, new ArrayList<>(), Boolean.TRUE, Boolean.TRUE, MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER, StringUtils.EMPTY,
				Boolean.FALSE,StringUtils.EMPTY,StringUtils.EMPTY,StringUtils.EMPTY);

		saveSigninSettings(StringUtils.EMPTY, StringUtils.EMPTY, Boolean.FALSE, Boolean.FALSE,
				Boolean.FALSE,"5", StringUtils.EMPTY, StringUtils.EMPTY, Boolean.FALSE, StringUtils.EMPTY, Boolean.FALSE, StringUtils.EMPTY,
				StringUtils.EMPTY, Boolean.FALSE, StringUtils.EMPTY, Boolean.FALSE,  Boolean.FALSE, new ArrayList<>(),"oauth_sso","false");
	}

	public Boolean isUserPresentInGroups(String username, List<String> groups) {
        LOGGER.debug("Testing for user " + username);
        try {
            BambooUser bambooUser = bambooUserManager.getBambooUser(username);
            List<String> existingGroupsOfUser = new ArrayList<>();
            existingGroupsOfUser = bambooUserManager.getGroupNamesAsList(bambooUser);
            if (bambooUser != null) {
                for (String group : groups) {
                    if (existingGroupsOfUser.contains(group)) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            return false;
        }
        return false;
	}

	public static URI getUri(HttpServletRequest request) {
		StringBuffer builder = request.getRequestURL();
		if (request.getQueryString() != null) {
			builder.append("?");
			builder.append(request.getQueryString());
		}
		return URI.create(builder.toString());
	}
	public MoOAuthSettings getSettings() {
		return settings;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public static BambooUserManager getBambooUserManager() {
		return bambooUserManager;
	}

	public static void setBambooUserManager(BambooUserManager bambooUserManager) {
		MoOAuthPluginHandler.bambooUserManager = bambooUserManager;
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
			LOGGER.error("An error occurred while applying regex on username. Kindly check Regex configuration.",e);
		}
		return groupname.toString();
	}
}
