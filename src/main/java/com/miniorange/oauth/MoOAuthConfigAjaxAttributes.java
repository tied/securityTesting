package com.miniorange.oauth;

import javax.persistence.Entity;

@Entity
public class MoOAuthConfigAjaxAttributes{
    private String loginButtonText;
    private Boolean isConfigured;
    private Boolean enableBackdoor;
	private Boolean disableDefaultLogin;
	private Boolean enableAutoRedirectDelay;
	private String autoRedirectDelayInterval;
	private String ssoButtonLocation;
	private Boolean restrictBackdoor;
	private Boolean enableLoginTemplate;
	private String loginTemplate;
	private String backdoorKey;
	private String backdoorValue;
	public MoOAuthConfigAjaxAttributes(String loginButtonText, Boolean isConfigured, Boolean enableBackdoor,
			Boolean disableDefaultLogin, Boolean enableAutoRedirectDelay,String autoRedirectDelayInterval,String ssoButtonLocation, Boolean restrictBackdoor,
			Boolean enableLoginTemplate,String loginTemplate,String backdoorKey, String backdoorValue) {
        this.loginButtonText = loginButtonText;
        this.isConfigured = isConfigured;
        this.enableBackdoor = enableBackdoor;
        this.disableDefaultLogin = disableDefaultLogin;
        this.enableAutoRedirectDelay = enableAutoRedirectDelay;
        this.autoRedirectDelayInterval=autoRedirectDelayInterval;
		this.ssoButtonLocation = ssoButtonLocation;
		this.restrictBackdoor=restrictBackdoor;
		this.enableLoginTemplate = enableLoginTemplate;
		this.loginTemplate = loginTemplate;
		this.backdoorKey = backdoorKey;
		this.backdoorValue = backdoorValue;
    }
    
	public Boolean getEnableBackdoor() {
		return enableBackdoor;
	}

	public void setEnableBackdoor(Boolean enableBackdoor) {
		this.enableBackdoor = enableBackdoor;
	}

	public Boolean getDisableDefaultLogin() {
		return disableDefaultLogin;
	}

	public void setDisableDefaultLogin(Boolean disableDefaultLogin) {
		this.disableDefaultLogin = disableDefaultLogin;
	}

	public String getLoginButtonText() {
		return loginButtonText;
	}

	public void setLoginButtonText(String loginButtonText) {
		this.loginButtonText = loginButtonText;
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

	public Boolean getIsConfigured() {
		return isConfigured;
	}

	public void setIsConfigured(Boolean isConfigured) {
		this.isConfigured = isConfigured;
	}

	public Boolean getEnableAutoRedirectDelay() {
		return enableAutoRedirectDelay;
	}

	public void setEnableAutoRedirectDelay(Boolean enableAutoRedirectDelay) {
		this.enableAutoRedirectDelay = enableAutoRedirectDelay;
	}

	public String getAutoRedirectDelayInterval() {
		return autoRedirectDelayInterval;
	}

	public void setAutoRedirectDelayInterval(String autoRedirectDelayInterval) {
		this.autoRedirectDelayInterval = autoRedirectDelayInterval;
	}

	public String getSsoButtonLocation() { return ssoButtonLocation; }

	public void setSsoButtonLocation(String ssoButtonLocation) {

		this.ssoButtonLocation = ssoButtonLocation;
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
}
