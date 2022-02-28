package com.miniorange.oauth.confluence.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import com.atlassian.xwork.RequireSecurityToken;
import com.atlassian.xwork.SimpleXsrfTokenGenerator;
import com.atlassian.xwork.XsrfTokenGenerator;
import com.miniorange.oauth.confluence.MoOAuthPluginConstants;
import com.miniorange.oauth.utils.MoOAuthUtils;
import com.opensymphony.webwork.ServletActionContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;

import javax.servlet.http.HttpServletRequest;

public class MoOAuthConfigurationAction extends ConfluenceActionSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(MoOAuthConfigurationAction.class);
	private static final long serialVersionUID = 1L;

	private String customCallbackParameter;
	private String customAppName;
	private String clientID;
	private String scope;
	private String clientSecret;
	private String tenantID;
	private String domainName;
	private String hostName;
	private String realmName;
	private String authorizeEndpoint;
	private String accessTokenEndpoint;
	private String userInfoEndpoint;
	private String fetchGroupsEndpoint;
	private Boolean isCredentialsVerified;
	private String publicKey;
	private String validateSignatureMethod;
	private String jWKSEndpointURL;
	private String singleLogoutURL;

	private String appName;
	private Map<String, String> defaultApps;
	private ArrayList<String> defaultOauthApps;

	private ArrayList<String> defaultOpenidApps;
	private Boolean submitted;
	private Boolean useStateParameter;
	private Boolean acrValueCheck;
	private Boolean nonceCheck; 

	private String checkIssuerFor;
	private String customIssuerValue;
	private Boolean enableCheckIssuerFor;

	private String sendTokenAuthParameterIn;
	private Boolean showRestApiMsg;
	private Map<String, String> oauth_request_parameters;

	private MoOAuthSettings settings;
	private MoOAuthPluginHandler pluginHandler;
	private PluginLicenseManager pluginLicenseManager;
	private String xsrfToken;


	public MoOAuthConfigurationAction(MoOAuthSettings settings, MoOAuthPluginHandler pluginHandler,
			PluginLicenseManager pluginLicenseManager) {
		this.settings = settings;
		this.pluginHandler = pluginHandler;
		this.pluginLicenseManager = pluginLicenseManager;

	}

	public Boolean validation() {
		LOGGER.debug("OAuth Configuration Action Validate");
		Boolean error = false;
		if (!BooleanUtils.toBoolean(this.submitted)) {
			error = true;
		}

		List<String> invalidValues = new ArrayList<>();

		UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES);

		if (StringUtils.isBlank(clientID))
			invalidValues.add("clientid");
		if (StringUtils.isBlank(clientSecret))
			invalidValues.add("clientsecret");
		if (StringUtils.isBlank(appName))
			invalidValues.add("appname");
		if (StringUtils.isBlank(tenantID) && StringUtils.isNotBlank(appName)
				&& StringUtils.equalsIgnoreCase(appName, "Azure AD")) {
			invalidValues.add("tenantid");
		}
		if (StringUtils.isBlank(tenantID) && StringUtils.isNotBlank(appName)
				&& StringUtils.equalsIgnoreCase(appName, "Azure B2C")) {
			invalidValues.add("tenantid");
		}
		if (StringUtils.isBlank(hostName) && StringUtils.isNotBlank(appName)
				&& StringUtils.equalsIgnoreCase(appName, "Keycloak")) {
			invalidValues.add("hostName");
		}
		if (StringUtils.isBlank(realmName) && StringUtils.isNotBlank(appName)
				&& StringUtils.equalsIgnoreCase(appName, "Keycloak")) {
			invalidValues.add("realmName");
		}

		if (StringUtils.equalsIgnoreCase(appName, "OpenID")){
			if (StringUtils.isBlank(customAppName)) {
				invalidValues.add("customappname");
			}
		}

		if (StringUtils.equalsIgnoreCase(appName, "miniOrange")){
			setDomainName(domainName);
			LOGGER.debug("domain Name::"+domainName);
		}

		if (StringUtils.equalsIgnoreCase(appName, "Custom App")) {

			if (StringUtils.isBlank(customAppName)) {
				invalidValues.add("customappname");
			}

			if (StringUtils.isBlank(authorizeEndpoint)) {
				invalidValues.add("authorizeendpoint");
			} else if (StringUtils.isNotBlank(this.authorizeEndpoint)) {
				try {
					new URL(this.authorizeEndpoint);
				} catch (MalformedURLException e) {
					LOGGER.error(e.getMessage());
					invalidValues.add("authorizeendpoint");
				}
			}

			if (StringUtils.isBlank(accessTokenEndpoint)) {
				invalidValues.add("accesstokenendpoint");
			} else if (StringUtils.isNotBlank(this.accessTokenEndpoint)) {
				try {
					new URL(this.accessTokenEndpoint);
				} catch (MalformedURLException e) {
					LOGGER.error(e.getMessage());
					invalidValues.add("accesstokenendpoint");
				}
			}

			if (StringUtils.isBlank(userInfoEndpoint)) {
				invalidValues.add("userinfoendpoint");
			} else if (StringUtils.isNotBlank(this.userInfoEndpoint)) {
				try {
					new URL(this.userInfoEndpoint);
				} catch (MalformedURLException e) {
					LOGGER.error(e.getMessage());
					invalidValues.add("userinfoendpoint");
				}
			}

			if (StringUtils.isNotBlank(this.fetchGroupsEndpoint)) {
				try {
					new URL(this.fetchGroupsEndpoint);
				} catch (MalformedURLException e) {
					LOGGER.error(e.getMessage());
					invalidValues.add("fetchgroupsgndpoint");
				}
			}

		/*	if (!urlValidator.isValid(authorizeEndpoint))
				invalidValues.add("authorizeendpoint");
			if (!urlValidator.isValid(accessTokenEndpoint))
				invalidValues.add("accesstokenendpoint");
			if (!urlValidator.isValid(userInfoEndpoint))
				invalidValues.add("userinfoendpoint");
			if (StringUtils.isBlank(customAppName)) {
				invalidValues.add("customappname");
			}*/
		}

		if (StringUtils.equalsIgnoreCase(appName, "ADFS") || StringUtils.equalsIgnoreCase(appName, "AWS Cognito" ) || StringUtils.equalsIgnoreCase(appName, "OKTA")
				|| StringUtils.equalsIgnoreCase(appName, "Keycloak") || StringUtils.equalsIgnoreCase(appName, "Azure B2C") || StringUtils.equalsIgnoreCase(appName, "OpenID")) {
			if (StringUtils.isNotBlank(this.publicKey)) {
				String publicCertificate = this.publicKey;
				publicCertificate = MoOAuthUtils.deserializePublicKey(publicCertificate);
				byte[] publicBytes = Base64.decodeBase64(publicCertificate);
				try {
					X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
					KeyFactory keyFactory = KeyFactory.getInstance("RSA");
					keyFactory.generatePublic(keySpec);
				} catch (GeneralSecurityException e) {
					invalidValues.add("publickey");
				}
			}
			if (StringUtils.isNotBlank(this.jWKSEndpointURL)) {
				try {
					new URL(this.jWKSEndpointURL);
				} catch (MalformedURLException e) {
					LOGGER.error(e.getMessage());
					invalidValues.add("jWKSEndpointURL");
				}
			}
		}

		if (StringUtils.isNotBlank(this.singleLogoutURL)) {
			try {
				new URL(this.singleLogoutURL);
			} catch (MalformedURLException e) {
				LOGGER.error(e.getMessage());
				invalidValues.add("singleLogoutEndpoint");
			}
		}

		if (StringUtils.equalsIgnoreCase(appName, "ADFS") || StringUtils.equalsIgnoreCase(appName, "AWS Cognito" )
				|| StringUtils.equalsIgnoreCase(appName, "Salesforce") || StringUtils.equalsIgnoreCase(appName, "OKTA") || StringUtils.equalsIgnoreCase(appName, "miniOrange")
                || StringUtils.equalsIgnoreCase(appName, "Gluu Server")) {

			if (StringUtils.isBlank(this.domainName)) {
				invalidValues.add("domainname");
			} 
			else if (StringUtils.isNotBlank(this.domainName)) {
				try {
					new URL(this.domainName);
				} catch (MalformedURLException e) {
					LOGGER.error(e.getMessage());
					invalidValues.add("domainname");
				}
			}
		}

		if (StringUtils.equalsIgnoreCase(appName, "Keycloak")){
			if (StringUtils.isBlank(this.hostName)) {
				invalidValues.add("hostname");
			} 
			else if (StringUtils.isNotBlank(this.hostName)) {
				try {
					new URL(this.hostName);
				} catch (MalformedURLException e) {
					LOGGER.error(e.getMessage());
					invalidValues.add("hostname");
				}
			}
		}

		for (String invalidValue : invalidValues) {
			error = true;
			addActionError(getText("oauth.error.invalid." + invalidValue));
		}

		if (BooleanUtils.toBoolean(error)) {
			initializeOAuthConfig();
		}

		return error;
	}

	@Override
	public String execute() throws Exception {
		try {
			LOGGER.debug("OAuth Configuration Action doExecute Submitted: " + this.submitted);
			HttpServletRequest request = ServletActionContext.getRequest();
			XsrfTokenGenerator tokenGenerator = new SimpleXsrfTokenGenerator();
			xsrfToken = tokenGenerator.generateToken(request);

			if (!settings.isLicenseValid()) {
				return "invalid";
			}

			String referer = request.getHeader("referer");
			//if user have selected 'do not show this msg again' then 'settings.getShowRestApiMsg()' has 'false'
			//if user have selected 'Remind me again' or just closed the window then 'settings.getShowRestApiMsg()' has 'true'
			this.showRestApiMsg = settings.getShowRestApiMsg();
			if(!StringUtils.endsWith(referer, "upm")){
				//if not opened for first time - do not show rest api msg
				this.showRestApiMsg = false;
			}

			if (settings.getResetSettings()) {
				addActionMessage(getText("oauth.success.reset.settings"), "success", true, null);
				settings.setResetSettings(Boolean.FALSE);
			}

			if (BooleanUtils.toBoolean(this.submitted)) {
				Boolean error;
				error = validation();

				if (!error) {
					if (!StringUtils.equalsIgnoreCase(appName, "OpenID")
							&& !StringUtils.equalsIgnoreCase(appName, "ADFS")
							&& !StringUtils.equalsIgnoreCase(appName, "AWS Cognito")
							&& !StringUtils.equalsIgnoreCase(appName, "OKTA")
							&& !StringUtils.equalsIgnoreCase(appName, "Keycloak")
							&& !StringUtils.equalsIgnoreCase(appName, "Azure B2C")) {
						this.publicKey = "";
						this.jWKSEndpointURL ="";
						this.enableCheckIssuerFor = Boolean.FALSE;
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
					
					if (StringUtils.isNoneBlank(customCallbackParameter)){    
						char firstChar = customCallbackParameter.charAt(0);  
	                    if(firstChar != '/'){  
	                    	customCallbackParameter = "/" + customCallbackParameter;  
	                    }  
	                }
					accessTokenEndpoint=accessTokenEndpoint.trim().replace(" ","%20");

					if ((StringUtils.equals(appName, "Custom App")) || (StringUtils.equals(appName, "OpenID"))) {
						pluginHandler.saveDefaultConfiguration();
						pluginHandler.saveOAuthConfiguration(customCallbackParameter, appName, customAppName, clientID, clientSecret, scope,
								authorizeEndpoint, accessTokenEndpoint, userInfoEndpoint, fetchGroupsEndpoint, useStateParameter, 
								acrValueCheck, nonceCheck, publicKey,jWKSEndpointURL,validateSignatureMethod,
								enableCheckIssuerFor, checkIssuerFor, customIssuerValue, singleLogoutURL, sendTokenAuthParameterIn,oauth_request_parameters);
					} else {
						pluginHandler.saveDefaultConfiguration();
						pluginHandler.saveOAuthConfiguration(customCallbackParameter, appName, clientID, clientSecret, scope, tenantID,
								domainName, hostName, realmName, publicKey, jWKSEndpointURL,validateSignatureMethod,enableCheckIssuerFor, checkIssuerFor, customIssuerValue,
								useStateParameter,singleLogoutURL,oauth_request_parameters,acrValueCheck, nonceCheck);
					}
					addActionMessage(getText("oauth.success.config"), "success", true, null);
				}
			}
			initializeOAuthConfig();
			return "success";
		} catch (MoOAuthPluginException e) {
			LOGGER.error("An error occured while loading the configuration",e);
			addActionError(e.getMessage());
			return "input";
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			LOGGER.debug("An error occured while loading the configuration " + e);
			e.printStackTrace();
			addActionError("An error occurred while saving your details. Please check logs for more info.");
			return "input";
		}
	}

	private void initializeOAuthConfig() {
		separateDefaultAppsByProtocol();
		this.customCallbackParameter = settings.getCustomCallbackParameter();
		this.appName = settings.getAppName();
		this.customAppName = settings.getCustomAppName();
		this.clientID = settings.getClientID();
		this.clientSecret = settings.getClientSecret();
		this.tenantID = settings.getTenantID();
		this.domainName = settings.getDomainName();
		this.authorizeEndpoint = settings.getAuthorizeEndpoint();
		this.accessTokenEndpoint = settings.getAccessTokenEndpoint();
		this.userInfoEndpoint = settings.getUserInfoEndpoint();
		this.scope = settings.getScope();
		this.defaultApps = getDefaultApplications();
		this.fetchGroupsEndpoint = settings.getFetchGroupsEndpoint();
		this.isCredentialsVerified = settings.getIsCredentialsVerified();
		this.useStateParameter = settings.getUseStateParameter();
		this.acrValueCheck = settings.getAcrValue();
		this.nonceCheck = settings.getNonceCheck(); 
		this.publicKey = StringUtils.trim(settings.getPublicKey());
		this.validateSignatureMethod= settings.getValidateSignatureMethod();
		this.jWKSEndpointURL= settings.getjWKSEndpointURL();
		this.hostName = settings.getHostName();
		this.realmName = settings.getRealmName();
		this.enableCheckIssuerFor = settings.getEnableCheckIssuerFor();
		this.checkIssuerFor = settings.getCheckIssuerFor();
		this.customIssuerValue = settings.getCustomIssuerValue();
		this.singleLogoutURL = settings.getSingleLogoutURL();
		this.sendTokenAuthParameterIn = settings.getSendTokenAuthParameterIn();
		this.oauth_request_parameters= settings.getOauth_request_parameters();

		LOGGER.debug(this.customAppName + "\n" + this.clientID + "\n" + this.clientSecret + "\n" + this.scope + "\n"
				+ this.tenantID + "\n" + this.authorizeEndpoint + "\n" + this.accessTokenEndpoint + "\n"
				+ this.userInfoEndpoint + "\n" + this.scope + "\n" + this.fetchGroupsEndpoint+ "\n" + this.useStateParameter+"\n"
				+ this.publicKey + "\n"+ this.jWKSEndpointURL + "\n"+ this.validateSignatureMethod +"\n" + this.enableCheckIssuerFor + "\n"
				+ this.checkIssuerFor + "\n" + this.customIssuerValue+" \n "+ this.sendTokenAuthParameterIn);
	}

	private void separateDefaultAppsByProtocol(){
		Map<String, String> apps = getDefaultApplications();
		this.defaultOauthApps = new ArrayList<>();
		this.defaultOpenidApps = new ArrayList<>();
		for (String app: apps.keySet()) {
			if (MoOAuthPluginConstants.applicationProtocols.get(app).equalsIgnoreCase("openid")){
				this.defaultOpenidApps.add(app);
			}
			else{
				this.defaultOauthApps.add(app);
			}
		}
	}

	private Map<String, String> getDefaultApplications() {
		Map<String, String> apps = new LinkedHashMap<>();
		apps.put("ADFS","ADFS");
		apps.put("AWS Cognito","AWS Cognito");
		apps.put("Azure AD","Azure AD");
		apps.put("Azure B2C","Azure B2C");
		apps.put("Discord","Discord");
		apps.put("Facebook","Facebook");
		apps.put("GitHub","GitHub");
		apps.put("GitHub Enterprise","GitHub Enterprise");
		apps.put("Gitlab","Gitlab");
		apps.put("Gluu Server","Gluu Server");
		apps.put("Google","Google");
		apps.put("Keycloak","Keycloak");
		apps.put("Meetup","Meetup");
		apps.put("miniOrange", "miniOrange");
		apps.put("Salesforce","Salesforce");
		apps.put("OKTA", "OKTA");
		apps.put("Slack","Slack");
		apps.put("Custom App","Custom App");
		apps.put("OpenID","OpenID");
		return apps;
	}

	public String getCustomCallbackParameter() {  
		return customCallbackParameter;  
	}  
  
	public void setCustomCallbackParameter(String customCallbackParameter) {  
		this.customCallbackParameter = customCallbackParameter;  
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

	public Boolean getSubmitted() {
		return submitted;
	}

	public void setSubmitted(Boolean submitted) {
		this.submitted = submitted;
	}

	public MoOAuthSettings getSettings() {
		return settings;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public MoOAuthPluginHandler getPluginHandler() {
		return pluginHandler;
	}

	public void setPluginHandler(MoOAuthPluginHandler pluginHandler) {
		this.pluginHandler = pluginHandler;
	}

	public String getFetchGroupsEndpoint() {
		return fetchGroupsEndpoint;
	}

	public void setFetchGroupsEndpoint(String fetchGroupsEndpoint) {
		this.fetchGroupsEndpoint = fetchGroupsEndpoint;
	}

	public Boolean getIsCredentialsVerified() {
		return isCredentialsVerified;
	}

	public void setIsCredentialsVerified(Boolean isCredentialsVerified) {
		this.isCredentialsVerified = isCredentialsVerified;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public Map<String, String> getDefaultApps() {
		return defaultApps;
	}

	public void setDefaultApps(Map<String, String> defaultApps) {
		this.defaultApps = defaultApps;
	}

	public ArrayList<String> getDefaultOauthApps() {
		return defaultOauthApps;
	}

	public void setDefaultOauthApps(ArrayList<String> defaultOauthApps) {
		this.defaultOauthApps = defaultOauthApps;
	}

	public ArrayList<String> getDefaultOpenidApps() {
		return defaultOpenidApps;
	}

	public void setDefaultOpenidApps(ArrayList<String> defaultOpenidApps) {
		this.defaultOpenidApps = defaultOpenidApps;
	}

	public String getTenantID() {
		return tenantID;
	}

	public void setTenantID(String tenantID) {
		this.tenantID = tenantID;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
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

	public Boolean getUseStateParameter() {
		return useStateParameter;
	}

	public void setUseStateParameter(Boolean useStateParameter) {
		this.useStateParameter = useStateParameter;
	}
	
	public Boolean getAcrValueCheck() {
		return acrValueCheck;
	}

	public void setAcrValueCheck(Boolean acrValueCheck) {
		this.acrValueCheck = acrValueCheck;
	}

	public String getValidateSignatureMethod() { return validateSignatureMethod; }

	public void setValidateSignatureMethod(String validateSignatureMethod) { this.validateSignatureMethod = validateSignatureMethod; }

	public String getjWKSEndpointURL() { return jWKSEndpointURL; }

	public void setjWKSEndpointURL(String jWKSEndpointURL) { this.jWKSEndpointURL = jWKSEndpointURL; }

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
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

	public Boolean getEnableCheckIssuerFor() {
		return enableCheckIssuerFor;
	}

	public void setEnableCheckIssuerFor(Boolean enableCheckIssuerFor) {
		this.enableCheckIssuerFor = enableCheckIssuerFor;
	}

	public String getSingleLogoutURL() {
		return singleLogoutURL;
	}

	public void setSingleLogoutURL(String singleLogoutURL) {
		this.singleLogoutURL = singleLogoutURL;
	}

	public String getSendTokenAuthParameterIn() {
		return sendTokenAuthParameterIn;
	}

	public void setSendTokenAuthParameterIn(String sendTokenAuthParameterIn) {
		this.sendTokenAuthParameterIn = sendTokenAuthParameterIn;
	}

	public Boolean getShowRestApiMsg() {
		return showRestApiMsg;
	}

	public void setShowRestApiMsg(Boolean showRestApiMsg) {
		this.showRestApiMsg = showRestApiMsg;
	}
	
	public Boolean getNonceCheck() { 
		return nonceCheck; 
	} 
 
	public void setNonceCheck(Boolean nonceCheck) { 
		this.nonceCheck = nonceCheck; 
	}

	public Map<String, String> getOauth_request_parameters() {
		return oauth_request_parameters;
	}

	public void setOauth_request_parameters(Map<String, String> oauth_request_parameters) {
		this.oauth_request_parameters = oauth_request_parameters;
	}
	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}
	
}
