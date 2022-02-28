package com.miniorange.oauth.confluence;

import java.util.HashMap;
import java.util.Map;

public interface MoOAuthPluginConstants {
	public static final String REMEMBERME_COOKIE = "miniorange.oauth.REMEMBERME_COOKIE";
	public static final String FETCH_GROUPS_ENDPOINT = "miniorange.oauth.FETCH_GROUPS_ENDPOINT";
	public static final String USE_STATE_PARAMETER = "miniorange.oauth.USE_STATE_PARAMETER";
	public static final String SEND_TOKEN_AUTHORIZATION_PARAMETERS_IN = "miniorange.oauth.SEND_TOKEN_AUTHORIZATION_PARAMETERS_IN";
	public static final String DEFAULT_IDP_ID ="miniorange.oauth.DEFAULT_IDP_ID";
	public static final String ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS = "newUsers";
	public static final String IDP_LIST = "miniorange.oauth.IDP_LIST";
	public static final String IDP_MAP = "miniorange.oauth.IDP_MAP";
	public static final String IDP_CONFIG = "miniorange.oauth.IDP_CONFIG";
	public static final String IDP_ID = "miniorange.oauth.IDP_ID";
	public static final String ENABLE_DEFAULT_GROUPS_FOR_NO_USERS = "noUsers";
	public static final String ENABLE_DEFAULT_GROUPS_FOR_ALL_USERS = "allUsers";

	public enum OAuthConfiguration {
		CUSTOM_CALLBACK_PARAMETER("miniorange.oauth.CUSTOM_CALLBACK_PARAMETER"), 
		CUSTOM_APP_NAME("miniorange.oauth.CUSTOM_APP_NAME"),
		DEFAULT_APP_NAME("miniorange.oauth.DEFAULT_APP_NAME"),
		CLIENT_ID("miniorange.oauth.CLIENT_ID"),
		CLIENT_SECRET("miniorange.oauth.CLIENT_SECRET"),
		AUTHORIZE_END_POINT("miniorange.oauth.AUTHORIZE_END_POINT"),
		ACCESSTOKEN_END_POINT("miniorange.oauth.ACCESSTOKEN_END_POINT"),
		USERINFO_END_POINT("miniorange.oauth.USERINFO_END_POINT"),
		FETCH_GROUPS_ENDPOINT("miniorange.oauth.FETCH_GROUPS_ENDPOINT"),
		HOST_NAME("miniorange.oauth.HOST_NAME"),
		DOMAIN_NAME("miniorange.oauth.DOMAIN_NAME"),
		REALM_NAME("miniorange.oauth.REALM_NAME"),
		USE_STATE_PARAMETER("miniorange.oauth.USE_STATE_PARAMETER"),
		ACR_VALUE_CHECK("miniorange.oauth.ACR_VALUE_CHECK"),
		NONCE_CHECK("miniorange.oauth.NONCE_CHECK"), 
		PUBLIC_KEY("miniorange.oauth.PUBLIC_KEY"),
		JWKS_END_POINT("miniorange.oauth.JWKS_END_POINT"),
		VALIDATE_SIGNATURE_METHOD("miniorange.oauth.VALIDATE_SIGNATURE_METHOD"),
		CHECK_ISSUER_FOR("miniorange.oauth.CHECK_ISSUER_FOR"),
		CUSTOM_ISSUER_VALUE("miniorange.oauth.CUSTOM_ISSUER_VALUE"),
		SCOPE("miniorange.oauth.SCOPE"),
		ENABLE_CHECK_ISSUER_FOR("miniorange.oauth.ENABLE_CHECK_ISSUER_FOR"),
		SINGLE_LOGOUT_URL("miniorange.oauth.SINGLE_LOGOUT_URL"),
		SHOW_REST_API_MSG("miniorange.oauth.SHOW_REST_API_MSG"),
		OAUTH_REQUEST_PARAMETERS("miniorange.oauth.OAUTH_REQUEST_PARAMETERS"),
		TENANT_ID("miniorange.oauth.TENANT_ID");

		private String key;

		OAuthConfiguration(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}

	/* Attribute mapping tab */
	public static enum AttributeMapping {

		EMAIL_ATTRIBUTE("miniorange.oauth.EMAIL_ATTRIBUTE"),
		FIRST_NAME_ATTRIBUTE("miniorange.oauth.FIRST_NAME_ATTRIBUTE"),
		LAST_NAME_ATTRIBUTE("miniorange.oauth.LAST_NAME_ATTRIBUTE"),
		FULL_NAME_ATTRIBUTE("miniorange.oauth.FULL_NAME_ATTRIBUTE"),
		USERNAME_ATTRIBUTE("miniorange.oauth.USERNAME_ATTRIBUTE"),
		USE_SEPARATE_NAME_ATTRIBUTE("miniorange.oauth.USE_SEPARATE_NAME_ATTRIBUTE"),
		KEEP_EXISTING_USER_ATTRIBUTES("miniorange.oauth.KEEP_EXISTING_USER_ATTRIBUTES"),
		LOGIN_USER_BY_ATTRIBUTE("miniorange.oauth.LOGIN_USER_BY_ATTRIBUTE"),
		ENABLE_REGEX_PATTERN("miniorange.oauth.ENABLE_REGEX_PATTERN"),
		REGEX_PATTERN("miniorange.oauth.REGEX_PATTERN"),
		CUSTOM_ATTRIBUTE_MAPPING("miniorange.oauth.CUSTOM_ATTRIBUTE_MAPPING");
		private String key;

		AttributeMapping(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}

	public static final String GOOGLE_REFRESH_TOKEN = "miniorange.oauth.GOOGLE_REFRESH_TOKEN";
	public static final String RESET_SETTINGS = "miniorange.oauth.RESET_SETTINGS";
	public static final String IS_TEST_CONFIGURATION_CLICKED = "miniorange.oauth.IS_TEST_CONFIGURATION_CLICKED";
	public static final String IS_VERIFY_CREDENTIALS_CLICKED = "miniorange.oauth.IS_VERIFY_CREDENTIALS_CLICKED";
	public static final String IS_CREDENTIALS_VERIFIED = "miniorange.oauth.IS_CREDENTIALS_VERIFIED";

	/* GROUP mapping tab */
	public static enum GroupMapping {
		KEEP_EXISTING_USER_ROLES("miniorange.oauth.KEEP_EXISTING_USER_ROLES"),
		DEFAULT_GROUP("miniorange.oauth.DEFAULT_GROUP"),
		DEFAULT_GROUPS("miniorange.oauth.DEFAULT_GROUPS"),
		CREATE_USER_IF_ROLE_MAPPED("miniorange.oauth.CREATE_USER_IF_ROLE_MAPPED"),
		ROLE_ATTRIBUTE("miniorange.oauth.ROLE_ATTRIBUTE"),
		ON_THE_FLY_FILTER_IDP_GROUPS_OPTION("miniorange.oauth.ON_THE_FLY_FILTER_IDP_GROUPS_OPTION"),
		ON_THE_FLY_FILTER_IDP_GROUPS_KEY("miniorange.oauth.ON_THE_FLY_FILTER_IDP_GROUPS_KEY"),
		ROLE_MAPPING("miniorange.oauth.ROLE_MAPPING"),
		GROUP_REGEX_PATTERN("miniorange.oauth.GROUP_REGEX_PATTERN"),
		ON_THE_FLY_GROUP_MAPPING("miniorange.oauth.ON_THE_FLY_GROUP_MAPPING"),
		ON_THE_FLY_DO_NOT_REMOVE_GROUPS("miniorange.oauth.ON_THE_FLY_DO_NOT_REMOVE_GROUPs"),
		DO_NOT_CREATE_USER("miniorange.oauth.DO_NOT_CREATE_USER"),
		ENABLE_DEFAULT_GROUPS_FOR("miniorange.oauth.ENABLE_DEFAULT_GROUPS_FOR"),
		ENABLE_DEFAULT_GROUPS_FOR_ALL_USERS("allUsers"),
		ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS("newUsers"),
		ENABLE_DEFAULT_GROUPS_FOR_NO_USERS("noUsers"),
		GROUP_REGEX_PATTERN_ENABLED("miniorange.oauth.GROUP_REGEX_PATTER_ENABLED"),
		REGEX_GROUPS("miniorange.oauth.REGEX_GROUPS"),
		REGEX_PATTERN_FOR_GROUPS("miniorange.oauth.REGEX_PATTERN_FOR_GROUPS"),
		TEST_REGEX("miniorange.oauth.TEST_REEGX"),
		CREATE_NEW_GROUPS("miniorange.oauth.CREATE_NEW_GROUPS"),
		ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY("miniorange.oauth.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY"),
		RESTRICT_USER_CREATION("miniorange.oauth.RESTRICT_USER_CREATION"),
		GROUP_MAPPING_CONFIG("miniorange.oauth.GROUP_MAPPING_CONFIG");
 
		private String key;

		GroupMapping(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}

	public static final String DEFAULT_GROUPS_REGEX_PATTERN = "confluence";

	/*SIGN In Settings Tab*/

	public static enum SignInSettings {

		DISABLE_DEFAULT_LOGIN("miniorange.oauth.DISABLE_DEFAULT_LOGIN"),
		DISABLE_ANONYMOUS_ACCESS("miniorange.oauth.DISABLE_ANONYMOUS_ACCESS"),
		ALLOW_GUEST_LOGIN("miniorange.oauth.ALLOW_GUEST_LOGIN"),
		GUEST_SESSION_TIMEOUT("miniorange.oauth.GUEST_SESSION_TIMEOUT"),
		ENABLE_BACKDOOR("miniorange.oauth.ENABLE_BACKDOOR"),
		ENABLE_AUTO_REDIRECT_DELAY("miniorange.saml.ENABLE_AUTO_REDIRECT_DELAY"),
		AUTO_REDIRECT_DELAY_INTERVAL("miniorange.oauth.AUTO_REDIRECT_DELAY_INTERVAL"),
		LOGIN_BUTTON_TEXT("miniorange.oauth.LOGIN_BUTTON_TEXT"),
		RELAY_STATE("miniorange.oauth.RELAY_STATE"),
		ALLOWED_DOMAINS("miniorange.oauth.ALLOWED_DOMAINS"),
		SSO_BUTTON_LOCATION("miniorange.oauth.SSO_BUTTON_LOCATION"),
		CUSTOM_LOGOUT_URL("miniorange.oauth.CUSTOM_LOGOUT_URL"),
		ADMIN_SESSION_OPTION("miniorange.oauth.ADMIN_SESSION_OPTION"),
//		DOMAINS("miniorange.oauth.DOMAINS"),
//		DOMAIN_MAPPING("miniorange.oauth.DOMAIN_MAPPING"),
//		USE_DOMAIN_MAPPING("miniorange.oauth.USE_DOMAIN_MAPPING"),
//		GROUP_MAPPING_CONFIG("miniorange.oauth.GROUP_MAPPING_CONFIG"),
//		ATTRIBUTE_MAPPING_CONFIG("miniorange.oauth.ATTRIBUTE_MAPPING_CONFIG"),
		BACKDOOR_KEY("miniorange.oauth.BACKDOOR_KEY"),
		BACKDOOR_VALUE("miniorange.oauth.BACKDOOR_VALUE"),
		RESTRICT_BACKDOOR("miniorange.oauth.RESTRICT_BACKDOOR"),
		BACKDOOR_GROUPS("miniorange.oauth.BACKDOOR_GROUPS"),
		AUTO_ACTIVATE_USER("miniorange.oauth.AUTO_ACTIVATE_USER"),
		CHECK_IDP_SESSION("miniorange.oauth.CHECK_IDP_SESSION"),
		ENABLE_LOGIN_TEMPLATE("miniorange.oauth.ENABLE_LOGIN_TEMPLATE"),
		LOGIN_TEMPLATE("miniorange.oauth.LOGIN_TEMPLATE"),
		PLUGIN_API_ACCESS_RESTRICTION("miniorange.oauth.PLUGIN_API_ACCESS_RESTRICTION"),
		ENABLE_OAUTH_SSO ("miniorange.oauth.ENABLE_OAuth_SSO"),
		ERROR_MESSAGE_MAPPING("miniorange.oauth.ERROR_MESSAGE_MAPPING");


		private String key;

		SignInSettings(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}
	
	public static enum SessionManagement {
		USER_SESSION_TIMEOUT("miniorange.oauth.USER_SESSION_TIMEOUT"),
		LOGIN_COOKIE("miniorange.oauth.LOGIN_COOKIE");
		
		private String key;

		SessionManagement(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}

	public static final Map<String,String> applicationProtocols = new HashMap<String,String>(){{
		put("ADFS","openid");
		put("AWS Cognito","openid");
		put("Azure AD","oauth");
		put("Azure B2C","openid");
		put("Discord","oauth");
		put("Facebook","oauth");
		put("GitHub","oauth");
		put("GitHub Enterprise","oauth");
		put("Gitlab","oauth");
		put("Gluu Server","oauth");
		put("Google","oauth");
		put("Meetup","oauth");
		put("miniOrange", "oauth");
		put("Slack","oauth");
		put("Salesforce","oauth");
		put("OKTA","openid");
		put("OpenID","openid");
		put("Custom App","oauth");
		put("Keycloak","openid");
	}};

	public static final String ACCESS_TOKEN = "miniorange.oauth.ACCESS_TOKEN";
	public static final String REFRESH_TOKEN = "miniorange.oauth.REFRESH_TOKEN";
	public static final String RETURN_URL = "miniorange.oauth.RETURN_URL";
	public static final String SP_BASE_URL = "miniorange.oauth.SP_BASE_URL";
	public static final String SESSION_TIME = "miniorange.oauth.SESSION_TIME";

	public static final String DEFAULT_CUSTOMER_KEY = "16555";
	public static final String DEFAULT_API_KEY = "fFd2XcvTGDemZvbw1bcUesNJWEqKbbUq";
	public static final String AREA_OF_INTEREST = "Confluence - OAuth Client Plugin";
	public static final String BCC_EMAIL = "info@miniorange.com";
	public static final String AUTH_BASE_URL = "https://login.xecurify.com/moas";
	//	public static final String AUTH_BASE_URL = "https://login.xecurify.com/moas";
	public static final String SUPPORT_QUERY_URL = AUTH_BASE_URL + "/rest/customer/contact-us";
	public static final String NOTIFY_API = AUTH_BASE_URL + "/api/notify/send";


	/* Google App Endpoints */
	public static final String GOOGLE = "Google";
	public static final String GOOGLE_AUTHORIZE_ENDPOINT = "https://accounts.google.com/o/oauth2/auth";
	public static final String GOOGLE_ACCESS_TOKEN_ENDPOINT = "https://www.googleapis.com/oauth2/v3/token";
	public static final String GOOGLE_USER_INFO_ENDPOINT = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&";
	public static final String GOOGLE_FETCH_GROUPS_ENDPOINT = "https://www.googleapis.com/admin/directory/v1/groups";

	/* Facebook App Endpoints */
	public static final String FACEBOOK = "Facebook";
	public static final String FACEBOOK_AUTHORIZE_ENDPOINT = "https://www.facebook.com/dialog/oauth";
	public static final String FACEBOOK_ACCESS_TOKEN_ENDPOINT = "https://graph.facebook.com/v2.8/oauth/access_token";
	public static final String FACEBOOK_USER_INFO_ENDPOINT = "https://graph.facebook.com/me/?fields=id,name,email,age_range,first_name,gender,last_name,link&access_token=";
	public static final String FACEBOOK_FETCH_GROUPS_ENDPOINT = "https://graph.facebook.com/v2.8/";

	/* Microsoft App Endpoints */
	public static final String MICROSOFT = "Microsoft";
	public static final String MICROSOFT_AUTHORIZE_ENDPOINT = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
	public static final String MICROSOFT_ACCESS_TOKEN_ENDPOINT  = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
	public static final String MICROSOFT_USER_INFO_ENDPOINT = "https://graph.microsoft.com/v1.0/me";
	public static final String MICROSOFT_FETCH_GROUPS_ENDPOINT = "https://graph.microsoft.com/v1.0/groups";

	/* Slack App Endpoints */
	public static final String SLACK = "Slack";
	public static final String SLACK_AUTHORIZE_ENDPOINT = "https://slack.com/oauth/authorize";
	public static final String SLACK_ACCESS_TOKEN_ENDPOINT = "https://slack.com/api/oauth.access";
	public static final String SLACK_USER_INFO_ENDPOINT = "https://slack.com/api/users.identity?token=";
	public static final String SLACK_FETCH_GROUPS_ENDPOINT = "https://slack.com/api/usergroups.list";

	/* Discord App Endpoints */
	public static final String DISCORD = "Discord";
	public static final String DISCORD_AUTHORIZE_ENDPOINT = "https://discordapp.com/api/oauth2/authorize";
	public static final String DISCORD_ACCESS_TOKEN_ENDPOINT = "https://discordapp.com/api/oauth2/token";
	public static final String DISCORD_USER_INFO_ENDPOINT = "https://discordapp.com/api/users/@me";
	public static final String DISCORD_FETCH_GROUPS_ENDPOINT = "";

	/* Gitlab App Endpoints */
	public static final String GITLAB = "Gitlab";
	public static final String GITLAB_AUTHORIZE_ENDPOINT = "https://gitlab.com/oauth/authorize";
	public static final String GITLAB_ACCESS_TOKEN_ENDPOINT = "https://gitlab.com/oauth/token";
	public static final String GITLAB_USER_INFO_ENDPOINT = "https://gitlab.com/api/v4/user";
	public static final String GITLAB_FETCH_GROUPS_ENDPOINT = "https://gitlab.com/api/v4/groups";

	/* Meetup App Endpoints */
	public static final String MEETUP = "Meetup";
	public static final String MEETUP_AUTHORIZE_ENDPOINT = "https://secure.meetup.com/oauth2/authorize";
	public static final String MEETUP_ACCESS_TOKEN_ENDPOINT = "https://secure.meetup.com/oauth2/access";
	public static final String MEETUP_USER_INFO_ENDPOINT = "https://api.meetup.com/2/member/self/";
	public static final String MEETUP_FETCH_GROUPS_ENDPOINT = "https://api.meetup.com/self/groups";

	/* Github App Endpoints */
	public static final String GITHUB = "GitHub";
	public static final String GITHUB_AUTHORIZE_ENDPOINT = "https://github.com/login/oauth/authorize";
	public static final String GITHUB_ACCESS_TOKEN_ENDPOINT = "https://github.com/login/oauth/access_token";
	public static final String GITHUB_USER_INFO_ENDPOINT = "https://api.github.com/user";
	public static final String GITHUB_FETCH_GROUPS_ENDPOINT = "";

	/* Keycloak App Endpoints */
	public static final String KEYCLOAK = "Keycloak";
	public static final String KEYCLOAK_AUTHORIZE_ENDPOINT = "/protocol/openid-connect/auth";
	public static final String KEYCLOAK_ACCESS_TOKEN_ENDPOINT = "/protocol/openid-connect/token";
	public static final String KEYCLOAK_USER_INFO_ENDPOINT = "/protocol/openid-connect/userinfo";
	public static final String KEYCLOAK_FETCH_GROUPS_ENDPOINT = "";

	/* AWS Cognito App Endpoints */
	public static final String AWS_COGNITO = "AWS Cognito";

	/* ADFS App Endpoints */
	public static final String ADFS = "ADFS";

	/* Azure AD App Endpoints */
	public static final String AZURE = "Azure AD";
	public static final String AZURE_USER_INFO_ENDPOINT = "https://graph.microsoft.com/v1.0/me";
	public static final String AZURE_FETCH_GROUPS_ENDPOINT = "https://graph.microsoft.com/v1.0/me/memberOf";

	/* Azure B2C App Endpoints */
	public static final String AZURE_B2C = "Azure B2C";
	public static final String AZURE_B2C_USER_INFO_ENDPOINT = "https://login.windows.net/common/openid/userinfo";
	//public static final String AZURE_B2C_FETCH_GROUPS_ENDPOINT = "https://graph.microsoft.com/v1.0/me/memberOf";

	/* Custom Template related keys */
	public static final String ENABLE_LOGOUT_TEMPLATE = "miniornage.oauth.ENABLE_LOGOUT_TEMPLATE";
	public static final String LOGOUT_TEMPLATE = "miniorange.oauth.LOGOUT_TEMPLATE";

	String ENABLE_ERRORMSG_TEMPLATE = "miniornage.oauth.ENABLE_ERRORMSG_TEMPLATE";
	String ERROR_MSG_TEMPLATE = "miniorange.oauth.ERROR_MSG_TEMPLATE";
	String CUSTOM_ERROR_TEMPLATE = "miniorange.oauth.CUSTOM_ERROR_TEMPLATE";
	
	/* Okta App Endpoints */
    String OKTA = "OKTA";

	/* miniOrange App Endpoints */
	String miniOrange = "miniOrange";

    /* Salesforce App Endpoints */
    String SALESFORCE = "Salesforce";

	/*Guest Session Cookie*/
	String GUEST_COOKIE = "miorange.oauth.GUESTCOOKIE";
	/* Gluu App Endpoints */
	String GLUU_SERVER = "Gluu Server";


	//migrating hardcoded request parameter to custom parameters (access_type=offline and include_granted_scope=true)
	String REQUEST_PARAMS_MIGRATED = "miniorange.oauth.REQUEST_PARAMS_MIGRATED";

	/*On the fly filter IDP Groups - option*/
	public static final String ON_THE_FLY_NO_GROUP_FILTER = "None";
	public static final String ON_THE_FLY_FILTER_GROUPS_STARTS_WITH = "Starts with";
	public static final String ON_THE_FLY_FILTER_GROUPS_CONTAINS = "Contains";
	public static final String ON_THE_FLY_FILTER_GROUPS_WITH_REGEX = "Regex";

}