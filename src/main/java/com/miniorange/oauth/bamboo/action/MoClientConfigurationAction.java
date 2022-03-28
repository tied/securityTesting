package com.miniorange.oauth.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.bamboo.MoOAuthPluginConstants;
import com.miniorange.oauth.bamboo.MoOAuthPluginHandler;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import com.miniorange.oauth.utils.MoOAuthUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoClientConfigurationAction extends BambooActionSupport {

	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(MoClientConfigurationAction.class);

	private String customAppName;
	private String clientID;
	private String scope;
	private String clientSecret;
	private String authorizeEndpoint;
	private String accessTokenEndpoint;
	private String userInfoEndpoint;
	private String fetchGroupsEndpoint;
	private String singleLogoutURL;
	private String publicKey;
	private Boolean nonceCheck;
	private Boolean isCredentialsVerified;
	private Boolean useStateParameter;
	private Boolean acrValueCheck;
	private Boolean enableCheckIssuerFor;
	private String checkIssuerFor;
	private String customIssuerValue;
	private String tenantID;
	private String hostName;
	private String realmName;
	private String domainName;

	private String appHostedOn;
	private String appName;
	private Boolean submitted;
	private Boolean deleterefreshtoken;

	private PluginLicenseManager pluginLicenseManager;
	private MoOAuthSettings settings;
	private String xsrfToken;

	private UserManager userManager;
	private LoginUriProvider loginUriProvider;
	private String isOpenIdProtocol;

	private ArrayList<String> defaultOpenIdApps = new ArrayList<>();
	private ArrayList<String> defaultOauthApps = new ArrayList<>();
    private Map<String, String> oauth_request_parameters;

	public MoClientConfigurationAction(MoOAuthSettings settings,
									   PluginLicenseManager pluginLicenseManager, UserManager userManager, LoginUriProvider loginUriProvider) {

		this.settings = settings;
		this.pluginLicenseManager = pluginLicenseManager;
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
	}

	public void validate() {
		LOGGER.info("OAuth Configuration Action validate");

		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		if (!BooleanUtils.toBoolean(this.submitted)) {
			return;
		}

		Boolean error = false;
		List<String> invalidValues = new ArrayList<>();

		UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

		if(StringUtils.isBlank(clientID))
			invalidValues.add("clientid");
		if(StringUtils.isBlank(clientSecret))
			invalidValues.add("clientsecret");
		if(StringUtils.isBlank(appName))
			invalidValues.add("appname");

		if(StringUtils.equalsIgnoreCase(appName,"Custom App") || StringUtils.equalsIgnoreCase(appName,"Custom OpenID")){
			if(!urlValidator.isValid(authorizeEndpoint))
				invalidValues.add("authorizeendpoint");
			if(!urlValidator.isValid(accessTokenEndpoint))
				invalidValues.add("accesstokenendpoint");
		}

		if(StringUtils.equalsIgnoreCase(appName,"Custom App")){
			if(!urlValidator.isValid(userInfoEndpoint))
				invalidValues.add("userinfoendpoint");
			if(StringUtils.isBlank(customAppName)){
				invalidValues.add("customappname");
			}
		}

		if(StringUtils.equalsIgnoreCase(appName,"Keycloak") && StringUtils.isBlank(hostName) ){
			invalidValues.add("hostName");
		}

		if(StringUtils.equalsIgnoreCase(appName,"Keycloak") && StringUtils.isBlank(realmName) ){
			invalidValues.add("realmName");
		}

		if((StringUtils.equalsIgnoreCase(appName,"AWS Cognito") || StringUtils.equalsIgnoreCase(appName,"ADFS")) && StringUtils.isBlank(domainName)){
			invalidValues.add("domainName");
		}

		if(StringUtils.equalsIgnoreCase(appName, "Azure AD") && StringUtils.isBlank(tenantID)){
			invalidValues.add("tenantid");
		}

		if(StringUtils.equalsIgnoreCase(appName, "GitLab")){
			if(StringUtils.equalsIgnoreCase(appHostedOn, MoOAuthPluginConstants.SELF_HOSTING) && StringUtils.isNotBlank(domainName)){
				if(!urlValidator.isValid(domainName)) {
					addActionError(getText("oauth.error.invalid.domainNameUrl"));
				}
			}else if(StringUtils.equalsIgnoreCase(appHostedOn, MoOAuthPluginConstants.SELF_HOSTING) && StringUtils.isBlank(domainName)){
				invalidValues.add("domainName");
			}
		}

		if (StringUtils.isNotBlank(this.singleLogoutURL)) {
			if (!urlValidator.isValid(singleLogoutURL))
				invalidValues.add("singleLogoutEndpoint");
		}

		if (MoOAuthUtils.isOpenIdProtocol(appName)) {
			if (StringUtils.isNotBlank(this.publicKey)) {
				String publicCertificate = this.publicKey;
				publicCertificate = MoOAuthUtils.deserializePublicKey(publicCertificate);
				byte[] publicBytes = Base64.decodeBase64(publicCertificate);
				try {
					X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
					KeyFactory keyFactory = KeyFactory.getInstance("RSA");
					keyFactory.generatePublic(keySpec);
				} catch (GeneralSecurityException e) {
					invalidValues.add("publicKey");
				}
			}
		}

		for(String invalidValue : invalidValues){
			error = true;
			addActionError(getText("oauth.error.invalid."+invalidValue));
		}

		if (super.hasActionErrors() || error) {
			xsrfToken = XsrfTokenUtils.getXsrfToken(request);
			initializeOAuthConfig();
			return;
		}
		super.validate();

	}

	public String execute() throws Exception {
		LOGGER.info("OAuth Configuration Action doExecute called");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		final UserProfile user = userManager.getRemoteUser();
		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				LOGGER.debug("OAuth Configuration Action doExecute Submitted: " + this.submitted);

				xsrfToken = XsrfTokenUtils.getXsrfToken(request);

				if (!settings.isLicenseValid()) {
					try {
						response.sendRedirect(settings.getManageAddOnURL());
						return null;
					} catch (IOException e) {
						e.printStackTrace();

					}
				}
				if (BooleanUtils.toBoolean(this.deleterefreshtoken))
					settings.setRefreshToken("");
				/* When Configuration is saved.*/
				if (BooleanUtils.toBoolean(this.submitted)) {
					//Save default values for all tabs only first time - when no provider is configured
					if(StringUtils.equals(settings.getClientID(), "")){
						MoOAuthPluginHandler.saveDefaultConfiguration();
					}
					if (StringUtils.equalsIgnoreCase(appName, "OKTA")) {
						this.useStateParameter = Boolean.TRUE;
					}
					if (StringUtils.equalsIgnoreCase(appName, "miniOrange")) {
						setUseStateParameter(Boolean.TRUE);
					}
					HashMap<String, String> oauth_request_parameters = new HashMap<>();
                    int totalOauthParameters = Integer.parseInt(StringUtils.defaultIfBlank(request.getParameter("totalOauthParameters"), "0"));
                    for (int i=0;i<totalOauthParameters;i++) {
                        String key = request.getParameter("oauthParameterKey[" + i + "]");
                        String value =request.getParameter("oauthParameterValue[" + i + "]");
                        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
                            oauth_request_parameters.put(key.trim(), value.trim());

                        }
                    }

					accessTokenEndpoint=accessTokenEndpoint.trim().replace(" ","%20");
					authorizeEndpoint=authorizeEndpoint.trim().replace(" ","%20");
					userInfoEndpoint=userInfoEndpoint.trim().replace(" ","%20");

					if (StringUtils.equals(appName, "Custom OAuth") || StringUtils.equals(appName, "Custom OpenID")) {
						MoOAuthPluginHandler.saveOAuthConfiguration(appName, customAppName, clientID, clientSecret, scope,
								authorizeEndpoint, accessTokenEndpoint, userInfoEndpoint, fetchGroupsEndpoint,
								useStateParameter, enableCheckIssuerFor,checkIssuerFor, customIssuerValue, singleLogoutURL, nonceCheck, publicKey,acrValueCheck, oauth_request_parameters);
					} else {
						MoOAuthPluginHandler.saveOAuthConfiguration(appName, appHostedOn, clientID, clientSecret, scope, tenantID, hostName,
								realmName, domainName, enableCheckIssuerFor,checkIssuerFor, customIssuerValue,
								singleLogoutURL, useStateParameter, nonceCheck, publicKey,acrValueCheck, oauth_request_parameters);
					}
					LOGGER.info("OAuth Configuration saved");
					addActionMessage(getText("oauth.success.config"));
				}
				initializeOAuthConfig();
				return "success";

			} catch(MoOAuthPluginException e){
				e.printStackTrace();
				addActionError(e.getMessage());
				return "input";
			} catch(Exception e){
				e.printStackTrace();
				LOGGER.error("Error is : " + e);
				addActionError("An error occurred while saving your details. Please check logs for more info.");
				return "input";
			}
		}else{
			response.sendRedirect(loginUriProvider.getLoginUri(MoOAuthPluginHandler.getUri(request)).toASCIIString());
			return null;
		}
	}


	private void initializeOAuthConfig() {
		LOGGER.info("initializing OAuthConfig");

		this.customAppName = settings.getCustomAppName();
		this.appName = settings.getAppName();

		classifyAppsInOauthAndOpenId();

		if(StringUtils.equals(this.appName, "Microsoft")){
			this.customAppName = "Azure AD";
			this.appName = "Azure AD";
			settings.setAppName(this.appName);
			settings.setCustomAppName(this.customAppName);
		}
		if(MoOAuthUtils.isOpenIdProtocol(appName)){
			this.isOpenIdProtocol = "true";
		} else{
			this.isOpenIdProtocol = "false";
		}
		LOGGER.debug("isOpenIdProtocol: " + isOpenIdProtocol);


		this.appHostedOn = settings.getAppHostedOn();
		this.clientID = settings.getClientID();
		this.clientSecret = settings.getClientSecret();
		this.authorizeEndpoint = settings.getAuthorizeEndpoint();
		this.accessTokenEndpoint = settings.getAccessTokenEndpoint();
		this.userInfoEndpoint = settings.getUserInfoEndpoint();
		this.fetchGroupsEndpoint = settings.getFetchGroupsEndpoint();
		this.scope = settings.getScope();
		this.isCredentialsVerified = settings.getIsCredentialsVerified();
		this.tenantID = settings.getTenantID();
		this.hostName = settings.getHostName();
		this.realmName = settings.getRealmName();
		this.domainName = settings.getDomainName();
		this.useStateParameter = settings.getUseStateParameter();
		this.enableCheckIssuerFor = settings.getEnableCheckIssuerFor();
		this.checkIssuerFor = settings.getCheckIssuerFor();
		this.customIssuerValue = settings.getCustomIssuerValue();
		this.singleLogoutURL = settings.getSingleLogoutURL();
		this.publicKey = StringUtils.trim(settings.getPublicKey());
		this.nonceCheck = settings.getNonceCheck();
		this.acrValueCheck = settings.getACRValueCheck();
        this.oauth_request_parameters = settings.getOauth_request_parameters();

		LOGGER.info("initializeOAuthConfig done....");
	}

	private void classifyAppsInOauthAndOpenId() {
		for(MoOAuthPluginConstants.OAuthApps oAuthApps : MoOAuthPluginConstants.OAuthApps.values()){
			this.defaultOauthApps.add(oAuthApps.getAppName());
		}
		for(MoOAuthPluginConstants.OpenIdApps openIdApps : MoOAuthPluginConstants.OpenIdApps.values()){
			this.defaultOpenIdApps.add(openIdApps.getAppName());
		}
	}


	public void setDefaultOpenIdApps(ArrayList<String> defaultOpenIdApps) {
		this.defaultOpenIdApps = defaultOpenIdApps;
	}

	public void setDefaultOauthApps(ArrayList<String> defaultOauthApps) {
		this.defaultOauthApps = defaultOauthApps;
	}

	public ArrayList<String> getDefaultOpenIdApps() {
		return defaultOpenIdApps;
	}

	public ArrayList<String> getDefaultOauthApps() {
		return defaultOauthApps;
	}


	public String getCustomAppName() {
		return customAppName;
	}

	public void setCustomAppName(String customAppName) {
		this.customAppName = customAppName;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getAuthorizeEndpoint() {
		return authorizeEndpoint;
	}

	public void setAuthorizeEndpoint(String authorizeEndpoint) {
		this.authorizeEndpoint = authorizeEndpoint;
	}

	public String getAccessTokenEndpoint() {
		return accessTokenEndpoint;
	}

	public void setAccessTokenEndpoint(String accessTokenEndpoint) {
		this.accessTokenEndpoint = accessTokenEndpoint;
	}

	public String getUserInfoEndpoint() {
		return userInfoEndpoint;
	}

	public void setUserInfoEndpoint(String userInfoEndpoint) {
		this.userInfoEndpoint = userInfoEndpoint;
	}

	public String getFetchGroupsEndpoint() {
		return fetchGroupsEndpoint;
	}

	public void setFetchGroupsEndpoint(String fetchGroupsEndpoint) {
		this.fetchGroupsEndpoint = fetchGroupsEndpoint;
	}

	public Boolean getCredentialsVerified() {
		return isCredentialsVerified;
	}

	public void setCredentialsVerified(Boolean credentialsVerified) {
		isCredentialsVerified = credentialsVerified;
	}

	public String getTenantID() {
		return tenantID;
	}

	public void setTenantID(String tenantID) {
		this.tenantID = tenantID;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Boolean getSubmitted() {
		return submitted;
	}

	public void setSubmitted(Boolean submitted) {
		this.submitted = submitted;
	}

	public Boolean getDeleterefreshtoken() {
		return deleterefreshtoken;
	}

	public void setDeleterefreshtoken(Boolean deleterefreshtoken) {
		this.deleterefreshtoken = deleterefreshtoken;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	public MoOAuthSettings getSettings() {
		return settings;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public PluginLicenseManager getPluginLicenseManager() {
		return pluginLicenseManager;
	}

	public void setPluginLicenseManager(PluginLicenseManager pluginLicenseManager) {
		this.pluginLicenseManager = pluginLicenseManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public LoginUriProvider getLoginUriProvider() {
		return loginUriProvider;
	}

	public void setLoginUriProvider(LoginUriProvider loginUriProvider) {
		this.loginUriProvider = loginUriProvider;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getRealmName() {
		return realmName;
	}

	public void setRealmName(String realmName) {
		this.realmName = realmName;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public Boolean getUseStateParameter() {
		return useStateParameter;
	}

	public void setUseStateParameter(Boolean useStateParameter) {
		this.useStateParameter = useStateParameter;
	}

	public Boolean getEnableCheckIssuerFor() {
		return enableCheckIssuerFor;
	}

	public void setEnableCheckIssuerFor(Boolean enableCheckIssuerFor) {
		this.enableCheckIssuerFor = enableCheckIssuerFor;
	}

	public String getCheckIssuerFor() {
		return checkIssuerFor;
	}

	public void setCheckIssuerFor(String checkIssuerFor) {
		this.checkIssuerFor = checkIssuerFor;
	}

	public String getCustomIssuerValue() {
		return customIssuerValue;
	}

	public void setCustomIssuerValue(String customIssuerValue) {
		this.customIssuerValue = customIssuerValue;
	}

	public String getSingleLogoutURL() {
		return singleLogoutURL;
	}

	public void setSingleLogoutURL(String singleLogoutURL) {
		this.singleLogoutURL = singleLogoutURL;
	}

	public String getAppHostedOn(){return appHostedOn;}

	public void setAppHostedOn(String appHostedOn){this.appHostedOn = appHostedOn;}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public Boolean getNonceCheck() { return nonceCheck; }

	public void setNonceCheck(Boolean nonceCheck) { this.nonceCheck = nonceCheck; }

	public String getIsOpenIdProtocol() {
		return isOpenIdProtocol;
	}

	public void setIsOpenIdProtocol(String isOpenIdProtocol) {
		this.isOpenIdProtocol = isOpenIdProtocol;
	}

	public Boolean getAcrValueCheck() {
		return acrValueCheck;
	}

	public void setAcrValueCheck(Boolean acrValueCheck) {
		this.acrValueCheck = acrValueCheck;
	}

	public Map<String, String> getOauth_request_parameters() {
		return oauth_request_parameters;
	}

	public void setOauth_request_parameters(Map<String, String> oauth_request_parameters) {
		this.oauth_request_parameters = oauth_request_parameters;
	}
}



