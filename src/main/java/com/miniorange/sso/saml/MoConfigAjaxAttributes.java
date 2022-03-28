package com.miniorange.sso.saml;

import javax.persistence.Entity;
import java.util.HashMap;
import java.util.List;

@Entity
public class MoConfigAjaxAttributes {

	private Boolean configured;
	private Boolean disableDefaultLogin;
	private Boolean backdoorEnabled;
	private Boolean enableAutoRedirectDelay;
	private String numberOfLoginAttempts;
	private String loginButtonText;
	private Boolean sloConfigured;
	private Boolean customLogoutUrlConfigured;
	private String backdoorKey;
	private String backdoorValue;
	private Boolean customLogoutTemplateEnabled;
	private HashMap<String, String> idpMap;
	private List<String> idpList;
	private Boolean useDomainMapping;
	private String idp;
	private Boolean restrictBackdoor;
	private Boolean enableLoginTemplate;
	private String loginTemplate;
	private Boolean canUserSaveDetails;
	private Boolean enablePasswordChange;
	private Boolean headerAuthentication;
	private String headerAuthenticationAttribute;
	private String defaultBambooIDP;
	private String defaultRedirectUrl;
	private Boolean enableSAMLSSO;
	private Boolean showLoginButtons;
	private Boolean pluginApiAccessRestriction;
	private String resetAssertionIDListInterval;
	private int customResetInterval;
	private List<String> ssoEnabledForIdpList;

	protected MoConfigAjaxAttributes() {
	}

	public MoConfigAjaxAttributes(Boolean configured, Boolean disableDefaultLogin, Boolean backdoorEnabled, Boolean enableAutoRedirectDelay,
								  String numberOfLoginAttempts, String loginButtonText, Boolean sloConfigured, Boolean customLogoutUrlConfigured, String backdoorKey,
								  String backdoorValue, Boolean customLogoutTemplateEnabled, HashMap<String, String> idpMap, List<String> idpList,
								  Boolean useDomainMapping, String idp, Boolean restrictBackdoor, Boolean enableLoginTemplate, String loginTemplate,
								  Boolean canUserSaveDetails, Boolean enablePasswordChange, Boolean headerAuthentication,
								  String headerAuthenticationAttribute, String defaultBambooIDP,String defaultRedirectUrl, Boolean enableSAMLSSO, Boolean showLoginButtons,
								  Boolean pluginApiAccessRestriction, String resetAssertionIDListInterval,int customResetInterval, List<String> ssoEnabledForIdpList) {
		this.configured = configured;
		this.disableDefaultLogin = disableDefaultLogin;
		this.backdoorEnabled = backdoorEnabled;
		this.enableAutoRedirectDelay = enableAutoRedirectDelay;
		this.numberOfLoginAttempts=numberOfLoginAttempts;
		this.loginButtonText = loginButtonText;
		this.sloConfigured = sloConfigured;
		this.customLogoutTemplateEnabled = customLogoutTemplateEnabled;
		this.customLogoutUrlConfigured = customLogoutUrlConfigured;
		this.idpMap = idpMap;
		this.idpList = idpList;
		this.useDomainMapping = useDomainMapping;
		this.idp = idp;
		this.backdoorKey = backdoorKey;
		this.backdoorValue = backdoorValue;
		this.restrictBackdoor = restrictBackdoor;
		this.enableLoginTemplate = enableLoginTemplate;
		this.loginTemplate = loginTemplate;
		this.canUserSaveDetails = canUserSaveDetails;
		this.enablePasswordChange = enablePasswordChange;
		this.headerAuthentication = headerAuthentication;
		this.headerAuthenticationAttribute = headerAuthenticationAttribute;
		this.defaultBambooIDP = defaultBambooIDP;
		this.defaultRedirectUrl = defaultRedirectUrl;
		this.enableSAMLSSO = enableSAMLSSO;
		this.showLoginButtons = showLoginButtons;
		this.pluginApiAccessRestriction= pluginApiAccessRestriction;
		this.resetAssertionIDListInterval= resetAssertionIDListInterval;
		this.customResetInterval= customResetInterval;
		this.ssoEnabledForIdpList = ssoEnabledForIdpList;
	}

	public Boolean getConfigured() {
		return configured;
	}

	public void setConfigured(Boolean configured) {
		this.configured = configured;
	}

	public String getLoginButtonText() {
		return loginButtonText;
	}

	public void setLoginButtonText(String loginButtonText) {
		this.loginButtonText = loginButtonText;
	}

	public Boolean getDisableDefaultLogin() {
		return disableDefaultLogin;
	}

	public void setDisableDefaultLogin(Boolean disableDefaultLogin) {
		this.disableDefaultLogin = disableDefaultLogin;
	}

	public Boolean getBackdoorEnabled() {
		return backdoorEnabled;
	}

	public void setBackdoorEnabled(Boolean backdoorEnabled) {
		this.backdoorEnabled = backdoorEnabled;
	}

	public Boolean getEnableAutoRedirectDelay() {
		return enableAutoRedirectDelay;
	}

	public void setEnableAutoRedirectDelay(Boolean enableAutoRedirectDelay) {
		this.enableAutoRedirectDelay = enableAutoRedirectDelay;
	}

	public String getNumberOfLoginAttempts() {
		return numberOfLoginAttempts;
	}

	public void setNumberOfLoginAttempts(String numberOfLoginAttempts) {
		this.numberOfLoginAttempts = numberOfLoginAttempts;
	}

	public Boolean getSloConfigured() {
		return sloConfigured;
	}

	public void setSloConfigured(Boolean sloConfigured) {
		this.sloConfigured = sloConfigured;
	}

	public Boolean getCustomLogoutUrlConfigured() {
		return customLogoutUrlConfigured;
	}

	public void setCustomLogoutUrlConfigured(Boolean customLogoutUrlConfigured) {
		this.customLogoutUrlConfigured = customLogoutUrlConfigured;
	}

	public Boolean getCustomLogoutTemplateEnabled() {
		return customLogoutTemplateEnabled;
	}

	public void setCustomLogoutTemplateEnabled(Boolean customLogoutTemplateEnabled) {
		this.customLogoutTemplateEnabled = customLogoutTemplateEnabled;
	}

	public HashMap<String, String> getIdpMap() {
		return idpMap;
	}

	public void setIdpMap(HashMap<String, String> idpMap) {
		this.idpMap = idpMap;
	}

	public List<String> getIdpList() {
		return idpList;
	}

	public void setIdpList(List<String> idpList) {
		this.idpList = idpList;
	}

	public Boolean getUseDomainMapping() {
		return useDomainMapping;
	}

	public void setUseDomainMapping(Boolean useDomainMapping) {
		this.useDomainMapping = useDomainMapping;
	}

	public String getIdp() {
		return idp;
	}

	public void setIdp(String idp) {
		this.idp = idp;
	}

	public String getBackdoorKey() {
		return backdoorKey;
	}

	public void setBackdoorKey(String backdoorKey) {
		this.backdoorKey = backdoorKey;
	}

	public String getBackdoorValue() {
		return backdoorValue;
	}

	public void setBackdoorValue(String backdoorValue) {
		this.backdoorValue = backdoorValue;
	}

	public Boolean getRestrictBackdoor() {
		return restrictBackdoor;
	}

	public void setRestrictBackdoor(Boolean restrictBackdoor) {
		this.restrictBackdoor = restrictBackdoor;
	}

	public Boolean getEnableLoginTemplate() {
		return enableLoginTemplate;
	}

	public void setEnableLoginTemplate(Boolean enableLoginTemplate) {
		this.enableLoginTemplate = enableLoginTemplate;
	}

	public String getLoginTemplate() {
		return loginTemplate;
	}

	public void setLoginTemplate(String loginTemplate) {
		this.loginTemplate = loginTemplate;
	}

	public Boolean getCanUserSaveDetails() {
		return canUserSaveDetails;
	}

	public void setCanUserSaveDetails(Boolean canUserSaveDetails) {
		this.canUserSaveDetails = canUserSaveDetails;
	}

	public Boolean getEnablePasswordChange() {
		return enablePasswordChange;
	}

	public void setEnablePasswordChange(Boolean enablePasswordChange) {
		this.enablePasswordChange = enablePasswordChange;
	}

	public Boolean getHeaderAuthentication() {
		return headerAuthentication;
	}

	public void setHeaderAuthentication(Boolean headerAuthentication) {
		this.headerAuthentication = headerAuthentication;
	}

	public String getHeaderAuthenticationAttribute() {
		return headerAuthenticationAttribute;
	}

	public void setHeaderAuthenticationAttribute(String headerAuthenticationAttribute) {
		this.headerAuthenticationAttribute = headerAuthenticationAttribute;
	}

	public String getDefaultBambooIDP() {
		return defaultBambooIDP;
	}

	public void setDefaultBambooIDP(String defaultBambooIDP) {
		this.defaultBambooIDP = defaultBambooIDP;
	}

	public String getDefaultRedirectUrl() {
		return defaultRedirectUrl;
	}

	public void setDefaultRedirectUrl(String defaultRedirectUrl) {
		this.defaultRedirectUrl = defaultRedirectUrl;
	}

	public Boolean getEnableSAMLSSO() {
		return enableSAMLSSO;
	}

	public void setEnableSAMLSSO(Boolean enableSAMLSSO) {
		this.enableSAMLSSO = enableSAMLSSO;
	}

	public Boolean getShowLoginButtons() {
		return showLoginButtons;
	}

	public void setShowLoginButtons(Boolean showLoginButtons) {
		this.showLoginButtons = showLoginButtons;
	}

	public Boolean getPluginApiAccessRestriction() {
		return pluginApiAccessRestriction;
	}

	public void setPluginApiAccessRestriction(Boolean pluginApiAccessRestriction) {
		this.pluginApiAccessRestriction = pluginApiAccessRestriction;
	}

	public String getResetAssertionIDListInterval() {
		return resetAssertionIDListInterval;
	}

	public void setResetAssertionIDListInterval(String resetAssertionIDListInterval) {
		this.resetAssertionIDListInterval = resetAssertionIDListInterval;
	}

	public int getCustomResetInterval() {
		return customResetInterval;
	}

	public void setCustomResetInterval(int customResetInterval) {
		this.customResetInterval = customResetInterval;
	}

	public List<String> getSsoEnabledForIdpList() {
		return ssoEnabledForIdpList;
	}

	public void setSsoEnabledForIdpList(List<String> ssoEnabledForIdpList) {
		this.ssoEnabledForIdpList = ssoEnabledForIdpList;
	}


}
