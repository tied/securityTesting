package com.miniorange.sso.saml.bamboo.action;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.miniorange.sso.saml.utils.MoHttpUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.plugin.PluginException;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
//import com.atlassian.sal.api.user.UserRole;
import com.atlassian.user.Group;
import com.atlassian.user.search.page.Pager;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSAMLUtils;

public class MoSpConfigurationAction extends BambooActionSupport {

	private UserManager userManager;
	private LoginUriProvider loginUriProvider;
	private MoSAMLSettings settings;

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MoSpConfigurationAction.class);

	private String spBaseUrl;
	private String spEntityId;
	private ArrayList idpGuides;
	private Boolean signing;
	private Boolean encryption;
	private String customOrganizationName;
	private String customOrganizationDisplayName;
	private String customOrganizationUrl;
	private String technicalContactName;
	private String technicalContactEmail;
	private String supportContactName;
	private String supportContactEmail;
	private Boolean isJCEInstalled;
	private Boolean certificatesSubmitted;
	private Map<String, String> certificateInfo;
	
	private Boolean submitted;
	private String xsrfToken;

	public MoSpConfigurationAction(MoSAMLSettings settings, UserManager userManager,
			LoginUriProvider loginUriProvider) {
		this.settings = settings;
		this.loginUriProvider = loginUriProvider;
		this.userManager = userManager;
	}

	public void validate() {		
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		if (!BooleanUtils.toBoolean(submitted)) {
			return;
		}

		if (BooleanUtils.toBoolean(this.submitted)) {
			if (StringUtils.isBlank(this.spBaseUrl)) {
				addActionError(getText("samlsso.error.config.spbaseurl.empty"));
			} else {
				try {
					new URL(this.spBaseUrl);
				} catch (MalformedURLException e) {
					addActionError(getText("samlsso.error.config.spbaseurl.malformed"));
				}
			}
			if (StringUtils.isBlank(this.spEntityId)) {
				addActionError("SP Entity ID can not be left blank.");
			}
		}

		if (super.hasActionErrors()) {
			xsrfToken = XsrfTokenUtils.getXsrfToken(request);
			initializeSAMLConfig();
		}
		super.validate();
	}

	@Override
	public String execute() throws Exception {
		
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);
		
		final UserProfile user = userManager.getRemoteUser();

		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				LOGGER.debug("SP Configure Action execute Submitted: " + this.submitted);
				if (!settings.isValidLicense()) {
					return "upm";
				}

				if(StringUtils.isNotBlank(settings.getSsoServiceUrl())){
					String idpID = UUID.randomUUID().toString();
					String idpName = "IDP";
					MoPluginHandler.replaceOldSettingsWithNew(idpID, idpName);
				}

				if (settings.getIdPList().isEmpty()){
					setCertificates(settings.getNewPrivateSPCertificate(),settings.getNewPublicSPCertificate());
				}
				//Set Certificate expire date
				String validTo=getCertificateInfo().get("Valid To");
				setSPCertExpirayDate(validTo);

				if (request.getParameter("reset") != null && StringUtils.equalsIgnoreCase(request.getParameter("reset"), "success")) {
					addActionMessage("SAML settings reset successfully!!!");
				}

				if (BooleanUtils.toBoolean(submitted)) {
					MoPluginHandler.saveSPConfiguration(spBaseUrl, spEntityId);
					addActionMessage(getText("samlsso.success.config"));
				}else if (BooleanUtils.toBoolean(this.certificatesSubmitted)) {
					MoPluginHandler.saveSPCertificates(this.signing, this.encryption);
					addActionMessage(getText("samlsso.success.config"));
				}
				certificateInfo = MoSAMLUtils.getCertificateInfo(settings.getPublicSPCertificate());
				if(certificateInfo!=null){
					LOGGER.debug("Certificate Info: "+certificateInfo.toString());
				}

				initializeSAMLConfig();
				return "success";
			} catch (PluginException e) {
				e.printStackTrace();
				addActionError(e.getMessage());
				return "input";
			} catch (Exception e) {
				e.printStackTrace();
				addActionError("An error occurred while saving your details. Please check logs for more info.");
				return "input";
			}
		} else {
			response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
			return null;
		}

	}

	private URI getUri(HttpServletRequest request) {
		StringBuffer builder = request.getRequestURL();
		if (request.getQueryString() != null) {
			builder.append("?");
			builder.append(request.getQueryString());
		}
		return URI.create(builder.toString());
	}
	
	public boolean isJCEEnable() {
		try {
			if (Cipher.getMaxAllowedKeyLength("SHA256") != Integer.MAX_VALUE) {
				return false;
			} else
				return true;
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("No Such Algorithm Exception" + e);
			return false;
		}

	}


	private void initializeSAMLConfig() {
		this.spBaseUrl = settings.getSpBaseUrl();
		this.spEntityId = settings.getSpEntityId();
		this.signing = settings.getSigning();
		this.encryption = settings.getEncryption();
		this.customOrganizationName = settings.getOrganizationName();
		this.customOrganizationDisplayName = settings.getOrganizationDisplayName();
		this.customOrganizationUrl = settings.getOrganizationUrl();
		this.technicalContactName = settings.getTechnicalContactName();
		this.technicalContactEmail = settings.getTechnicalContactEmail();
		this.supportContactName = settings.getSupportContactName();
		this.supportContactEmail = settings.getSupportContactEmail();
		this.isJCEInstalled = isJCEEnable();
	}

	public ArrayList<String> getIdpGuides() {
		ArrayList<String> idp = new ArrayList<>();
		idp.add("ADFS");
		idp.add("GoogleApps");
		idp.add("Centrify");
		idp.add("Okta");
		idp.add("OneLogin");
		idp.add("Salesforce");
		idp.add("JBoss Keycloak");
		idp.add("Oracle(OEM)");
		idp.add("Bitium");
		idp.add("PingFederate");
		idp.add("Ping One");
		idp.add("WSO2");
		idp.add("OpenAM");
		idp.add("miniOrange");
		idp.add("Simplesaml");
		idp.add("Azure AD");
		idp.add("Shibboleth 2");
		idp.add("Shibboleth 3");
		idp.add("RSA SecurID");
		idp.add("AuthAnvil");
		idp.add("Auth0");
		idp.add("CA Identity");
		idp.add("AWS");
		Collections.sort(idp);
		this.idpGuides = idp;
		return this.idpGuides;
	}

	public void setCertificates(String privateCertificate,String publicCertificate){
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("action", "setCert"));
		postParameters.add(new BasicNameValuePair("privateCertificate", privateCertificate));
		postParameters.add(new BasicNameValuePair("publicCertificate", publicCertificate));
		MoHttpUtils.sendPostRequest(settings.getBaseUrl().concat("/plugins/servlet/saml/mogencert"), postParameters,
				"application/x-www-form-urlencoded", null);
	}

	public void setSPCertExpirayDate(String validTo){
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("action", "setExpirayDate"));
		postParameters.add(new BasicNameValuePair("validTo", validTo));
		MoHttpUtils.sendPostRequest(settings.getBaseUrl().concat("/plugins/servlet/saml/mogencert"), postParameters,
				"application/x-www-form-urlencoded", null);
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

	public ArrayList<String> getIdpList(){
		return settings.getIdPList();
	}

	public HashMap<String, String> getIdpMap(){
		return settings.getIdpMap();
	}

	public String getSpBaseUrl() {
		return spBaseUrl;
	}

	public void setSpBaseUrl(String spBaseUrl) {
		this.spBaseUrl = spBaseUrl;
	}

	public String getSpEntityId() {
		return spEntityId;
	}

	public void setSpEntityId(String spEntityId) {
		this.spEntityId = spEntityId;
	}

	
	public Boolean getSubmitted() {
		return submitted;
	}

	public void setSubmitted(Boolean submitted) {
		this.submitted = submitted;
	}

	public MoSAMLSettings getSettings() {
		return settings;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	public BambooUserManager getBambooUserManager() {
		return bambooUserManager;
	}

	public void setBambooUserManager(BambooUserManager bambooUserManager) {
		this.bambooUserManager = bambooUserManager;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	public Boolean getSigning() {
		return signing;
	}

	public void setSigning(Boolean signing) {
		this.signing = signing;
	}

	public Boolean getEncryption() {
		return encryption;
	}

	public void setEncryption(Boolean encryption) {
		this.encryption = encryption;
	}

	public Boolean getCertificatesSubmitted() {
		return certificatesSubmitted;
	}

	public void setCertificatesSubmitted(Boolean certificatesSubmitted) {
		this.certificatesSubmitted = certificatesSubmitted;
	}

	public Boolean getIsJCEInstalled() {
		return isJCEInstalled;
	}

	public void setIsJCEInstalled(Boolean isJCEInstalled) {
		this.isJCEInstalled = isJCEInstalled;
	}

	public Map<String, String> getCertificateInfo() {
		return MoSAMLUtils.getCertificateInfo(settings.getPublicSPCertificate());
	}

	public void setCertificateInfo(Map<String, String> certificateInfo) {
		this.certificateInfo = certificateInfo;
	}
	public String getCustomOrganizationName() {
		return customOrganizationName;
	}

	public void setCustomOrganizationName(String customOrganizationName) {
		this.customOrganizationName = customOrganizationName;
	}

	public String getCustomOrganizationDisplayName() {
		return customOrganizationDisplayName;
	}

	public void setCustomOrganizationDisplayName(String customOrganizationDisplayName) {
		this.customOrganizationDisplayName = customOrganizationDisplayName;
	}

	public String getCustomOrganizationUrl() {
		return customOrganizationUrl;
	}

	public void setCustomOrganizationUrl(String customOrganizationUrl) {
		this.customOrganizationUrl = customOrganizationUrl;
	}

	public String getTechnicalContactName() {
		return technicalContactName;
	}

	public void setTechnicalContactName(String technicalContactName) {
		this.technicalContactName = technicalContactName;
	}

	public String getTechnicalContactEmail() {
		return technicalContactEmail;
	}

	public void setTechnicalContactEmail(String technicalContactEmail) {
		this.technicalContactEmail = technicalContactEmail;
	}

	public String getSupportContactName() {
		return supportContactName;
	}

	public void setSupportContactName(String supportContactName) {
		this.supportContactName = supportContactName;
	}

	public String getSupportContactEmail() {
		return supportContactEmail;
	}

	public void setSupportContactEmail(String supportContactEmail) {
		this.supportContactEmail = supportContactEmail;
	}
}
