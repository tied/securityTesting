package com.miniorange.oauth.bamboo;

public class MoOAuthPluginConstants {

	public static final String PLUGIN_NAME = "Bamboo OAuth 1.0.8";
	public static final String LOGOUTCOOKIE = "mo.bamboo-oauth.logoutcookie";
	public static final String REMEMBERME_COOKIE = "miniorange.oauth.REMEMBERME_COOKIE";
	/* OAuth Configuration */
	public enum OAuthConfiguration  {
		CUSTOM_APP_NAME("miniorange.oauth.CUSTOM_APP_NAME"),
		DEFAULT_APP_NAME("miniorange.oauth.DEFAULT_APP_NAME"),
		APP_HOSTED_ON("miniorange.oauth.APP_HOSTED_ON"),
		CLIENT_ID("miniorange.oauth.CLIENT_ID"),
		CLIENT_SECRET("miniorange.oauth.CLIENT_SECRET"),
        CUSTOMIZABLE_CALLBACK_URL("miniorange.oauth.CUSTOMIZABLE_CALLBACK_URL"),
		SCOPE("miniorange.oauth.SCOPE"),
		AUTHORIZE_END_POINT("miniorange.oauth.AUTHORIZE_END_POINT"),
		ACCESS_TOKEN_END_POINT("miniorange.oauth.ACCESSTOKEN_END_POINT"),
		USER_INFO_END_POINT("miniorange.oauth.USERINFO_END_POINT"),
		FETCH_GROUPS_ENDPOINT("miniorange.oauth.FETCH_GROUPS_ENDPOINT"),
		DOMAIN_NAME("miniorange.oauth.DOMAIN_NAME"),
		HOST_NAME("miniorange.oauth.HOST_NAME"),
		REALM_NAME("miniorange.oauth.REALM_NAME"),
		TENANT_ID("miniorange.oauth.TENANT_ID"),
		PUBLIC_KEY("miniorange.oauth.PUBLIC_KEY"),
		USE_STATE_PARAMETER("miniorange.oauth.USE_STATE_PARAMETER"),
		CHECK_ISSUER_FOR("miniorange.oauth.CHECK_ISSUER_FOR"),
		CUSTOM_ISSUER_VALUE("miniorange.oauth.CUSTOM_ISSUER_VALUE"),
		ENABLE_CHECK_ISSUER_FOR("miniorange.oauth.ENABLE_CHECK_ISSUER_FOR"),
		NONCE_VALIDATION ("miniorange.oauth.NONCE_VALIDATION "),
		ACR_VALUE_CHECK("miniorange.oauth.ACR_VALUE_CHECK"),
		OAUTH_REQUEST_PARAMETERS("miniorange.oauth.OAUTH_REQUEST_PARAMETERS");

		private String key;
		OAuthConfiguration(String key) {
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

	/* Attribute mapping */
	public enum AttributeMapping {
		DISABLE_ATTRIBUTE_MAPPING( "miniorange.oauth.KEEP_EXISTING_USER_ATTRIBUTES"),
		LOGIN_USER_BY_ATTRIBUTE( "miniorange.oauth.LOGIN_USER_BY_ATTRIBUTE"),
		USERNAME_ATTRIBUTE( "miniorange.oauth.USERNAME_ATTRIBUTE"),
		ENABLE_REGEX_PATTERN( "miniorange.oauth.ENABLE_REGEX_PATTERN"),
		REGEX_PATTERN( "miniorange.oauth.REGEX_PATTERN"),
		EMAIL_ATTRIBUTE("miniorange.oauth.EMAIL_ATTRIBUTE"),
		FULL_NAME_ATTRIBUTE( "miniorange.oauth.FULL_NAME_ATTRIBUTE"),
		USE_SEPARATE_NAME_ATTRIBUTE( "miniorange.oauth.USE_SEPARATE_NAME_ATTRIBUTE"),
		FIRST_NAME_ATTRIBUTE("miniorange.oauth.FIRST_NAME_ATTRIBUTE"),
		LAST_NAME_ATTRIBUTE( "miniorange.oauth.LAST_NAME_ATTRIBUTE");

		private String key;
		AttributeMapping(String key) {
			this.key = key;
		}
		public String getKey() {
			return key;
		}
	}

	/*Group Mapping */
	public enum GroupMapping {
		DISABLE_USER_CREATION("miniorange.oauth.DO_NOT_CREATE_USER"),	//RESTRICT_USER_CREATION
		DEFAULT_GROUP("miniorange.oauth.DEFAULT_GROUP"),
		DEFAULT_GROUPS("miniorange.oauth.DEFAULT_GROUPS"),
		ASSIGN_DEFAULT_GROUP_TO("miniorange.oauth.ENABLE_DEFAULT_GROUPS_FOR"),
		ON_THE_FLY_GROUP_MAPPING("miniorange.oauth.ON_THE_FLY_GROUP_MAPPING"),
		DISABLE_GROUP_MAPPING("miniorange.oauth.KEEP_EXISTING_USER_ROLES"),
		GROUP_ATTRIBUTE("miniorange.oauth.ROLE_ATTRIBUTE"),
		ON_THE_FLY_FILTER_IDP_GROUPS_OPTION("miniorange.oauth.ON_THE_FLY_FILTER_IDP_GROUPS_OPTION"),
		ON_THE_FLY_FILTER_IDP_GROUPS_KEY("miniorange.oauth.ON_THE_FLY_FILTER_IDP_GROUPS_KEY"),
		CREATE_USER_IF_ROLE_MAPPED("miniorange.oauth.CREATE_USER_IF_ROLE_MAPPED"),
		ROLE_MAPPING("miniorange.oauth.ROLE_MAPPING"),
		ON_THE_FLY_CREATE_NEW_GROUPS("miniorange.oauth.ON_THE_FLY_CREATE_NEW_GROUPS"),
		ON_THE_FLY_KEEP_EXISTING_USERS_GROUPS("miniorange.oauth.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY"),
		ON_THE_FLY_EXCLUDE_GROUPS("miniorange.oauth.ON_THE_FLY_DO_NOT_REMOVE_GROUPs"),
		//Group regex
		GROUP_REGEX_PATTERN_ENABLED("miniorange.saml.GROUP_REGEX_PATTERN_ENABLED"),
		REGEX_PATTERN_FOR_GROUP("miniorange.saml.REGEX_PATTERN_FOR_GROUP"),
		REGEX_GROUPS("miniorange.saml.REGEX_GROUPS"),
		TEST_REGEX("miniorange.saml.TEST_REGEX");

		private String key;
		GroupMapping(String key) {
			this.key = key;
		}
		public String getKey() {
			return key;
		}
	}
	public static final String ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS = "newUsers";
	public static final String ENABLE_DEFAULT_GROUPS_FOR_ALL_USERS = "allUsers";
	public static final String ENABLE_DEFAULT_GROUPS_FOR_DO_NOT_ASSIGN_DEFAULT_GROUPS = "doNotAssignDefaultGroup";


	/*Sign In Settings */
	public enum SignInSettings  {
		DISABLE_DEFAULT_LOGIN("miniorange.oauth.DISABLE_DEFAULT_LOGIN"),
		LOGIN_BUTTON_TEXT("miniorange.oauth.LOGIN_BUTTON_TEXT"),
		SSO_BUTTON_LOCATION("miniorange.oauth.SSO_BUTTON_LOCATION"),
		RELAY_STATE("miniorange.oauth.RELAY_STATE"),
		ENABLE_BACKDOOR("miniorange.oauth.ENABLE_BACKDOOR"),
		ENABLE_AUTO_REDIRECT_DELAY("miniorange.oauth.ENABLE_AUTO_REDIRECT_DELAY"),
		AUTO_REDIRECT_DELAY_INTERVAL("miniorange.oauth.AUTO_REDIRECT_DELAY_INTERVAL"),
		RESTRICT_BACKDOOR("miniorange.oauth.RESTRICT_BACKDOOR"),
		BACKDOOR_GROUPS("miniorange.oauth.BACKDOOR_GROUPS"),
		ALLOWED_DOMAINS("miniorange.oauth.ALLOWED_DOMAINS"),
		CUSTOM_LOGOUT_URL("miniorange.oauth.CUSTOM_LOGOUT_URL"),
		ENABLE_LOGIN_TEMPLATE("miniorange.oauth.ENABLE_LOGIN_TEMPLATE"),
		LOGIN_TEMPLATE("miniorange.oauth.LOGIN_TEMPLATE"),
		ENABLE_LOGOUT_TEMPLATE("miniorange.oauth.ENABLE_LOGOUT_TEMPLATE"),
		LOGOUT_TEMPLATE("miniorange.oauth.LOGOUT_TEMPLATE"),
		ENABLE_ERROR_MSG_TEMPLATE("miniorange.oauth.ENABLE_ERROR_MSG_TEMPLATE"),
		ERROR_MSG_TEMPLATE("miniorange.oauth.ERROR_MSG_TEMPLATE"),
		DEFAULT_LOGOUT_TEMPLATE("miniorange.oauth.DEFAULT_LOGOUT_TEMPLATE"),
		PLUGIN_API_ACCESS_RESTRICTION("miniorange.oauth.PLUGIN_API_ACCESS_RESTRICTION"),
		BACKDOOR_KEY("miniorange.oauth.BACKDOOR_KEY"),
		BACKDOOR_VALUE("miniorange.oauth.BACKDOOR_VALUE");
		private String key;
		SignInSettings(String key) {
			this.key = key;
		}
		public String getKey() {
			return key;
		}
	}
	public static final String CONFIGURATION_STATUS = "miniorange.oauth.CONFIGURATION_STATUS";

	public static final String ACCESS_TOKEN = "miniorange.oauth.ACCESS_TOKEN";
	public static final String REFRESH_TOKEN = "miniorange.oauth.REFRESH_TOKEN";
	public static final String RETURN_URL = "miniorange.oauth.RETURN_URL";
	public static final String SP_BASE_URL = "miniorange.oauth.SP_BASE_URL";
	public static final String SESSION_TIME ="miniorange.oauth.SESSION_TIME";

	public static final String USER_INFO_MAP = "miniorange.oauth.USER_INFO_MAP";

	public static final String DEFAULT_CUSTOMER_KEY = "16555";
	public static final String DEFAULT_API_KEY = "fFd2XcvTGDemZvbw1bcUesNJWEqKbbUq";
	public static final String AREA_OF_INTEREST = "Bamboo - OAuth Client Plugin";
	public static final String BCC_EMAIL = "info@miniorange.com";

	public static final String AUTH_BASE_URL = "https://auth.miniorange.com/moas";
	public static final String SUPPORT_QUERY_URL = AUTH_BASE_URL + "/rest/customer/contact-us";
	public static final String CODE = "code";
	public static final String NOTIFY_API = AUTH_BASE_URL + "/api/notify/send";

	public static final String SINGLE_LOGOUT_URL = "miniorange.oauth.SINGLE_LOGOUT_URL";



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

	/* Slack App Endpoints */
	public static final String SLACK = "Slack";
	public static final String SLACK_AUTHORIZE_ENDPOINT = "https://slack.com/oauth/authorize";
	public static final String SLACK_ACCESS_TOKEN_ENDPOINT = "https://slack.com/api/oauth.access";
	public static final String SLACK_USER_INFO_ENDPOINT = "https://slack.com/api/users.identity?token=";
	public static final String SLACK_FETCH_GROUPS_ENDPOINT = "https://slack.com/api/usergroups.list";

	/* Discord App Endpoints */
	public static final String DISCORD ="Discord";
	public static final String DISCORD_AUTHORIZE_ENDPOINT = "https://discordapp.com/api/oauth2/authorize";
	public static final String DISCORD_ACCESS_TOKEN_ENDPOINT = "https://discordapp.com/api/oauth2/token";
	public static final String DISCORD_USER_INFO_ENDPOINT = "https://discordapp.com/api/users/@me";
	public static final String DISCORD_FETCH_GROUPS_ENDPOINT = "";

	/* Gitlab App Endpoints */
	public static final String GITLAB = "GitLab";
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

	/* Github App Endpoints */
	public static final String GITHUB = "GitHub";
	public static final String GITHUB_AUTHORIZE_ENDPOINT = "https://github.com/login/oauth/authorize";
	public static final String GITHUB_ACCESS_TOKEN_ENDPOINT = "https://github.com/login/oauth/access_token";
	public static final String GITHUB_USER_INFO_ENDPOINT = "https://api.github.com/user";
	public static final String GITHUB_FETCH_GROUPS_ENDPOINT = "";

	/* Github Enterprise App Endpoints */
	public static final String  GITHUB_ENTERPRISE = "GitHub Enterprise";
	// other endpoints are same as GitHub

	/* Azure AD App Endpoints */
	public static final String AZURE = "Azure AD";
	public static final String AZURE_USER_INFO_ENDPOINT = "https://graph.microsoft.com/v1.0/me";
	public static final String AZURE_FETCH_GROUPS_ENDPOINT = "https://graph.microsoft.com/v1.0/me/memberOf";

	/* Azure B2C App Endpoints */
	public static final String AZURE_B2C = "Azure B2C";
	// other endpoints are same as Azure AD

	/* Okta App Endpoints */
	public static final String OKTA = "Okta";

	/* miniOrange App Endpoints */
	public static final String miniOrange = "miniOrange";

	/* Salesforce App Endpoints */
	public static final String SALESFORCE = "Salesforce";

	/*Gluu App endpoints*/
    public static final String GLUU_SERVER = "Gluu Server";
	public static final String GLUU_AUTHORIZE_ENDPOINT = "/oxauth/restv1/authorize";
	public static final String GLUU_ACCESS_TOKEN_ENDPOINT = "/oxauth/restv1/token";
	public static final String GLUU_USER_INFO_ENDPOINT = "/oxauth/restv1/userinfo";
	/* Add more Default Apps here  */

	public enum OAuthApps{
		AZURE("Azure AD"),
		DISCORD("Discord"),
		FACEBOOK("Facebook"),
		GOOGLE("Google"),
		GITHUB("GitHub"),
		GITHUB_ENTERPRISE("GitHub Enterprise"),
		GITLAB("GitLab"),
		MEETUP("Meetup"),
		miniOrange("miniOrange"),
		SALESFORCE("Salesforce"),
		SLACK("Slack"),
        GLUU_SERVER("Gluu Server"),
		CUSTOM_OAUTH("Custom OAuth");
		private String appName;
		OAuthApps(String appName){
			this.appName = appName;
		}
		public String getAppName() {
			return appName;
		}
	}

	public enum OpenIdApps{
		ADFS("ADFS"),
		AWS_COGNITO("AWS Cognito"),
		AZURE_B2C("Azure B2C"),
		KEYCLOAK("Keycloak"),
		OKTA("Okta"),
		CUSTOM_OPENID("Custom OpenID");
		private String appName;
		OpenIdApps(String appName) {
			this.appName = appName;
		}
		public String getAppName() {
			return appName;
		}
	}

	/*GitLab Hosting Types*/
	public static final String CLOUD_HOSTING = "cloud";
	public static final String SELF_HOSTING = "self-hosted";

	/*On The Fly IDP Group Filter Options*/
	public static final String ON_THE_FLY_NO_GROUP_FILTER = "None";
	public static final String ON_THE_FLY_FILTER_GROUPS_STARTS_WITH = "Starts with";
	public static final String ON_THE_FLY_FILTER_GROUPS_CONTAINS = "Contains";
	public static final String ON_THE_FLY_FILTER_GROUPS_WITH_REGEX = "Regex";

}
