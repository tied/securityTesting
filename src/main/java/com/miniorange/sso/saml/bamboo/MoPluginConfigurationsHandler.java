package com.miniorange.sso.saml.bamboo;

import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * MoPluginConfigurationsHandler
 */
public class MoPluginConfigurationsHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(MoPluginConfigurationsHandler.class);
	private MoSAMLSettings settings;
	private List<String> certificates;

	public MoPluginConfigurationsHandler( MoSAMLSettings settings){
		this.settings = settings;
	}

	public String generateConfigurationsJson() throws JSONException {
		// TODO Auto-generated method stub
		LOGGER.debug("Generating configurations JSON string ");
		JSONObject config = new JSONObject();

		config.putOpt("PLUGIN_NAME",MoPluginConstants.PLUGIN_NAME);

		HashMap<String, String> idpMap = settings.getIdpMap();
		for (String idpID : idpMap.keySet()) {
			JSONObject idpConfig = new JSONObject();
			MoIDPConfig idpConfigObj = MoPluginHandler.constructIdpConfigObject(idpID);
			idpConfig.put("ID", idpID);
			idpConfig.put("Name", idpMap.get(idpID));
			insertConfigSP(idpConfig, idpConfigObj);

			insertAttributeMapping(idpConfig, idpConfigObj);
			insertGroupMapping(idpConfig, idpConfigObj);
			insertImportMetadata(idpConfig, idpConfigObj);
			insertAdvancedSSOConfiguration(idpConfig,idpConfigObj);
			config.append("Identity Providers", idpConfig);
		}

		insertConfigIdP(config);
		insertSignOnSettings(config);
		//insertCustomTemplateConfigurations(config);
		insertCertificates(config);
		//insertDomainMapping(config);
		insertLooknFeelSettings(config);
		insertPostLogoutSettings(config);
		insertGlobalSSOSettings(config);
		insertRedirectionRules(config);
		return config.toString(2);
	}

	private void insertConfigIdP(JSONObject config) {
		// TODO Auto-generated method stub
		LOGGER.debug("Inserting SP in JSON Object.");
		JSONObject configIdp = new JSONObject();
		try {
			configIdp.putOpt("SP Entity ID", settings.getSpEntityId());
			configIdp.putOpt("SP Base URL", settings.getSpBaseUrl());
			configIdp.putOpt("Include Signing Certificate in Metadata", settings.getSigning());
			configIdp.putOpt("Include Encryption Certificate in Metadata", settings.getEncryption());
			configIdp.putOpt("Organization Name",settings.getOrganizationName());
			configIdp.putOpt("Organizaton Display name",settings.getOrganizationDisplayName());
			configIdp.putOpt("Organization Url",settings.getOrganizationUrl());
			configIdp.putOpt("Technical Contact Name",settings.getTechnicalContactName());
			configIdp.putOpt("Technical Contact Email",settings.getTechnicalContactEmail());
			configIdp.putOpt("Support Contact Name",settings.getSupportContactName());
			configIdp.putOpt("Support Contact Email",settings.getTechnicalContactEmail());
			config.putOpt("SP Info", configIdp);

		} catch (JSONException e) {
			// TODO: handle exception
			LOGGER.error("Error occurred while inserting SP configuration in JSON Object " + e.getMessage() + " Cause: "
					+ e.getCause());
			e.printStackTrace();
		}
	}

	private void importConfigureIdPConfigurations(JSONObject configureIdPObj) throws JSONException {
		/* Boolean signing, Boolean encryption */
		LOGGER.debug("importConfigureIdPConfigurations called ");
		Boolean signing = configureIdPObj.optBoolean("Include Signing Certificate in Metadata", Boolean.TRUE);
		Boolean encryption = configureIdPObj.optBoolean("Include Encryption Certificate in Metadata", Boolean.FALSE);
		MoPluginHandler.saveSPCertificates(signing, encryption);
	}

	private void importConfigureCustomizeMetadataConfigurations(JSONObject configureIdPObj){
		LOGGER.debug("importing Customize Metadata Configurations ");
		String orgName = configureIdPObj.optString("Organization Name", settings.getOrganizationName());
		String orgDisplayName = configureIdPObj.optString("Organizaton Display name", settings.getOrganizationDisplayName());
		String orgDomain = configureIdPObj.optString("Organization Url", settings.getOrganizationUrl());
		String techContactName = configureIdPObj.optString("Technical Contact Name", settings.getTechnicalContactName());
		String techContactEmail = configureIdPObj.optString("Technical Contact Email", settings.getTechnicalContactEmail());
		String supportContactName = configureIdPObj.optString("Support Contact Name", settings.getSupportContactName());
		String supportContactEmail = configureIdPObj.optString("Support Contact Email", settings.getTechnicalContactEmail());

		MoPluginHandler.saveCustomMetadata(orgName,orgDisplayName,orgDomain,techContactName,techContactEmail,supportContactName,supportContactEmail);

	}

	private void insertConfigSP(JSONObject config, MoIDPConfig idpConfigObj) {
		// TODO Auto-generated method stub
		LOGGER.debug("Inserting IdP configuration in JSON Object.");
		JSONObject configSP = new JSONObject();

		try {
			configSP.putOpt("IDP Entity ID / Issuer", idpConfigObj.getIdpEntityId());
			configSP.putOpt("Send Signed Requests", idpConfigObj.getSignedRequest());
			configSP.putOpt("SSO Binding Type", idpConfigObj.getSsoBindingType());
			configSP.putOpt("Single Sign On URL", idpConfigObj.getSsoUrl());
			configSP.putOpt("SLO Binding Type", idpConfigObj.getSloBindingType());
			configSP.putOpt("Single Logout URL", idpConfigObj.getSloUrl());
			configSP.putOpt("NameID Format", idpConfigObj.getNameIdFormat());
			if (idpConfigObj.getCertificates() != null) {
				certificates = (List<String>) idpConfigObj.getCertificates();
				for (String certificate : certificates) {
					configSP.append("IdP Signing Certificates", certificate);

				}
			} else {
				configSP.append("IdP Signing Certificates", StringUtils.EMPTY);
			}

			config.putOpt("Configure SP", configSP);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LOGGER.debug("Error occurred while inserting IdP Configuration in JSON " + e.getMessage() + " Cause: "
					+ e.getCause());
			e.printStackTrace();
		}

	}

	public void importConfigSPConfigurations(JSONObject configSP, String idpName, String idpID) throws JSONException {
		LOGGER.debug("importConfigSPConfigurations called ");
		String idpEntityId = configSP.optString("IDP Entity ID / Issuer", "");
		String ssoBinding = configSP.optString("SSO Binding Type", "");
		String ssoUrl = configSP.optString("Single Sign On URL", "");
		String sloBinding = configSP.optString("SLO Binding Type", "");
		String sloUrl = configSP.optString("Single Logout URL", "");
		String nameIdFormat=configSP.optString("NameID Format","urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
		Boolean signedRequest = configSP.optBoolean("Send Signed Requests", Boolean.TRUE);
		JSONArray signingCertificates = configSP.optJSONArray("IdP Signing Certificates");
		Boolean enableSsoForIdP = Boolean.TRUE;
		List<String> x509AllCertificates = new ArrayList<String>();
		String x509Certificate = "";
		if (signingCertificates != null) {
			x509Certificate = signingCertificates.optString(0, "");

			for (int i = 0; i < signingCertificates.length(); i++) {
				x509AllCertificates.add(signingCertificates.optString(i, ""));
			}
		}

		MoPluginHandler.saveIdPConfiguration(idpID, idpName, idpEntityId, ssoBinding, ssoUrl, sloBinding, sloUrl, x509AllCertificates,
				x509Certificate, signedRequest,nameIdFormat, enableSsoForIdP);
	}

	private void insertImportMetadata(JSONObject config, MoIDPConfig idpConfigObj) {
		LOGGER.debug("Inserting refresh metadata in JSON Object.   ");
		JSONObject importMetadata = new JSONObject();
		try {
			importMetadata.putOpt("IDP/Metadata Option", idpConfigObj.getMetadataOption());
			importMetadata.putOpt("Refresh Metadata", idpConfigObj.getRefreshMetadata());
			if (BooleanUtils.toBoolean(idpConfigObj.getRefreshMetadata())) {
				importMetadata.putOpt("Refresh Interval", idpConfigObj.getRefreshInterval());
				if (idpConfigObj.getRefreshInterval().equals("custom")) {
					importMetadata.putOpt("Custom Refresh Interval", idpConfigObj.getCustomRefreshInterval());
					importMetadata.putOpt("Custom Refresh Interval Unit", idpConfigObj.getCustomRefreshIntervalUnit());
				}
				// importMetadata.putOpt("Effective Metadata Url",
				// settings.getIdpMetadataUrl());
			}
			importMetadata.putOpt("Metadata Url/Domain", idpConfigObj.getInputUrl());
			config.put("Import Metadata", importMetadata);
		} catch (Exception e) {
			LOGGER.error("Error occurred while inserting refresh metadata configuration in JSON " + e.getMessage()
					+ " Cause: " + e.getCause());
			e.printStackTrace();
		}
	}

	private void importImportMetadataConfigurations(JSONObject importMetadataObj, String idpName, String idpID) throws Exception {
		LOGGER.debug("importImportMetadataConfigurations called ");
		String metadataOption = importMetadataObj.optString("IDP/Metadata Option", "");
		String metadataUrl = importMetadataObj.optString("Metadata Url/Domain", "");
		String effectiveMetadataUrl = importMetadataObj.optString("Effective Metadata Url", "");
		Boolean refreshMetadata = importMetadataObj.optBoolean("Refresh Metadata", Boolean.FALSE);
		String refreshInterval = importMetadataObj.optString("Refresh Interval", "hourly");
		Integer customRefreshInterval = importMetadataObj.optInt("Custom Refresh Interval", 60);
		String customRefreshIntervalUnit = importMetadataObj.optString("Custom Refresh Interval Unit", "minutes");
//		if (BooleanUtils.toBoolean(refreshMetadata)) {
			settings.setMetadataOption(idpID, metadataOption);
//		} else {
//			settings.setMetadataOption(idpID, "");
//		}
		MoPluginHandler.fetchMetadata(idpID, idpName, metadataUrl, effectiveMetadataUrl,  metadataOption);
		MoPluginHandler.toggleSchedulerService(idpID);
	}

	private void insertAttributeMapping(JSONObject config, MoIDPConfig idpConfigObj) {
		LOGGER.debug("Inserting Attribute Mapping configuration in JSON Object. ");
		JSONObject attributeMapping = new JSONObject();
		try {
			attributeMapping.putOpt("Username", idpConfigObj.getUsernameAttribute());
			attributeMapping.putOpt("Email", idpConfigObj.getEmailAttribute());
			attributeMapping.putOpt("Fullname", idpConfigObj.getFullNameAttribute());
			attributeMapping.putOpt("First Name", idpConfigObj.getFirstNameAttribute());
			attributeMapping.putOpt("Last Name", idpConfigObj.getLastNameAttribute());
			attributeMapping.putOpt("Disable Attribute Mapping", idpConfigObj.getKeepExistingUserAttributes());
			attributeMapping.putOpt("Separate Name Attributes", idpConfigObj.getUseSeparateNameAttributes());
			attributeMapping.putOpt("Regex Enabled", idpConfigObj.getRegexPatternEnabled());
			attributeMapping.putOpt("Login Bamboo user account by", idpConfigObj.getLoginUserAttribute());
			if (idpConfigObj.getRegexPatternEnabled()) {
				attributeMapping.putOpt("Regex Pattern", idpConfigObj.getRegexPattern());
			}
			config.putOpt("Attribute Mapping", attributeMapping);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error occurred while inserting Attribute Mapping configuration in JSON " + e.getMessage()
					+ " Cause: " + e.getCause());
			e.printStackTrace();
		}
	}

	private void insertAdvancedSSOConfiguration(JSONObject config, MoIDPConfig idpConfigObj){
		LOGGER.debug("Inserting Advanced SSO option configuration in JSON Object");
		JSONObject importAdvancedSsoObj = new JSONObject();
		try {
			importAdvancedSsoObj.putOpt("Allow User Creation" , idpConfigObj.getAllowUserCreation());
			importAdvancedSsoObj.putOpt("Enable Passive SSO",idpConfigObj.getEnablePassiveSso());
			importAdvancedSsoObj.putOpt("Force Authentication" , idpConfigObj.getForceAuthentication());
			importAdvancedSsoObj.putOpt("Metadata Url/Domain",idpConfigObj.getInputUrl());
			importAdvancedSsoObj.putOpt("Refresh Metadata",idpConfigObj.getRefreshMetadata());
			importAdvancedSsoObj.putOpt("Refresh Interval",idpConfigObj.getRefreshInterval());
			importAdvancedSsoObj.putOpt("Custom Refresh Interval",idpConfigObj.getCustomRefreshInterval());
			importAdvancedSsoObj.putOpt("Custom Refresh Interval Unit",idpConfigObj.getCustomRefreshIntervalUnit());
			importAdvancedSsoObj.putOpt("Relay State URL",idpConfigObj.getRelayState());
			importAdvancedSsoObj.putOpt("Relay State Redirection Type", idpConfigObj.getRelayStateRedirectionType());
			importAdvancedSsoObj.putOpt("Time delay",settings.getTimeDelay());

			config.putOpt("Advanced SSO Options", importAdvancedSsoObj);
		} catch (JSONException e){
			LOGGER.error("Error occurred while inserting Attribute Mapping configuration in JSON " , e);
		}
	}

	private void insertLooknFeelSettings(JSONObject config) {
		// TODO Auto-generated method stub
		LOGGER.debug("Inserting Look n Feel settings configuration.");
		JSONObject looknfeelSettings = new JSONObject();
		try {
			looknfeelSettings.putOpt("Login Button Text", settings.getLoginButtonText());
			looknfeelSettings.putOpt("Enable Custom Error Message Template", settings.getEnableErrorMsgTemplate());
			looknfeelSettings.putOpt("Custom Error Message Template", settings.getErrorMsgTemplate());
			looknfeelSettings.putOpt("Use Custom Login Template", settings.getEnableLoginTemplate());
			looknfeelSettings.putOpt("Custom Login Template", settings.getLoginTemplate());
			looknfeelSettings.putOpt("Show Buttons", settings.getShowLoginButtons());
			config.putOpt("Look and Feel Settings", looknfeelSettings);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error occurred while inserting Sign in settings configuration in JSON " ,e);
		}
	}
	private void insertPostLogoutSettings(JSONObject config) {
		// TODO Auto-generated method stub
		LOGGER.debug("Inserting Post Logout settings configuration.");
		JSONObject postLogoutSettings = new JSONObject();
		try {
			postLogoutSettings.putOpt("Custom Logout URL", settings.getCustomLogoutURL());
			postLogoutSettings.putOpt("Use Custom Logout Template", settings.getEnableLogoutTemplate());
			postLogoutSettings.putOpt("Custom Logout Template", settings.getLogoutTemplate());
			config.putOpt("Post Logout Settings", postLogoutSettings);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error occurred while inserting Sign in settings configuration in JSON " ,e);
		}
	}

	private void insertGlobalSSOSettings(JSONObject config) {
		LOGGER.debug("Inserting global SSO configuration in JSON Object. ");
		JSONObject globalSSOMapping = new JSONObject();
		try {
			globalSSOMapping.putOpt("Enable SSO For Bamboo",settings.getEnableSAMLSSO());
			globalSSOMapping.putOpt("Enable Password Change", settings.getEnablePasswordChange());
			globalSSOMapping.putOpt("Auto-Activate User",settings.getAutoActivateUser());
			globalSSOMapping.putOpt("Restrict Plugin's API", settings.getPluginApiAccessRestriction());
			globalSSOMapping.putOpt("Restrict Duplicate Assertion",settings.getRestrictDuplicateAssertion());
			globalSSOMapping.putOpt("AssertionID Reset Interval", settings.getResetAssertionIDListInterval());
			globalSSOMapping.putOpt("AssertionID  Custom Reset Interval", settings.getCustomResetInterval());


			config.putOpt("Global SSO Settings", globalSSOMapping);
		} catch (JSONException e) {

			LOGGER.error("Error occurred while inserting global SSO configuration in JSON " , e);

		}
	}

	private void insertRedirectionRules(JSONObject config) {
		LOGGER.debug("Adding Redirection Rules to the configuration");
		JSONObject redirectionRules = new JSONObject();
		JSONArray bambooRedirectionRules = settings.getRedirectionRules();
		try {
			redirectionRules.put("bamboo", bambooRedirectionRules);
			redirectionRules.put("Default Bamboo IDP",settings.getDefaultBambooIDP());
			config.put("redirectionRules", redirectionRules);
		} catch (JSONException e) {
			LOGGER.error("An Error Occurred while adding redirection rules to config file", e);
		}
	}

	private void importAdvancedSSOConfiguration(JSONObject importAdvancedSsoObj,String idpID) throws JSONException {
		Boolean allowUserCreation = importAdvancedSsoObj.optBoolean("Allow User Creation", Boolean.TRUE);
		Boolean enablePassiveSso = importAdvancedSsoObj.optBoolean("Enable Passive SSO", Boolean.TRUE);
		Boolean forceAuthentication = importAdvancedSsoObj.optBoolean("Force Authentication", Boolean.FALSE);
		String metadataUrl = importAdvancedSsoObj.optString("Metadata Url/Domain", "");
		Boolean refreshMetadata = importAdvancedSsoObj.optBoolean("Refresh Metadata", Boolean.FALSE);
		String refreshInterval = importAdvancedSsoObj.optString("Refresh Interval", "hourly");
		Integer customRefreshInterval = importAdvancedSsoObj.optInt("Custom Refresh Interval", 60);
		String customRefreshIntervalUnit = importAdvancedSsoObj.optString("Custom Refresh Interval Unit", "minutes");
		String relayState = importAdvancedSsoObj.optString("Relay State URL",settings.getRelayState());
		String relayStateRedirectionType = importAdvancedSsoObj.optString("Relay State Redirection Type", settings.getRelayStateRedirectionType());
		String timeDelay=importAdvancedSsoObj.optString("Time delay", settings.getTimeDelay());
		MoPluginHandler.saveAdvancedOption(idpID,allowUserCreation,forceAuthentication,metadataUrl,timeDelay, refreshMetadata,
				refreshInterval,customRefreshInterval,customRefreshIntervalUnit,relayState, relayStateRedirectionType,enablePassiveSso);
	}

	private void importRedirectionRules(JSONObject redirectionRulesObj) {
		LOGGER.debug("Importing Redirection Rules");

		JSONArray bambooRedirectionRules = redirectionRulesObj.optJSONArray("bamboo");
		try {
			for (int i = 0; i < bambooRedirectionRules.length(); i++) {
				JSONObject bambooRule = bambooRedirectionRules.getJSONObject(i);
				String ruleName = bambooRule.getString("name");
				String idp = bambooRule.getString("idp");
				JSONObject condition = bambooRule.getJSONObject("condition");
				String decisionFactor = condition.getString("decisionFactor");
				JSONObject value = condition.getJSONObject(decisionFactor);
				String conditionOperation = value.getString("conditionOperation");
				String conditionValue = value.getString(conditionOperation);
				MoPluginHandler.saveRedirectionRules(ruleName,decisionFactor,conditionOperation,conditionValue,idp);
			}
		} catch (JSONException e) {
			LOGGER.error("An error occurred while importing Redirection Rules ", e);
		}
		String defaultIdp = redirectionRulesObj.optString("Default Bamboo IDP");
		settings.setDefaultBambooIDP(defaultIdp);
	}

	private void importLookandFeelSettings(JSONObject lookandFeelSettingsObj) throws JSONException {

		LOGGER.debug("importLookandFeelSettings called ");
		String loginButtonText = lookandFeelSettingsObj.optString("Login Button Text", "Login with IdP");
		String errorMsgTemplate = lookandFeelSettingsObj.optString("Error Message Template", "");
		String loginTemplate = lookandFeelSettingsObj.optString("Login Template", "");
		Boolean enableLoginTemplate = lookandFeelSettingsObj.optBoolean("Use Custom Login Template", Boolean.FALSE);
		Boolean enableErrorMsgTemplate = lookandFeelSettingsObj.optBoolean("Enable Custom Error Message Template",
				Boolean.FALSE);
		Boolean showLoginButtons = lookandFeelSettingsObj.optBoolean("Show Login Buttons",Boolean.TRUE);

		MoPluginHandler.saveLookAndFeelSettings(loginButtonText,errorMsgTemplate,enableErrorMsgTemplate,enableLoginTemplate,loginTemplate, showLoginButtons);
	}
	private void importPostLogoutSettings(JSONObject postLogoutSettingsObj) throws JSONException {

		LOGGER.debug("importpostLogoutSettingsConfigurations called ");
		String customLogoutUrl = postLogoutSettingsObj.optString("Custom Logout URL", "");
		String logoutTemplate = postLogoutSettingsObj.optString("Logout Template", "");
		Boolean enablelogoutTemplate = postLogoutSettingsObj.optBoolean("Use Custom Logout Template", Boolean.FALSE);
		MoPluginHandler.savePostLogoutSettings(customLogoutUrl,enablelogoutTemplate,logoutTemplate);

	}

	private void importGlobalSSOSettings(JSONObject globalSettingsObj) throws JSONException {

		LOGGER.debug("importGlobalSettingsConfigurations called ");
		Boolean enableForSoftware = globalSettingsObj.optBoolean("Enable SSO For Bamboo", Boolean.TRUE);
		Boolean autoActivateUser = globalSettingsObj.optBoolean("Auto-Activate User",Boolean.FALSE);
		Boolean enablePasswordChange = globalSettingsObj.optBoolean("Enable Password Change",Boolean.TRUE);
		Boolean pluginApiAccessRestriction = globalSettingsObj.optBoolean("Restrict Plugin's API",Boolean.FALSE);
		Boolean restrictDuplicateAssertion = globalSettingsObj.optBoolean("Restrict Duplicate Assertion",false);

		String resetAssertionIDListInterval =globalSettingsObj.optString("AssertionID Reset Interval" ,"daily");
		int customResetInterval= globalSettingsObj.optInt("AssertionID  Custom Reset Interval",24);
		MoPluginHandler.saveGlobalSSOSettings( enableForSoftware, enablePasswordChange,autoActivateUser ,pluginApiAccessRestriction,restrictDuplicateAssertion,resetAssertionIDListInterval,customResetInterval );
	}

	private void importAttributeMappingConfigurations(JSONObject attributeMappingObj, String idpName, String idpID) throws JSONException {
		/*
		 * String usernameAttr, String emailAttr, String fullNameAttr, Boolean
		 * useSeparateNameAttribute, String firstNameAttribute, String
		 * lastNameAttribute, Boolean keepExistingUserAttributes,String
		 * regexPattern, Boolean regexPatternEnabled
		 */
		LOGGER.debug("importAttributeMappingConfigurations called ");
		String usernameAttr = attributeMappingObj.optString("Username", "NameID");
		String emailAttr = attributeMappingObj.optString("Email", "NameID");
		String fullNameAttr = attributeMappingObj.optString("Fullname", "");
		Boolean useSeparateNameAttribute = attributeMappingObj.optBoolean("Separate Name Attributes", Boolean.FALSE);
		String firstNameAttribute = attributeMappingObj.optString("First Name", "");
		String lastNameAttribute = attributeMappingObj.optString("Last Name", "");
		Boolean keepExistingUserAttributes = attributeMappingObj.optBoolean("Disable Attribute Mapping", Boolean.FALSE);
		String regexPattern = attributeMappingObj.optString("Regex Pattern", "");
		Boolean regexPatternEnabled = attributeMappingObj.optBoolean("Regex Enabled", Boolean.FALSE);
		String loginUserAttribute = attributeMappingObj.optString("Login Bamboo user account by", "");
		MoPluginHandler.saveAttributeMapping(idpID, usernameAttr, emailAttr, fullNameAttr, useSeparateNameAttribute,
				firstNameAttribute, lastNameAttribute, keepExistingUserAttributes, regexPattern, regexPatternEnabled,
				loginUserAttribute);
	}

	private void insertGroupMapping(JSONObject config, MoIDPConfig idpConfigObj) {
		// TODO Auto-generated method stub
		LOGGER.debug("Inserting Group Mapping configuration in JSON Object. ");
		JSONObject groupMapping = new JSONObject();
		try {
			groupMapping.putOpt("Disable Group Mapping", idpConfigObj.getKeepExistingUserRoles());
			groupMapping.putOpt("Group Attribute", idpConfigObj.getRoleAttribute());
			groupMapping.putOpt("Restrict User Creation", idpConfigObj.getRestrictUserCreation());
			groupMapping.putOpt("Restrict User Creation based on Group Mapping", idpConfigObj.getCreateUsersIfRoleMapped());
			groupMapping.putOpt("Default Group", idpConfigObj.getDefaultGroup());
			//group regex
			groupMapping.putOpt("Enable Group Regex", idpConfigObj.getGroupRegexPatternEnabled());
			groupMapping.putOpt("Regex Pattern for Group",idpConfigObj.getRegexPatternForGroup());
			groupMapping.putOpt("Regex Groups", idpConfigObj.getRegexGroups());
			groupMapping.putOpt("test Regex for Groups", idpConfigObj.getTestRegex());

			List<String> defaultGroups = (List<String>) idpConfigObj.getDefaultGroupsList();
			for (String defaultGroup : defaultGroups) {
				groupMapping.append("Default Groups", defaultGroup);
			}

			JSONObject mapping = new JSONObject();
			if (idpConfigObj.getRoleMapping() != null) {
				for (String bamboogroup : idpConfigObj.getRoleMapping().keySet()) {
					mapping.putOpt(bamboogroup, idpConfigObj.getRoleMapping().get(bamboogroup));
				}
			}
			groupMapping.putOpt("Mapping", mapping);
			groupMapping.putOpt("Enable Default Group For", idpConfigObj.getEnableDefaultGroupsFor());
			groupMapping.putOpt("On The Fly Group Mapping", idpConfigObj.getOnTheFlyGroupCreation());
			groupMapping.putOpt("Keep users in existing groups", idpConfigObj.getOnTheFlyAssignNewGroupsOnly());
			List<String> getOnTheFlyDoNotRemoveGroups = idpConfigObj.getOnTheFlyDoNotRemoveGroups();
			for (String removedGroup : getOnTheFlyDoNotRemoveGroups) {
				groupMapping.append("Do Not Remove User From These Groups", removedGroup);
			}
			groupMapping.putOpt("Create New Groups", idpConfigObj.getCreateNewGroups());
			config.putOpt("Group Mapping", groupMapping);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error occurred while inserting Group Mapping configuration in JSON " + e.getMessage()
					+ " Cause: " + e.getCause());
			e.printStackTrace();
		}
	}

	private void importGroupMappingConfigurations(JSONObject groupMappingObj, String idpName, String idpID) throws JSONException {
		/*
		 * String roleAttribute, Boolean createUsersIfRoleMapped, Boolean
		 * keepExistingUserRoles, HashMap<String, String> roleMapping, String
		 * defaultGroup, List<String> defaultGroups
		 */
		LOGGER.debug("importGroupMappingConfigurations called ");
		String roleAttribute = groupMappingObj.optString("Group Attribute", "");
		Boolean createUsersIfRoleMapped = groupMappingObj.optBoolean("Restrict User Creation based on Group Mapping",
				Boolean.FALSE);
		Boolean restrictUserCreation = groupMappingObj.optBoolean("Restrict User Creation", Boolean.FALSE);
		Boolean keepExistingUserRoles = groupMappingObj.optBoolean("Disable Group Mapping", Boolean.FALSE);
		//group regex
		Boolean groupRegexPatternEnabled=groupMappingObj.optBoolean("Enable Group Regex", Boolean.FALSE);
		String regexPatternForGroup=groupMappingObj.optString("Regex Pattern for Group",StringUtils.EMPTY);
		String regexGroups=groupMappingObj.optString("Regex Groups", StringUtils.EMPTY);
		String testRegex=groupMappingObj.optString("test Regex for Groups", StringUtils.EMPTY);

		JSONObject mapping = groupMappingObj.optJSONObject("Mapping");
		HashMap<String, String> roleMapping = new HashMap<>();
		Iterator<String> roleMappingIterator = mapping.keys();
		while (roleMappingIterator.hasNext()) {
			String bambooGroup = roleMappingIterator.next();

			roleMapping.put(bambooGroup, mapping.getString(bambooGroup));
		}
		String defaultGroup = groupMappingObj.optString("Default Group", "");
		List<String> defaultGroups = new ArrayList<String>();
		JSONArray defaultGroupsArray = groupMappingObj.optJSONArray("Default Groups");
		if (defaultGroupsArray != null) {
			for (int i = 0; i < defaultGroupsArray.length(); i++) {
				defaultGroups.add(defaultGroupsArray.getString(i));
			}
		}
		String enableDefaultGroupsFor = groupMappingObj.optString("Enable Default Group For",
				MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS);
		Boolean onTheFlyGroupMapping = groupMappingObj.optBoolean("On The Fly Group Mapping", Boolean.FALSE);
		Boolean onTheFlyAssignNewGroupsOnly = groupMappingObj.optBoolean("Keep users in existing groups", Boolean.TRUE);
		List<String> onTheFlyDoNotRemoveGroups = new ArrayList<String>();
		JSONArray groupsArray = groupMappingObj.optJSONArray("Do Not Remove User From These Groups");
		if (groupsArray != null) {
			for (int i = 0; i < groupsArray.length(); i++) {
				onTheFlyDoNotRemoveGroups.add(groupsArray.getString(i));
			}
		}
		Boolean canCreateNewGroups = groupMappingObj.optBoolean("Create New Groups", Boolean.FALSE);

		MoPluginHandler.saveRoleMapping(idpID, roleAttribute, createUsersIfRoleMapped, keepExistingUserRoles, roleMapping,
				defaultGroup, defaultGroups, restrictUserCreation, enableDefaultGroupsFor,
				onTheFlyGroupMapping, onTheFlyDoNotRemoveGroups, onTheFlyAssignNewGroupsOnly, canCreateNewGroups,
				groupRegexPatternEnabled,regexPatternForGroup,regexGroups,testRegex);
	}

	private void insertSignOnSettings(JSONObject config) {
		// TODO Auto-generated method stub
		LOGGER.debug("Inserting Sign in settings configuration.");
		JSONObject signInSettings = new JSONObject();
		try {
			signInSettings.putOpt("Enable SAML SSO", settings.getEnableSAMLSSO());
			signInSettings.putOpt("Auto Redirect to IdP", settings.getDefaultLoginDisabled());
			signInSettings.putOpt("Enable backdoor login", settings.getBackdoorEnabled());
			signInSettings.putOpt("Enable AutoRedirect Delay", settings.getEnableAutoRedirectDelay());
			signInSettings.putOpt("Number Of Login Attempts",settings.getNumberOfLoginAttempts());
			signInSettings.putOpt("Backdoor Key", settings.getBackdoorKey());
			signInSettings.putOpt("Backdoor Value", settings.getBackdoorValue());
			signInSettings.putOpt("Restrict Backdoor Access", settings.getRestrictBackdoor());
			signInSettings.putOpt("NO SSO URL List", settings.getNoSSOUrls());
			List<String> groupList = settings.getBackdoorGroups();
			if (groupList.size() <= 0) {
				groupList.add("bamboo-admin");
			}
			for (String backdoorGroup : groupList) {
				signInSettings.append("Restrict Backdoor Access For", backdoorGroup);
			}

			config.putOpt("Sign In Settings", signInSettings);
		} catch (JSONException e) {
			e.printStackTrace();
			LOGGER.error("Error occurred while inserting Sign in settings configuration in JSON " + e.getMessage()
					+ " Cause: " + e.getCause());
		}
	}

	private void importSignOnSettingsConfigurations(JSONObject signInSettingsObj) throws JSONException {
		LOGGER.debug("importSignOnSettingsConfigurations called ");
		Boolean disableDefaultLogin = signInSettingsObj.optBoolean("Auto Redirect to IdP", Boolean.FALSE);
		Boolean enableBackdoor = signInSettingsObj.optBoolean("Enable backdoor login", Boolean.FALSE);
		Boolean enableAutoRedirectDelay = signInSettingsObj.optBoolean("Enable AutoRedirect Delay", Boolean.FALSE);
		String numberOfLoginAttempts=signInSettingsObj.optString("Number Of Login Attempts","3");
		String loginButtonText = signInSettingsObj.optString("Login Button Text", "Login with IdP");
		String relayState = signInSettingsObj.optString("Relay State URL", "");
		String relayStateRedirectionType = signInSettingsObj.optString("Relay State Redirection Type", MoPluginConstants.FORCE_REDIRECT);
		String timeDelay = signInSettingsObj.optString("Time Skew", "00:00");
		String customLogoutURL = signInSettingsObj.optString("Custom Logout URL", "");
        Boolean enablelogoutTemplate = signInSettingsObj.optBoolean("Use Custom Logout Template", Boolean.FALSE);
       // Boolean enableLogoutRedirect = signInSettingsObj.optBoolean("Redirect after Logout", Boolean.FALSE);
        String errorMsgTemplate = signInSettingsObj.optString("Error Message Template", "");
        String logoutTemplate = signInSettingsObj.optString("Logout Template", "");
        Boolean enableErrorMsgTemplate = signInSettingsObj.optBoolean("Enable Custom Error Message Template",
                Boolean.FALSE);
        Boolean enableSAMLSSO=signInSettingsObj.optBoolean("Enable SAML SSO", Boolean.TRUE);
		Boolean restrictBackdoor = signInSettingsObj.optBoolean("Restrict Backdoor Access", Boolean.FALSE);
		String backdoorKey = signInSettingsObj.optString("Backdoor Key", "saml_sso");
		String backdoorValue = signInSettingsObj.optString("Backdoor Value", "false");

		List<String> noSsoUrls = new ArrayList<>();

		JSONArray no_sso_url_list = signInSettingsObj.optJSONArray("NO SSO URL List");
		JSONArray backdoorGroupsArray = signInSettingsObj.optJSONArray("Restrict Backdoor Access For");
		List<String> backdoorGroupsList = settings.convertJasonArrayToList(backdoorGroupsArray);

		if (no_sso_url_list != null) {
			for (int i = 0; i < no_sso_url_list.length(); i++) {
				noSsoUrls.add(no_sso_url_list.getString(i));
			}
		}

		Boolean enableLoginTemplate = signInSettingsObj.optBoolean("Use Custom Login Template", Boolean.FALSE);
		String loginTemplate = signInSettingsObj.optString("Login Template", "");
		Boolean enableRememberMeCookie = signInSettingsObj.optBoolean("Remember Me", Boolean.FALSE);
		Boolean enablePasswordChange = signInSettingsObj.optBoolean("Enable Password Change", Boolean.TRUE);
		Boolean headerAuthentication = signInSettingsObj.optBoolean("Enable Header Based Authentication",
				Boolean.FALSE);
		String headerAuthenticationAttribute = signInSettingsObj.optString("Authentication Attribute Name", "bamboo-user");

		Boolean pluginApiAccessRestriction = signInSettingsObj.optBoolean("Restrict Plugin's API", Boolean.FALSE);

		MoPluginHandler.saveAdvancedRedirectionSettings(enableAutoRedirectDelay,
				enableBackdoor,restrictBackdoor,backdoorGroupsList,numberOfLoginAttempts,noSsoUrls);

		MoPluginHandler.saveSigninSettings(disableDefaultLogin, enableBackdoor, loginButtonText, relayState, relayStateRedirectionType,
				customLogoutURL, enablelogoutTemplate, timeDelay, logoutTemplate, enableErrorMsgTemplate,
				errorMsgTemplate, enableSAMLSSO, enableAutoRedirectDelay,numberOfLoginAttempts, restrictBackdoor, backdoorGroupsList,
				enableLoginTemplate, loginTemplate, enableRememberMeCookie, enablePasswordChange, headerAuthentication,
				headerAuthenticationAttribute, pluginApiAccessRestriction);

		MoPluginHandler.saveBackdoorValues(backdoorKey, backdoorValue);
	}

	private void insertCertificates(JSONObject config) {
		// TODO Auto-generated method stub
		LOGGER.debug("Inserting custom certificates configuration in JSON Object.");
		JSONObject spCertificates = new JSONObject();
		try {
			spCertificates.putOpt("Public SP Certificate", settings.getPublicSPCertificate());
			spCertificates.putOpt("Private SP Certificate", settings.getPrivateSPCertificate());
			config.putOpt("Certificates", spCertificates);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.error("Error occurred while inserting custom certificate sp configuration in JSON " + e.getMessage()
					+ " Cause: " + e.getCause());
		}
	}

	private void importCertificatesConfigurations(JSONObject certificatesConfigObj) throws JSONException {
		/* String publicCertificate, String privateCertificate */
		LOGGER.debug("importCertificatesConfigurations called ");
		String publicCertificate = certificatesConfigObj.optString("Public SP Certificate", "");
		String privateCertificate = certificatesConfigObj.optString("Private SP Certificate", "");
		MoPluginHandler.saveSPCertificates(publicCertificate, privateCertificate);
	}

	/*private void insertCustomTemplateConfigurations(JSONObject config) {
		LOGGER.debug("Inserting custom template configuration in JSON Object. ");
		try {
			JSONObject templateObj = new JSONObject();
			templateObj.putOpt("Error Message Template", settings.getErrorMsgTemplate());
			templateObj.putOpt("Logout Template", settings.getLogoutTemplate());
			templateObj.putOpt("Enable Custom Error Message Template", settings.getEnableErrorMsgTemplate());
			config.putOpt("Custom Template", templateObj);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Error occurred while inserting custom template configuration in JSON " + e.getMessage()
					+ " Cause: " + e.getCause());
		}
	}*/

	//private void importCustomTemplateConfigurations(JSONObject customTemplateConfigObj) throws JSONException {
		/*
		 * String errorMsgTemplate, String logoutTemplate,Boolean
		 * enableErrorMsgTemplate
		 */
		//LOGGER.debug("importCustomTemplateConfigurations called ");
		//String errorMsgTemplate = customTemplateConfigObj.optString("Error Message Template", "");
		//String logoutTemplate = customTemplateConfigObj.optString("Logout Template", "");
		//Boolean enableErrorMsgTemplate = customTemplateConfigObj.optBoolean("Enable Custom Error Message Template",
		//		Boolean.FALSE);
		//pluginHandler.saveCustomTemplate(errorMsgTemplate, logoutTemplate, enableErrorMsgTemplate);
	//}

	private void insertDomainMapping(JSONObject config) {
		LOGGER.info("Inserting domain mapping configuration in plugin configurations file");
		try {
			JSONObject domainMappingObject = new JSONObject();
			domainMappingObject.putOpt("Use Domain Mapping", settings.getUseDomainMapping());
			for (String domain : settings.getDomains()) {
				domainMappingObject.append("Domains", domain);
			}
			JSONObject domainMapping = new JSONObject();
			HashMap<String, String> mapping = settings.getDomainMapping();
			for (String domain : mapping.keySet()) {
				domainMapping.putOpt(domain, mapping.get(domain));
			}
			domainMappingObject.putOpt("Domain Mapping", domainMapping);
			config.putOpt("Domain Mapping Configurations", domainMappingObject);

		} catch (Exception e) {
			e.printStackTrace();
			;
			LOGGER.error("Error occurred while inserting custom template configuration in JSON " + e.getMessage()
					+ " Cause: " + e.getCause());
		}
	}

	private void importDomainMappingConfigurations(JSONObject domainMappingConfigObject) throws JSONException {
		LOGGER.info("importing Domain Mapping Configurations");
		try {
			Boolean useDomainMapping = domainMappingConfigObject.optBoolean("Use Domain Mapping");
			List<String> domainsList = new ArrayList<>();
			JSONArray domainsListObject = domainMappingConfigObject.optJSONArray("Domains");
			if (domainsListObject != null) {
				for (int i = 0; i < domainsListObject.length(); i++) {
					domainsList.add(domainsListObject.optString(i));
				}
			}
			JSONObject domainMappingObject = domainMappingConfigObject.optJSONObject("Domain Mapping");
			HashMap<String, String> domainMappingMap = new HashMap<>();
			Iterator<String> domains = domainMappingObject.keys();
			while (domains.hasNext()) {
				String domain = domains.next();
				String idpId = domainMappingObject.optString(domain);
				domainMappingMap.put(domain, idpId);
			}
			// Setting all extracted values
			settings.setUseDomainMapping(useDomainMapping);
			settings.setDomains(domainsList.toArray(new String[0]));
			settings.setDomainMapping(domainMappingMap);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Error occurred while inserting custom template configuration in JSON " + e.getMessage()
					+ " Cause: " + e.getCause());
		}
	}

	public void importPluginConfigurations(String pluginConfigurationsJson) throws Exception {
		LOGGER.debug("Importing Plugin Configurations in JSON Object : "+pluginConfigurationsJson);
		try {
			JSONObject pluginConfigObj = new JSONObject(pluginConfigurationsJson);

			JSONArray identityProviders = pluginConfigObj.optJSONArray("Identity Providers");
			if (identityProviders == null) {
				JSONObject configIDP = pluginConfigObj.optJSONObject("Configure IDP");
				if (configIDP == null) {
					throw new MoPluginException(MoPluginException.PluginErrorCode.EMPTY_CONFIGURATIONS,
						MoPluginException.PluginErrorCode.EMPTY_CONFIGURATIONS.getMessage());
				}
			}

			if (identityProviders != null) {
				LOGGER.debug("identityProviders = " + identityProviders.length());

				for (int i = 0; i < identityProviders.length(); i++) {
					JSONObject idpConfig = identityProviders.getJSONObject(i);
					String idpID = idpConfig.optString("ID");
					String idpName = idpConfig.optString("Name");

					JSONObject configSP = idpConfig.optJSONObject("Configure SP");
					if (configSP != null) {
						importConfigSPConfigurations(configSP, idpName, idpID);
					}

					JSONObject attributeMapping = idpConfig.optJSONObject("Attribute Mapping");
					if (attributeMapping != null) {
						importAttributeMappingConfigurations(attributeMapping, idpName, idpID);
					}

					JSONObject groupMappingObj = idpConfig.optJSONObject("Group Mapping");
					if (groupMappingObj != null) {
						importGroupMappingConfigurations(groupMappingObj, idpName, idpID);
					}

					JSONObject importMetadataObj = idpConfig.optJSONObject("Import Metadata");
					if (importMetadataObj != null) {
						importImportMetadataConfigurations(importMetadataObj, idpName, idpID);
					}
					JSONObject importAdvancedSsoObj = idpConfig.optJSONObject("Advanced SSO Options");
					if (importAdvancedSsoObj != null){
						importAdvancedSSOConfiguration(importAdvancedSsoObj,idpID);
					}
					JSONObject redirectionRules = pluginConfigObj.optJSONObject("redirectionRules");
					if (redirectionRules != null) {
						importRedirectionRules(redirectionRules);
					}
				}
			} else {
				LOGGER.debug("Configuration Has old data");
				String idpName = "IDP";
				String idpID = UUID.randomUUID().toString();

				JSONObject configSP = pluginConfigObj.optJSONObject("Configure IDP");
				if (configSP != null) {
					importConfigSPConfigurations(configSP, idpName, idpID);
				}

				JSONObject attributeMapping = pluginConfigObj.optJSONObject("Attribute Mapping");
				if (attributeMapping != null) {
					importAttributeMappingConfigurations(attributeMapping, idpName, idpID);
				}

				JSONObject groupMappingObj = pluginConfigObj.optJSONObject("Group Mapping");
				if (groupMappingObj != null) {
					importGroupMappingConfigurations(groupMappingObj, idpName, idpID);
				}

				JSONObject importMetadataObj = pluginConfigObj.optJSONObject("Import Metadata");
				if (importMetadataObj != null) {
					importImportMetadataConfigurations(importMetadataObj, idpName, idpID);
				}
			}

			JSONObject configureIdPObj = pluginConfigObj.optJSONObject("SP Info");
			if (configureIdPObj != null) {
				importConfigureIdPConfigurations(configureIdPObj);
				importConfigureCustomizeMetadataConfigurations(configureIdPObj);
			}

			JSONObject signInSettingsObj = pluginConfigObj.optJSONObject("Sign In Settings");
			if (signInSettingsObj != null) {
				importSignOnSettingsConfigurations(signInSettingsObj);
			}

			JSONObject certificatesConfigObj = pluginConfigObj.optJSONObject("Certificates");
			if (certificatesConfigObj != null) {
				importCertificatesConfigurations(certificatesConfigObj);
			}

			JSONObject domainMappingConfigObj = pluginConfigObj.optJSONObject("Domain Mapping Configurations");
			if (domainMappingConfigObj != null) {
				importDomainMappingConfigurations(domainMappingConfigObj);
			}

			JSONObject looknFeelSettingsObj = pluginConfigObj.optJSONObject("Look and Feel Settings");
			if (looknFeelSettingsObj != null) {
				importLookandFeelSettings(looknFeelSettingsObj);
			}

			JSONObject postLogoutSettingsObj = pluginConfigObj.optJSONObject("Post Logout Settings");
			if (postLogoutSettingsObj != null) {
				importPostLogoutSettings(postLogoutSettingsObj);
			}
			JSONObject globalSSOSettingsObj = pluginConfigObj.optJSONObject("Global SSO Settings");
			if (globalSSOSettingsObj != null) {
				importGlobalSSOSettings(globalSSOSettingsObj);
			}

			/*JSONObject customTemplateConfigObj = pluginConfigObj.optJSONObject("Custom Template");
			if (customTemplateConfigObj != null) {
				importCustomTemplateConfigurations(customTemplateConfigObj);
			}*/
		} catch (JSONException e) {
			LOGGER.error("An error occurred while importing plugin Configurations " + e.getMessage() + " with Cause: "
					+ e.getCause());
		}

	}

	/**
	 * @return the settings
	 */
	public MoSAMLSettings getSettings() {
		return settings;
	}

	/**
	 * @param settings
	 *            the settings to set
	 */
	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}


}