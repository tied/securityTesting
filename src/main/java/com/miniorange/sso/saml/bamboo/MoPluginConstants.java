package com.miniorange.sso.saml.bamboo;

public interface MoPluginConstants {
	
	/*Configure Service Provider related keys*/
	String PLUGIN_NAME = "Bamboo SAML 1.1.19";
	String SP_BASE_URL = "miniorange.saml.SP_BASE_URL";
	String SP_ENTITY_ID = "miniorange.saml.SP_ENTITY_ID";
	String SIGNING = "miniorange.saml.SIGNING";
	String ENCRYPTION = "miniorange.saml.ENCRYPTION";
	
	/*Manual Configuration of Identity Provider related keys*/
	String IDP_ENTITY_ID = "miniorange.saml.IDP_ENTITY_ID";
	String SSO_URL = "miniorange.saml.SSO_URL";
	String SLO_URL = "miniorange.saml.SLO_URL";
	String X509_CERTIFICATE = "miniorange.saml.X509_CERTIFICATE";
	String ALL_X509_CERTIFICATES = "miniorange.saml.ALL_X509_CERTIFICATES";
	String SIGNED_REQUEST = "miniorange.saml.SIGNED_REQUEST";
	String NAME_ID_FORMAT ="miniorange.saml.NAME_ID_FORMAT";
	String AUTHN_CONTEXT_CLASS = "miniorange.saml.AUTHN_CONTEXT_CLASS";
	String OTHER_AUTHN_CONTEXT_CLASS = "miniorange.saml.OTHER_AUTHN_CONTEXT_CLASS";
	String ENABLE_SSO_FOR_IDP = "miniorange.saml.ENABLE_SSO_FOR_IDP";
	String SSO_BINDING_TYPE = "miniorange.saml.SSO_BINDING_TYPE";
	String SLO_BINDING_TYPE = "miniorange.saml.SLO_BINDING_TYPE";
	String SHOW_INTRO_PAGE = "miniorange.saml.SHOW_INTRO_PAGE";
	String PAGE_NUMBER = "miniorange.saml.PAGE_NUMBER";
	
	/*Import from Metadata related keys*/
	String IDP_METADATA_URL = "miniorange.saml.IDP_METADATA_URL";
	String REFRESH_METADATA = "miniorange.saml.REFRESH_METADATA";
	String METADATA_REFRESH_INTERVAL = "miniorange.saml.METADATA_REFRESH_INTERVAL";
	String CUSTOM_REFRESH_INTERVAL = "miniorange.saml.CUSTOM_REFRESH_INTERVAL";
	String CUSTOM_REFRESH_INTERVAL_UNIT = "miniorange.saml.CUSTOM_REFRESH_INTERVAL_UNIT";
	String INPUT_METADATA_URL = "miniorange.saml.INPUT_METADATA_URL";
	String METADATA_OPTION = "miniorange.saml.METADATA_OPTION";
	String SP_CONFIGURATION_STATUS = "miniorange.saml.SP_CONFIGURATION_STATUS";

    /*Multiple IDP keys*/
    String DEFAULT_IDP_ID = "miniorange.saml.DEFAULT_IDP_ID";
    String IDP_NAME = "miniorange.saml.IDP_NAME";
    String IDP_ID = "miniorange.saml.IDP_ID";
    String IDP_LIST = "miniorange.saml.IDP_LIST";
    String IDP_MAP = "miniorange.saml.IDP_MAP";
    String IDP_CONFIG = "miniorange.saml.IDP_CONFIG";
    String DOMAINS = "miniorange.saml.DOMAINS";
    String USE_DOMAIN_MAPPING = "miniorange.saml.USE_DOMAIN_MAPPING";
    String DOMAIN_MAPPING = "miniorange.saml.DOMAIN_MAPPING";
    String IMPORT_METADATA_CONFIG = "miniorange.saml.DOMAIN_MAPPING";
    String ATTRIBUTE_MAPPING_CONFIG = "miniorange.saml.ATTRIBUTE_MAPPING_CONFIG";
    String GROUP_MAPPING_CONFIG = "miniorange.saml.GROUP_MAPPING_CONFIG";

	/*Attribute Mapping related keys*/
	String USERNAME_ATTRIBUTE = "miniorange.saml.USERNAME_ATTRIBUTE";
	String EMAIL_ATTRIBUTE = "miniorange.saml.EMAIL_ATTRIBUTE";
	String FULL_NAME_ATTRIBUTE = "miniorange.saml.FULL_NAME_ATTRIBUTE";
	String USE_SEPARATE_NAME_ATTRIBUTE = "miniorange.saml.USE_SEPARATE_NAME_ATTRIBUTE";
	String KEEP_EXISTING_ATTRIBUTE = "miniorange.saml.KEEP_EXISTING_ATTRIBUTE";
	String FIRST_NAME_ATTRIBUTE = "miniorange.saml.FIRST_NAME_ATTRIBUTE";
	String LAST_NAME_ATTRIBUTE = "miniorange.saml.LAST_NAME_ATTRIBUTE";
	
	String LOGIN_USER_ATTRIBUTE = "miniorange.saml.LOGIN_USER_ATTRIBUTE";
	String ENABLE_REGEX_PATTERN = "miniorange.saml.ENABLE_REGEX_PATTERN";
	String REGEX_PATTERN = "miniorange.saml.REGEX_PATTERN";
	
	/*Group Mapping related key*/
	String ROLE_ATTRIBUTE = "miniorange.saml.ROLE_ATTRIBUTE";
	String DEFAULT_GROUP = "miniorange.saml.DEFAULT_GROUP";
	String DEFAULT_GROUPS = "miniorange.saml.DEFAULT_GROUPs";
	String ROLE_MAPPING = "miniorange.saml.ROLE_MAPPING";
	String CREATE_USER_IF_ROLE_MAPPED = "miniorange.saml.CREATE_USER_IF_ROLE_MAPPED";
	String KEEP_EXISTING_USER_ROLES = "miniorange.saml.KEEP_EXISTING_USER_ROLES";
	String ENABLE_DEFAULT_GROUPS_FOR = "miniorange.saml.ENABLE_DEFAULT_GROUPS_FOR";
	String ON_THE_FLY_GROUP_MAPPING = "miniorange.saml.ON_THE_FLY_GROUP_MAPPING";
	String ON_THE_FLY_DO_NOT_REMOVE_GROUPS = "miniorange.saml.ON_THE_FLY_DO_NOT_REMOVE_GROUPS";
	String ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY = "miniorange.saml.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY";
	String ON_THE_FLY_CREATE_NEW_GROUPS = "miniorange.saml.ON_THE_FLY_CREATE_NEW_GROUPS";
	String ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS = "newUsers";
	String ENABLE_DEFAULT_GROUPS_FOR_ALL_USERS = "allUsers";
	String ENABLE_DEFAULT_GROUPS_FOR_DO_NOT_ASSIGN_DEFAULT_GROUPS = "doNotAssignDefaultGroup";
	String ENABLE_DEFAULT_GROUPS_FOR_USERS_WITH_NO_IDP_GROUPS = "NoIDPGroupUsers";

	//Group regex
	String GROUP_REGEX_PATTERN_ENABLED = "miniorange.saml.GROUP_REGEX_PATTERN_ENABLED";
	String REGEX_PATTERN_FOR_GROUP = "miniorange.saml.REGEX_PATTERN_FOR_GROUP";
	String REGEX_GROUPS = "miniorange.saml.REGEX_GROUPS";
	String TEST_REGEX = "miniorange.saml.TEST_REGEX";

	/*Advanced SSO Options*/
	String ALLOW_USER_CREATION = "miniorange.saml.ALLOW_USER_CREATION";
	String ENABLE_PASSIVE_SSO = "miniorange.saml.ENABLE_PASSIVE_SSO";
	String FORCE_AUTHENTICATION = "miniorange.saml.FORCE_AUTHENTICATION";
	String ADVANCED_SSO_CONFIG = "miniorange.saml.ADVANCED_SSO_CONFIG";
	String ISSUER_MAP = "miniorange.saml.ISSUER_MAP";
	String SSO_ENABLED_FOR_IDP_LIST = "miniorange.saml.SSO_ENABLED_FOR_IDP_LIST";
	String DEFAULT_BAMBOO_IDP = "miniorange.saml.DEFAULT_BAMBOO_IDP";
	String RESTRICT_USER_CREATION = "miniorange.saml.RESTRICT_USER_CREATION";

	/*SSO Settings related key*/
	String DISABLE_DEFAULT_LOGIN = "miniorange.saml.DISABLE_DEFAULT_LOGIN";
	String ENABLE_BACKDOOR = "miniorange.saml.ENABLE_BACKDOOR";
	String ENABLE_AUTO_REDIRECT_DELAY = "miniorange.saml.ENABLE_AUTO_REDIRECT_DELAY";
	String NUMBER_OF_LOGIN_ATTEMPTS="miniorange.saml.NUMBER_OF_LOGIN_ATTEMPTS";
	String BACKDOOR_KEY = "miniorange.saml.BACKDOOR_KEY";
	String BACKDOOR_VALUE = "miniorange.saml.BACKDOOR_VALUE";
	String RESTRICT_BACKDOOR = "miniorange.saml.RESTRICT_BACKDOOR";
	String BACKDOOR_GROUPS = "miniorange.saml.BACKDOOR_GROUPS";
	String LOGIN_BUTTON_TEXT = "miniorange.saml.LOGIN_BUTTON_TEXT";
	String RELAY_STATE = "miniorange.saml.RELAY_STATE";
	String RELAY_STATE_REDIRECTION_TYPE = "miniorange.saml.RELAY_STATE_REDIRECTION_TYPE";
	String ENABLE_LOGOUT_TEMPLATE = "miniorange.saml.ENABLE_LOGOUT_TEMPLATE";
	String CUSTOM_LOGOUT_URL = "miniorange.saml.CUSTOM_LOGOUT_URL";
	String REMEMBERME_COOKIE = "miniorange.saml.REMEMBERME_COOKIE";
	String ENABLE_PASSWORD_CHANGE="miniorange.saml.ENABLE_PASSWORD_CHANGE";
	String QUICKSETUP_IDP="miniorange.saml.QUICKSETUP_IDP";
	String BAMBOO_REDIRECTION_RULES ="miniorange.saml.BAMBOO_REDIRECTION_RULES";
	String REDIRECTION_RULE="miniorange.saml.REDIRECTION_RULE";
	String DEFAULT_REDIRECT_URL="miniorange.saml.DEFAULT_REDIRECT_URL";
	String NO_SSO_URLS = "miniorange.saml.NO_SSO_URLS";

	String IS_MIGRATED = "miniorange.saml.IS_MIGRATED";

	String TIME_DELAY = "miniorange.saml.TIME_DELAY";
	String VALIDATE_SAML_RESPONSE = "miniorange.saml.VALIDATE_SAML_RESPONSE";
	String SESSION_TIME = "miniorange.saml.SESSION_TIME";
	String ENABLE_SAML_SSO = "miniorange.saml.ENABLE_SAML_SSO";
	String AUTO_ACTIVATE_USER = "miniorange.saml.AUTO_ACTIVATE_USER";

	/** Restrict /saml/getconfig and /saml/moapi API within the bamboo domain.  **/
	String PLUGIN_API_ACCESS_RESTRICTION = "miniornage.saml.PLUGIN_API_ACCESS_RESTRICTION";
	String ASSERTIONID_RESET_INTERVAL = "miniorange.saml.ASSERTIONID_RESET_INTERVAL";
	String RESTRICT_DUPLICATE_ASSERTION = "miniorange.saml.RESTRICT_DUPLICATE_ASSERTION";
	String CUSTOM_RESET_INTERVAL = "miniorange.saml.CUSTOM_RESET_INTERVAL";


	/*Custom cert related key*/
	String PUBLIC_SP_CERTIFICATE = "miniorange.saml.PUBLIC_SP_CERTIFICATE";
	String PRIVATE_SP_CERTIFICATE = "miniorange.saml.PRIVATE_SP_CERTIFICATE";
	String OLD_CONFIGURED_PUBLIC_SP_CERTIFICATE="miniorange.saml.OLD_CONFIGURED_PUBLIC_SP_CERTIFICATE";
	String OLD_CONFIGURED_PRIVATE_SP_CERTIFICATE="miniorange.saml.OLD_CONFIGURED_PRIVATE_SP_CERTIFICATE";
	String SP_CERTIFICATE_EXPIRE_ON="miniorange.saml.SP_CERTIFICATE_EXPIRE_ON";
	String CERT_EXPIRED = "miniorange.saml.CERT_EXPIRED";
	
	/*Custom template related key*/
	String ERROR_MSG_TEMPLATE = "miniorange.saml.ERROR_MSG_TEMPLATE";
	String LOGOUT_TEMPLATE = "miniorange.saml.LOGOUT_TEMPLATE";
	String ENABLE_ERRORMSG_TEMPLATE = "miniorange.saml.ENABLE_ERRORMSG_TEMPLATE";
	String SHOW_LOGIN_BUTTONS = "miniorange.saml.SHOW_LOGIN_BUTTON";

	String ENABLE_LOGIN_TEMPLATE = "miniorange.saml.ENABLE_LOGIN_TEMPLATE";
	String LOGIN_TEMPLATE = "miniorange.saml.LOGIN_TEMPLATE";

	/*Header Based Authentication*/
	String HEADER_AUTHENTICATION_ATTRIBUTE = "miniorange.saml.HEADER_AUTHENTICATION_ATTRIBUTE";
	String ENABLE_HEADER_AUTHENTICATION = "miniorange.saml.ENABLE_HEADER_AUTHENTICATION";
	String HEADER_AUTHENTICATION_RELAY_STATE_ATTRIBUTE = "miniorange.saml.HEADER_AUTHENTICATION_RELAY_STATE_ATTRIBUTE";

	/*Custom Metadata*/
	String ORGANIZATION_NAME ="miniorange.saml.ORGANIZATION_NAME";
	String ORGANIZATION_DISPLAY_NAME="miniorange.saml.ORGANIZATION_DISPLAY_NAME";
	String ORGANIZATION_URL="miniorange.saml.ORGANIZATION_URL";
	String TECHNICAL_CONTACT_NAME="miniorange.saml.TECHNICAL_CONTACT_NAME";
	String TECHNICAL_CONTACT_EMAIL="miniorange.saml.TECHNICAL_CONTACT_DISPLAY_NAME";
	String SUPPORT_CONTACT_NAME="miniorange.saml.SUPPORT_CONTACT_NAME";
	String SUPPORT_CONTACT_EMAIL="miniorange.saml.SUPPORT_CONTACT_EMAIL";
	/*Customer license related keys*/
	//String ACTIVE_USERS = "bamboo.sso.mo.active.users";
	//String LICENCED_USERS = "bamboo.sso.mo.lu";
	//String USER_EXCEEDED_ALERT_EMAILS_SENT = "miniorange.saml.USER_EXCEEDED_ALERT_EMAILS_SENT";
	//Integer USER_EXCEEDED_ALERT_EMAILS_ALLOWED = 3;
	//String IS_OLD_CUSTOMER = "bamboo.sso.mo.ioc";
	
	/* Customer related keys */
	//String CUSTOMER_ID = "miniorange.saml.CUSTOMER_ID";
	//String CUSTOMER_EMAIL = "miniorange.saml.CUSTOMER_EMAIL";
	//String CUSTOMER_API_KEY = "miniorange.saml.CUSTOMER_API_KEY";
	//String CUSTOMER_TOKEN_KEY = "miniorange.saml.CUSTOMER_TOKEN_KEY";
	//String CUSTOMER_LICENSE_KEY = "miniorange.saml.CUSTOMER_LICENSE_KEY";
	//String LICENSE_VERIFIED = "miniorange.saml.LICENSE_VERIFIED";

	/* Customer registration related URLs and default values */
	String AUTH_BASE_URL = "https://auth.miniorange.com/moas";
	String CUSTOMER_FETCH_URL = AUTH_BASE_URL + "/rest/customer/key";
	String CUSTOMER_VERIFY_URL = AUTH_BASE_URL + "/rest/customer/check-if-exists";
	String CUSTOMER_CREATE_URL = AUTH_BASE_URL + "/rest/customer/add";
	String VERIFY_LICENSE_URL = AUTH_BASE_URL + "/api/backupcode/verify";
	String CHECK_CUSTOMER_LICENSE = AUTH_BASE_URL + "/rest/customer/license";
	String DEACTIVATE_LICENSE_URL = AUTH_BASE_URL + "/api/backupcode/updatestatus";
	String SUPPORT_QUERY_URL = AUTH_BASE_URL + "/rest/customer/contact-us";
	String NOTIFY_API = AUTH_BASE_URL + "/api/notify/send";
	String CHALLENGE_URL = AUTH_BASE_URL + "/api/auth/challenge";
	String VALIDATE_URL = AUTH_BASE_URL + "/api/auth/validate";

	String DEFAULT_CUSTOMER_KEY = "16555";
	String DEFAULT_API_KEY = "fFd2XcvTGDemZvbw1bcUesNJWEqKbbUq";
	String AREA_OF_INTEREST = "Bamboo - SAML Single Sign On Plugin";
	
	String DEFAULT_AUTOCREATE_USER_GROUP = "bamboo-users";
	String BCC_EMAIL = "info@xecurify.com";

	String IDP_ID_COOKIE = "mo.bamboo-sso.IDPIDCOOKIE";

	//Relay State Redirection Type
	String FORCE_REDIRECT = "forceRedirect";
	String REDIRECT_ON_NO_RELAY_STATE = "redirectOnNoRelayState";

	enum idpGuides{
		ADFS("https://plugins.miniorange.com/saml-single-sign-on-for-bamboo-using-adfs?version=2.0.0"),
		Azure_AD("https://plugins.miniorange.com/saml-single-sign-into-bamboo-using-azure-ad?version=2.0.0"),
		G_Suite("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-google-apps?version=2.0.0"),
		Centrify("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-centrify?version=2.0.0"),
		Okta("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-okta?version=2.0.0"),
		OneLogin("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-onelogin?version=2.0.0"),
		Salesforce("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-salesforce?version=2.0.0"),
		JBoss_Keycloak("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-jboss-keyclock?version=2.0.0"),
		Oracle("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-oracle-enterprise-manager?version=2.0.0"),
		Bitium("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-bitium?version=2.0.0"),
		PingFederate("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-ping-federate?version=2.0.0"),
		WSO2("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-wso2?version=2.0.0"),
		OpenAM("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-openam?version=2.0.0"),
		miniOrange("https://plugins.miniorange.com/saml-single-sign-into-bamboo-using-miniorange?version=2.0.0"),
		SimpleSAMLphp("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-simplesaml?version=2.0.0"),
		Shibboleth_2("https://plugins.miniorange.com/saml-single-sign-into-bamboo-using-shibboleth-2?version=2.0.0"),
		Shibboleth_3("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-shibboleth-3?version=2.0.0"),
		Ping_One("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-ping-one?version=2.0.0"),
		AuthAnvil("https://plugins.miniorange.com/saml-single-sign-on-sso-for-bamboo-using-authanvil?version=2.0.0"),
		Auth0("https://plugins.miniorange.com/saml-single-sign-sso-into-bamboo-using-auth0?version=2.0.0"),
		CA_Identity("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-ca-identity?version=2.0.0"),
		RSA_SecurID("https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-rsa-securid?version=2.0.0"),
		AWS("https://plugins.miniorange.com/saml-single-sign-on-sso-into-bamboo-using-aws-idp?version=2.0.0"),
		custom("https://plugins.miniorange.com/setup-saml-single-sign-on-sso-for-bamboo?version=2.0.0");

		private String key;

		idpGuides(String key){ this.key = key; }

		public String getKey(){ return this.key; }
	}
}
