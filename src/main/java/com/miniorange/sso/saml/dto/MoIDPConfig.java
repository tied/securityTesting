package com.miniorange.sso.saml.dto;

import org.apache.commons.lang3.BooleanUtils;

import java.util.HashMap;
import java.util.List;

public class MoIDPConfig {

	//Unique ID of IDP
	private String id;

	//Add Identity Provider
	private String idpName;
	private String idpEntityId;
	private String ssoUrl;
	private String ssoBindingType;
	private String sloUrl;
	private String sloBindingType;
	private String x509Certificate;
	private List<String> certificates;
	private Boolean signedRequest;
	private String nameIdFormat;
	private Boolean enableSSOForIDP;

	//Upload Metadata
	private String metadataOption;
	private String inputUrl;
	private String idpMetadataUrl;
	private Boolean refreshMetadata;
	private String refreshInterval;
	private Integer customRefreshInterval;
	private String customRefreshIntervalUnit;

	//Enable Disable IDP
	private Boolean enableSsoForIdp;

	// Attribute Mapping
	private String usernameAttribute;
	private String emailAttribute;
	private String fullNameAttribute;
	private Boolean useSeparateNameAttributes;
	private String firstNameAttribute;
	private String lastNameAttribute;
	private Boolean keepExistingUserAttributes;
	private String regexPattern;
	private Boolean regexPatternEnabled;
	private String loginUserAttribute;

	// Group Mapping
	private Boolean createUsersIfRoleMapped;
	private Boolean keepExistingUserRoles;
	private String defaultGroup;
	private List<String> defaultGroupsList;
	HashMap<String, String> roleMapping;
	private String roleAttribute;
	private Boolean restrictUserCreation;
	private String enableDefaultGroupsFor;
	private Boolean onTheFlyGroupCreation;
	private List<String> onTheFlyDoNotRemoveGroups;
	private Boolean onTheFlyAssignNewGroupsOnly;
	private Boolean createNewGroups;

	//Group regex
	private Boolean groupRegexPatternEnabled;
	private String regexPatternForGroup;
		private String regexGroups;
	private String testRegex;

	// Advanced SSO Options
	private Boolean allowUserCreation;
	private Boolean forceAuthentication;
	private String timeDelay;
	private String relayState;
	private String relayStateRedirectionType;
	private Boolean enablePassiveSso;

	public Boolean getEnableSSOForIDP() {
		return enableSSOForIDP;
	}

	public void setEnableSSOForIDP(Boolean enableSSOForIDP) {
		this.enableSSOForIDP = enableSSOForIDP;
	}

	public String getTimeDelay() {
		return timeDelay;
	}

	public void setTimeDelay(String timeDelay) {
		this.timeDelay = timeDelay;
	}

	public Boolean getAllowUserCreation() {
		return allowUserCreation;
	}

	public void setAllowUserCreation(Boolean allowUserCreation) {
		this.allowUserCreation = allowUserCreation;
	}

	public Boolean getEnablePassiveSso(){
		return enablePassiveSso;
	}

	public void setEnablePassiveSso(Boolean enablePassiveSso) {
		this.enablePassiveSso = enablePassiveSso;
	}

	public Boolean getForceAuthentication() {
		return forceAuthentication;
	}

	public void setForceAuthentication(Boolean forceAuthentication) {
		this.forceAuthentication = forceAuthentication;
	}

	public String getRelayState() {
		return relayState;
	}

	public void setRelayState(String relayState) {
		this.relayState = relayState;
	}

	public String getRelayStateRedirectionType() {
		return relayStateRedirectionType;
	}

	public void setRelayStateRedirectionType(String relayStateRedirectionType) {
		this.relayStateRedirectionType = relayStateRedirectionType;
	}

	public Boolean getEnableSsoForIdp() {
		return enableSsoForIdp;
	}

	public void setEnableSsoForIdp(Boolean enableSsoForIdp) {
		this.enableSsoForIdp = enableSsoForIdp;
	}

	public String getIdpName() {
		return idpName;
	}

	public void setIdpName(String idpName) {
		this.idpName = idpName;
	}

	public String getIdpEntityId() {
		return idpEntityId;
	}

	public void setIdpEntityId(String idpEntityId) {
		this.idpEntityId = idpEntityId;
	}

	public String getSsoUrl() {
		return ssoUrl;
	}

	public void setSsoUrl(String ssoUrl) {
		this.ssoUrl = ssoUrl;
	}

	public String getSsoBindingType() {
		return ssoBindingType;
	}

	public void setSsoBindingType(String ssoBindingType) {
		this.ssoBindingType = ssoBindingType;
	}

	public String getSloUrl() {
		return sloUrl;
	}

	public void setSloUrl(String sloUrl) {
		this.sloUrl = sloUrl;
	}

	public String getSloBindingType() {
		return sloBindingType;
	}

	public void setSloBindingType(String sloBindingType) {
		this.sloBindingType = sloBindingType;
	}

	public String getX509Certificate() {
		return x509Certificate;
	}

	public void setX509Certificate(String x509Certificate) {
		this.x509Certificate = x509Certificate;
	}

	public List<String> getCertificates() {
		return certificates;
	}

	public void setCertificates(List<String> certificates) {
		this.certificates = certificates;
	}

	public Boolean getSignedRequest() {
		return BooleanUtils.toBooleanDefaultIfNull(signedRequest,Boolean.TRUE);
	}

	public void setSignedRequest(Boolean signedRequest) {
		this.signedRequest = signedRequest;
	}

	/**
	 * @param customRefreshInterval the customRefreshInterval to set
	 */
	public void setCustomRefreshInterval(Integer customRefreshInterval) {
		this.customRefreshInterval = customRefreshInterval;
	}

	/**
	 * @return the customRefreshInterval
	 */
	public Integer getCustomRefreshInterval() {
		return customRefreshInterval;
	}

	/**
	 * @param customRefreshIntervalUnit the customRefreshIntervalUnit to set
	 */
	public void setCustomRefreshIntervalUnit(String customRefreshIntervalUnit) {
		this.customRefreshIntervalUnit = customRefreshIntervalUnit;
	}

	/**
	 * @return the customRefreshIntervalUnit
	 */
	public String getCustomRefreshIntervalUnit() {
		return customRefreshIntervalUnit;
	}

	/**
	 * @param idpMetadataUrl the idpMetadataUrl to set
	 */
	public void setIdpMetadataUrl(String idpMetadataUrl) {
		this.idpMetadataUrl = idpMetadataUrl;
	}

	/**
	 * @return the idpMetadataUrl
	 */
	public String getIdpMetadataUrl() {
		return idpMetadataUrl;
	}

	/**
	 * @param inputUrl the inputUrl to set
	 */
	public void setInputUrl(String inputUrl) {
		this.inputUrl = inputUrl;
	}

	/**
	 * @return the inputUrl
	 */
	public String getInputUrl() {
		return inputUrl;
	}

	/**
	 * @param refreshInterval the refreshInterval to set
	 */
	public void setRefreshInterval(String refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	/**
	 * @return the refreshInterval
	 */
	public String getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @param refreshMetadata the refreshMetadata to set
	 */
	public void setRefreshMetadata(Boolean refreshMetadata) {
		this.refreshMetadata = refreshMetadata;
	}

	/**
	 * @return the refreshMetadata
	 */
	public Boolean getRefreshMetadata() {
		return BooleanUtils.toBooleanDefaultIfNull(refreshMetadata, Boolean.FALSE);
	}

	/**
	 * @param metadataOption the metadataOption to set
	 */
	public void setMetadataOption(String metadataOption) {
		this.metadataOption = metadataOption;
	}

	/**
	 * @return the metadataOption
	 */
	public String getMetadataOption() {
		return metadataOption;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	public String getNameIdFormat() {
		return nameIdFormat;
	}

	public void setNameIdFormat(String nameIdFormat) {
		this.nameIdFormat = nameIdFormat;
	}

	public String getUsernameAttribute() {
		return usernameAttribute;
	}

	public void setUsernameAttribute(String usernameAttribute) {
		this.usernameAttribute = usernameAttribute;
	}

	public String getEmailAttribute() {
		return emailAttribute;
	}

	public void setEmailAttribute(String emailAttribute) {
		this.emailAttribute = emailAttribute;
	}

	public String getFullNameAttribute() {
		return fullNameAttribute;
	}

	public void setFullNameAttribute(String fullNameAttribute) {
		this.fullNameAttribute = fullNameAttribute;
	}

	public Boolean getUseSeparateNameAttributes() {
		return useSeparateNameAttributes;
	}

	public void setUseSeparateNameAttributes(Boolean useSeparateNameAttributes) {
		this.useSeparateNameAttributes = useSeparateNameAttributes;
	}

	public String getFirstNameAttribute() {
		return firstNameAttribute;
	}

	public void setFirstNameAttribute(String firstNameAttribute) {
		this.firstNameAttribute = firstNameAttribute;
	}

	public String getLastNameAttribute() {
		return lastNameAttribute;
	}

	public void setLastNameAttribute(String lastNameAttribute) {
		this.lastNameAttribute = lastNameAttribute;
	}

	public Boolean getKeepExistingUserAttributes() {
		return keepExistingUserAttributes;
	}

	public void setKeepExistingUserAttributes(Boolean keepExistingUserAttributes) {
		this.keepExistingUserAttributes = keepExistingUserAttributes;
	}

	public String getRegexPattern() {
		return regexPattern;
	}

	public void setRegexPattern(String regexPattern) {
		this.regexPattern = regexPattern;
	}

	public Boolean getRegexPatternEnabled() {
		return regexPatternEnabled;
	}

	public void setRegexPatternEnabled(Boolean regexPatternEnabled) {
		this.regexPatternEnabled = regexPatternEnabled;
	}

	public String getLoginUserAttribute() {
		return loginUserAttribute;
	}

	public void setLoginUserAttribute(String loginUserAttribute) {
		this.loginUserAttribute = loginUserAttribute;
	}

	public String getEnableDefaultGroupsFor() {
		return enableDefaultGroupsFor;
	}

	public void setEnableDefaultGroupsFor(String enableDefaultGroupsFor) {
		this.enableDefaultGroupsFor = enableDefaultGroupsFor;
	}

	public Boolean getCreateUsersIfRoleMapped() {
		return createUsersIfRoleMapped;
	}

	public void setCreateUsersIfRoleMapped(Boolean createUsersIfRoleMapped) {
		this.createUsersIfRoleMapped = createUsersIfRoleMapped;
	}

	public Boolean getKeepExistingUserRoles() {
		return keepExistingUserRoles;
	}

	public void setKeepExistingUserRoles(Boolean keepExistingUserRoles) {
		this.keepExistingUserRoles = keepExistingUserRoles;
	}

	public String getDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(String defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	public List<String> getDefaultGroupsList() {
		return defaultGroupsList;
	}

	public void setDefaultGroupsList(List<String> defaultGroupsList) {
		this.defaultGroupsList = defaultGroupsList;
	}

	public String getRoleAttribute() {
		return roleAttribute;
	}

	public void setRoleAttribute(String roleAttribute) {
		this.roleAttribute = roleAttribute;
	}

	public Boolean getRestrictUserCreation() {
		return restrictUserCreation;
	}

	public void setRestrictUserCreation(Boolean restrictUserCreation) {
		this.restrictUserCreation = restrictUserCreation;
	}

	public HashMap<String, String> getRoleMapping() {
		return roleMapping;
	}

	public void setRoleMapping(HashMap<String, String> roleMapping) {
		this.roleMapping = roleMapping;
	}

	public Boolean getOnTheFlyGroupCreation() {
		return onTheFlyGroupCreation;
	}

	public void setOnTheFlyGroupCreation(Boolean onTheFlyGroupCreation) {
		this.onTheFlyGroupCreation = onTheFlyGroupCreation;
	}

	public List<String> getOnTheFlyDoNotRemoveGroups() {
		return onTheFlyDoNotRemoveGroups;
	}

	public void setOnTheFlyDoNotRemoveGroups(List<String> onTheFlyDoNotRemoveGroups) {
		this.onTheFlyDoNotRemoveGroups = onTheFlyDoNotRemoveGroups;
	}

	public Boolean getOnTheFlyAssignNewGroupsOnly() {
		return BooleanUtils.toBooleanDefaultIfNull(onTheFlyAssignNewGroupsOnly, Boolean.TRUE);
	}

	public void setOnTheFlyAssignNewGroupsOnly(Boolean onTheFlyAssignNewGroupsOnly) {
		this.onTheFlyAssignNewGroupsOnly = onTheFlyAssignNewGroupsOnly;
	}

	public Boolean getGroupRegexPatternEnabled() {
		return groupRegexPatternEnabled;
	}

	public void setGroupRegexPatternEnabled(Boolean groupRegexPatternEnabled) {
		this.groupRegexPatternEnabled = groupRegexPatternEnabled;
	}

	public String getRegexPatternForGroup() {
		return regexPatternForGroup;
	}

	public void setRegexPatternForGroup(String regexPatternForGroup) {
		this.regexPatternForGroup = regexPatternForGroup;
	}

	public String getRegexGroups() {
		return regexGroups;
	}

	public void setRegexGroups(String regexGroups) {
		this.regexGroups = regexGroups;
	}

	public String getTestRegex() {
		return testRegex;
	}

	public void setTestRegex(String testRegex) {
		this.testRegex = testRegex;
	}

	public Boolean getCreateNewGroups() {
		return createNewGroups;
	}

	public void setCreateNewGroups(Boolean createNewGroups) {
		this.createNewGroups = createNewGroups;
	}

	@Override
	public String toString() {
		return "MoIDPConfig{" +
			"id='" + id + '\'' +
			", idpName='" + idpName + '\'' +
			", idpEntityId='" + idpEntityId + '\'' +
			", ssoUrl='" + ssoUrl + '\'' +
			", ssoBindingType='" + ssoBindingType + '\'' +
			", sloUrl='" + sloUrl + '\'' +
			", sloBindingType='" + sloBindingType + '\'' +
			", x509Certificate='" + x509Certificate + '\'' +
			", certificates=" + certificates +
			", signedRequest=" + signedRequest +
			", nameIdFormat='" + nameIdFormat + '\'' +
			", metadataOption='" + metadataOption + '\'' +
			", inputUrl='" + inputUrl + '\'' +
			", idpMetadataUrl='" + idpMetadataUrl + '\'' +
			", refreshMetadata=" + refreshMetadata +
			", refreshInterval='" + refreshInterval + '\'' +
			", customRefreshInterval=" + customRefreshInterval +
			", customRefreshIntervalUnit='" + customRefreshIntervalUnit + '\'' +
			", enablePassiveSso ='" + enablePassiveSso + '\'' +
			", usernameAttribute='" + usernameAttribute + '\'' +
			", emailAttribute='" + emailAttribute + '\'' +
			", fullNameAttribute='" + fullNameAttribute + '\'' +
			", useSeparateNameAttributes='" + useSeparateNameAttributes + '\'' +
			", firstNameAttribute='" + firstNameAttribute + '\'' +
			", lastNameAttribute='" + lastNameAttribute + '\'' +
			", keepExistingUserAttributes='" + keepExistingUserAttributes + '\'' +
			", regexPattern='" + regexPattern + '\'' +
			", regexPatternEnabled='" + regexPatternEnabled + '\'' +
			", loginUserAttribute='" + loginUserAttribute + '\'' +
			", createUsersIfRoleMapped='" + createUsersIfRoleMapped + '\'' +
			", keepExistingUserRoles='" + keepExistingUserRoles + '\'' +
			", defaultGroup='" + defaultGroup + '\'' +
			", roleAttribute='" + roleAttribute + '\'' +
			", restrictUserCreation='" + restrictUserCreation + '\'' +
			", enableDefaultGroupsFor='" + enableDefaultGroupsFor + '\'' +
			", onTheFlyGroupCreation='" + onTheFlyGroupCreation + '\'' +
			", onTheFlyDoNotRemoveGroups='" + onTheFlyDoNotRemoveGroups + '\'' +
			", onTheFlyAssignNewGroupsOnly='" + onTheFlyAssignNewGroupsOnly + '\'' +
			", createNewGroups='" + createNewGroups + '\''+
			", groupRegexPatternEnabled=" + groupRegexPatternEnabled +
			", regexPatternForGroup='" + regexPatternForGroup + '\'' +
			", regexGroups='" + regexGroups + '\'' +
			", testRegex='" + testRegex + '\'' +
			'}';
	}
}