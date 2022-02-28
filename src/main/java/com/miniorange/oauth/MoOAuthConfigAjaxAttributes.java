package com.miniorange.oauth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.miniorange.oauth.confluence.MoOAuthPluginConfigurationsHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;


public class MoOAuthConfigAjaxAttributes {
	private static final Logger LOGGER = LoggerFactory.getLogger(MoOAuthConfigAjaxAttributes.class);

	private String loginButtonText;
	private Boolean configured;
	private Boolean disableDefaultLogin;
	private Boolean backdoorEnabled;
	private Boolean enableAutoRedirectDelay;
	private String autoRedirectDelayInterval;
	private String ssoButtonLocation;
	private Boolean restrictBackdoor;
	private String backdoorKey;
	private String backdoorValue;
	private Boolean enableOAuthSSO;
	private Boolean isGuestCookieExpired;
	private Boolean disableAnonymousAccess;
	private HashMap<String, String> userSessionDetails;
	private Boolean checkIDPSession;
	private String autoLoginURL;
	private Boolean enableLoginTemplate;
	private String loginTemplate;
	
	public MoOAuthConfigAjaxAttributes(Boolean configured, Boolean enableOAuthSSO,String loginButtonText, Boolean disableDefaultLogin,
			Boolean disableAnonymousAccess, Boolean isGuestCookieExpired, Boolean backdoorEnabled, Boolean enableAutoRedirectDelay,
			String autoRedirectDelayInterval, String ssoButtonLocation, Boolean restrictBackdoor,
			String backdoorKey, String backdoorValue, HashMap<String, String> userSessionDetails, Boolean enableLoginTemplate, String loginTemplate
				, Boolean checkIDPSession, String autoLoginURL){
		this.configured = configured;
		this.enableOAuthSSO = enableOAuthSSO;
		this.loginButtonText = loginButtonText;
		this.disableDefaultLogin = disableDefaultLogin;
		this.disableAnonymousAccess=disableAnonymousAccess;
		this.isGuestCookieExpired=isGuestCookieExpired;
	    this.backdoorEnabled = backdoorEnabled;
	    this.enableAutoRedirectDelay = enableAutoRedirectDelay;
	    this.autoRedirectDelayInterval=autoRedirectDelayInterval;
	    this.ssoButtonLocation = ssoButtonLocation;
	    this.restrictBackdoor = restrictBackdoor;
	    this.backdoorKey = backdoorKey;
		this.backdoorValue = backdoorValue;
		this.userSessionDetails = userSessionDetails;
		this.checkIDPSession = checkIDPSession;
		this.autoLoginURL = autoLoginURL;
		this.enableLoginTemplate = enableLoginTemplate;
		this.loginTemplate = loginTemplate;
	}
	
	public String getJSON() {
        JsonObject json = new JsonObject();
        try {
        	json.addProperty("configured", configured);
        	json.addProperty("enableOAuthSSO", enableOAuthSSO);
            json.addProperty("loginButtonText", loginButtonText);
			json.addProperty("disableDefaultLogin", disableDefaultLogin);
			json.addProperty("disableAnonymousAccess",disableAnonymousAccess);
			json.addProperty("isGuestCookieExpired",isGuestCookieExpired);
            json.addProperty("backdoorEnabled", backdoorEnabled);
            json.addProperty("enableAutoRedirectDelay", enableAutoRedirectDelay);
            json.addProperty("autoRedirectDelayInterval",autoRedirectDelayInterval);
            json.addProperty("ssoButtonLocation", ssoButtonLocation);
            json.addProperty("restrictBackdoor", restrictBackdoor);
            json.addProperty("backdoorKey", backdoorKey);
			json.addProperty("backdoorValue", backdoorValue);
			json.addProperty("userSessionDetails", userSessionDetails.keySet().size());
			json.addProperty("checkIDPSession",checkIDPSession);
			json.addProperty("autoLoginURL",autoLoginURL);
			json.addProperty("enableLoginTemplate", enableLoginTemplate);
			json.addProperty("loginTemplate", loginTemplate);

            return json.toString();
        } catch (JsonParseException e) {
            LOGGER.error(e.getMessage());
        }
        return StringUtils.EMPTY;
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

	public String getSsoButtonLocation() {
		return ssoButtonLocation;
	}

	public void setSsoButtonLocation(String ssoButtonLocation) {
		this.ssoButtonLocation = ssoButtonLocation;
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

	public Boolean getEnableOAuthSSO() {
		return enableOAuthSSO;
	}

	public void setEnableOAuthSSO(Boolean enableOAuthSSO) {
		this.enableOAuthSSO = enableOAuthSSO;
	}

	public Boolean getCheckIDPSession() {return checkIDPSession;}

	public void setCheckIDPSession(Boolean checkIDPSession) {this.checkIDPSession = checkIDPSession;}

	public String getAutoCallbackURL() {return autoLoginURL;}

	public void setAutoCallbackURL(String autoLoginURL) {this.autoLoginURL = autoLoginURL;}

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
}
