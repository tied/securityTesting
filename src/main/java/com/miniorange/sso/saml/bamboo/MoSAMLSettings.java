package com.miniorange.sso.saml.bamboo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.util.BuildUtils;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.miniorange.sso.saml.utils.MoJSONUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.bamboo.buildqueue.manager.AgentManager;
import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.v2.build.agent.BuildAgent;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.Contact;
import com.atlassian.user.Group;
import com.atlassian.user.search.page.Pager;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import com.atlassian.plugin.PluginAccessor;

public class MoSAMLSettings {

	private PluginSettings pluginSettings;
	private PluginSettingsFactory pluginSettingsFactory;
	private AgentManager agentManager;
	private AdministrationConfigurationAccessor administrationConfigurationAccessor;
	private PluginLicenseManager pluginLicenseManager;
	private static Log LOGGER = LogFactory.getLog(MoSAMLSettings.class);
	private static final String PUBLIC_CERT_PATH = "/certificates/com/miniorange/sso/saml/bamboo/sp-certificate.crt";
	private static final String PRIVATE_CERT_PATH = "/certificates/com/miniorange/sso/saml/bamboo/sp-key.key";
	private static final String NEW_PUBLIC_CERT_PATH = "/certificates/com/miniorange/sso/saml/bamboo/new-sp-certificate.crt";
	private static final String NEW_PRIVATE_CERT_PATH = "/certificates/com/miniorange/sso/saml/bamboo/new-sp-key.key";
	public static String PUBLIC_CERTIFICATE = "";
	public static String PRIVATE_CERTIFICATE = "";
	private static final String LOGOUT_TEMPLATE_PATH = "/templates/com/miniorange/sso/saml/bamboo/logoutTemplate.ftl";
	private static final String LOGIN_TEMPLATE_PATH = "/templates/com/miniorange/sso/saml/bamboo/loginTemplate.ftl";
	private static final String ERRORMSG_TEMPLATE_PATH = "/templates/com/miniorange/sso/saml/bamboo/errorMessageTemplate.ftl";
	public static String LOGOUT_TEMPLATE = "";
	public static String LOGIN_TEMPLATE = "";
	public static String ERRORMSG_TEMPLATE = "";

	public static final String CUSTOMER_TOKEN_KEY = generateRandomAlphaNumericKey(16);
	public static Boolean isMetadataUpload = Boolean.FALSE;
	public static Boolean isPluginConfigurationFileUploaded = Boolean.FALSE;
	private BambooUserManager bambooUserManager;
	private BambooPermissionManager bambooPermissionManager;

	public MoSAMLSettings(PluginSettingsFactory pluginSettingsFactory, PluginLicenseManager pluginLicenseManager, AgentManager agentManager,
						  AdministrationConfigurationAccessor administrationConfigurationAccessor, BambooUserManager bambooUserManager,
						  BambooPermissionManager bambooPermissionManager ){
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
		this.pluginLicenseManager = pluginLicenseManager;
		this.agentManager = agentManager;
		this.administrationConfigurationAccessor = administrationConfigurationAccessor;
		this.bambooUserManager = bambooUserManager;
		this.bambooPermissionManager = bambooPermissionManager;

	}
	static {
		try {
			PUBLIC_CERTIFICATE = IOUtils.toString(MoSAMLSettings.class.getResourceAsStream(PUBLIC_CERT_PATH), "UTF-8");
			PUBLIC_CERTIFICATE = MoSAMLUtils.serializePublicCertificate(PUBLIC_CERTIFICATE);

			PRIVATE_CERTIFICATE = IOUtils.toString(MoSAMLSettings.class.getResourceAsStream(PRIVATE_CERT_PATH), "UTF-8");
			PRIVATE_CERTIFICATE = MoSAMLUtils.serializePrivateCertificate(PRIVATE_CERTIFICATE);

			LOGOUT_TEMPLATE = IOUtils.toString(MoSAMLSettings.class.getResourceAsStream(LOGOUT_TEMPLATE_PATH), "UTF-8");
			LOGIN_TEMPLATE = IOUtils.toString(MoSAMLSettings.class.getResourceAsStream(LOGIN_TEMPLATE_PATH), "UTF-8");
			ERRORMSG_TEMPLATE = IOUtils.toString(MoSAMLSettings.class.getResourceAsStream(ERRORMSG_TEMPLATE_PATH), "UTF-8");
		} catch (IOException e) {
			LOGGER.error("An I/O error occurred while initializing the SAML Settings.", e);
		}
	}

	public Boolean getSigning() {
		String signing = (String) this.pluginSettings.get(MoPluginConstants.SIGNING);
		if(StringUtils.isBlank(signing)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(signing);
		}
	}
	
	public void setSigning(Boolean signing) {
		this.pluginSettings.put(MoPluginConstants.SIGNING, BooleanUtils.toString(signing,
				"true", "false", "false"));
	}
	
	public Boolean getEncryption() {
		String encryption = (String) this.pluginSettings.get(MoPluginConstants.ENCRYPTION);
		if(StringUtils.isBlank(encryption)) {
			return Boolean.FALSE;
		} else {
			return BooleanUtils.toBoolean(encryption);
		}
	}
	
	public void setEncryption(Boolean encryption) {
		this.pluginSettings.put(MoPluginConstants.ENCRYPTION, BooleanUtils.toString(encryption,
				"true", "false", "false"));
	}

	

	public String getSpBaseUrl() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.SP_BASE_URL), getBaseUrl());
	}

	public void setSpBaseUrl(String spBaseUrl) {
		this.pluginSettings.put(MoPluginConstants.SP_BASE_URL, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(spBaseUrl, getBaseUrl())));
	}

	public String getLogSettingsUrl(){
		return getSpBaseUrl() + "/admin/configLog4j.action";
	}

	public String getTroubleshootingUrl(){
		return getSpBaseUrl() + "/plugins/servlet/troubleshooting/view/";
	}

	public String getSpEntityId() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.SP_ENTITY_ID), getBaseUrl());
	}

	public void setSpEntityId(String spEntityId) {
		this.pluginSettings.put(MoPluginConstants.SP_ENTITY_ID, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(spEntityId, getBaseUrl())));
	}

	public void setIdpName(String idpName) {
		this.pluginSettings.put(MoPluginConstants.IDP_NAME,
				StringUtils.defaultIfBlank(idpName, StringUtils.EMPTY));
	}

	public String getIdpName() {
		return (String) this.pluginSettings.get(MoPluginConstants.IDP_NAME);
	}

	public String getSsoServiceUrl() {
		return StringUtils.defaultString((String) this.pluginSettings.get(MoPluginConstants.SSO_URL));
	}

	public void setSsoServiceUrl(String ssoServiceUrl) {
		this.pluginSettings.put(MoPluginConstants.SSO_URL, StringUtils.trimToEmpty(ssoServiceUrl));
	}

	public String getSloServiceUrl() {
		return StringUtils.defaultString((String) this.pluginSettings.get(MoPluginConstants.SLO_URL));
	}

	public void setSloServiceUrl(String sloServiceUrl) {
		this.pluginSettings.put(MoPluginConstants.SLO_URL, StringUtils.trimToEmpty(sloServiceUrl));
	}

	public String getIdpEntityId() {
		return StringUtils.defaultString((String) this.pluginSettings.get(MoPluginConstants.IDP_ENTITY_ID));
	}

	public void setIdpEntityId(String idpEntityId) {
		this.pluginSettings.put(MoPluginConstants.IDP_ENTITY_ID, StringUtils.trimToEmpty(idpEntityId));
	}

	public String getX509Certificate() {
		return StringUtils.defaultString((String) this.pluginSettings.get(MoPluginConstants.X509_CERTIFICATE));
	}

	public void setX509Certificate(String x509Certificate) {
		this.pluginSettings.put(MoPluginConstants.X509_CERTIFICATE,
				MoSAMLUtils.serializePublicCertificate(x509Certificate));
	}

	public Object getAllX509Certificates() {
		return this.pluginSettings.get(MoPluginConstants.ALL_X509_CERTIFICATES);
	}

	public void setAllX509Certificates(List<String> x509certificates) {
		for(String certificate : x509certificates)
			x509certificates.set(x509certificates.indexOf(certificate), MoSAMLUtils.serializePublicCertificate(certificate));
		this.pluginSettings.put(MoPluginConstants.ALL_X509_CERTIFICATES, x509certificates );
	}
	
	public String getSsoBindingType() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants
				.SSO_BINDING_TYPE), "HttpRedirect");
	}

	public void setSsoBindingType(String ssoBindingType) {
		this.pluginSettings.put(MoPluginConstants.SSO_BINDING_TYPE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(ssoBindingType, "HttpRedirect")));
	}

	public String getSloBindingType() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants
				.SLO_BINDING_TYPE), "HttpRedirect");
	}

	public void setSloBindingType(String sloBindingType) {
		this.pluginSettings.put(MoPluginConstants.SLO_BINDING_TYPE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(sloBindingType, "HttpRedirect")));
	}
	
	public String getLoginUserAttribute() {
		return StringUtils.defaultString((String) this.pluginSettings.get(MoPluginConstants.LOGIN_USER_ATTRIBUTE));
	}

	public void setLoginUserAttribute(String loginUserAttribute) {
		this.pluginSettings.put(MoPluginConstants.LOGIN_USER_ATTRIBUTE,
				StringUtils.defaultIfBlank(loginUserAttribute, "username"));
	}

	public String getUsernameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants
				.USERNAME_ATTRIBUTE), "NameID");
	}

	public void setUsernameAttribute(String usernameAttribute) {
		this.pluginSettings.put(MoPluginConstants.USERNAME_ATTRIBUTE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(usernameAttribute, "NameID")));
	}

	public String getEmailAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants
				.EMAIL_ATTRIBUTE), "NameID");
	}

	public void setEmailAttribute(String emailAttribute) {
		this.pluginSettings.put(MoPluginConstants.EMAIL_ATTRIBUTE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(emailAttribute, "NameID")));
	}
	
	public String getFullNameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants
				.FULL_NAME_ATTRIBUTE), "");
	}

	public void setFullNameAttribute(String fullNameAttribute) {
		this.pluginSettings.put(MoPluginConstants.FULL_NAME_ATTRIBUTE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(fullNameAttribute, "")));
	}
	
	public Boolean getUseSeparateNameAttributes() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get
				(MoPluginConstants.USE_SEPARATE_NAME_ATTRIBUTE)), false);
	}

	public void setUseSeparateNameAttributes(Boolean defaultLoginDisabled) {
		this.pluginSettings.put(MoPluginConstants.USE_SEPARATE_NAME_ATTRIBUTE, BooleanUtils.toString(defaultLoginDisabled,
				"true", "false", "false"));
	}
	
	public Boolean getKeepExistingUserAttributes() {
		String keepExistingUserAttributes = (String) this.pluginSettings.get(MoPluginConstants.KEEP_EXISTING_ATTRIBUTE);
		if (StringUtils.isBlank(keepExistingUserAttributes)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(keepExistingUserAttributes);
		}
	}

	public void setKeepExistingUserAttributes(Boolean keepExistingUserAttributes) {
		this.pluginSettings.put(MoPluginConstants.KEEP_EXISTING_ATTRIBUTE, BooleanUtils.toString(keepExistingUserAttributes,
				"true", "false", "false"));
	}

	public String getFirstNameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants
				.FIRST_NAME_ATTRIBUTE), "");
	}
	//group regex
	public Boolean getGroupRegexPatternEnabled() {
		return BooleanUtils.toBoolean(
				StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.GROUP_REGEX_PATTERN_ENABLED),"false"));
	}

	public void setGroupRegexPatternEnabled(Boolean groupRegexPatternEnabled) {
		this.pluginSettings.put(MoPluginConstants.GROUP_REGEX_PATTERN_ENABLED,
				BooleanUtils.toString(groupRegexPatternEnabled, "true", "false", "false"));
	}

	public String getRegexPatternForGroup() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.REGEX_PATTERN), "");
	}

	public void setRegexPatternForGroup(String regexPatternForGroup) {
		this.pluginSettings.put(MoPluginConstants.REGEX_PATTERN_FOR_GROUP,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(regexPatternForGroup, "")));
	}
	public String getRegexGroups() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.REGEX_GROUPS), "");
	}

	public void setRegexGroups(String regexGroups) {
		this.pluginSettings.put(MoPluginConstants.REGEX_GROUPS,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(regexGroups, "")));
	}
	public String getTestRegex() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.TEST_REGEX), "");
	}

	public void setTestRegex(String testRegex) {
		this.pluginSettings.put(MoPluginConstants.TEST_REGEX,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(testRegex, "")));
	}


	public void setFirstNameAttribute(String fullNameAttribute) {
		this.pluginSettings.put(MoPluginConstants.FIRST_NAME_ATTRIBUTE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(fullNameAttribute, "")));
	}

	public String getLastNameAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants
				.LAST_NAME_ATTRIBUTE), "");
	}

	public void setLastNameAttribute(String fullNameAttribute) {
		this.pluginSettings.put(MoPluginConstants.LAST_NAME_ATTRIBUTE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(fullNameAttribute, "")));
	}
	
	public Boolean getRegexPatternEnabled() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.ENABLE_REGEX_PATTERN)),
				false);
	}

	public void setRegexPatternEnabled(Boolean regexPatternEnabled) {
		this.pluginSettings.put(MoPluginConstants.ENABLE_REGEX_PATTERN,
				BooleanUtils.toString(regexPatternEnabled, "true", "false", "false"));
	}

	public String getRegexPattern() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.REGEX_PATTERN), "");
	}

	public void setRegexPattern(String regexPattern) {
		this.pluginSettings.put(MoPluginConstants.REGEX_PATTERN,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(regexPattern, "")));
	}

	public String getRoleAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants
				.ROLE_ATTRIBUTE), "");
	}

	public void setRoleAttribute(String roleAttribute) {
		this.pluginSettings.put(MoPluginConstants.ROLE_ATTRIBUTE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(roleAttribute, "")));
	}

	public String getDefaultGroup() {

		String defaultGroup = StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.DEFAULT_GROUP),
				"");
		if (StringUtils.isBlank(defaultGroup)|| StringUtils.isEmpty(defaultGroup)) {
			try {
				ArrayList<String> existingGroups = new ArrayList<String>();
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

			} catch (Exception e) {
				e.printStackTrace();
			}

//			setDefaultGroup(defaultGroup);
		}
		return defaultGroup;
	}

	public void setDefaultGroup(String defaultGroup) {
		this.pluginSettings.put(MoPluginConstants.DEFAULT_GROUP, StringUtils.trimToEmpty(defaultGroup));
	}

	//public Object getDefaultGroups() {
		//return this.pluginSettings.get(MoPluginConstants.DEFAULT_GROUPS);
	//}

	public Object getDefaultGroups() {
		if (this.pluginSettings.get(MoPluginConstants.DEFAULT_GROUPS) == null)
		{
			List<String> defaultGroups = new ArrayList<>();
			ArrayList<String> existingGroups = new ArrayList<String>();
			Pager groupObjects = bambooUserManager.getGroups();
			Iterator<Group> itr = groupObjects.iterator();
			while (itr.hasNext()) {
				Group group = itr.next();
				existingGroups.add(group.getName());
			}

			if (existingGroups.size() > 1) {
				defaultGroups.add(existingGroups.get(1));
			}
			else {
				defaultGroups.add(existingGroups.get(0));
			}
			return defaultGroups;
		}
		return this.pluginSettings.get(MoPluginConstants.DEFAULT_GROUPS);
	}

	public void setDefaultGroups(List<String> defaultGroups) {
		this.pluginSettings.put(MoPluginConstants.DEFAULT_GROUPS, defaultGroups);
	}

	public HashMap<String, String> getRoleMapping() {
		Object roleMapping = this.pluginSettings.get(MoPluginConstants.ROLE_MAPPING);
		if (roleMapping != null) {
			return (HashMap) roleMapping;
		}
		return new HashMap<>();
	}
	
	public Boolean getSignedRequest(){
		
		String signedRequestStr = (String) this.pluginSettings.get(MoPluginConstants.SIGNED_REQUEST);
		if(StringUtils.isBlank(signedRequestStr)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(signedRequestStr);
		}
			
	}
	
	public void setSignedRequest(Boolean signedRequest){
		this.pluginSettings.put(MoPluginConstants.SIGNED_REQUEST,
				BooleanUtils.toString(signedRequest, "true", "false", "false"));
	}

	public void setRoleMapping(HashMap<String, String> roleMapping) {
		this.pluginSettings.put(MoPluginConstants.ROLE_MAPPING, roleMapping);
	}

	public Boolean getKeepExistingUserRoles() {
		String keepExistingUserRoles = (String) this.pluginSettings.get(MoPluginConstants.KEEP_EXISTING_USER_ROLES);
		if (StringUtils.isBlank(keepExistingUserRoles)) {
			return Boolean.FALSE;
		} else {
			return BooleanUtils.toBoolean(keepExistingUserRoles);
		}
	}

	public void setKeepExistingUserRoles(Boolean keepExistingUserRoles) {
		this.pluginSettings
				.put(MoPluginConstants.KEEP_EXISTING_USER_ROLES, BooleanUtils.toString(keepExistingUserRoles,
						"true", "false", "false"));
	}
	
	public Boolean getCreateUsersIfRoleMapped() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get
				(MoPluginConstants.CREATE_USER_IF_ROLE_MAPPED)), false);
	}

	public void setCreateUsersIfRoleMapped(Boolean createUsersIfRoleMapped) {
		this.pluginSettings
				.put(MoPluginConstants.CREATE_USER_IF_ROLE_MAPPED, BooleanUtils.toString(createUsersIfRoleMapped,
						"true", "false", "false"));
	}
	
	public Boolean getRestrictUserCreation() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.RESTRICT_USER_CREATION)),
				false);
	}

	public void setRestrictUserCreation(Boolean restrictUserCreation) {
		this.pluginSettings.put(MoPluginConstants.RESTRICT_USER_CREATION,
				BooleanUtils.toString(restrictUserCreation, "true", "false", "false"));
	}

	public void setEnableDefaultGroupsFor(String enableDefaultGroupsFor) {
		this.pluginSettings.put(MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR, StringUtils
				.defaultIfEmpty(enableDefaultGroupsFor, MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS));
	}

	public String getEnableDefaultGroupsFor() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR),
				MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS);
	}

	public Object getOnTheFlyDoNotRemoveGroups() {
		if (this.pluginSettings.get(MoPluginConstants.ON_THE_FLY_DO_NOT_REMOVE_GROUPS) == null)
		{
			List<String> defaultExcludeGroups = new ArrayList<>();
			ArrayList<String> existingGroups = new ArrayList<String>();
			Pager groupObjects = bambooUserManager.getGroups();
			Iterator<Group> itr = groupObjects.iterator();
			while (itr.hasNext()) {
				Group group = itr.next();
				existingGroups.add(group.getName());
			}
			defaultExcludeGroups.add(existingGroups.get(0));
//			if (existingGroups.size() > 1) {
//				defaultGroups.add(existingGroups.get(1));
//			}
//			else {
//
//			}
			return defaultExcludeGroups;
		}
		return this.pluginSettings.get(MoPluginConstants.ON_THE_FLY_DO_NOT_REMOVE_GROUPS);
	}

	public void setOnTheFlyDoNotRemoveGroups(List<String> groups) {
		this.pluginSettings.put(MoPluginConstants.ON_THE_FLY_DO_NOT_REMOVE_GROUPS, groups);
	}

	public void setCreateNewGroups(Boolean createNewGroups) {
		this.pluginSettings.put(MoPluginConstants.ON_THE_FLY_CREATE_NEW_GROUPS,
				BooleanUtils.toString(createNewGroups, "true", "false", "false"));
	}

	public Boolean getCreateNewGroups() {
		String createNewGroups = (String) this.pluginSettings.get(MoPluginConstants.ON_THE_FLY_CREATE_NEW_GROUPS);
		if (StringUtils.isBlank(createNewGroups)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(createNewGroups);
		}
	}

	public void setOnTheFlyGroupMapping(Boolean createUsersIfRoleMapped) {
		this.pluginSettings.put(MoPluginConstants.ON_THE_FLY_GROUP_MAPPING,
				BooleanUtils.toString(createUsersIfRoleMapped, "true", "false", "false"));
	}

	public Boolean getOnTheFlyGroupMapping() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.ON_THE_FLY_GROUP_MAPPING)),
				false);
	}

	public void setOnTheFlyAssignNewGroupsOnly(Boolean onTheFlyAssignNewGroupsOnly) {
		this.pluginSettings.put(MoPluginConstants.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY,
				BooleanUtils.toString(onTheFlyAssignNewGroupsOnly, "true", "false", "false"));
	}

	public Boolean getOnTheFlyAssignNewGroupsOnly() {

		String onTheFlyAssignNewGroupsOnly = (String) this.pluginSettings
				.get(MoPluginConstants.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY);
		if (StringUtils.isBlank(onTheFlyAssignNewGroupsOnly)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(onTheFlyAssignNewGroupsOnly);
		}

	}

	public Boolean getDefaultLoginDisabled() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.DISABLE_DEFAULT_LOGIN));
	}

	public void setDefaultLoginDisabled(Boolean defaultLoginDisabled) {
		this.pluginSettings.put(MoPluginConstants.DISABLE_DEFAULT_LOGIN, BooleanUtils.toString(defaultLoginDisabled,
				"true", "false", "false"));
	}

	public Boolean getBackdoorEnabled() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.ENABLE_BACKDOOR));
	}

	public void setRelayState(String relayState) {
		this.pluginSettings.put(MoPluginConstants.RELAY_STATE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(relayState, StringUtils.EMPTY)));
	}

	public String getRelayState( ) {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.RELAY_STATE),StringUtils.EMPTY);
	}

	public void setRelayStateRedirectionType(String relayStateRedirectionType) {
		this.pluginSettings.put(MoPluginConstants.RELAY_STATE_REDIRECTION_TYPE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(relayStateRedirectionType, MoPluginConstants.FORCE_REDIRECT)));
	}

	public String getRelayStateRedirectionType() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.RELAY_STATE_REDIRECTION_TYPE),MoPluginConstants.FORCE_REDIRECT);
	}

	public void setBackdoorEnabled(Boolean backdoorEnabled) {
		this.pluginSettings.put(MoPluginConstants.ENABLE_BACKDOOR, BooleanUtils.toString(backdoorEnabled,
				"true", "false", "false"));
	}

	public String getLoginButtonText() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants
				.LOGIN_BUTTON_TEXT), "Use IDP Login");
	}

	public void setLoginButtonText(String loginButtonText) {
		LOGGER.info("setLoginButtontest called");
		this.pluginSettings.put(MoPluginConstants.LOGIN_BUTTON_TEXT, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(loginButtonText, "Use IDP Login")));
	}
	
	public String getPublicSPCertificate() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants
				.PUBLIC_SP_CERTIFICATE), PUBLIC_CERTIFICATE);
	}

	public void setPublicSPCertificate(String publicSPCertificate) {
		this.pluginSettings.put(MoPluginConstants.PUBLIC_SP_CERTIFICATE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(publicSPCertificate, PUBLIC_CERTIFICATE)));
	}

	public String getPrivateSPCertificate() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants
				.PRIVATE_SP_CERTIFICATE), PRIVATE_CERTIFICATE);
	}

	public void setPrivateSPCertificate(String privateSPCertificate) {
		this.pluginSettings.put(MoPluginConstants.PRIVATE_SP_CERTIFICATE, StringUtils.trimToEmpty(StringUtils
				.defaultIfBlank(privateSPCertificate, PRIVATE_CERTIFICATE)));
	}

	public String getNameIdFormat() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.NAME_ID_FORMAT),
				"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
	}

	public void setNameIdFormat(String nameIdFormat) {
		this.pluginSettings.put(MoPluginConstants.NAME_ID_FORMAT,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(nameIdFormat, "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified")));
	}

	/*public String getAuthnContextClass() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.AUTHN_CONTEXT_CLASS),
				"None");
	}

	public void setAuthnContextClass(String authnContextClass) {
		this.pluginSettings.put(MoPluginConstants.AUTHN_CONTEXT_CLASS, StringUtils.trimToEmpty(
				StringUtils.defaultIfBlank(authnContextClass, "None")));
	}

	public String getOtherAuthnContextClass() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.OTHER_AUTHN_CONTEXT_CLASS),
				"");
	}

	public void setOtherAuthnContextClass(String otherAuthnContextClass) {
		this.pluginSettings.put(MoPluginConstants.OTHER_AUTHN_CONTEXT_CLASS, StringUtils.trimToEmpty(
				StringUtils.defaultIfBlank(otherAuthnContextClass, "")));
	}*/

	//IDP specific - enable SSO
	public void setEnableSsoForIdp(Boolean enableSsoForIdp) {
		this.pluginSettings.put(MoPluginConstants.ENABLE_SSO_FOR_IDP,
				BooleanUtils.toString(enableSsoForIdp, "true", "false", "false"));
	}

	public Boolean getEnableSsoForIdp() {
		String enableSsoForIdp = (String) this.pluginSettings.get(MoPluginConstants.ENABLE_SSO_FOR_IDP);
		if (StringUtils.isBlank(enableSsoForIdp)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(enableSsoForIdp);
		}
	}

	public void setIssuerMap(HashMap<String, String> issuerMap) {
		this.pluginSettings.put(MoPluginConstants.ISSUER_MAP, issuerMap);
	}

	public HashMap<String, String> getIssuerMap() {
		HashMap<String, String> issuerMap = (HashMap<String, String>) this.pluginSettings.get(MoPluginConstants.ISSUER_MAP);
		if (issuerMap == null) {
			return new HashMap<String, String>();
		}

		return issuerMap;
	}

	public void setSsoEnabledForIdPList(ArrayList<String> ssoEnabledForIdPList) {
		this.pluginSettings.put(MoPluginConstants.SSO_ENABLED_FOR_IDP_LIST, ssoEnabledForIdPList);
	}

	public ArrayList<String> getSsoEnabledForIdPList() {
		ArrayList<String> ssoEnabledForIdPList = (ArrayList<String>) this.pluginSettings.get(MoPluginConstants.SSO_ENABLED_FOR_IDP_LIST);
		if (ssoEnabledForIdPList == null) {
			ArrayList<String> idpList = getIdPList();
			LOGGER.debug("Using idpList as ssoEnabledForIdPList: " + idpList);
			return idpList;
		}
		return ssoEnabledForIdPList;
	}

	public void setRedirectionRule(String rule, String ruleName) {
		pluginSettings.put(MoPluginConstants.REDIRECTION_RULE + ruleName, rule);
	}

	public JSONObject getRedirectionRule(String ruleName) {
		String rule = (String) pluginSettings.get(MoPluginConstants.REDIRECTION_RULE + ruleName);
		try {
			JSONObject redirectionRule = new JSONObject(rule);
			return redirectionRule;
		} catch (JSONException e) {
			LOGGER.error("An error occurred while getting Redirection Rule", e);
			return null;
		}
	}

	public void setBambooRedirectionRulesMap(String bambooRedirectionRules) {
		pluginSettings.put(MoPluginConstants.BAMBOO_REDIRECTION_RULES, bambooRedirectionRules);
	}

	public Map<String, String> getBambooRedirectionRulesMap() {
		String bambooRedirectionRules = getBambooRedirectionRuleJson();
		LOGGER.debug("DB rules in Json " + bambooRedirectionRules);
		Map<String, String> bambooRedirectionRulesMap = new LinkedHashMap<String, String>();

		if (StringUtils.isBlank(bambooRedirectionRules))
			return bambooRedirectionRulesMap;

		bambooRedirectionRulesMap = MoJSONUtils.convertJsonToMap(bambooRedirectionRules, bambooRedirectionRulesMap);
		return bambooRedirectionRulesMap;
	}

	public String getBambooRedirectionRuleJson() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.BAMBOO_REDIRECTION_RULES), new JSONObject().toString());
	}

	public List<String> convertSelect2StringToList(String groups) {
		List<String> groupsList = new ArrayList<>();
		if (StringUtils.isBlank(groups))
			return groupsList;

		LOGGER.debug("multiple groups -" + groups.toString());
		Collections.addAll(groupsList, StringUtils.split(groups,","));
		groupsList.removeAll(Arrays.asList(null, ""));
		return groupsList;
	}

	public void setDefaultRedirectURL(String defaultRedirectURL){
		pluginSettings.put(MoPluginConstants.DEFAULT_REDIRECT_URL,StringUtils.defaultIfBlank(defaultRedirectURL,StringUtils.EMPTY));
	}

	public String getDefaultRedirectURL(){
		return StringUtils.defaultIfBlank((String)pluginSettings.get(MoPluginConstants.DEFAULT_REDIRECT_URL),StringUtils.EMPTY);
	}

	public void deleteRedirectionRule(String ruleId) {
		pluginSettings.remove(MoPluginConstants.REDIRECTION_RULE + ruleId);
		Map<String, String> rulesMap = getBambooRedirectionRulesMap();
		rulesMap.remove(ruleId);
		String redirectionRules = MoJSONUtils.convertMapToJSON(rulesMap);
		setBambooRedirectionRulesMap(redirectionRules);
	}

	public void setDefaultBambooIDP(String defaultBambooIDP) {
		pluginSettings.put(MoPluginConstants.DEFAULT_BAMBOO_IDP, StringUtils.defaultIfBlank(defaultBambooIDP, "loginPage"));
	}

	public String getDefaultBambooIDP() {
		String defaultBambooIDP;
		if (getSsoEnabledForIdPList().isEmpty() || getSsoEnabledForIdPList().size() > 1)
			defaultBambooIDP = "loginPage";
		else
			defaultBambooIDP = getSsoEnabledForIdPList().get(0);
		return StringUtils.defaultIfBlank((String) pluginSettings.get(MoPluginConstants.DEFAULT_BAMBOO_IDP), defaultBambooIDP);
	}
	public String getBaseUrl() {
		String baseURL = administrationConfigurationAccessor.getAdministrationConfiguration().getBaseUrl();
		return baseURL;
	}

	public void removeRedirectionRulesForIdp(String idpID) {
		JSONArray redirectionRules = getRedirectionRules();
		try {
			for (int i = 0; i < redirectionRules.length(); i++) {
				JSONObject redirectionRule = redirectionRules.getJSONObject(i);
				String idp = redirectionRule.optString("idp");
				if (StringUtils.equals(idp, idpID)) {
					String ruleKey = redirectionRule.optString("name");
					deleteRedirectionRule(ruleKey);
				}
			}
		} catch (JSONException e) {
			LOGGER.error("An Error Occurred while deleting redirection rule for IDP " + idpID, e);
		}
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
	
	public String getImportExportPageUrl() {
		return getBaseUrl().concat("/plugins/servlet/bamboo-sso/backuprestore.action");
	}
	
	public String getLoginServletUrl() {
		return getSpBaseUrl().concat("/plugins/servlet/saml/auth");
	}

	public String getLogoutServletUrl() {
		return getSpBaseUrl().concat("/plugins/servlet/saml/logout");
	}
	
	public String getRegisterActionUrl() {
		return getBaseUrl().concat("/plugins/servlet/bamboo-sso/doregister.action");
	}

	public String getConfigureActionUrl() {
		return getBaseUrl().concat("/plugins/servlet/bamboo-sso/configure.action");
	}

	public String getReplaceOldSettingWithNewUrl() {
		return getBaseUrl().concat("/plugins/servlet/saml/replaceOldSettingsWithNew");
	}

	public String getResetPluginSettings() {
		return getBaseUrl().concat("/plugins/servlet/saml/moconfreset");
	}

	public String getVerifyLicenseActionUrl() {
		return getBaseUrl().concat("/plugins/servlet/bamboo-sso/verifylicense.jspa");
	}

	public String getLoginActionUrl() {
		return getBaseUrl().concat("/plugins/servlet/bamboo-sso/dologin.action");
	}
	
	public String getMiniorangeUrl() { return MoPluginConstants.AUTH_BASE_URL; }

	/*public String getCustomerID() {
		String customerId = StringUtils.defaultString((String) this.pluginSettings.get(PluginConstants.CUSTOMER_ID));
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.pluginSettings.put(PluginConstants.CUSTOMER_ID, customerId);
	}

	public String getCustomerEmail() {
		return StringUtils.defaultString((String) this.pluginSettings.get(PluginConstants.CUSTOMER_EMAIL));
	}

	public void setCustomerEmail(String email) {
		this.pluginSettings.put(PluginConstants.CUSTOMER_EMAIL, StringUtils.trimToEmpty(email));
	}
	
	public String getCustomerAPIKey() {
		return StringUtils.defaultString((String) this.pluginSettings.get(PluginConstants.CUSTOMER_API_KEY));
	}

	public void setCustomerAPIKey(String customerId) {
		this.pluginSettings.put(PluginConstants.CUSTOMER_API_KEY, customerId);
	} */
	public String getCustomerTokenKey() {
		return StringUtils.defaultString((String)CUSTOMER_TOKEN_KEY);
	}

	/*
	public void setCustomerTokenKey(String customerId) {
		this.pluginSettings.put(PluginConstants.CUSTOMER_TOKEN_KEY, customerId);
	}*/

	/*public String getCustomerLicenseKey() {
		return StringUtils.defaultString((String) this.pluginSettings.get(MoPluginConstants.CUSTOMER_LICENSE_KEY));
	}

	public void setCustomerLicenseKey(String licenseKey) {
		this.pluginSettings.put(MoPluginConstants.CUSTOMER_LICENSE_KEY, StringUtils.trimToEmpty(licenseKey));
	}
	
    public void setLicenseVerified(Boolean backdoorEnabled) {
		String value = MoEncryptionUtils.encrypt(getCustomerTokenKey(), BooleanUtils.toString(backdoorEnabled,
				"true", "false", "false"));
		this.pluginSettings.put(MoPluginConstants.LICENSE_VERIFIED, value);
	}*/

    public Boolean isLicenseDefine(){
    	if (pluginLicenseManager.getLicense().isDefined()) {
			return true;
		} 
		return false;
    }
    
	public Boolean isValidLicense() {
		try {
			if (pluginLicenseManager.getLicense().isDefined()) {
				if (!pluginLicenseManager.getLicense().get().isValid()){
					return false;
				} 
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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

	public Boolean isEvaluationOrSubscriptionLicense() {
		if (!isLicenseValid()) {
			PluginLicense pluginLicense = pluginLicenseManager.getLicense().get();
			if (pluginLicense.isEvaluation() || pluginLicense.isSubscription()) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	
	
	public String getCurrentLicenseEmail(){
		Contact contact= null;
		String email = "empty";
		try {	
			if (pluginLicenseManager.getLicense().isDefined()) {
				Iterator<Contact>  itr = pluginLicenseManager.getLicense().get().getContacts().iterator();
				if(itr.hasNext())
					contact = itr.next();
				if(contact!=null && contact.getEmail()!=null){
					email= contact.getEmail();
				}
				return email;
			}
			return email;
		} catch (Exception e) {
			e.printStackTrace();
			return email;
		}
	}

	
	public Integer getMaxRemoteAgentsCount(){
		if(pluginLicenseManager.getLicense().isDefined()){
			try{
				Integer licensedAgents = pluginLicenseManager.getLicense().get().getEdition().get();
				return licensedAgents;
			}catch(Exception e){
				return 0;
			}
		}
		return -1;
	}
	
	public int getActiveRemoteAgents(){
		List <BuildAgent> remoteAgentsCount = agentManager.getAllRemoteAgents();
		if(remoteAgentsCount!=null)
			return remoteAgentsCount.size();
		return -1;
	}
	
	
	public String getUPMURL() {
		return getBaseUrl().concat("/plugins/servlet/upm");
	}

	/*public HashMap<String, String> getActiveUsers() {
		HashMap<String, String> activeUsers = (HashMap<String, String>) this.pluginSettings.get(MoPluginConstants.ACTIVE_USERS);
		if (activeUsers == null) {
			activeUsers = new HashMap<String, String>();
		}
		return activeUsers;
	}

	public void setActiveUsers(HashMap<String, String> activeUsers) {
		this.pluginSettings.put(MoPluginConstants.ACTIVE_USERS, activeUsers);
	}
	
	
	public Integer getLicencedUsers() {
		try {
			String licencedUsers = (String) this.pluginSettings.get(MoPluginConstants.LICENCED_USERS);
			return Integer.valueOf(licencedUsers);
		} catch(NumberFormatException e){
			return -1;
		}
	}

	public void setLicencedUsers(Integer userCount) {
		String licencedUsers = userCount.toString();
		this.pluginSettings.put(MoPluginConstants.LICENCED_USERS, licencedUsers);
	}
	
	
	public Boolean getOldCustomer() {
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) this.pluginSettings.get
				(MoPluginConstants.IS_OLD_CUSTOMER)), false);
	}

	public void setOldCustomer(Boolean isOldCustomer) {
		this.pluginSettings.put(MoPluginConstants.IS_OLD_CUSTOMER, BooleanUtils.toString(isOldCustomer,
				"true", "false", "false"));
	}
	
	
	public Integer getAlertEmailsSentCount() {
		String alertEmailsSentCount = (String) this.pluginSettings.get(MoPluginConstants.USER_EXCEEDED_ALERT_EMAILS_SENT);
		if (StringUtils.isBlank(alertEmailsSentCount))
			return 0;
		else
			return Integer.valueOf(alertEmailsSentCount);
	}

	public void setAlertEmailsSentCount(Integer alertEmailsSentCount) {
		String alertEmailsSent = alertEmailsSentCount.toString();
		this.pluginSettings.put(MoPluginConstants.USER_EXCEEDED_ALERT_EMAILS_SENT, alertEmailsSent);
	}*/

	public void setPluginSettingsFactory(PluginSettingsFactory pluginSettingsFactory) {
		this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
	}

	public PluginSettings getPluginSettings() {
		return pluginSettings;
	}

	public void setPluginSettings(PluginSettings pluginSettings) {
		this.pluginSettings = pluginSettings;
	}

	public AdministrationConfigurationAccessor getAdministrationConfigurationAccessor() {
		return administrationConfigurationAccessor;
	}

	public void setAdministrationConfigurationAccessor(
			AdministrationConfigurationAccessor administrationConfigurationAccessor) {
		this.administrationConfigurationAccessor = administrationConfigurationAccessor;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public PluginLicenseManager getPluginLicenseManager() {
		return pluginLicenseManager;
	}

	public void setPluginLicenseManager(PluginLicenseManager pluginLicenseManager) {
		this.pluginLicenseManager = pluginLicenseManager;
	}
	
	public static String generateRandomAlphaNumericKey(int bytes) {
		String randomString = RandomStringUtils.random(bytes, true, true);
		return randomString;
	}
	
	public void setSpConfigurationStatus(String status){
		this.pluginSettings.put(MoPluginConstants.SP_CONFIGURATION_STATUS, StringUtils.defaultIfBlank(status, StringUtils.EMPTY));
	}
	
	public String getSpConfigurationStatus(){
		return (String)this.pluginSettings.get(MoPluginConstants.SP_CONFIGURATION_STATUS);
	}

	public String getErrorMsgTemplate() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.ERROR_MSG_TEMPLATE), ERRORMSG_TEMPLATE);
	}

	public void setErrorMsgTemplate(String errorMsgTemplate) {
		this.pluginSettings.put(MoPluginConstants.ERROR_MSG_TEMPLATE,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(errorMsgTemplate, ERRORMSG_TEMPLATE)));
	}
	public void setPageNumber(String pageNumber) {
		this.pluginSettings.put(MoPluginConstants.PAGE_NUMBER, StringUtils.defaultIfEmpty(pageNumber, "1"));
	}

	public String getPageNumber(Boolean resetPageNumber) {
		if(resetPageNumber){
			return "1";
		}
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.PAGE_NUMBER), "1");
	}

	public Boolean getEnableSAMLSSO() {
		String enableSAMLSSO = (String) this.pluginSettings.get(MoPluginConstants.ENABLE_SAML_SSO);
		if (StringUtils.isBlank(enableSAMLSSO)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(enableSAMLSSO);
		}
	}

	public void setEnableSAMLSSO(Boolean enableSAMLSSO) {
		this.pluginSettings.put(MoPluginConstants.ENABLE_SAML_SSO,
				BooleanUtils.toString(enableSAMLSSO, "true", "false", "false"));
	}

	public Boolean getAutoActivateUser() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.AUTO_ACTIVATE_USER));
	}

	public void setAutoActivateUser(Boolean autoActivateUser) {
		this.pluginSettings.put(MoPluginConstants.AUTO_ACTIVATE_USER,
				BooleanUtils.toString(autoActivateUser, "true", "false", "false"));
	}

	public Boolean getIsCertificateExpired() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.CERT_EXPIRED)), false);
	}

	/**
	 * Set in the database that certificate has expired
	 */
	public void setIsCertificateExpired(Boolean certificateExpired) {
		this.pluginSettings.put(MoPluginConstants.CERT_EXPIRED,
				BooleanUtils.toString(certificateExpired, "true", "false", "false"));
	}

	public Boolean getEnableAutoRedirectDelay() {
		return BooleanUtils.toBoolean(StringUtils.defaultString((String) pluginSettings.get(MoPluginConstants.ENABLE_AUTO_REDIRECT_DELAY), "true"));
	}

	public void setEnableAutoRedirectDelay(Boolean enableAutoRedirectDelay) {
		this.pluginSettings.put(MoPluginConstants.ENABLE_AUTO_REDIRECT_DELAY,
				BooleanUtils.toString(enableAutoRedirectDelay, "true", "false", "false"));
	}

	public void setNumberOfLoginAttempts(String numberOfLoginAttempts) {
		this.pluginSettings.put(MoPluginConstants.NUMBER_OF_LOGIN_ATTEMPTS, StringUtils.trimToEmpty(numberOfLoginAttempts));
	}

	public String getNumberOfLoginAttempts() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.NUMBER_OF_LOGIN_ATTEMPTS),"3");
	}

	public void setBackdoorKey(String backdoorKey) {
		this.pluginSettings.put(MoPluginConstants.BACKDOOR_KEY, StringUtils.defaultIfBlank(backdoorKey, "saml_sso"));
	}

	public String getBackdoorKey() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.BACKDOOR_KEY), "saml_sso");
	}

	public void setBackdoorValue(String backdoorValue) {
		this.pluginSettings.put(MoPluginConstants.BACKDOOR_VALUE, StringUtils.defaultIfBlank(backdoorValue, "false"));
	}

	public String getBackdoorValue() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.BACKDOOR_VALUE), "false");
	}

	public Boolean getRestrictBackdoor() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.RESTRICT_BACKDOOR));
	}

	public List<String> getBackdoorGroups() {
		List<String> backdoorGroups = (List<String>) this.pluginSettings.get(MoPluginConstants.BACKDOOR_GROUPS);
		if (backdoorGroups == null) {
			backdoorGroups = new ArrayList<>();
			backdoorGroups.addAll(bambooPermissionManager.getAdminGroups());
		} else if (backdoorGroups.size() == 0) {
			backdoorGroups.addAll(bambooPermissionManager.getAdminGroups());
		}

		return backdoorGroups;
	}

	public JSONArray getRedirectionRules() {
		JSONArray rules = new JSONArray();
		JSONObject rule;

		Map<String, String> rulesMap = new LinkedHashMap();
		rulesMap = getBambooRedirectionRulesMap();
		for (String key : rulesMap.keySet()) {
			rule = getRedirectionRule(key);
			rules.put(rule);
		}
		return rules;

	}

	public void setBackdoorGroups(List<String> backdoorGroups) {
		this.pluginSettings.put(MoPluginConstants.BACKDOOR_GROUPS, backdoorGroups);
	}

	public void setRestrictBackdoor(Boolean restrictBackdoor) {
		this.pluginSettings.put(MoPluginConstants.RESTRICT_BACKDOOR,
				BooleanUtils.toString(restrictBackdoor, "true", "false", "false"));
	}

	public String getLogoutTemplate() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.LOGOUT_TEMPLATE),LOGOUT_TEMPLATE);
	}

	public void setLogoutTemplate(String logoutTemplate) {
		this.pluginSettings.put(MoPluginConstants.LOGOUT_TEMPLATE,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(logoutTemplate, LOGOUT_TEMPLATE)));
	}

	public Boolean getEnableLoginTemplate() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.ENABLE_LOGIN_TEMPLATE)),
				false);
	}

	public void setEnableLoginTemplate(Boolean enableLoginTemplate) {
		this.pluginSettings.put(MoPluginConstants.ENABLE_LOGIN_TEMPLATE,
				BooleanUtils.toString(enableLoginTemplate, "true", "false", "false"));
	}

	public String getLoginTemplate() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.LOGIN_TEMPLATE), LOGIN_TEMPLATE);
	}

	public void setLoginTemplate(String loginTemplate) {
		this.pluginSettings.put(MoPluginConstants.LOGIN_TEMPLATE,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(loginTemplate, LOGIN_TEMPLATE)));
	}

	public Boolean getEnableErrorMsgTemplate() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.ENABLE_ERRORMSG_TEMPLATE)),
				false);
	}

	public void setEnableErrorMsgTemplate(Boolean enableErrorMsgTemplate) {
		this.pluginSettings.put(MoPluginConstants.ENABLE_ERRORMSG_TEMPLATE,
				BooleanUtils.toString(enableErrorMsgTemplate, "true", "false", "false"));
	}

	public void setShowLoginButtons(Boolean showLoginButtons){
		this.pluginSettings.put(MoPluginConstants.SHOW_LOGIN_BUTTONS,
				BooleanUtils.toString(showLoginButtons, "true", "false", "false"));
	}

	public Boolean getShowLoginButtons() {
		return  BooleanUtils.toBoolean(StringUtils.defaultString(
				(String)this.pluginSettings.get(MoPluginConstants.SHOW_LOGIN_BUTTONS), "true"));
	}

	public String getTimeDelay() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.TIME_DELAY), "01");
	}

	public void setTimeDelay(String timeDelay) {
		this.pluginSettings.put(MoPluginConstants.TIME_DELAY,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(timeDelay, "01")));
	}

	public Boolean getAllowUserCreation() {
		return BooleanUtils.toBoolean(StringUtils.defaultString(
				(String) this.pluginSettings.get(MoPluginConstants.ALLOW_USER_CREATION), "true"));
	}

	public void setAllowUserCreation(Boolean allowUserCreation) {
		this.pluginSettings.put(MoPluginConstants.ALLOW_USER_CREATION,
				BooleanUtils.toString(allowUserCreation, "true", "false", "true"));
	}
	public Boolean getForceAuthentication() {
		return BooleanUtils.toBoolean(StringUtils.defaultString(
				(String) this.pluginSettings.get(MoPluginConstants.FORCE_AUTHENTICATION), "false"));
	}

	public Boolean getEnablePassiveSso(){
		return BooleanUtils.toBoolean(StringUtils.defaultString((String) this.pluginSettings.get(MoPluginConstants.ENABLE_PASSIVE_SSO),"false"));
	}

	public void setEnablePassiveSso(Boolean enablePassiveSso)
	{
		LOGGER.info("setEnablePassiveSSO called");
		this.pluginSettings.put(MoPluginConstants.ENABLE_PASSIVE_SSO,BooleanUtils.toString(enablePassiveSso,"true","false","false"));
	}

	public void setAdvancedSSOConfig(JSONObject advnacedSSOConfig, String idpID) {
		this.pluginSettings.put(MoPluginConstants.ADVANCED_SSO_CONFIG + idpID, advnacedSSOConfig.toString());
	}
	public void setForceAuthentication(Boolean forceAuthentication) {
		this.pluginSettings.put(MoPluginConstants.FORCE_AUTHENTICATION,
				BooleanUtils.toString(forceAuthentication, "true", "false", "false"));
	}

	public JSONObject getAdvancedSSOConfig(String idpID) throws JSONException {
		String advancedSsoConfig = (String) this.pluginSettings.get(MoPluginConstants.ADVANCED_SSO_CONFIG + idpID);
		if (StringUtils.isNotBlank(advancedSsoConfig)) {
			return new JSONObject(advancedSsoConfig);
		}
		return null;
	}

	public boolean checkIfTestConfig(String idpId) {
		try {
			if (getIdpConfig(idpId).has("testConfig")) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.debug("Error while checking if test config has been performed");
			return false;
		}
		return false;
	}

	public void setShowIntroPage(Boolean showIntroPage) {
		pluginSettings.put(MoPluginConstants.SHOW_INTRO_PAGE, BooleanUtils.toString(showIntroPage, "true", "false", "false"));
	}

	public Boolean getShowIntroPage() {
		return BooleanUtils.toBoolean(StringUtils.defaultString(
				(String) pluginSettings.get(MoPluginConstants.SHOW_INTRO_PAGE), "true"));
	}
	
	public Boolean getisMigrated() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.IS_MIGRATED)),
				false);
	}
	public void setisMigrated(boolean isMigrated) {
		this.pluginSettings.put(MoPluginConstants.IS_MIGRATED,
				BooleanUtils.toString(isMigrated, "true", "false", "false"));
	}

	public Boolean getEnableLogoutTemplate(){
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.ENABLE_LOGOUT_TEMPLATE)),
				false);
	}

	public void setEnableLogoutTemplate(Boolean enablelogoutTemplate) {
		this.pluginSettings.put(MoPluginConstants.ENABLE_LOGOUT_TEMPLATE,
				BooleanUtils.toString(enablelogoutTemplate, "true", "false", "false"));
	}

	public String getCustomLogoutURL() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.CUSTOM_LOGOUT_URL),
				StringUtils.EMPTY);
	}

	public void setCustomLogoutURL(String customLogoutURL) {
		this.pluginSettings.put(MoPluginConstants.CUSTOM_LOGOUT_URL,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(customLogoutURL, StringUtils.EMPTY)));
	}

	public Boolean getRememberMeCookieEnabled() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.REMEMBERME_COOKIE)), false);
	}

	public void setRememberMeCookieEnabled(Boolean isRememberMeCookieEnabled) {
		LOGGER.debug("is cookie enabled?:" + isRememberMeCookieEnabled);
		this.pluginSettings.put(MoPluginConstants.REMEMBERME_COOKIE,
				BooleanUtils.toString(isRememberMeCookieEnabled, "true", "false", null));
	}

	private String getDefaultLogoutTemplate() {
		String logoutTemplate ="<html>"
			+"<head>"
			+"<title>Logout</title>"
			+"$webResourceManager.requireResource('$pluginproperties.pluginkey:resources')"
			+"<meta name='decorator' content='atl.general'>"
			+"</head>"
			+"<body class='aui-layout aui-theme-default page-type-login' >"
			+"<div class='aui-page-panel'>"
			+"<div class='aui-page-panel-inner'>"
			+"<section class='aui-page-panel-content'>"
			+"<header><h1>You have logged out successfully</h1></header>"
			+"<div class='aui-message info'>"
			+"<span class='aui-icon icon-info'></span>"
			+"<p class='title'>You are now logged out. Any automatic login has also been stopped.</p>"
			+"<p>Didn't mean to log out?<a href='$baseUrl'> Log in again.</a><p>"
			+"</div>"
			+"</section>"
			+"</div>"
			+"</div>"
			+"</body>"
			+"</html>";
		return logoutTemplate;
	}

	private String getDefaultErrorMessageTemplate() {
		String errorMessageTemplate="<html>"
			+"<head>"
			+"<title>SAML SSO Error</title>"
			+"$webResourceManager.requireResource('$pluginproperties.pluginkey:resources')"
			+"<meta name='decorator' content='atl.general'>"
			+"</head>"
			+"<body class='aui-layout aui-theme-default page-type-login' >"
			+"<div class='aui-page-panel'>"
			+"<div class='aui-page-panel-inner'>"
			+"<section class='aui-page-panel-content'>"
			+"<header><h1>SAML Single Sign On Failed</h1></header>"
			+"<div class='aui-message aui-message-error'>"
//			+"<span class='aui-icon icon-warning'></span>"
			+"<p>Please contact your administrator for more information</p>"
			+"</div>"
			+"<p>Click <a href='$baseUrl'>here</a> to go to the login page</p>"
			+"</section>"
			+"</div>"
			+"</div>"
			+"</body>"
			+"</html>";
		return errorMessageTemplate;
	}

	private String getDefaultLoginTemplate() {
		String loginTemplate="Custom Login Template";
		return loginTemplate;
	}

	public void setMetadataOption(String idpID, String metadataOption) {
		this.pluginSettings.put(MoPluginConstants.METADATA_OPTION + idpID,
				StringUtils.defaultIfBlank(metadataOption, StringUtils.EMPTY));
	}

	public String getMetadataOption(String idpID) {
		if (StringUtils.equals(idpID, MoPluginConstants.DEFAULT_IDP_ID)) {
			return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.METADATA_OPTION),
					StringUtils.EMPTY);
		}
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.METADATA_OPTION + idpID),
				StringUtils.EMPTY);
	}

	public String getInputMetadataUrl() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.INPUT_METADATA_URL),
				StringUtils.EMPTY);
	}

	public void setInputMetadataUrl(String url) {
		this.pluginSettings.put(MoPluginConstants.INPUT_METADATA_URL, StringUtils.trimToEmpty(url));
	}


	public String getIdpMetadataURL() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.IDP_METADATA_URL),
				StringUtils.EMPTY);
	}

	public void setIdpMetadataURL(String effectiveUrl) {
		this.pluginSettings.put(MoPluginConstants.IDP_METADATA_URL, StringUtils.trimToEmpty(effectiveUrl));
	}

	public Boolean getRefreshMetadata() {
		
		String refreshMetadata = (String) this.pluginSettings.get(MoPluginConstants.REFRESH_METADATA);
		if(StringUtils.isBlank(refreshMetadata)) {
			return Boolean.FALSE;
		} else {
			return BooleanUtils.toBoolean(refreshMetadata);
		}
		
	}

	public void setRefreshMetadata(Boolean refreshMetadata) {
		this.pluginSettings.put(MoPluginConstants.REFRESH_METADATA,
				BooleanUtils.toString(refreshMetadata, "true", "false", "false"));
	}

	public String getRefreshInterval() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.METADATA_REFRESH_INTERVAL),
				"hourly");
	}

	public void setRefreshInterval(String refreshInterval) {
		if(refreshInterval != null){
			this.pluginSettings.put(MoPluginConstants.METADATA_REFRESH_INTERVAL, StringUtils.trimToEmpty(refreshInterval));
		}else{
			this.pluginSettings.put(MoPluginConstants.METADATA_REFRESH_INTERVAL, StringUtils.trimToEmpty("hourly"));
		}
	}

	public Integer getCustomRefreshInterval() {
		String interval = (String) this.pluginSettings.get(MoPluginConstants.CUSTOM_REFRESH_INTERVAL);
		if (NumberUtils.isDigits(interval)) {
			return NumberUtils.createInteger(interval);
		} else {
			return MoSAMLUtils.getMetadataRefreshInterval(getRefreshInterval(), 60, "minutes");
		}
	}

	public void setCustomRefreshInterval(Integer customRefreshInterval) {
		this.pluginSettings.put(MoPluginConstants.CUSTOM_REFRESH_INTERVAL, String.valueOf(customRefreshInterval));
	}

	public String getCustomRefreshIntervalUnit() {
		return StringUtils.defaultIfBlank(
				(String) this.pluginSettings.get(MoPluginConstants.CUSTOM_REFRESH_INTERVAL_UNIT), "minutes");
	}

	public void setCustomRefreshIntervalUnit(String customRefreshIntervalUnit) {
		this.pluginSettings.put(MoPluginConstants.CUSTOM_REFRESH_INTERVAL_UNIT,
				StringUtils.trimToEmpty(customRefreshIntervalUnit));
	}

	public BambooUserManager getBambooUserManager() {
		return bambooUserManager;
	}

	public void setBambooUserManager(BambooUserManager bambooUserManager) {
		this.bambooUserManager = bambooUserManager;
	}

	public PluginAccessor getPluginAccessor() {
		return pluginAccessor;
	}

	private PluginAccessor pluginAccessor;

	public void setEnablePasswordChange(Boolean enableSAMLSSO) {
		this.pluginSettings.put(MoPluginConstants.ENABLE_PASSWORD_CHANGE,
				BooleanUtils.toString(enableSAMLSSO, "true", "false", "false"));
	}

	public Boolean getEnablePasswordChange(){
		String enablePasswordChange = (String) this.pluginSettings.get(MoPluginConstants.ENABLE_PASSWORD_CHANGE);
		if (StringUtils.isBlank(enablePasswordChange)) {
			return Boolean.TRUE;
		} else {
			return BooleanUtils.toBoolean(enablePasswordChange);
		}
		//return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String)pluginSettings.get(MoPluginConstants.ENABLE_PASSWORD_CHANGE)),true);
	}
	public BambooPermissionManager getBambooPermissionManager() {
		return bambooPermissionManager;
	}

	public void setBambooPermissionManager(BambooPermissionManager bambooPermissionManager) {
		this.bambooPermissionManager = bambooPermissionManager;
	}

	public void setIdpMap(HashMap<String, String> idpMap) {
		this.pluginSettings.put(MoPluginConstants.IDP_MAP, idpMap);
	}

	public HashMap<String, String> getIdpMap() {
		HashMap<String, String> idpMap = (HashMap<String, String>) this.pluginSettings.get(MoPluginConstants.IDP_MAP);
		if (idpMap == null) {
			return new HashMap<String, String>();
		}
		return idpMap;
	}

	public void setIdPList(ArrayList<String> idpList) {
		this.pluginSettings.put(MoPluginConstants.IDP_LIST, idpList);
	}

	public ArrayList<String> getIdPList() {
		ArrayList<String> idpList = (ArrayList<String>) this.pluginSettings.get(MoPluginConstants.IDP_LIST);
		if (idpList == null) {
			return new ArrayList<String>();
		}

		return idpList;
	}

	public void setIdpConfig(JSONObject idpConfigObj, String idpName) {
		this.pluginSettings.put(MoPluginConstants.IDP_CONFIG + idpName, idpConfigObj.toString());
	}

	public JSONObject getIdpConfig(String idpName) throws JSONException {
		String idpConfigString = (String) this.pluginSettings.get(MoPluginConstants.IDP_CONFIG + idpName);

		if (StringUtils.isNotBlank(idpConfigString)) {
			return new JSONObject(idpConfigString);
		}
		return null;
	}

	public void setDomains(String[] domains) {
		ArrayList<String> domainList = new ArrayList<>(domains.length);
		for (int i = 0; i < domains.length; i++) {
			domainList.add(i, domains[i]);
		}
		this.pluginSettings.put(MoPluginConstants.DOMAINS, domainList);
	}

	public List<String> getDomains() {
		List<String> domains = (List<String>) this.pluginSettings.get(MoPluginConstants.DOMAINS);
		return (domains != null) ? domains : new ArrayList<>();
	}

	public void setUseDomainMapping(Boolean useDomainMapping) {
		this.pluginSettings.put(MoPluginConstants.USE_DOMAIN_MAPPING,
				BooleanUtils.toString(useDomainMapping, "true", "false", "false"));
	}

	public Boolean getUseDomainMapping() {
		return BooleanUtils.toBooleanDefaultIfNull(
				BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.USE_DOMAIN_MAPPING)), false);
	}

	public void setDomainMapping(HashMap<String, String> domainMapping) {
		this.pluginSettings.put(MoPluginConstants.DOMAIN_MAPPING, domainMapping);
	}

	public HashMap<String, String> getDomainMapping() {
		HashMap<String, String> domainMapping = (HashMap<String, String>) this.pluginSettings
				.get(MoPluginConstants.DOMAIN_MAPPING);
		return (domainMapping != null) ? domainMapping : new HashMap<String, String>();
	}

	public JSONObject getImportMetadataConfig(String idpID) throws JSONException {
		String importMetadataConfig = (String) this.pluginSettings
				.get(MoPluginConstants.IMPORT_METADATA_CONFIG + idpID);
		if (StringUtils.isNotBlank(importMetadataConfig)) {
			return new JSONObject(importMetadataConfig);
		}
		return null;
	}

	public void setImportMetadataConfig(JSONObject importMetadataConfig, String idpID) {
		this.pluginSettings.put(MoPluginConstants.IMPORT_METADATA_CONFIG + idpID, importMetadataConfig.toString());
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

	public List<String> convertJasonArrayToList(JSONArray agentGroupsArray) throws JSONException {
		List<String> agentGroups = new ArrayList<>();
		if (agentGroupsArray != null) {
			for (int i = 0; i < agentGroupsArray.length(); i++) {
				agentGroups.add(agentGroupsArray.getString(i));
			}
		}
		return agentGroups;
	}

	public boolean checkIfAllQuickSetupsComplete() {
		boolean flag = true;
		for (String idp : getIdPList()) {
			if (!checkIfQuickSetupComplete(idp)) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	public boolean checkIfQuickSetupComplete(String idpId) {
		LOGGER.debug("checkIfQuickSetupComplete called for " + idpId);
		try {
			if (getIdpConfig(idpId).has("pageNumber")) {
				if (StringUtils.equalsIgnoreCase((String) getIdpConfig(idpId).get("pageNumber"), "4")
						|| StringUtils.equalsIgnoreCase((String) getIdpConfig(idpId).get("pageNumber"), "5")
						|| getIdpConfig(idpId).has("finishQuickSetup")) {
					LOGGER.debug("Quick setup complete for " + idpId);
					return true;
				} else if (getIdpConfig(idpId).has("attributeMappingSubmitted")
						&& getIdpConfig(idpId).has("roleMappingSubmitted")) {
					LOGGER.debug("Quick setup not required, manual setup done for " + idpId);
					return true;
				}
			} else {
				/**
				 This is to handle the cases where user chooses manual setup. If pageNumber isn't found in the idpConfig object, then that
				 means quicksetup was never started for this IDP.
				 **/
				return true;
			}
			LOGGER.debug("Quick setup not complete for " + idpId + ". Page :" + (String) getIdpConfig(idpId).get("pageNumber"));
		} catch (Exception e) {
			LOGGER.debug("Error while checking if quick setup was completed");
			return false;
		}
		return false;
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

	public void removeIdp(String idpID) {
		this.pluginSettings.remove(MoPluginConstants.IDP_CONFIG + idpID);
		this.pluginSettings.remove(MoPluginConstants.METADATA_OPTION + idpID);
		this.pluginSettings.remove(MoPluginConstants.ATTRIBUTE_MAPPING_CONFIG + idpID);
		this.pluginSettings.remove(MoPluginConstants.GROUP_MAPPING_CONFIG + idpID);
		this.pluginSettings.remove(MoPluginConstants.ADVANCED_SSO_CONFIG +idpID);

		HashMap<String, String> domainMapping = getDomainMapping();
		List<String> domains = getDomains();
		ArrayList<String> idpList = getIdPList();
		HashMap<String, String> idpMap = getIdpMap();
		ArrayList<String> ssoEnabledForIdPList = getSsoEnabledForIdPList();
		HashMap<String, String> issuerMap = getIssuerMap();

		Iterator<Map.Entry<String, String>> domainMappingIterator = domainMapping.entrySet().iterator();

		while (domainMappingIterator.hasNext()) {
			Map.Entry<String, String> currentEntry = domainMappingIterator.next();
			if (currentEntry.getValue().equals(idpID)) {
				domainMappingIterator.remove();
			}
		}

		if (idpList != null && idpList.contains(idpID)) {
			int index = idpList.indexOf(idpID);
			if (domains.size() > index)
				domains.remove(index);
			idpList.remove(idpID);
			idpMap.remove(idpID);
			ssoEnabledForIdPList.remove(idpID);
			issuerMap.remove(idpID);
		}

		setIdpMap(idpMap);
		setIdPList(idpList);
		setSsoEnabledForIdPList(ssoEnabledForIdPList);
		setIssuerMap(issuerMap);

		LOGGER.debug("[removeIdP] Domain Mapping: " + domainMapping.toString());
		LOGGER.debug("[removeIdP] Domains: " + domains.toString());

		setDomainMapping(domainMapping);
		setDomains(domains.toArray(new String[0]));
	}

	public void clearPluginSettings() {
		// TODO Auto-generated method stub
		this.pluginSettings.remove(MoPluginConstants.IDP_NAME);
		this.pluginSettings.remove(MoPluginConstants.ALL_X509_CERTIFICATES);
		this.pluginSettings.remove(MoPluginConstants.CREATE_USER_IF_ROLE_MAPPED);
		this.pluginSettings.remove(MoPluginConstants.CUSTOM_LOGOUT_URL);
		this.pluginSettings.remove(MoPluginConstants.DEFAULT_GROUP);
		this.pluginSettings.remove(MoPluginConstants.DEFAULT_GROUPS);
		this.pluginSettings.remove(MoPluginConstants.DISABLE_DEFAULT_LOGIN);
		this.pluginSettings.remove(MoPluginConstants.EMAIL_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_BACKDOOR);
		this.pluginSettings.remove(MoPluginConstants.ENCRYPTION);
		this.pluginSettings.remove(MoPluginConstants.FIRST_NAME_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.FULL_NAME_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.IDP_ENTITY_ID);
		this.pluginSettings.remove(MoPluginConstants.IDP_METADATA_URL);
		this.pluginSettings.remove(MoPluginConstants.KEEP_EXISTING_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.KEEP_EXISTING_USER_ROLES);
		this.pluginSettings.remove(MoPluginConstants.LAST_NAME_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.LOGIN_BUTTON_TEXT);
		this.pluginSettings.remove(MoPluginConstants.LOGOUT_TEMPLATE);
		this.pluginSettings.remove(MoPluginConstants.CUSTOM_REFRESH_INTERVAL);
		this.pluginSettings.remove(MoPluginConstants.CUSTOM_REFRESH_INTERVAL_UNIT);
		this.pluginSettings.remove(MoPluginConstants.METADATA_REFRESH_INTERVAL);
		this.pluginSettings.remove(MoPluginConstants.PRIVATE_SP_CERTIFICATE);
		this.pluginSettings.remove(MoPluginConstants.PUBLIC_SP_CERTIFICATE);
		this.pluginSettings.remove(MoPluginConstants.ALLOW_USER_CREATION);
		this.pluginSettings.remove(MoPluginConstants.REFRESH_METADATA);
		this.pluginSettings.remove(MoPluginConstants.RELAY_STATE);
		this.pluginSettings.remove(MoPluginConstants.RELAY_STATE_REDIRECTION_TYPE);
		this.pluginSettings.remove(MoPluginConstants.ROLE_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.ROLE_MAPPING);
		this.pluginSettings.remove(MoPluginConstants.SESSION_TIME);
		this.pluginSettings.remove(MoPluginConstants.SIGNED_REQUEST);
		this.pluginSettings.remove(MoPluginConstants.SIGNING);
		this.pluginSettings.remove(MoPluginConstants.SLO_BINDING_TYPE);
		this.pluginSettings.remove(MoPluginConstants.SLO_URL);
		this.pluginSettings.remove(MoPluginConstants.SP_BASE_URL);
		this.pluginSettings.remove(MoPluginConstants.SP_ENTITY_ID);
		this.pluginSettings.remove(MoPluginConstants.SSO_BINDING_TYPE);
		this.pluginSettings.remove(MoPluginConstants.SSO_URL);
		this.pluginSettings.remove(MoPluginConstants.NAME_ID_FORMAT);
		this.pluginSettings.remove(MoPluginConstants.TIME_DELAY);
		this.pluginSettings.remove(MoPluginConstants.VALIDATE_SAML_RESPONSE);
		this.pluginSettings.remove(MoPluginConstants.USE_SEPARATE_NAME_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.USERNAME_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.X509_CERTIFICATE);
		this.pluginSettings.remove(MoPluginConstants.ERROR_MSG_TEMPLATE);
		this.pluginSettings.remove(MoPluginConstants.INPUT_METADATA_URL);
		this.pluginSettings.remove(MoPluginConstants.METADATA_OPTION);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_REGEX_PATTERN);
		this.pluginSettings.remove(MoPluginConstants.REGEX_PATTERN);
		this.pluginSettings.remove(MoPluginConstants.LOGIN_USER_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_LOGOUT_TEMPLATE);
		this.pluginSettings.remove(MoPluginConstants.RESTRICT_USER_CREATION);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_ERRORMSG_TEMPLATE);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_SAML_SSO);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR);
		this.pluginSettings.remove(MoPluginConstants.ON_THE_FLY_GROUP_MAPPING);
		this.pluginSettings.remove(MoPluginConstants.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY);
		this.pluginSettings.remove(MoPluginConstants.ON_THE_FLY_DO_NOT_REMOVE_GROUPS);
		this.pluginSettings.remove(MoPluginConstants.ON_THE_FLY_CREATE_NEW_GROUPS);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_AUTO_REDIRECT_DELAY);
		this.pluginSettings.remove(MoPluginConstants.BACKDOOR_KEY);
		this.pluginSettings.remove(MoPluginConstants.BACKDOOR_VALUE);
		this.pluginSettings.remove(MoPluginConstants.RESTRICT_BACKDOOR);
		this.pluginSettings.remove(MoPluginConstants.BACKDOOR_GROUPS);
		this.pluginSettings.remove(MoPluginConstants.LOGIN_TEMPLATE);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_LOGIN_TEMPLATE);
		this.pluginSettings.remove(MoPluginConstants.REMEMBERME_COOKIE);

		for (int i = 0; i < getIdPList().size(); i++) {
			removeIdp(getIdPList().get(i));
		}
		this.pluginSettings.remove(MoPluginConstants.DOMAINS);
		this.pluginSettings.remove(MoPluginConstants.DOMAIN_MAPPING);
		this.pluginSettings.remove(MoPluginConstants.USE_DOMAIN_MAPPING);
		this.pluginSettings.remove(MoPluginConstants.IDP_LIST);
		this.pluginSettings.remove(MoPluginConstants.IDP_MAP);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_PASSWORD_CHANGE);
		this.pluginSettings.remove(MoPluginConstants.NUMBER_OF_LOGIN_ATTEMPTS);
		this.pluginSettings.remove(MoPluginConstants.BAMBOO_REDIRECTION_RULES);
		this.pluginSettings.remove(MoPluginConstants.SSO_ENABLED_FOR_IDP_LIST);
		this.pluginSettings.remove(MoPluginConstants.DEFAULT_BAMBOO_IDP);
		this.pluginSettings.remove(MoPluginConstants.HEADER_AUTHENTICATION_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.DEFAULT_REDIRECT_URL);
		this.pluginSettings.remove(MoPluginConstants.ISSUER_MAP);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_HEADER_AUTHENTICATION);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_SSO_FOR_IDP);
		this.pluginSettings.remove(MoPluginConstants.SHOW_INTRO_PAGE);
		this.pluginSettings.remove(MoPluginConstants.PAGE_NUMBER);
		this.pluginSettings.remove(MoPluginConstants.IS_MIGRATED);
		this.pluginSettings.remove(MoPluginConstants.AUTO_ACTIVATE_USER);
		this.pluginSettings.remove(MoPluginConstants.PLUGIN_API_ACCESS_RESTRICTION);
		this.pluginSettings.remove(MoPluginConstants.SP_CONFIGURATION_STATUS);
		this.pluginSettings.remove(MoPluginConstants.SHOW_LOGIN_BUTTONS);
		this.pluginSettings.remove(MoPluginConstants.NO_SSO_URLS);

		/*Certificate related keys*/
		this.pluginSettings.remove(MoPluginConstants.PUBLIC_SP_CERTIFICATE);
		this.pluginSettings.remove(MoPluginConstants.PRIVATE_SP_CERTIFICATE);
		this.pluginSettings.remove(MoPluginConstants.OLD_CONFIGURED_PRIVATE_SP_CERTIFICATE);
		this.pluginSettings.remove(MoPluginConstants.OLD_CONFIGURED_PUBLIC_SP_CERTIFICATE);
		this.pluginSettings.remove(MoPluginConstants.SP_CERTIFICATE_EXPIRE_ON);
		this.pluginSettings.remove(MoPluginConstants.CERT_EXPIRED);

		/*Metadata related keys*/
		this.pluginSettings.remove(MoPluginConstants.ORGANIZATION_NAME);
		this.pluginSettings.remove(MoPluginConstants.ORGANIZATION_DISPLAY_NAME);
		this.pluginSettings.remove(MoPluginConstants.ORGANIZATION_URL);
		this.pluginSettings.remove(MoPluginConstants.TECHNICAL_CONTACT_NAME);
		this.pluginSettings.remove(MoPluginConstants.TECHNICAL_CONTACT_EMAIL);
		this.pluginSettings.remove(MoPluginConstants.SUPPORT_CONTACT_NAME);
		this.pluginSettings.remove(MoPluginConstants.SUPPORT_CONTACT_EMAIL);

	}

	public void setAttributeMappingConfig(JSONObject attributeMappingConfig, String idpID) {
		this.pluginSettings.put(MoPluginConstants.ATTRIBUTE_MAPPING_CONFIG + idpID, attributeMappingConfig.toString());
	}

	public JSONObject getAttributeMappingConfig(String idpID) throws JSONException {
		String attributeMappingConfig = (String) this.pluginSettings
				.get(MoPluginConstants.ATTRIBUTE_MAPPING_CONFIG + idpID);
		if (StringUtils.isNotBlank(attributeMappingConfig)) {
			return new JSONObject(attributeMappingConfig);
		}
		return null;
	}

	public void setGroupMappingConfig(JSONObject groupMappingConfig, String idpID) {
		this.pluginSettings.put(MoPluginConstants.GROUP_MAPPING_CONFIG + idpID, groupMappingConfig.toString());
	}

	public JSONObject getGroupMappingConfig(String idpID) throws JSONException {
		String groupMappingConfig = (String) this.pluginSettings.get(MoPluginConstants.GROUP_MAPPING_CONFIG + idpID);
		if (StringUtils.isNotBlank(groupMappingConfig)) {
			return new JSONObject(groupMappingConfig);
		}
		return null;
	}

    public void clearOldConfiguration() {
		this.pluginSettings.remove(MoPluginConstants.ALL_X509_CERTIFICATES);
		this.pluginSettings.remove(MoPluginConstants.CREATE_USER_IF_ROLE_MAPPED);
		this.pluginSettings.remove(MoPluginConstants.DEFAULT_GROUP);
		this.pluginSettings.remove(MoPluginConstants.DEFAULT_GROUPS);
		this.pluginSettings.remove(MoPluginConstants.EMAIL_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.ENCRYPTION);
		this.pluginSettings.remove(MoPluginConstants.FIRST_NAME_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.FULL_NAME_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.IDP_ENTITY_ID);
		this.pluginSettings.remove(MoPluginConstants.IDP_METADATA_URL);
		this.pluginSettings.remove(MoPluginConstants.KEEP_EXISTING_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.KEEP_EXISTING_USER_ROLES);
		this.pluginSettings.remove(MoPluginConstants.LAST_NAME_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.CUSTOM_REFRESH_INTERVAL);
		this.pluginSettings.remove(MoPluginConstants.CUSTOM_REFRESH_INTERVAL_UNIT);
		this.pluginSettings.remove(MoPluginConstants.METADATA_REFRESH_INTERVAL);
		this.pluginSettings.remove(MoPluginConstants.REFRESH_METADATA);
		this.pluginSettings.remove(MoPluginConstants.ROLE_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.ROLE_MAPPING);
		this.pluginSettings.remove(MoPluginConstants.SIGNED_REQUEST);
		this.pluginSettings.remove(MoPluginConstants.SIGNING);
		this.pluginSettings.remove(MoPluginConstants.SLO_BINDING_TYPE);
		this.pluginSettings.remove(MoPluginConstants.SLO_URL);
		this.pluginSettings.remove(MoPluginConstants.SSO_BINDING_TYPE);
		this.pluginSettings.remove(MoPluginConstants.SSO_URL);
		this.pluginSettings.remove(MoPluginConstants.USE_SEPARATE_NAME_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.USERNAME_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.X509_CERTIFICATE);
		this.pluginSettings.remove(MoPluginConstants.INPUT_METADATA_URL);
		this.pluginSettings.remove(MoPluginConstants.METADATA_OPTION);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_REGEX_PATTERN);
		this.pluginSettings.remove(MoPluginConstants.REGEX_PATTERN);
		this.pluginSettings.remove(MoPluginConstants.LOGIN_USER_ATTRIBUTE);
		this.pluginSettings.remove(MoPluginConstants.RESTRICT_USER_CREATION);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_SAML_SSO);
		this.pluginSettings.remove(MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR);
		this.pluginSettings.remove(MoPluginConstants.ON_THE_FLY_GROUP_MAPPING);
		this.pluginSettings.remove(MoPluginConstants.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY);
		this.pluginSettings.remove(MoPluginConstants.ON_THE_FLY_DO_NOT_REMOVE_GROUPS);
		this.pluginSettings.remove(MoPluginConstants.ON_THE_FLY_CREATE_NEW_GROUPS);
    }

    public Integer getCurrentBuildNumber() {
		String buildNumber = BuildUtils.getCurrentBuildNumber();
		return Integer.parseInt(buildNumber);
	}

	public String getOrganizationName(){
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.ORGANIZATION_NAME), "miniorange");
	}

	public void setOrganizationName(String organizationName) {
		this.pluginSettings.put(MoPluginConstants.ORGANIZATION_NAME,
				StringUtils.defaultIfBlank(organizationName, "miniorange"));
	}
	public String getOrganizationDisplayName(){
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.ORGANIZATION_DISPLAY_NAME), "miniorange");
	}

	public void setOrganizationDisplayName(String organizationDisplayName){
		this.pluginSettings.put(MoPluginConstants.ORGANIZATION_DISPLAY_NAME,
				StringUtils.defaultIfBlank(organizationDisplayName, "miniorange"));
	}
	public String getOrganizationUrl(){
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.ORGANIZATION_URL), "http://miniorange.com");
	}

	public void setOrganizationUrl(String organizationUrl){
		this.pluginSettings.put(MoPluginConstants.ORGANIZATION_URL,
				StringUtils.defaultIfBlank(organizationUrl, "http://miniorange.com"));
	}
	public String getTechnicalContactName(){
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.TECHNICAL_CONTACT_NAME), "Xecurify");
	}

	public void setTechnicalContactName(String technicalContactName){
		this.pluginSettings.put(MoPluginConstants.TECHNICAL_CONTACT_NAME,
				StringUtils.defaultIfBlank(technicalContactName, "Xecurify"));
	}
	public String getTechnicalContactEmail(){
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.TECHNICAL_CONTACT_EMAIL), "info@xecurify.com");
	}

	public void setTechnicalContactEmail(String technicalContactEmail){
		this.pluginSettings.put(MoPluginConstants.TECHNICAL_CONTACT_EMAIL,
				StringUtils.defaultIfBlank(technicalContactEmail, "info@xecurify.com"));
	}
	public String getSupportContactName(){
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.SUPPORT_CONTACT_NAME), "Xecurify");
	}

	public void setSupportContactName(String supportContactName){
		this.pluginSettings.put(MoPluginConstants.SUPPORT_CONTACT_NAME,
				StringUtils.defaultIfBlank(supportContactName, "Xecurify"));
	}
	public String getSupportContactEmail(){
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.SUPPORT_CONTACT_EMAIL), "info@xecurify.com");
	}

	public void setSupportContactEmail(String supportContactEmail){
		this.pluginSettings.put(MoPluginConstants.SUPPORT_CONTACT_EMAIL,
				StringUtils.defaultIfBlank(supportContactEmail, "info@xecurify.com"));
	}

	public Boolean getPluginApiAccessRestriction() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.PLUGIN_API_ACCESS_RESTRICTION));
	}

	public void setPluginApiAccessRestriction(Boolean pluginApiAccessRestriction) {
		this.pluginSettings.put(MoPluginConstants.PLUGIN_API_ACCESS_RESTRICTION,
				BooleanUtils.toString(pluginApiAccessRestriction, "true", "false", "false"));
	}

	public void setResetAssertionIDListInterval(String resetAssertionIDListInterval) {
		this.pluginSettings.put(MoPluginConstants.ASSERTIONID_RESET_INTERVAL, StringUtils.trimToEmpty(resetAssertionIDListInterval));
	}

	public String getResetAssertionIDListInterval() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.ASSERTIONID_RESET_INTERVAL),
				"daily");
	}

	public Boolean getRestrictDuplicateAssertion() {
		return BooleanUtils.toBoolean((String) this.pluginSettings.get(MoPluginConstants.RESTRICT_DUPLICATE_ASSERTION));
	}

	public void setRestrictDuplicateAssertion(Boolean restrictDuplicateAssertion) {
		this.pluginSettings.put(MoPluginConstants.RESTRICT_DUPLICATE_ASSERTION,
				BooleanUtils.toString(restrictDuplicateAssertion, "true", "false", "false"));
	}


	public void setCustomResetInterval(Integer customResetIntervalInterval) {
		this.pluginSettings.put(MoPluginConstants.CUSTOM_RESET_INTERVAL, String.valueOf(customResetIntervalInterval));
	}


	public Integer getCustomResetInterval() {
		String interval = (String) this.pluginSettings.get(MoPluginConstants.CUSTOM_RESET_INTERVAL);
		if (NumberUtils.isDigits(interval)) {
			return NumberUtils.createInteger(interval);
		} else {
			return MoSAMLUtils.getAssertionIDResetInterval(getResetAssertionIDListInterval(), 24);
		}
	}

	public Boolean isRelayStateDomainValid(String savedRelayState,String relayState){
		try{
			URL relayStateURL = new URL(relayState);
			URL baseURL = new URL(getBaseUrl());

			if(StringUtils.equalsIgnoreCase(relayStateURL.getHost(), baseURL.getHost()) && relayState.contains(getBaseUrl())){
				return true;
			}
			if(StringUtils.isNotBlank(savedRelayState)){
				URL savedRelayStateUrl = new URL(savedRelayState);
				if (StringUtils.equalsIgnoreCase(savedRelayStateUrl.getHost(), relayStateURL.getHost())) {
					return true;
				}
			}

		}catch (MalformedURLException e){
			LOGGER.debug("Error in relayState :" + e.getMessage());
		}
		LOGGER.debug("Relay state domain is not valid.");
		return false;
	}

	public void setHeaderAuthenticationSettings(Boolean headerAuthentication) {
		this.pluginSettings.put(MoPluginConstants.ENABLE_HEADER_AUTHENTICATION,
				BooleanUtils.toString(headerAuthentication,"true","false","false"));
	}

	public Boolean getHeaderAuthenticationSettings(){
		return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject((String)this.pluginSettings.get(
				MoPluginConstants.ENABLE_HEADER_AUTHENTICATION)),false);
	}

	public String getHeaderAuthenticationAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.HEADER_AUTHENTICATION_ATTRIBUTE),
				"bamboo-user");
	}

	public void setHeaderAuthenticationAttribute(String headerAuthenticationAttribute) {
		this.pluginSettings.put(MoPluginConstants.HEADER_AUTHENTICATION_ATTRIBUTE,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(headerAuthenticationAttribute, "bamboo-user")));
	}

	public String getHeaderAuthenticationRelayStateAttribute() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.HEADER_AUTHENTICATION_RELAY_STATE_ATTRIBUTE),
				"relay-state-url");
	}

	public void setHeaderAuthenticationRelayStateAttribute(String headerAuthenticationAttribute) {
		this.pluginSettings.put(MoPluginConstants.HEADER_AUTHENTICATION_RELAY_STATE_ATTRIBUTE,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(headerAuthenticationAttribute, "relay-state-url")));
	}


	//If new Certs are null then assign plugin's default key pairs
	public String getNewPublicSPCertificate() {
		try {
			return IOUtils.toString(MoSAMLSettings.class.getResourceAsStream(NEW_PUBLIC_CERT_PATH),
					"UTF-8");
		} catch (IOException e) {
			LOGGER.error("An I/O error occurred while initializing the New Certificate .", e);
			return null;
		}
	}

	public String getNewPrivateSPCertificate() {
		try {
			return IOUtils.toString(MoSAMLSettings.class.getResourceAsStream(NEW_PRIVATE_CERT_PATH),
					"UTF-8");
		} catch (IOException e) {
			LOGGER.error("An I/O error occurred while initializing the New Certificate .", e);
			return null;
		}
	}

	/**In case fails to restore last configured then assign new certificate & Key*/

	public String getOldConfiguredPublicSPCertificate() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.OLD_CONFIGURED_PUBLIC_SP_CERTIFICATE),
				PUBLIC_CERTIFICATE);
	}

	public void setOldConfiguredPublicSPCertificate(String publicSPCertificate) {
		this.pluginSettings.put(MoPluginConstants.OLD_CONFIGURED_PUBLIC_SP_CERTIFICATE,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(publicSPCertificate, PUBLIC_CERTIFICATE)));
	}

	public String getOldConfiguredPrivateSPCertificate() {
		return StringUtils.defaultIfBlank((String) this.pluginSettings.get(MoPluginConstants.OLD_CONFIGURED_PRIVATE_SP_CERTIFICATE),
				PRIVATE_CERTIFICATE);
	}

	public void setOldConfiguredPrivateSPCertificate(String publicSPCertificate) {
		this.pluginSettings.put(MoPluginConstants.OLD_CONFIGURED_PRIVATE_SP_CERTIFICATE,
				StringUtils.trimToEmpty(StringUtils.defaultIfBlank(publicSPCertificate, PRIVATE_CERTIFICATE)));
	}

	public void setSPCertExpireOn(String validTo){
		try {
			Date from = new Date();
			SimpleDateFormat myformat = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
			from = myformat.parse(from.toString());
			Date to = myformat.parse(validTo);
			long difference = (to.getTime() - from.getTime()) / 86400000;
			this.pluginSettings.put(MoPluginConstants.SP_CERTIFICATE_EXPIRE_ON, String.valueOf(difference));
		} catch (Exception e){
			LOGGER.error("An error occurred while setting certificate expiry date");
		}
	}

	public Long getSPCertExpireOn() {
		String expireOn = (String) this.pluginSettings.get(MoPluginConstants.SP_CERTIFICATE_EXPIRE_ON);
		return NumberUtils.createLong(expireOn);
	}

	public List<String> getNoSSOUrls(){
		List<String> noSsoUrls = (List<String>) this.pluginSettings.get(MoPluginConstants.NO_SSO_URLS);
		if (noSsoUrls==null){
			return new ArrayList<String>();
		}
		return noSsoUrls;
	}

	public void setNoSSOUrls(List<String> noSsoUrls){
		this.pluginSettings.put(MoPluginConstants.NO_SSO_URLS,noSsoUrls);
	}
}
