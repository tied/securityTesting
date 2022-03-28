package com.miniorange.sso.saml.bamboo;

import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.sal.api.user.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.bamboo.schedulers.MoMetadataJobRunnerImpl;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import com.miniorange.sso.saml.dto.MoIDPMetadata;
import com.miniorange.sso.saml.utils.MoHttpUtils;
import com.miniorange.sso.saml.utils.MoJSONUtils;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.xml.SAMLConstants;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.sal.api.user.UserManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

public class MoPluginHandler {

	//private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static Log LOGGER = LogFactory.getLog(MoPluginHandler.class);
	private static MoSAMLSettings settings;
	private static MoPluginConfigurationsHandler moPluginConfigurationsHandler;
	static MoMetadataJobRunnerImpl metadataJobRunnerImpl;

	public static DirectoryManager directoryManager;
	private UserManager userManager;

	public MoPluginHandler(MoSAMLSettings settings, MoPluginConfigurationsHandler moPluginConfigurationsHandler,
						   MoMetadataJobRunnerImpl metadataJobRunnerImpl, DirectoryManager directoryManager, UserManager userManager){
		this.settings = settings;
		this.moPluginConfigurationsHandler = moPluginConfigurationsHandler;
		this.metadataJobRunnerImpl = metadataJobRunnerImpl;
		this.directoryManager = directoryManager;
		this.userManager = userManager;
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

	private static Boolean isJSONString(String string) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			mapper.readTree(string);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static void saveIdPConfiguration(String idpID, String idpName, String idpEntityId, String ssoBinding,
								String ssoUrl, String sloBinding, String sloUrl, List<String> x509AllCertificates,
								String x509Certificate, Boolean signedRequest,String nameIdFormat, Boolean enableSsoForIdp) {
		LOGGER.debug("saveIdPConfiguration Saving IDP Info, EntityID: " + idpEntityId);
		if (StringUtils.equals(idpID, MoPluginConstants.DEFAULT_IDP_ID)) {
			settings.setIdpName(MoSAMLUtils.sanitizeText(idpName));
			settings.setIdpEntityId(MoSAMLUtils.sanitizeText(idpEntityId));
			settings.setSsoServiceUrl(MoSAMLUtils.sanitizeText(ssoUrl));
			settings.setSsoBindingType(ssoBinding);
			settings.setSloServiceUrl(MoSAMLUtils.sanitizeText(sloUrl));
			settings.setSloBindingType(sloBinding);
			settings.setNameIdFormat(nameIdFormat);
			if (x509Certificate != null)
				settings.setX509Certificate(x509Certificate);
			else
				settings.setX509Certificate(x509AllCertificates.get(0));
			settings.setAllX509Certificates(x509AllCertificates);
			settings.setSignedRequest(signedRequest);
			settings.setNameIdFormat(nameIdFormat);
		} else {
			JSONObject idpConfigObj = constructIdpConfig(idpID, idpName, idpEntityId, ssoBinding, ssoUrl, sloBinding,
					sloUrl, x509AllCertificates, x509Certificate, signedRequest, nameIdFormat, enableSsoForIdp);
			LOGGER.debug("saveIdPConfigurations idpConfigObj: " + idpConfigObj.toString());

			HashMap<String, String> idpMap = settings.getIdpMap();
			ArrayList<String> idpList = settings.getIdPList();
			HashMap<String, String> issuerMap = settings.getIssuerMap();
			ArrayList<String> ssoEnabledForIdPList = settings.getSsoEnabledForIdPList();

			LOGGER.debug("Before Saving IDP Map: " + idpMap.toString());
			idpMap.put(idpID, idpName);
			issuerMap.put(idpID, idpEntityId);
			LOGGER.debug("After saving IDP Map: " + idpMap.toString());
			if (!idpList.contains(idpID)) {
				saveDefaultAttributeAndGroupMapping(idpID);
				saveDefaultAdvancedSSOSetting(idpID);
				idpList.add(idpID);
				ssoEnabledForIdPList.add(idpID);
			}else if(settings.checkIfTestConfig(idpID)){
				try {
					JSONObject existingConfigObj = settings.getIdpConfig(idpID);
					JSONObject testConfigObject = (JSONObject) existingConfigObj.get("testConfig");
					idpConfigObj.put("testConfig",testConfigObject);
				}catch (Exception e){
					LOGGER.debug("Existing idpConfig Object not found");
				}
			}
			settings.setIdpMap(idpMap);
			settings.setIssuerMap(issuerMap);
			settings.setIdPList(idpList);
			settings.setSsoEnabledForIdPList(ssoEnabledForIdPList);
			LOGGER.debug("After Saving" + settings.getIdpMap().toString());
			settings.setIdpConfig(idpConfigObj, idpID);
		}
	}

	public static void saveSPConfiguration(String spBaseUrl, String spEntityId) {
		LOGGER.debug("saveSPConfiguration Saving SP Info, SPBaseUrl: " + spBaseUrl + ", SPEntityID: " + spEntityId);
		settings.setSpBaseUrl(StringUtils.stripEnd(MoSAMLUtils.sanitizeText(spBaseUrl),"/"));
		settings.setSpEntityId(StringUtils.stripEnd(MoSAMLUtils.sanitizeText(spEntityId),"/"));
	}

	public static void saveCustomMetadata(String customOrganizationName,String customOrganizationDisplayName,String customOrganizationUrl,
								   String technicalContactName,String technicalContactEmail,String supportContactName,String supportContactEmail){
		LOGGER.debug("Saving Custom metadata - customOrganizationName:" + customOrganizationName + ",customOrganizationDisplayname:" + customOrganizationDisplayName + ",customOrganizationUrl:" + customOrganizationUrl +
				",technicalContactName:" + technicalContactName + ",technicalContactEmail:" + technicalContactEmail + ",supportContactName:" + supportContactName+",supportContactEmail:" + supportContactEmail);

		settings.setOrganizationName(customOrganizationName);
		settings.setOrganizationDisplayName(customOrganizationDisplayName);
		settings.setOrganizationUrl(customOrganizationUrl);
		settings.setTechnicalContactName(technicalContactName);
		settings.setTechnicalContactEmail(technicalContactEmail);
		settings.setSupportContactName(supportContactName);
		settings.setSupportContactEmail(supportContactEmail);
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


	public static void saveSPCertificates(Boolean signing, Boolean encryption) {
		LOGGER.debug("saveSPCertificates Saving SP Info, Signing: " + signing + ", Encryption: " + encryption);
		settings.setSigning(signing);
		settings.setEncryption(encryption);
	}

	public static void saveSPCertificates(String publicCertificate, String privateCertificate) {
		LOGGER.debug("saveSPCertificates Saving certificates " + publicCertificate);
		settings.setPublicSPCertificate(MoSAMLUtils.serializePublicCertificate(publicCertificate));
		settings.setPrivateSPCertificate(MoSAMLUtils.serializePrivateCertificate(privateCertificate));
	}

	public static void saveAttributeMapping(String idpID, String usernameAttr, String emailAttr, String fullNameAttr,
			Boolean useSeparateNameAttribute, String firstNameAttribute, String lastNameAttribute,
			Boolean keepExistingUserAttributes, String regexPattern, Boolean regexPatternEnabled,
			String loginUserAttribute) {
		LOGGER.debug("saveAttributeMapping Saving Attribute Mapping: UsernameAttr: " + usernameAttr + ", EmailAttr: "
				+ emailAttr + ", NameAttr: " + fullNameAttr + ", useSeparateName: " + useSeparateNameAttribute + ", "
				+ "firstName: " + firstNameAttribute + ", lastName: " + lastNameAttribute
				+ ", keepExistingUserAttributes " + keepExistingUserAttributes + ", regexPattern: " + regexPattern
				+ ", regexPatternEnabled: " + regexPatternEnabled + ", loginUserAttribute: " + loginUserAttribute);
		if (StringUtils.equals(idpID, MoPluginConstants.DEFAULT_IDP_ID)) {
			settings.setLoginUserAttribute(loginUserAttribute);
			settings.setUsernameAttribute(MoSAMLUtils.sanitizeText(usernameAttr));
			settings.setEmailAttribute(MoSAMLUtils.sanitizeText(emailAttr));
			if (BooleanUtils.toBoolean(useSeparateNameAttribute)) {
				settings.setFirstNameAttribute(MoSAMLUtils.sanitizeText(firstNameAttribute));
				settings.setLastNameAttribute(MoSAMLUtils.sanitizeText(lastNameAttribute));
			} else {
				settings.setFullNameAttribute(MoSAMLUtils.sanitizeText(fullNameAttr));
			}
			settings.setRegexPatternEnabled((BooleanUtils.toBoolean(regexPatternEnabled)));
			if (BooleanUtils.toBoolean(regexPatternEnabled))
				settings.setRegexPattern(MoSAMLUtils.sanitizeText(regexPattern));	//EXPERIMENTAL
			settings.setKeepExistingUserAttributes(keepExistingUserAttributes);
			settings.setUseSeparateNameAttributes(BooleanUtils.toBoolean(useSeparateNameAttribute));
		} else {
			JSONObject attributeConfig = constructAttributeConfig(usernameAttr, emailAttr, fullNameAttr,
					useSeparateNameAttribute, firstNameAttribute, lastNameAttribute, keepExistingUserAttributes,
					regexPattern, regexPatternEnabled, loginUserAttribute);

			settings.setAttributeMappingConfig(attributeConfig, idpID);
		}
	}

	public static void saveBackdoorValues(String backdoorKey, String backdoorValue) {
		LOGGER.debug("saveBackdoorValues backdoorKey= " + backdoorKey + ", backdoorValue= " + backdoorValue);
		settings.setBackdoorKey(backdoorKey);
		settings.setBackdoorValue(backdoorValue);
	}

	public static void saveSigninSettings(Boolean disableDefaultLogin, Boolean enableBackdoor, String loginButtonText,
			String relayState, String relayStateRedirectionType, String customLogoutURL, Boolean enablelogoutTemplate,
			String timeDelay , String logoutTemplate,Boolean enableErrorMsgTemplate, String errorMsgTemplate,
			Boolean enableSAMLSSO, Boolean enableAutoRedirectDelay,String numberOfLoginAttempts, Boolean restrictBackdoor, List<String> backdoorGroupsList,
			Boolean enableLoginTemplate, String loginTemplate, Boolean enableRememberMeCookie, Boolean enablePasswordChange,
			Boolean headerAuthentication, String headerAuthenticationAttribute, Boolean pluginApiAccessRestriction) {
		LOGGER.debug("Saving the Sign in Settings : disableDefaultLogin: " + disableDefaultLogin
				+ ", enableBackdoor: " + enableBackdoor + ", LoginButtonText: " + loginButtonText
				+ " timeDelay: " + timeDelay+",enableSAMLSSO :"+enableSAMLSSO+" enableLoginTemplate: "+enableLoginTemplate+" numberOfLoginAttempts: "+ numberOfLoginAttempts
				+ " enableRememberMeCookie: "+enableRememberMeCookie + " headerAuthentication: "+headerAuthentication
				+ " headerAuthenticationAttribute: "+headerAuthenticationAttribute +" pluginApiAccessRestriction : "+pluginApiAccessRestriction
		);
		settings.setDefaultLoginDisabled(BooleanUtils.toBoolean(disableDefaultLogin));
		settings.setBackdoorEnabled(BooleanUtils.toBoolean(enableBackdoor));
		settings.setLoginButtonText(MoSAMLUtils.sanitizeText(loginButtonText));
		settings.setRelayState(MoSAMLUtils.sanitizeText(relayState));
		settings.setRelayStateRedirectionType(relayStateRedirectionType);
		settings.setTimeDelay(timeDelay);
		settings.setEnableLogoutTemplate(BooleanUtils.toBoolean(enablelogoutTemplate));
		if(pluginApiAccessRestriction == null)
			pluginApiAccessRestriction = false;

		settings.setPluginApiAccessRestriction(BooleanUtils.toBoolean(pluginApiAccessRestriction));

		if (!BooleanUtils.toBoolean(enablelogoutTemplate))
		{
			settings.setCustomLogoutURL(MoSAMLUtils.sanitizeText(customLogoutURL));}
		 else {
			settings.setCustomLogoutURL(StringUtils.EMPTY);
		}
		 settings.setLogoutTemplate(logoutTemplate);
		settings.setEnableErrorMsgTemplate(BooleanUtils.toBoolean(enableErrorMsgTemplate));
		settings.setErrorMsgTemplate(errorMsgTemplate);
		settings.setEnableSAMLSSO(BooleanUtils.toBoolean(enableSAMLSSO));
		settings.setEnableAutoRedirectDelay(enableAutoRedirectDelay);
		settings.setNumberOfLoginAttempts(numberOfLoginAttempts);
		settings.setRestrictBackdoor(restrictBackdoor);
		settings.setBackdoorGroups(backdoorGroupsList);
		settings.setEnableLoginTemplate(enableLoginTemplate);
		settings.setLoginTemplate(loginTemplate);
		settings.setRememberMeCookieEnabled(enableRememberMeCookie);
		settings.setEnablePasswordChange(enablePasswordChange);
		settings.setHeaderAuthenticationSettings(BooleanUtils.toBoolean(headerAuthentication));
		settings.setHeaderAuthenticationAttribute(headerAuthenticationAttribute);
	}


	public static void saveRoleMapping(String idpID, String roleAttribute, Boolean createUsersIfRoleMapped, Boolean keepExistingUserRoles,
			HashMap<String, String> roleMapping, String defaultGroup, List<String> defaultGroupList, Boolean restrictUserCreation,
			String enableDefaultGroupsFor, Boolean onTheFlyGroupCreation, List<String> onTheFlyDoNotRemoveGroupsList,
			Boolean onTheFlyAssignNewGroupsOnly, Boolean createNewGroups,Boolean groupRegexPatternEnabled,String regexPatternForGroup,String regexGroups,String testRegex) {
		LOGGER.info("Saving Group Mapping Configuration ");
		LOGGER.debug("roleAttribute = " + roleAttribute + " createUsersIfRoleMapped = " + createUsersIfRoleMapped
				+ " keepExistingUserRoles = " + keepExistingUserRoles + " roleMapping = " + roleMapping
				+ " defaultGroup = " + defaultGroup + " defaultGroupList = " + defaultGroupList
				+ " restrictUserCreation = " + restrictUserCreation
				+ " enableDefaultGroupsFor = " + enableDefaultGroupsFor + " onTheFlyGroupCreation = "+onTheFlyGroupCreation
				+ " onTheFlyDoNotRemoveGroupsList = " + onTheFlyDoNotRemoveGroupsList + " onTheFlyAssignNewGroupsOnly = "+onTheFlyAssignNewGroupsOnly
				+"createNewGroups = "+createNewGroups + " groupRegexPatternEnabled = " + groupRegexPatternEnabled + " regexPatternForGroup = " + regexPatternForGroup + " regexGroups " + regexGroups+ " testRegex " + testRegex );
		if (StringUtils.equals(idpID, MoPluginConstants.DEFAULT_IDP_ID)) {
			settings.setRoleAttribute(MoSAMLUtils.sanitizeText(roleAttribute));
			settings.setCreateUsersIfRoleMapped(BooleanUtils.toBoolean(createUsersIfRoleMapped));
			settings.setKeepExistingUserRoles(BooleanUtils.toBoolean(keepExistingUserRoles));
			settings.setRoleMapping(roleMapping);
			settings.setRestrictUserCreation(restrictUserCreation);
			settings.setEnableDefaultGroupsFor(enableDefaultGroupsFor);
//			if (BooleanUtils.toBoolean(restrictUserCreation) == false) {
				settings.setCreateUsersIfRoleMapped(BooleanUtils.toBoolean(createUsersIfRoleMapped));
//				if (BooleanUtils.toBoolean(createUsersIfRoleMapped) == false) {
					settings.setDefaultGroup(defaultGroup);
					settings.setDefaultGroups(defaultGroupList);
//				}
//			}
			settings.setOnTheFlyGroupMapping(onTheFlyGroupCreation);
			if (BooleanUtils.toBoolean(onTheFlyGroupCreation)) {
				settings.setOnTheFlyDoNotRemoveGroups(onTheFlyDoNotRemoveGroupsList);
				settings.setOnTheFlyAssignNewGroupsOnly(onTheFlyAssignNewGroupsOnly);
				settings.setCreateNewGroups(createNewGroups);

				//group regex
				settings.setGroupRegexPatternEnabled(groupRegexPatternEnabled);
				settings.setRegexPatternForGroup(regexPatternForGroup);
				settings.setRegexGroups(regexGroups);
				settings.setTestRegex(testRegex);


			}
		} else {
			JSONObject groupMappingObject = constructGroupMappingConfigObj(roleAttribute, createUsersIfRoleMapped,
					keepExistingUserRoles, roleMapping, defaultGroup, defaultGroupList, restrictUserCreation,
					enableDefaultGroupsFor, onTheFlyGroupCreation, onTheFlyDoNotRemoveGroupsList,
					onTheFlyAssignNewGroupsOnly, createNewGroups,groupRegexPatternEnabled,regexPatternForGroup,regexGroups,testRegex);

			LOGGER.debug("[saveRoleMapping] Group Mapping Config " + groupMappingObject.toString());
			settings.setGroupMappingConfig(groupMappingObject, idpID);
		}
	}

	public static void
	saveAdvancedOption(String idpID, Boolean allowUserCreation,Boolean forceAuthentication,String url, String timeDelay,Boolean refreshMetadata,
										  String refreshInterval, Integer customRefreshInterval, String customRefreshIntervalUnit, String relayState, String relayStateRedirectionType, Boolean enablePassiveSso){

		settings.setAllowUserCreation(allowUserCreation);
		settings.setForceAuthentication(forceAuthentication);
		settings.setTimeDelay(timeDelay);
		settings.setEnablePassiveSso(enablePassiveSso);

		if (StringUtils.equals(idpID, MoPluginConstants.DEFAULT_IDP_ID)) {
			if (BooleanUtils.toBoolean(refreshMetadata)) {
				saveImportMetadata(idpID, StringUtils.trim(url), StringUtils.trim(url), refreshMetadata, refreshInterval, customRefreshInterval,
						customRefreshIntervalUnit);
			} else {
				saveImportMetadata(idpID, url, url, false, "hourly", 60, "minutes");
			}
		}  else {
			JSONObject advancedSSOConfig = constructAdvancedSSOConfig(allowUserCreation,forceAuthentication,url,refreshMetadata,
					refreshInterval, customRefreshInterval, customRefreshIntervalUnit, relayState, relayStateRedirectionType, timeDelay, enablePassiveSso);
			settings.setAdvancedSSOConfig(advancedSSOConfig,idpID);
		}
		toggleSchedulerService(idpID);
	}

	public Map<String, String> getIdpGuides() {
		Map<String, String> idp = new TreeMap();
		for (MoPluginConstants.idpGuides idpGuide:MoPluginConstants.idpGuides.values()
		) {
			idp.put(idpGuide.name(),idpGuide.getKey());
		}
		return idp;
	}

//<<<<<<< HEAD
	private static JSONObject constructAdvancedSSOConfig(Boolean allowUserCreation,Boolean forceAuthentication,String url,Boolean refreshMetadata, String refreshInterval, Integer customRefreshInterval,
														 String customRefreshIntervalUnit, String relayState, String relayStateRedirectionType, String timeDelay, Boolean enablePassiveSso) {
		JSONObject advancedSSOConfigObj = new JSONObject();
		try {
			advancedSSOConfigObj.put(MoPluginConstants.ALLOW_USER_CREATION, BooleanUtils.toBoolean(allowUserCreation));
			advancedSSOConfigObj.put(MoPluginConstants.FORCE_AUTHENTICATION, BooleanUtils.toBoolean(forceAuthentication));
			advancedSSOConfigObj.put(MoPluginConstants.INPUT_METADATA_URL,StringUtils.defaultIfBlank(url,"") );
			advancedSSOConfigObj.put(MoPluginConstants.REFRESH_METADATA,BooleanUtils.toBoolean(refreshMetadata));
			advancedSSOConfigObj.put(MoPluginConstants.METADATA_REFRESH_INTERVAL,StringUtils.defaultIfBlank(refreshInterval,"hourly"));
			advancedSSOConfigObj.put(MoPluginConstants.CUSTOM_REFRESH_INTERVAL, (customRefreshInterval != null) ? customRefreshInterval : 60);
			advancedSSOConfigObj.put(MoPluginConstants.CUSTOM_REFRESH_INTERVAL_UNIT, StringUtils.defaultIfBlank(customRefreshIntervalUnit, "minutes"));
			advancedSSOConfigObj.put(MoPluginConstants.RELAY_STATE, StringUtils.trimToEmpty(StringUtils.defaultIfBlank(relayState, "")));
			advancedSSOConfigObj.put(MoPluginConstants.RELAY_STATE_REDIRECTION_TYPE, StringUtils.defaultIfBlank(relayStateRedirectionType, MoPluginConstants.FORCE_REDIRECT));
			advancedSSOConfigObj.put(MoPluginConstants.TIME_DELAY, StringUtils.defaultIfBlank(timeDelay, "01"));
			advancedSSOConfigObj.put(MoPluginConstants.ENABLE_PASSIVE_SSO, BooleanUtils.toBoolean(enablePassiveSso));
		} catch (Exception e) {
			LOGGER.error("An error occurred while initializing Advanced SSO configurations",e);
		}
		return advancedSSOConfigObj;
	}

	private static void insertAdvancedSSOConfig(MoIDPConfig idpConfig, JSONObject importAdvancedSSOConfig){
		if (importAdvancedSSOConfig != null) {
			idpConfig.setAllowUserCreation(importAdvancedSSOConfig.optBoolean(MoPluginConstants.ALLOW_USER_CREATION,true));
			idpConfig.setForceAuthentication(importAdvancedSSOConfig.optBoolean(MoPluginConstants.FORCE_AUTHENTICATION,false));
			idpConfig.setRefreshMetadata(importAdvancedSSOConfig.optBoolean(MoPluginConstants.REFRESH_METADATA, false));
			idpConfig.setInputUrl(importAdvancedSSOConfig.optString(MoPluginConstants.INPUT_METADATA_URL, ""));
			idpConfig.setRefreshInterval(importAdvancedSSOConfig.optString(MoPluginConstants.METADATA_REFRESH_INTERVAL, "hourly"));
			idpConfig.setCustomRefreshInterval(importAdvancedSSOConfig.optInt(MoPluginConstants.CUSTOM_REFRESH_INTERVAL, 60));
			idpConfig.setCustomRefreshIntervalUnit(importAdvancedSSOConfig.optString(MoPluginConstants.CUSTOM_REFRESH_INTERVAL_UNIT, "minutes"));
			idpConfig.setRelayState(importAdvancedSSOConfig.optString(MoPluginConstants.RELAY_STATE, ""));
			idpConfig.setRelayStateRedirectionType(importAdvancedSSOConfig.optString(MoPluginConstants.RELAY_STATE_REDIRECTION_TYPE, MoPluginConstants.FORCE_REDIRECT));
			idpConfig.setTimeDelay(importAdvancedSSOConfig.optString(MoPluginConstants.TIME_DELAY, "01"));
			idpConfig.setEnablePassiveSso(importAdvancedSSOConfig.optBoolean(MoPluginConstants.ENABLE_PASSIVE_SSO,false));
		} else {
			idpConfig.setAllowUserCreation(Boolean.TRUE);
			idpConfig.setForceAuthentication(Boolean.FALSE);
			idpConfig.setRefreshMetadata(Boolean.FALSE);
			idpConfig.setInputUrl("");
			idpConfig.setRefreshInterval("hourly");
			idpConfig.setCustomRefreshInterval(60);
			idpConfig.setCustomRefreshIntervalUnit("minutes");
			idpConfig.setRelayState("");
			idpConfig.setRelayStateRedirectionType(MoPluginConstants.FORCE_REDIRECT);
			idpConfig.setTimeDelay("01");
			idpConfig.setEnablePassiveSso(Boolean.FALSE);
		}
	}

	public static void submitSupportQuery(String email, String phone, String query) {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("company", settings.getSpBaseUrl());
			jsonObject.put("email", email);
			jsonObject.put("phone", phone);
			jsonObject.put("query", StringUtils.prependIfMissing(query, "[Bamboo SAML SSO Plugin] "));
			String json = jsonObject.toString();
			String response = MoHttpUtils.sendPostRequest(MoPluginConstants.SUPPORT_QUERY_URL, json,
					MoHttpUtils.CONTENT_TYPE_JSON, getAuthorizationHeaders(
							Long.valueOf(MoPluginConstants.DEFAULT_CUSTOMER_KEY), MoPluginConstants.DEFAULT_API_KEY));
			LOGGER.debug("Submit Query response: " + response);
		} catch (Exception e) {
			LOGGER.error("An error occurred while saving your details.", e);
			throw new MoPluginException(MoPluginException.PluginErrorCode.UNKNOWN,
					"An error occurred while saving your details. Please check logs for more info.", e);
		}
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}

	public MoMetadataJobRunnerImpl getMoJobRunnerImpl() {
		return metadataJobRunnerImpl;
	}

	public static void saveCustomTemplate(String errorMsgTemplate, String logoutTemplate, Boolean enableErrorMsgTemplate) {
		LOGGER.info("Saving Custom Template");
		settings.setErrorMsgTemplate(StringUtils.trimToEmpty(errorMsgTemplate));
		settings.setLogoutTemplate(StringUtils.trimToEmpty(logoutTemplate));
		settings.setEnableErrorMsgTemplate(enableErrorMsgTemplate);
	}

	public static void uploadMetadata(String idpID, String idpName, File xmlFile, String metadataOption) {
		LOGGER.debug("uploadMetadata called ");
		try {

			String metadata = IOUtils.toString(new FileInputStream(xmlFile));

			LOGGER.debug("Metadata String from file = " + metadata);
			configureFromMetadata(idpID, idpName, metadata, false);
			saveMetadataOption(idpID, metadataOption);
			//saveImportMetadata(idpID, StringUtils.EMPTY, StringUtils.EMPTY, false, "hourly", 60, "minutes");
			saveAdvancedOption(idpID, true, false, "", "", false, "hourly",
					60, "minutes", "", MoPluginConstants.FORCE_REDIRECT,false);
			toggleSchedulerService(idpID);
		} catch (Exception e) {
			LOGGER.error("An error occurred while parsing IDP Metadata XML.");
			LOGGER.error(MoPluginException.PluginErrorCode.METADATA_PARSE.getMessage(), e);
			throw new MoPluginException(MoPluginException.PluginErrorCode.METADATA_PARSE, e.getMessage(), e);
		}
	}

	public static void saveMetadataOption(String idpID, String metadataOption) {
		LOGGER.info("Saving Metadata Options");
		settings.setMetadataOption(idpID, metadataOption);
	}

	public static void fetchMetadata(String idpID, String idpName, String metadataUrlString, String effectiveUrl,
			String metadataOption) throws Exception {
		if (StringUtils.isNotEmpty(effectiveUrl)) {
			String metadata = MoHttpUtils.sendGetRequest(effectiveUrl);
			configureFromMetadata(idpID, idpName, metadata, false);
		}
		settings.setMetadataOption(idpID, metadataOption);
		saveAdvancedOption(idpID, true,false, effectiveUrl, "", false,
				"hourly", 60, "minutes","", MoPluginConstants.FORCE_REDIRECT,false);

		toggleSchedulerService(idpID);
	}
	public static void savePostLogoutSettings(String customLogoutURL, Boolean enableLogoutTemplate, String logoutTemplate)  {
		LOGGER.info("Saving Post Logout Settings Configuration");
		settings.setCustomLogoutURL(customLogoutURL);
		settings.setLogoutTemplate(logoutTemplate);
		settings.setEnableLogoutTemplate(enableLogoutTemplate);
	}

	public void saveHeaderBasedAuthSettings(Boolean headerAuthentication, String headerAuthenticationAttribute){
		settings.setHeaderAuthenticationSettings(headerAuthentication);
		settings.setHeaderAuthenticationAttribute(headerAuthenticationAttribute);
	}

	public static void toggleSchedulerService(String idpID) {
		try {
			MoIDPConfig idpConfig = constructIdpConfigObject(idpID);

			if(idpConfig != null) {
				if (BooleanUtils.toBoolean(idpConfig.getRefreshMetadata())) {
					int interval = MoSAMLUtils.getMetadataRefreshInterval(idpConfig.getRefreshInterval(),
							idpConfig.getCustomRefreshInterval(), idpConfig.getCustomRefreshIntervalUnit());

					metadataJobRunnerImpl.schedule(interval, idpConfig);
				} else {
					metadataJobRunnerImpl.deleteSchedule(idpID);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error occurred while scheduling metadata refresh: " + e.getMessage());
		}
	}

	public static void configureFromMetadata(String idpID, String idpName, String metadata, Boolean isRefreshTask) {
		LOGGER.debug("Saving IdP Configuration from Meatadata file");
		metadata = metadata.replaceAll("[^\\x20-\\x7e]", "");
		MoIDPMetadata idpMetadata = new MoIDPMetadata(metadata);

		String idpEntityId = idpMetadata.getEntityId();
		String ssoBinding = "HttpRedirect";
		String ssoUrl = "";
		String sloBinding = "HttpRedirect";
		String sloUrl = "";
		String nameIdFormat = "";
		Boolean isRequestSigned = Boolean.TRUE;
		Boolean enableSsoForIdp = Boolean.TRUE;

		if (!BooleanUtils.toBoolean(isRefreshTask)) {

			nameIdFormat = StringUtils.defaultIfBlank(idpMetadata.nameIdFormat,
					"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
			if (idpMetadata.getSingleSignOnServices().containsKey(SAMLConstants.SAML2_REDIRECT_BINDING_URI)) {
				ssoBinding = "HttpRedirect";
				ssoUrl = idpMetadata.getSingleSignOnServices().get(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
			} else {
				ssoBinding = "HttpPost";
				ssoUrl = idpMetadata.getSingleSignOnServices().get(SAMLConstants.SAML2_POST_BINDING_URI);
			}
			if (idpMetadata.getSingleLogoutServices().size() > 0) {
				if (idpMetadata.getSingleLogoutServices().containsKey(SAMLConstants.SAML2_REDIRECT_BINDING_URI)) {
					sloBinding = "HttpRedirect";
					sloUrl = idpMetadata.getSingleLogoutServices().get(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
				} else {
					sloBinding = "HttpPost";
					sloUrl = idpMetadata.getSingleLogoutServices().get(SAMLConstants.SAML2_POST_BINDING_URI);
				}
			}
		} else {
			MoIDPConfig idpConfig = constructIdpConfigObject(idpID);
			nameIdFormat = idpConfig.getNameIdFormat();
			ssoBinding = idpConfig.getSsoBindingType();
			ssoUrl = idpConfig.getSsoUrl();
			sloUrl = idpConfig.getSloUrl();
			sloBinding = idpConfig.getSloBindingType();
			isRequestSigned = idpConfig.getSignedRequest();
			enableSsoForIdp = idpConfig.getEnableSsoForIdp();

		}
		String x509Certificate = idpMetadata.getSigningCertificates().get(0);
		List<String> x509AllCertificates = idpMetadata.getSigningCertificates();

		saveIdPConfiguration(idpID, idpName, idpEntityId, ssoBinding, ssoUrl, sloBinding, sloUrl, x509AllCertificates,
				x509Certificate, isRequestSigned, nameIdFormat, enableSsoForIdp);
	}

	public static void importConfigurations(String pluginConfigurations) throws IOException {
		LOGGER.debug("Importing app's configuration from config file");
		try {
			LOGGER.debug("importConfigurations :  pluginConfigurations  = " + pluginConfigurations);
			// Below code is added to clear the previous setting before importing the new one.
			stopAllSchedulers();
			settings.clearOldConfiguration();
			settings.clearPluginSettings();
			moPluginConfigurationsHandler.importPluginConfigurations(pluginConfigurations);
		} catch (JSONException e) {
			LOGGER.error(e.getMessage());
			throw new MoPluginException(MoPluginException.PluginErrorCode.SAVING_DETAILS, "Invalid file uploaded");
		} catch (Exception e) {
			e.printStackTrace();
			throw new MoPluginException(MoPluginException.PluginErrorCode.SAVING_DETAILS,
					"An error occurred while saving the configuration.");
		}
	}

	public MoPluginConfigurationsHandler getMoPluginConfigurationsHandler() {
		return moPluginConfigurationsHandler;
	}

	public void setMoPluginConfigurationsHandler(MoPluginConfigurationsHandler moPluginConfigurationsHandler) {
		this.moPluginConfigurationsHandler = moPluginConfigurationsHandler;
	}

	public void setMetadataJobRunnerImpl(MoMetadataJobRunnerImpl metadataJobRunnerImpl) {
		this.metadataJobRunnerImpl = metadataJobRunnerImpl;
	}

	public MoSAMLSettings getSettings() {
		return settings;
	}

	private static JSONObject constructIdpConfig(String idpID, String idpName, String idpEntityId, String ssoBinding,
										  String ssoUrl, String sloBinding, String sloUrl, List<String> x509AllCertificates, String x509Certificate,
										  Boolean signedRequest, String nameIdFormat, Boolean enableSSOForIDP) {
		JSONObject idpConfigObj = new JSONObject();
		try {
			idpConfigObj.put(MoPluginConstants.IDP_ID, idpID);
			idpConfigObj.put(MoPluginConstants.IDP_NAME, StringUtils.trimToEmpty(MoSAMLUtils.sanitizeText(idpName)));
			idpConfigObj.put(MoPluginConstants.IDP_ENTITY_ID, StringUtils.trimToEmpty(MoSAMLUtils.sanitizeText(idpEntityId)));
			idpConfigObj.put(MoPluginConstants.SSO_URL, StringUtils.trimToEmpty(MoSAMLUtils.sanitizeText(ssoUrl)));
			idpConfigObj.put(MoPluginConstants.SLO_URL, StringUtils.trimToEmpty(MoSAMLUtils.sanitizeText(sloUrl)));
			idpConfigObj.put(MoPluginConstants.SSO_BINDING_TYPE,
					StringUtils.defaultIfBlank(ssoBinding, "HttpRedirect"));
			idpConfigObj.put(MoPluginConstants.SLO_BINDING_TYPE,
					StringUtils.defaultIfBlank(sloBinding, "HttpRedirect"));

			idpConfigObj.putOpt(MoPluginConstants.X509_CERTIFICATE, StringUtils.trimToEmpty(
					Objects.toString(MoSAMLUtils.serializePublicCertificate(x509Certificate), StringUtils.EMPTY)));

			idpConfigObj.put(MoPluginConstants.NAME_ID_FORMAT, StringUtils.trimToEmpty(nameIdFormat));
			idpConfigObj.put(MoPluginConstants.ENABLE_SSO_FOR_IDP, BooleanUtils.toBooleanDefaultIfNull(enableSSOForIDP, true));

			idpConfigObj.put(MoPluginConstants.SIGNED_REQUEST,
					BooleanUtils.toBooleanDefaultIfNull(signedRequest, false));

			for (String certificate : x509AllCertificates) {
				idpConfigObj.append(MoPluginConstants.ALL_X509_CERTIFICATES,
						StringUtils.trimToEmpty(MoSAMLUtils.serializePublicCertificate(certificate)));
			}

		} catch (JSONException e) {
			LOGGER.error(e.getMessage());
		}
		return idpConfigObj;
	}

	public static MoIDPConfig constructIdpConfigObject(String idpID) {
		LOGGER.debug("constructIdPConfigObject called");
		MoIDPConfig idpConfig = null;
		JSONObject idpConfigObj;

		try {
			if (StringUtils.equals(idpID, MoPluginConstants.DEFAULT_IDP_ID)) {
				idpConfigObj = constructIdpConfig(idpID, "IDP", settings.getIdpEntityId(), settings.getSsoBindingType(),
						settings.getSsoServiceUrl(), settings.getSloBindingType(), settings.getSloServiceUrl(),
						(List<String>)settings.getAllX509Certificates(), settings.getX509Certificate(), settings.getSignedRequest(),
						settings.getNameIdFormat(), settings.getEnableSsoForIdp());

				if (idpConfigObj != null) {
					idpConfig = new MoIDPConfig();

					insertIDPConfig(idpConfig, idpConfigObj);
					JSONObject attributeMappingConfigObj = constructAttributeConfig(settings.getUsernameAttribute(),
							settings.getEmailAttribute(), settings.getFullNameAttribute(),
							settings.getUseSeparateNameAttributes(), settings.getFirstNameAttribute(),
							settings.getLastNameAttribute(), settings.getKeepExistingUserAttributes(),
							settings.getRegexPattern(), settings.getRegexPatternEnabled(),
							settings.getLoginUserAttribute());
					if (attributeMappingConfigObj != null) {
						insertAttributeMappingConfig(idpConfig, attributeMappingConfigObj);
					}
					JSONObject groupMappingConfigObj = constructGroupMappingConfigObj(settings.getRoleAttribute(),
							settings.getCreateUsersIfRoleMapped(), settings.getKeepExistingUserRoles(),
							settings.getRoleMapping(), settings.getDefaultGroup(),
							(List<String>) settings.getDefaultGroups(), settings.getRestrictUserCreation(),
							settings.getEnableDefaultGroupsFor(),
							settings.getOnTheFlyGroupMapping(), (List<String>) settings.getOnTheFlyDoNotRemoveGroups(),
							settings.getOnTheFlyAssignNewGroupsOnly(), settings.getCreateNewGroups(),
							settings.getGroupRegexPatternEnabled(),settings.getRegexPatternForGroup(),settings.getRegexPattern(),settings.getTestRegex());
					if (groupMappingConfigObj != null) {
						insertGroupMappingConfig(idpConfig, groupMappingConfigObj);
					}

					JSONObject advancedSettings = settings.getAdvancedSSOConfig(idpID);
					if(advancedSettings != null){
						insertAdvancedSSOConfig(idpConfig, advancedSettings);
					}

					idpConfig.setMetadataOption(settings.getMetadataOption(idpID));

					JSONObject importMetadataConfigObj = constructImportMetadataObj(settings.getInputMetadataUrl(),
							settings.getIdpMetadataURL(), settings.getRefreshMetadata(), settings.getRefreshInterval(),
							settings.getCustomRefreshInterval(), settings.getCustomRefreshIntervalUnit());
					insertImportMetadataConfig(idpConfig, importMetadataConfigObj);
				}
			} else {
				LOGGER.debug("[constructIdPConfigObject]IDP ID: " + idpID);
				idpConfigObj = settings.getIdpConfig(idpID);
				// LOGGER.debug("constructIdpConfigObject idpConfigObj:
				// "+idpConfigObj.toString());
				if (idpConfigObj != null) {
					idpConfig = new MoIDPConfig();
					insertIDPConfig(idpConfig, idpConfigObj);
					JSONObject attributeMappingConfigObj = settings.getAttributeMappingConfig(idpID);
					if (attributeMappingConfigObj != null) {
						insertAttributeMappingConfig(idpConfig, attributeMappingConfigObj);
					}
					JSONObject groupMappingConfigObj = settings.getGroupMappingConfig(idpID);
					if (groupMappingConfigObj != null) {
						insertGroupMappingConfig(idpConfig, groupMappingConfigObj);
					}

					JSONObject advancedSettings = settings.getAdvancedSSOConfig(idpID);
					if(advancedSettings != null){
						insertAdvancedSSOConfig(idpConfig, advancedSettings);
					}

					idpConfig.setMetadataOption(settings.getMetadataOption(idpID));

					//below fields are covered in advanced SSO settings
					//JSONObject importMetadataConfigObj = settings.getImportMetadataConfig(idpID);
					//insertImportMetadataConfig(idpConfig, importMetadataConfigObj);

				}
			}

		} catch (Throwable t) {
			t.printStackTrace();
			LOGGER.error("An exception occurred while initializing IDP Configurations " + t.getMessage());
		}
		return idpConfig;
	}

	public static void saveAdvancedRedirectionSettings( Boolean enableAutoRedirectDelay, Boolean enableBackdoor,
												 Boolean restrictBackdoor, List<String> backdoorGroupsList, String numberOfLoginAttempts,List<String> noSSOUrls) {
		settings.setEnableAutoRedirectDelay(enableAutoRedirectDelay);
		settings.setBackdoorEnabled(enableBackdoor);
		settings.setRestrictBackdoor(restrictBackdoor);
		settings.setBackdoorGroups(backdoorGroupsList);
		settings.setNumberOfLoginAttempts(numberOfLoginAttempts);
		settings.setNoSSOUrls(noSSOUrls);

	}

	public void saveBambooRedirectionRules(Map<String, String> bambooRedirectionRulesMap) {
		String bambooRedirectionRules = MoJSONUtils.convertMapToJSON(bambooRedirectionRulesMap);
		settings.setBambooRedirectionRulesMap(bambooRedirectionRules);

	}

	public static void saveRedirectionRules(String ruleName, String decisionFactor, String conditionOperation, String conditionValue, String idp) throws JSONException{
		JSONObject rule = new JSONObject();
		rule.put("name",ruleName);
		rule.put("idp",idp);
		JSONObject condition = new JSONObject();
		JSONObject value = new JSONObject();
		value.put("conditionOperation",conditionOperation);
		value.put(conditionOperation,conditionValue);
		condition.put("decisionFactor",decisionFactor);
		condition.put(decisionFactor,value);
		rule.put("condition",condition);
		settings.setRedirectionRule(rule.toString(),ruleName);
		String ruleExpression = createExpression(decisionFactor,conditionOperation,conditionValue,idp);
		String bambooRedirectionRules = settings.getBambooRedirectionRuleJson();
		bambooRedirectionRules = MoJSONUtils.addKeyValue(bambooRedirectionRules,ruleName,ruleExpression);
		settings.setBambooRedirectionRulesMap(bambooRedirectionRules);

	}

	private static String createExpression(String decisionFactor, String conditionOperation, String conditionValue, String idp) {
		StringBuilder expression = new StringBuilder("<label><b>If user's</b></label> ");
		expression.append("<em>"+decisionFactor+"</em>");
		expression.append(" <label><b>"+conditionDisplayValue(conditionOperation)+"</b></label> ");
		if(StringUtils.equals(decisionFactor,"directory")) {
			try {
				if(!StringUtils.equalsIgnoreCase(conditionOperation,"regex")) {
					conditionValue = directoryManager.findDirectoryByName(conditionValue).getName();
				}
			} catch (DirectoryNotFoundException e) {
				LOGGER.error("Error Occurred while creating Rule Expression ",e);
			}
		}
		expression.append("<em>"+conditionValue+"</em> ");
		expression.append("<label><b>then redirect to</b></label> ");
		if(StringUtils.equalsIgnoreCase(idp,"loginpage"))
			expression.append("<em>Login Page</em>");
		else
			expression.append("<em>"+settings.getIdpMap().get(idp)+"</em>");
		return expression.toString();
	}
	public static String conditionDisplayValue(String conditionOperation){
		LOGGER.debug("Condition Operation : " + conditionOperation);
		switch (conditionOperation){
			case "equals" :
				return "equals";
			case "regex":
				return "contains";
		}
		return null;
	}
	private static void insertIDPConfig(MoIDPConfig idpConfig, JSONObject idpConfigObj) {
		try {
			idpConfig.setId(idpConfigObj.optString(MoPluginConstants.IDP_ID, ""));
			idpConfig.setIdpName(idpConfigObj.optString(MoPluginConstants.IDP_NAME, ""));
			idpConfig.setIdpEntityId(idpConfigObj.optString(MoPluginConstants.IDP_ENTITY_ID, ""));
			idpConfig.setSsoUrl(idpConfigObj.optString(MoPluginConstants.SSO_URL, ""));
			idpConfig.setSsoBindingType(idpConfigObj.optString(MoPluginConstants.SSO_BINDING_TYPE, "HttpRedirect"));
			idpConfig.setSloUrl(idpConfigObj.optString(MoPluginConstants.SLO_URL, ""));
			idpConfig.setSloBindingType(idpConfigObj.optString(MoPluginConstants.SLO_BINDING_TYPE, "HttpRedirect"));
			idpConfig.setX509Certificate(idpConfigObj.optString(MoPluginConstants.X509_CERTIFICATE, ""));
			JSONArray certificates = idpConfigObj.optJSONArray(MoPluginConstants.ALL_X509_CERTIFICATES);
			List<String> x509AllCertificates = new ArrayList<String>();
			if (certificates != null) {
				for (int i = 0; i < certificates.length(); i++) {
					x509AllCertificates.add(certificates.getString(i));
				}
			}
			idpConfig.setCertificates(x509AllCertificates);
			idpConfig.setSignedRequest(idpConfigObj.optBoolean(MoPluginConstants.SIGNED_REQUEST, true));
			idpConfig.setNameIdFormat(idpConfigObj.optString(MoPluginConstants.NAME_ID_FORMAT,
					"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified"));
			idpConfig.setEnableSsoForIdp(idpConfigObj.optBoolean(MoPluginConstants.ENABLE_SSO_FOR_IDP, true));
		} catch (Exception e) {
			LOGGER.error("An error occurred while creating IDP configurations", e);
		}
	}

	public static Boolean isValidDomainMapping(String[] domains, Boolean useDomainMapping) {
		int count = 0;
		if (useDomainMapping != null && useDomainMapping) {
			if (domains != null && domains.length > 0) {
				for (String domain : domains) {
					if (StringUtils.isBlank(domain)) {
						return false;
					}
					count++;
				}
				if (domains.length == count) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	public Boolean isValidDomainEntered(String[] domains, Boolean useDomainMapping) {
		int count = 0;
		if (useDomainMapping != null && useDomainMapping) {
			if (domains != null && domains.length > 0) {
				for (String domain : domains) {
					if (StringUtils.isBlank(domain) && StringUtils.endsWith(domain, ";")) {
						return false;
					}
					String[] subDomains = domain.trim().split(";");
					for (String subDomain : subDomains) {
						LOGGER.debug("Sub Domain name : "+subDomain);
						if (!isValidDomain(subDomain)) {
							return false;
						}
					}
					count++;
				}
				if (domains.length == count) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean isValidDomain(String domainName) {
		boolean ret = true;

		if("".equals(domainName) || domainName==null) {
			ret = false;
		} else if(domainName.startsWith("-")||domainName.endsWith("-")) {
			ret = false;
		} else if(!domainName.contains(".")) {
			ret = false;
		} else if (StringUtils.endsWith(domainName.trim(), ".")) {
			ret = false;
		} else {
			String domainEle[] = domainName.split("\\.");
			int size = domainEle.length;
			for(int i=0;i<size;i++) {
				String domainEleStr = domainEle[i];
				if(StringUtils.isEmpty(domainEleStr.trim()) || StringUtils.isBlank(domainEleStr.trim())) {
					return false;
				}
			}

			char[] domainChar = domainName.toCharArray();
			size = domainChar.length;
			for(int i=0;i<size;i++) {
				char eleChar = domainChar[i];
				String charStr = String.valueOf(eleChar);

				if(!".".equals(charStr) && !"-".equals(charStr) && !charStr.matches("[a-zA-Z]") && !charStr.matches("[0-9]")) {
					ret = false;
					break;
				}
			}
		}
		return ret;
	}

	public static void saveListIDPConfigurations(String idpID, Boolean isChecked) {
		JSONObject idpConfigObj;
		idpConfigObj = settings.getIdpConfig(idpID);
		idpConfigObj.put(MoPluginConstants.ENABLE_SSO_FOR_IDP,
				BooleanUtils.toBooleanDefaultIfNull(isChecked, false));
		settings.setIdpConfig(idpConfigObj, idpID);

		ArrayList<String> ssoEnabledForIdpList = settings.getSsoEnabledForIdPList();
		if(isChecked){
			ssoEnabledForIdpList.add(idpID);
		} else{
			ssoEnabledForIdpList.remove(idpID);
			if(StringUtils.equals(settings.getDefaultBambooIDP(), idpID)){
				settings.setDefaultBambooIDP("loginPage");
			}
		}
		LOGGER.debug("ssoEnabled IDPs: "+ssoEnabledForIdpList);
		settings.setSsoEnabledForIdPList(ssoEnabledForIdpList);
	}

	private static void saveDomainMapping(String[] domains) {
		HashMap<String, String> domainMapping = new HashMap<String, String>();
		ArrayList<String> idpList = settings.getIdPList();

		LOGGER.debug("idpList " + idpList.toString());
		LOGGER.debug("domains: " + Arrays.asList(domains).toString());
		for (int i = 0; i < domains.length; i++) {
			if (StringUtils.isNotBlank(domains[i])) {
				String[] multipleDomains = domains[i].trim().split(";");
				for (String domain : multipleDomains) {
					if (domainMapping.containsKey(domain)
							&& !StringUtils.equalsIgnoreCase(idpList.get(i), domainMapping.get(domain))) {
						throw new MoPluginException(MoPluginException.PluginErrorCode.SAVING_DETAILS,
								"Domain names must be unique");
					}
					domainMapping.put(StringUtils.trimToEmpty(domain), idpList.get(i));
				}
			}
		}
		LOGGER.debug("Domain Mapping: " + domainMapping.toString());
		settings.setDomains(domains);
		settings.setDomainMapping(domainMapping);
	}

	public static JSONObject constructImportMetadataObj(String inputUrl, String idpMetadataUrl, Boolean refreshMetadata,
												 String refreshInterval, Integer customRefreshInterval, String customRefreshIntervalUnit) {
		JSONObject importMetadataObj = new JSONObject();
		try {
			importMetadataObj.put(MoPluginConstants.INPUT_METADATA_URL, StringUtils.defaultIfBlank(inputUrl, ""));
			importMetadataObj.put(MoPluginConstants.IDP_METADATA_URL, StringUtils.defaultIfBlank(idpMetadataUrl, ""));
			importMetadataObj.put(MoPluginConstants.REFRESH_METADATA, BooleanUtils.toBoolean(refreshMetadata));
			importMetadataObj.put(MoPluginConstants.METADATA_REFRESH_INTERVAL,
					StringUtils.defaultIfBlank(refreshInterval, "hourly"));
			importMetadataObj.put(MoPluginConstants.CUSTOM_REFRESH_INTERVAL,
					(customRefreshInterval != null) ? customRefreshInterval : 60);
			importMetadataObj.put(MoPluginConstants.CUSTOM_REFRESH_INTERVAL_UNIT,
					StringUtils.defaultIfBlank(customRefreshIntervalUnit, "minutes"));
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			LOGGER.error("An error occurred while initializing import metadata configurations");
		}
		return importMetadataObj;
	}

	public static void saveImportMetadata(String idpID, String inputUrl, String idpMetadataUrl, Boolean refreshMetadata,
								   String refreshInterval, Integer customRefreshInterval, String customRefreshIntervalUnit) {
		LOGGER.info("Saving Configuration for Import Metadata");
		LOGGER.debug("inputUrl = " + inputUrl + " idpMetadataUrl = " + idpMetadataUrl + " refreshMetadata = "
				+ refreshMetadata + "refreshInterval = " + refreshInterval + " customRefreshInterval = "
				+ customRefreshInterval + " customRefreshIntervalUnit = " + customRefreshIntervalUnit);
		JSONObject importMetadataObj = constructImportMetadataObj(inputUrl, idpMetadataUrl, refreshMetadata,
				refreshInterval, customRefreshInterval, customRefreshIntervalUnit);
		settings.setImportMetadataConfig(importMetadataObj, idpID);

	}

	public  static void saveLookAndFeelSettings(String loginButtonText, String errorMsgTemplate, Boolean enableErrorMsgTemplate, Boolean enableLoginTemplate, String loginTemplate, Boolean showLoginButtons) {

		LOGGER.info("Saving Look And Feel Configuration");
		settings.setLoginButtonText(loginButtonText);
		settings.setEnableLoginTemplate(enableLoginTemplate);
		settings.setLoginTemplate(loginTemplate);
		settings.setErrorMsgTemplate(StringUtils.trimToEmpty(errorMsgTemplate));
		settings.setEnableErrorMsgTemplate(enableErrorMsgTemplate);
		settings.setShowLoginButtons(showLoginButtons);

	}

	public static void saveGlobalSSOSettings(Boolean enableSAMLSSO, Boolean enablePasswordChange, Boolean  autoActivateUser, Boolean pluginApiAccessRestriction,Boolean restrictDuplicateAssertion, String resetAssertionIDListInterval,int customResetInterval){
		settings.setEnableSAMLSSO(enableSAMLSSO);
		settings.setEnablePasswordChange(enablePasswordChange);
		settings.setAutoActivateUser(autoActivateUser);
		settings.setPluginApiAccessRestriction(pluginApiAccessRestriction);
		settings.setRestrictDuplicateAssertion(restrictDuplicateAssertion);
		settings.setResetAssertionIDListInterval(resetAssertionIDListInterval);
		settings.setCustomResetInterval(customResetInterval);
		if(BooleanUtils.toBoolean(restrictDuplicateAssertion)) {
			settings.setResetAssertionIDListInterval(resetAssertionIDListInterval);
			settings.setCustomResetInterval(customResetInterval);
			/*Toggle Scheduler Code. Add all set functions above this one*/
			if (StringUtils.equals(resetAssertionIDListInterval, settings.getResetAssertionIDListInterval())) {
				if (StringUtils.equals(resetAssertionIDListInterval, "daily"))
					return;
				else if (customResetInterval == settings.getCustomResetInterval())
					return;
			}
			//toggleResetSchedulerService();
		}else{
			//moAssertionIDResetService.unschedule();
		}
	}

	/*public static void toggleResetSchedulerService() {
		moAssertionIDResetService.unschedule();
		moAssertionIDResetService.reschedule(settings,MoSAMLUtils.getAssertionIDResetInterval(settings.getResetAssertionIDListInterval(),settings.getCustomResetInterval()));
	}*/
	public static void replaceOldSettingsWithNew(String idpID, String idpName) throws Exception {
		if (settings.getIdPList().size() == 0) {
			MoSAMLUserManager.replaceOldSettingsWithNew(settings.getReplaceOldSettingWithNewUrl(), idpID,
					idpName);

/*
			saveIdPConfiguration(idpID, idpName, settings.getIdpEntityId(), settings.getSsoBindingType(),
					settings.getSsoServiceUrl(), settings.getSloBindingType(), settings.getSloServiceUrl(),
					(List<String>) settings.getAllX509Certificates(), settings.getX509Certificate(), settings.getSignedRequest(),
					settings.getNameIdFormat());
			saveAttributeMapping(idpID, settings.getUsernameAttribute(), settings.getEmailAttribute(),
					settings.getFullNameAttribute(), settings.getUseSeparateNameAttributes(),
					settings.getFirstNameAttribute(), settings.getLastNameAttribute(),
					settings.getKeepExistingUserAttributes(), settings.getRegexPattern(),
					settings.getRegexPatternEnabled(), settings.getLoginUserAttribute());
			saveRoleMapping(idpID, settings.getRoleAttribute(), settings.getCreateUsersIfRoleMapped(),
					settings.getKeepExistingUserAttributes(), settings.getRoleMapping(), settings.getDefaultGroup(),
					(List<String>) settings.getDefaultGroups(), settings.getRestrictUserCreation(),
					settings.getUpdateUserOnlyIfRoleMapped());
			saveImportMetadata(idpID, settings.getInputMetadataUrl(), settings.getIdpMetadataURL(),
					settings.getRefreshMetadata(), settings.getRefreshInterval(), settings.getCustomRefreshInterval(),
					settings.getCustomRefreshIntervalUnit());
			metadataJobRunnerImpl.deleteSchedule(MoPluginConstants.DEFAULT_IDP_ID);
			toggleSchedulerService(idpID);

			settings.clearOldConfiguration();
*/
		}
	}

	private static void insertImportMetadataConfig(MoIDPConfig idpConfig, JSONObject importMetadataConfigObj) {
		if (importMetadataConfigObj != null) {
			idpConfig.setRefreshMetadata(importMetadataConfigObj.optBoolean(MoPluginConstants.REFRESH_METADATA, false));
			idpConfig.setRefreshInterval(
					importMetadataConfigObj.optString(MoPluginConstants.METADATA_REFRESH_INTERVAL, "hourly"));
			idpConfig.setCustomRefreshInterval(
					importMetadataConfigObj.optInt(MoPluginConstants.CUSTOM_REFRESH_INTERVAL, 60));
			idpConfig.setCustomRefreshIntervalUnit(
					importMetadataConfigObj.optString(MoPluginConstants.CUSTOM_REFRESH_INTERVAL_UNIT, "minutes"));
			idpConfig.setInputUrl(importMetadataConfigObj.optString(MoPluginConstants.INPUT_METADATA_URL, ""));
			idpConfig.setIdpMetadataUrl(importMetadataConfigObj.optString(MoPluginConstants.IDP_METADATA_URL, ""));

		} else {
			idpConfig.setRefreshMetadata(Boolean.FALSE);
			idpConfig.setRefreshInterval("hourly");
			idpConfig.setCustomRefreshInterval(60);
			idpConfig.setCustomRefreshIntervalUnit("minutes");
			idpConfig.setInputUrl("");
			idpConfig.setIdpMetadataUrl("");
		}
	}

	private static JSONObject constructAttributeConfig(String usernameAttribute, String emailAttribute,
			String fullNameAttribute, Boolean useSeparateNameAttributes, String firstNameAttribute,
			String lastNameAttribute, Boolean keepExistingUserAttributes, String regexPattern,
			Boolean regexPatternEnabled, String loginUserAttribute) {
		JSONObject attributeMappingConfigObj = new JSONObject();
		try {
			attributeMappingConfigObj.put(MoPluginConstants.USERNAME_ATTRIBUTE,
					StringUtils.defaultIfBlank(MoSAMLUtils.sanitizeText(usernameAttribute), "NameID"));
			attributeMappingConfigObj.put(MoPluginConstants.EMAIL_ATTRIBUTE,
					StringUtils.defaultIfBlank(MoSAMLUtils.sanitizeText(emailAttribute), "NameID"));
			attributeMappingConfigObj.put(MoPluginConstants.ENABLE_REGEX_PATTERN,
					BooleanUtils.toBooleanDefaultIfNull(regexPatternEnabled, false));
			attributeMappingConfigObj.put(MoPluginConstants.REGEX_PATTERN,
					StringUtils.defaultIfBlank(MoSAMLUtils.sanitizeText(regexPattern), ""));
			attributeMappingConfigObj.put(MoPluginConstants.KEEP_EXISTING_ATTRIBUTE,
					BooleanUtils.toBooleanDefaultIfNull(keepExistingUserAttributes, Boolean.FALSE));
			attributeMappingConfigObj.put(MoPluginConstants.USE_SEPARATE_NAME_ATTRIBUTE,
					BooleanUtils.toBooleanDefaultIfNull(useSeparateNameAttributes, false));
			attributeMappingConfigObj.put(MoPluginConstants.FIRST_NAME_ATTRIBUTE,
					StringUtils.defaultIfBlank(MoSAMLUtils.sanitizeText(firstNameAttribute), ""));
			attributeMappingConfigObj.put(MoPluginConstants.LAST_NAME_ATTRIBUTE,
					StringUtils.defaultIfBlank(MoSAMLUtils.sanitizeText(lastNameAttribute), ""));
			attributeMappingConfigObj.put(MoPluginConstants.FULL_NAME_ATTRIBUTE,
					StringUtils.defaultIfBlank(MoSAMLUtils.sanitizeText(fullNameAttribute), ""));
			attributeMappingConfigObj.put(MoPluginConstants.LOGIN_USER_ATTRIBUTE,
					StringUtils.defaultIfBlank(MoSAMLUtils.sanitizeText(loginUserAttribute), "username"));
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			LOGGER.error("An error occurred while initializing attribute mapping configurations");
		}
		return attributeMappingConfigObj;
	}

	private static void insertAttributeMappingConfig(MoIDPConfig idpConfig, JSONObject attributeMappingConfigObj) {
		LOGGER.debug("insertAttributeMappingConfig called");
		idpConfig.setUsernameAttribute(attributeMappingConfigObj.optString(MoPluginConstants.USERNAME_ATTRIBUTE, "NameID"));
		idpConfig.setEmailAttribute(attributeMappingConfigObj.optString(MoPluginConstants.EMAIL_ATTRIBUTE, "NameID"));
		idpConfig.setFullNameAttribute(attributeMappingConfigObj.optString(MoPluginConstants.FULL_NAME_ATTRIBUTE, ""));
		idpConfig.setUseSeparateNameAttributes(
				attributeMappingConfigObj.optBoolean(MoPluginConstants.USE_SEPARATE_NAME_ATTRIBUTE, false));
		idpConfig
				.setFirstNameAttribute(attributeMappingConfigObj.optString(MoPluginConstants.FIRST_NAME_ATTRIBUTE, ""));
		idpConfig.setLastNameAttribute(attributeMappingConfigObj.optString(MoPluginConstants.LAST_NAME_ATTRIBUTE, ""));
		idpConfig.setKeepExistingUserAttributes(
				attributeMappingConfigObj.optBoolean(MoPluginConstants.KEEP_EXISTING_ATTRIBUTE, Boolean.TRUE));
		idpConfig.setRegexPattern(attributeMappingConfigObj.optString(MoPluginConstants.REGEX_PATTERN, ""));
		idpConfig.setRegexPatternEnabled(
				attributeMappingConfigObj.optBoolean(MoPluginConstants.ENABLE_REGEX_PATTERN, false));
		idpConfig.setLoginUserAttribute(
				attributeMappingConfigObj.optString(MoPluginConstants.LOGIN_USER_ATTRIBUTE, "username"));
	}

	private static void saveDefaultAttributeAndGroupMapping(String idpID) {
		saveAttributeMapping(idpID, "NameID", "NameID", "", Boolean.FALSE, "", "",
				Boolean.FALSE, "", Boolean.FALSE,"username");
		saveRoleMapping(idpID, "", Boolean.FALSE, Boolean.FALSE, new HashMap<String, String>(), settings.getDefaultGroup(),
				new ArrayList<String>(), Boolean.FALSE, MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS,
				Boolean.FALSE, new ArrayList<>(), Boolean.TRUE, Boolean.TRUE, Boolean.FALSE,StringUtils.EMPTY,StringUtils.EMPTY,StringUtils.EMPTY);
	}
	private static void saveDefaultAdvancedSSOSetting(String idpID){
		saveAdvancedOption(idpID,Boolean.TRUE,Boolean.FALSE,StringUtils.EMPTY,"01",Boolean.FALSE,StringUtils.EMPTY,
				60,"Minutes",StringUtils.EMPTY,MoPluginConstants.FORCE_REDIRECT,Boolean.FALSE);
	}

	private static JSONObject constructGroupMappingConfigObj(String roleAttribute, Boolean createUsersIfRoleMapped,
		  Boolean keepExistingUserRoles, HashMap<String, String> roleMapping, String defaultGroup,
		  List<String> defaultGroupList, Boolean restrictUserCreation,
		  String enableDefaultGroupsFor, Boolean onTheFlyGroupCreation, List<String> onTheFlyDoNotRemoveGroupsList,
		  Boolean onTheFlyAssignNewGroupsOnly, Boolean createNewGroups,Boolean groupRegexPatternEnabled,String regexPatternForGroup,String regexGroups,String testRegex) {
		LOGGER.debug("constructGroupMappingConfigObj called " + "roleAttribute=" + roleAttribute
				+ ", createUsersIfRoleMapped=" + createUsersIfRoleMapped + ", keepExistingUserRoles="
				+ keepExistingUserRoles + ", roleMapping=" + roleMapping + ", defaultGroup=" + defaultGroup
				+ ", defaultGroups=" + defaultGroupList + ",restrictUserCreation = " + restrictUserCreation
				+ ", enableDefaultGroupsFor = "+enableDefaultGroupsFor
				+ ", onTheFlyGroupCreation = "+onTheFlyGroupCreation +", onTheFlyDoNotRemoveGroupsList = "+onTheFlyDoNotRemoveGroupsList
				+ ", onTheFlyAssignNewGroupsOnly = "+onTheFlyAssignNewGroupsOnly + ", createNewGroups = "+createNewGroups + " regexPatternForGroup " + regexPatternForGroup
				+ " regexGroups " + regexGroups + " testRegex " + testRegex
				+ "]");
		JSONObject groupMappingConfigObj = new JSONObject();
		try {
			groupMappingConfigObj.put(MoPluginConstants.ROLE_ATTRIBUTE, StringUtils.defaultIfBlank(MoSAMLUtils.sanitizeText(roleAttribute), ""));
			groupMappingConfigObj.put(MoPluginConstants.CREATE_USER_IF_ROLE_MAPPED,
					BooleanUtils.toBooleanDefaultIfNull(createUsersIfRoleMapped, false));
			groupMappingConfigObj.put(MoPluginConstants.KEEP_EXISTING_USER_ROLES,
					BooleanUtils.toBooleanDefaultIfNull(keepExistingUserRoles, Boolean.FALSE));
			groupMappingConfigObj.put(MoPluginConstants.DEFAULT_GROUP,
					StringUtils.defaultIfBlank(defaultGroup, settings.getDefaultGroup()));

			JSONArray defaultGroupsObj = new JSONArray();
			groupMappingConfigObj.put(MoPluginConstants.DEFAULT_GROUPS, defaultGroupsObj);
			if (defaultGroupList.size() == 0) {
				defaultGroupList.add(settings.getDefaultGroup());
			}

			for (String group : defaultGroupList) {
				groupMappingConfigObj.append(MoPluginConstants.DEFAULT_GROUPS, group);
			}


			JSONObject groupMappingObj = new JSONObject();
			if (roleMapping != null) {
				for (String bambooGroup : roleMapping.keySet()) {
					groupMappingObj.putOpt(bambooGroup, roleMapping.get(bambooGroup));
				}
			}

			groupMappingConfigObj.put(MoPluginConstants.ROLE_MAPPING, groupMappingObj);
			//group regex
			groupMappingConfigObj.put(MoPluginConstants.GROUP_REGEX_PATTERN_ENABLED, groupRegexPatternEnabled );
			groupMappingConfigObj.put(MoPluginConstants.REGEX_PATTERN_FOR_GROUP, regexPatternForGroup);
			groupMappingConfigObj.put(MoPluginConstants.REGEX_GROUPS, regexGroups);
			groupMappingConfigObj.put(MoPluginConstants.TEST_REGEX, testRegex);


			groupMappingConfigObj.put(MoPluginConstants.RESTRICT_USER_CREATION,
					BooleanUtils.toBoolean(restrictUserCreation));
			groupMappingConfigObj.put(MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR, enableDefaultGroupsFor);

			groupMappingConfigObj.put(MoPluginConstants.ON_THE_FLY_GROUP_MAPPING,
					BooleanUtils.toBooleanDefaultIfNull(onTheFlyGroupCreation, false));

			groupMappingConfigObj.put(MoPluginConstants.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY,
					BooleanUtils.toBooleanDefaultIfNull(onTheFlyAssignNewGroupsOnly, Boolean.FALSE));

			groupMappingConfigObj.put(MoPluginConstants.ON_THE_FLY_CREATE_NEW_GROUPS,
					BooleanUtils.toBooleanDefaultIfNull(createNewGroups, Boolean.FALSE));

			if (onTheFlyDoNotRemoveGroupsList != null) {
				for (String group : onTheFlyDoNotRemoveGroupsList) {
					groupMappingConfigObj.append(MoPluginConstants.ON_THE_FLY_DO_NOT_REMOVE_GROUPS, group);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			LOGGER.error("An error occurred while initializing group mapping configurations");
		}
		return groupMappingConfigObj;
	}

	private static void insertGroupMappingConfig(MoIDPConfig idpConfig, JSONObject groupMappingConfigObj) {
		LOGGER.debug("insertGroupMappingConfig called");
		idpConfig
				.setRoleAttribute(groupMappingConfigObj.optString(MoPluginConstants.ROLE_ATTRIBUTE, StringUtils.EMPTY));
		idpConfig.setCreateUsersIfRoleMapped(
				groupMappingConfigObj.optBoolean(MoPluginConstants.CREATE_USER_IF_ROLE_MAPPED, false));
		idpConfig.setKeepExistingUserRoles(
				groupMappingConfigObj.optBoolean(MoPluginConstants.KEEP_EXISTING_USER_ROLES, Boolean.TRUE));
		idpConfig.setDefaultGroup(
				groupMappingConfigObj.optString(MoPluginConstants.DEFAULT_GROUP, settings.getDefaultGroup()));
		JSONArray defaultGroups = groupMappingConfigObj.optJSONArray(MoPluginConstants.DEFAULT_GROUPS);
		List<String> defaultGroupsList = new ArrayList<String>();
		if (defaultGroups != null) {
			for (int i = 0; i < defaultGroups.length(); i++) {
				defaultGroupsList.add(defaultGroups.getString(i));
			}
		}

		idpConfig.setDefaultGroupsList(
				defaultGroupsList.isEmpty() ? (List<String>) settings.getDefaultGroups() : defaultGroupsList);

		JSONObject groupMapping = groupMappingConfigObj.optJSONObject(MoPluginConstants.ROLE_MAPPING);
		HashMap<String, String> roleMapping = new HashMap<String, String>();
		if (groupMapping != null) {
			Iterator<String> bambooGroups = groupMapping.keys();
			while (bambooGroups.hasNext()) {
				String bambooGroup = bambooGroups.next();
				if (StringUtils.isNotBlank(groupMapping.getString(bambooGroup))) {
					roleMapping.put(bambooGroup, groupMapping.getString(bambooGroup));
				}
			}
		}
		idpConfig.setRoleMapping(roleMapping);
		idpConfig.setRestrictUserCreation(
				groupMappingConfigObj.optBoolean(MoPluginConstants.RESTRICT_USER_CREATION, false));
		idpConfig.setEnableDefaultGroupsFor(
				groupMappingConfigObj.optString(MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR, MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS));
		idpConfig.setOnTheFlyGroupCreation(
				groupMappingConfigObj.optBoolean(MoPluginConstants.ON_THE_FLY_GROUP_MAPPING, false));
		idpConfig.setOnTheFlyAssignNewGroupsOnly(
				groupMappingConfigObj.optBoolean(MoPluginConstants.ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY, Boolean.TRUE));
		//group regex
		idpConfig.setGroupRegexPatternEnabled(
				groupMappingConfigObj.optBoolean(MoPluginConstants.GROUP_REGEX_PATTERN_ENABLED, false));
		idpConfig.setRegexPatternForGroup(
				groupMappingConfigObj.optString(MoPluginConstants.REGEX_PATTERN_FOR_GROUP, StringUtils.EMPTY));
		idpConfig.setRegexGroups(
				groupMappingConfigObj.optString(MoPluginConstants.REGEX_GROUPS, StringUtils.EMPTY));
		idpConfig.setTestRegex(
				groupMappingConfigObj.optString(MoPluginConstants.TEST_REGEX, StringUtils.EMPTY));


		idpConfig.setCreateNewGroups(
				groupMappingConfigObj.optBoolean(MoPluginConstants.ON_THE_FLY_CREATE_NEW_GROUPS, Boolean.TRUE));

		JSONArray doNotRemoveGroups = groupMappingConfigObj
				.optJSONArray(MoPluginConstants.ON_THE_FLY_DO_NOT_REMOVE_GROUPS);
		List<String> doNotRemoveGroupsList = new ArrayList<String>();
		if (doNotRemoveGroups != null) {
			for (int i = 0; i < doNotRemoveGroups.length(); i++) {
				doNotRemoveGroupsList.add(doNotRemoveGroups.getString(i));
			}
		}
		idpConfig.setOnTheFlyDoNotRemoveGroups(
				doNotRemoveGroupsList.isEmpty() ? (List<String>) settings.getOnTheFlyDoNotRemoveGroups() : doNotRemoveGroupsList);

	}

	public static void stopAllSchedulers() throws Exception {
		ArrayList<String> idpList = settings.getIdPList();
		for (String idp : idpList) {
			metadataJobRunnerImpl.deleteSchedule(idp);
		}
		metadataJobRunnerImpl.deleteSchedule(MoPluginConstants.DEFAULT_IDP_ID);
	}

	public static ArrayList<String> dynamicSearchAttributes(List<String> attributesList, String searchPattern){
		String startsWith = searchPattern;
		if (StringUtils.equalsIgnoreCase(searchPattern, "*")) {
			startsWith = StringUtils.EMPTY;
		}
		ArrayList<String>searchedAttributes = new ArrayList<>();
		for (String attribute:attributesList
		) {
			if (StringUtils.startsWithIgnoreCase(attribute,startsWith)){
				searchedAttributes.add(attribute);
			}
		}
		return searchedAttributes;
	}

	public boolean checkForAdminSession(){
		final UserProfile user = userManager.getRemoteUser();
		if (user!=null) {
			return userManager.isAdmin(user.getUserKey());
		}
		return false;
	}
	public static boolean isParsable(String totalNumberOfRolesStr) {
		boolean isParsable = true;
		try{
			Integer.parseInt(totalNumberOfRolesStr);
		}catch (NumberFormatException e){
			LOGGER.error("Error occurred while parsing string ",e);
			isParsable = false;
		}
		return isParsable;
	}
}
