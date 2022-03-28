package com.miniorange.sso.saml.bamboo.action;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import com.miniorange.sso.saml.dto.MoIDPMetadata;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.struts2.ServletActionContext;
import org.opensaml.saml2.core.NameIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.ctc.wstx.util.StringUtil;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSAMLUtils;

public class MoAddIDPConfigurationAction extends BambooActionSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(MoAddIDPConfigurationAction.class);

	private UserManager userManager;
	private LoginUriProvider loginUriProvider;
	private MoSAMLSettings settings;

	private String idpEntityId;
	private String ssoUrl;
	private String ssoBindingType;
	private String sloUrl;
	private String sloBindingType;
	private String nameIdFormat;
	private String x509Certificate;
	private List<String> certificates;
	private String[] x509AllCertificates;
	private Boolean signedRequest;
	private List<String> authnContextClasses;
	private String authnContextClass;
	private String otherAuthnContextClass;
	private String xsrfToken;
	private Boolean submitted;

	private ArrayList<String> metadataOptions;
	private String metadataOption;
	private String inputUrl;
	private Boolean refreshMetadata;
	private String refreshInterval;
	private Integer customRefreshInterval;
	private String customRefreshIntervalUnit;
	private Boolean shouldContainsAddIdp;
	private Boolean shouldShowBackButton;

	private String idpID;
	private String idpName;
	private List<String> idpList;

	private String acsUrl;

	private Boolean addIdpSubmitted;
	private Boolean metadataImported;
	private Boolean fileSubmitted;
	private Boolean urlSubmitted;
	private MoIDPConfig idpConfig;
	private List<String> nameIdFormats;
	private Boolean enableSsoForIdp;

	public MoAddIDPConfigurationAction(UserManager userManager, LoginUriProvider loginUriProvider,
			MoSAMLSettings settings) {
		super();
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.settings = settings;
	}

	public Boolean doValidate() {
		LOGGER.debug("Add IDP Action: validate() called");
		Boolean error = false;
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		if (!BooleanUtils.toBoolean(addIdpSubmitted)) {
			error = true;
		}
		if (BooleanUtils.toBoolean(this.addIdpSubmitted)) {

			if (StringUtils.isBlank(this.idpName)) {
				addActionError(getText("samlsso.error.config.idpName.empty"));
				error = true;
			}
			if (StringUtils.isBlank(this.ssoUrl)) {
				addActionError(getText("samlsso.error.config.ssourl.empty"));
				error = true;
			} else {
				try {
					new URL(this.ssoUrl);
				} catch (MalformedURLException e) {
					addActionError(getText("samlsso.error.config.ssourl.malformed"));
					error = true;
				}
			}
			if (StringUtils.isNotBlank(this.sloUrl)) {
				try {
					new URL(this.sloUrl);
				} catch (MalformedURLException e) {
					addActionError(getText("samlsso.error.config.slourl.malformed"));
					error = true;
				}
			}
			if (StringUtils.isBlank(this.idpEntityId)) {
				addActionError(getText("samlsso.error.config.idpentityid.empty"));
				error = true;
			}
			if (x509AllCertificates != null) {
				if (x509AllCertificates.length == 1) {
					if (StringUtils.isBlank(x509AllCertificates[0])) {
						addActionError("IDP Signing Certificate cannot be left blank.");
						error = true;
					} else if (!MoSAMLUtils.isValidPublicCertificate(x509AllCertificates[0])) {
						addActionError("Invalid IDP Signing Certificate.");
						error = true;
					}
				} else {
					int isBlankCertificate = 0;
					for (String certificate : x509AllCertificates) {
						if (!MoSAMLUtils.isValidPublicCertificate(certificate).booleanValue()
								&& !StringUtils.isBlank(certificate)) {
							addActionError("Invalid IDP Signing Certificate "
									+ ((Arrays.asList(x509AllCertificates).indexOf(certificate) + 1)));
							error = true;
						}
						if (StringUtils.isBlank(certificate)) {
							isBlankCertificate++;
						}
					}
					if (isBlankCertificate == x509AllCertificates.length) {
						addActionError("Signing Certificate cannot be left blank.");
						error = true;
					}
				}
			} else {
				if (StringUtils.isBlank(this.x509Certificate)) {
					addActionError("Signing Certificate cannot be left blank.");
					error = true;
				} else if (!MoSAMLUtils.isValidPublicCertificate(this.x509Certificate).booleanValue()) {
					addActionError("Invalid IDP Signing Certificate.");
					error = true;
				}
			}

			if ((!StringUtils.equals(this.ssoBindingType, "HttpRedirect"))
					&& (!StringUtils.equals(this.ssoBindingType, "HttpPost"))) {
				addActionError(getText("samlsso.error.config.ssobinding.invalid"));
				error = true;
			}
			if ((!StringUtils.equals(this.sloBindingType, "HttpRedirect"))
					&& (!StringUtils.equals(this.sloBindingType, "HttpPost"))) {
				addActionError(getText("samlsso.error.config.slobinding.invalid"));
				error = true;
			}
		}

		if (super.hasActionErrors()) {
			xsrfToken = XsrfTokenUtils.getXsrfToken(request);
			initializeSAMLConfig();
		}

		return error;
	}

	@Override
	public String execute() throws Exception {
		LOGGER.debug("Add IDP Action execute() called");

		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);
		shouldShowBackButton = false;

		final UserProfile user = userManager.getRemoteUser();

		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				LOGGER.debug("Add IdP Action execute submitted: " + this.submitted);
				if (!settings.isValidLicense()) {
					LOGGER.debug("Invalid License");
					return "upm";
				}

				idpList = getIdpList();

				if(StringUtils.isBlank(this.idpID)){
					if(StringUtils.isNotBlank(request.getParameter("idpid"))){
						this.idpID = request.getParameter("idpid");
					} else {
						this.idpID = UUID.randomUUID().toString();
						if(StringUtils.isNotBlank(settings.getSsoServiceUrl())){
							LOGGER.debug("Storing old IDP config values and initializing idp: "+this.idpID);
							this.idpName = "IDP";
							MoPluginHandler.replaceOldSettingsWithNew(this.idpID,this.idpName);
						}
					}
				}

				if (BooleanUtils.toBoolean(this.addIdpSubmitted)) {
					LOGGER.debug("add IDP Submitted..");
					Boolean error;


					error = doValidate();
					if (!error) {
						List<String> x509allcertificates = new ArrayList<String>();
						if (x509Certificate != null) {
							x509allcertificates.add(x509Certificate);
						}
						if (x509AllCertificates != null) {
							x509allcertificates = new ArrayList<String>(Arrays.asList(x509AllCertificates));
							x509allcertificates.removeAll(Arrays.asList(null, ""));
							if (StringUtils.isBlank(this.x509Certificate)) {
								this.x509Certificate = x509allcertificates.get(0);
							}
						}


						MoPluginHandler.saveIdPConfiguration(this.idpID, this.idpName, this.idpEntityId, this.ssoBindingType, this.ssoUrl,
								this.sloBindingType, this.sloUrl, x509allcertificates, this.x509Certificate, this.signedRequest,
								this.nameIdFormat, this.enableSsoForIdp);
						addActionMessage(getText("samlsso.success.config"));
						initializeSAMLConfig();
						return SUCCESS;
					}
				}

				LOGGER.debug("request.getParameter(\"operation\") = "+request.getParameter("operation"));
				if (StringUtils.equalsIgnoreCase("add", request.getParameter("operation")) || idpList.size() > 1) {
					shouldShowBackButton = Boolean.TRUE;
				}

				if (MoSAMLSettings.isMetadataUpload) {
					MoSAMLSettings.isMetadataUpload = Boolean.FALSE;
					if (request.getSession().getAttribute("success") != null
							|| request.getSession().getAttribute("error") != null) {
						if (request.getSession().getAttribute("success") != null) {
							addActionMessage(getText("samlsso.success.config"));
							request.getSession().removeAttribute("success");
						} else if (request.getSession().getAttribute("error") != null) {
							addActionError(request.getSession().getAttribute("error").toString());
							request.getSession().removeAttribute("error");
						}
					}
					request.getSession().removeAttribute("error");
					request.getSession().removeAttribute("success");
				}
				initializeSAMLConfig();
				return "success";

			} catch (MoPluginException e) {
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

	private void initializeSAMLConfig() {
		this.acsUrl = settings.getLoginServletUrl()+"?idp="+this.idpID;

		MoIDPConfig idpConfig = MoPluginHandler.constructIdpConfigObject(this.idpID);
		if(idpConfig == null){
			initializeNewForm();
			return;
		}

		this.idpName = StringUtils.defaultIfBlank(idpConfig.getIdpName(), StringUtils.EMPTY);
		this.idpEntityId = StringUtils.defaultIfBlank(idpConfig.getIdpEntityId(), StringUtils.EMPTY);
		this.ssoUrl = StringUtils.defaultIfBlank(idpConfig.getSsoUrl(), StringUtils.EMPTY);
		this.sloUrl = StringUtils.defaultIfBlank(idpConfig.getSloUrl(), StringUtils.EMPTY);
		this.ssoBindingType = StringUtils.defaultIfBlank(idpConfig.getSsoBindingType(), "HttpRedirect");
		this.sloBindingType = StringUtils.defaultIfBlank(idpConfig.getSloBindingType(), "HttpRedirect");
		this.certificates = idpConfig.getCertificates();
		this.x509Certificate = StringUtils.defaultIfBlank(idpConfig.getX509Certificate(), StringUtils.EMPTY);
		this.signedRequest = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getSignedRequest(), true);
		this.refreshMetadata = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getRefreshMetadata(), false);
		this.refreshInterval = StringUtils.defaultIfBlank(idpConfig.getRefreshInterval(), "hourly");
		this.inputUrl = StringUtils.defaultIfBlank(idpConfig.getInputUrl(), StringUtils.EMPTY);
		this.customRefreshInterval = idpConfig.getCustomRefreshInterval() != null ? idpConfig.getCustomRefreshInterval() : 60;
		this.customRefreshIntervalUnit = StringUtils.defaultIfBlank(idpConfig.getCustomRefreshIntervalUnit(),
				"minutes");
		this.metadataOption = StringUtils.defaultIfBlank(idpConfig.getMetadataOption(), StringUtils.EMPTY);
		this.nameIdFormat = StringUtils.defaultIfBlank(idpConfig.getNameIdFormat(),"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
		this.shouldContainsAddIdp = true;
	}

	private void initializeNewForm() {
		this.acsUrl = settings.getLoginServletUrl()+"?idp="+this.idpID;
		this.idpName = StringUtils.EMPTY;
		this.idpEntityId = StringUtils.EMPTY;
		this.ssoUrl = StringUtils.EMPTY;
		this.sloUrl = StringUtils.EMPTY;
		this.ssoBindingType = "HttpRedirect";
		this.sloBindingType = "HttpRedirect";
		this.certificates = new ArrayList<>();
		this.x509Certificate = StringUtils.EMPTY;
		this.inputUrl = StringUtils.EMPTY;
		this.signedRequest = Boolean.TRUE;
		this.refreshMetadata = Boolean.FALSE;
		this.refreshInterval = "hourly";
		this.customRefreshInterval = 60;
		this.customRefreshIntervalUnit = "minutes";
		this.metadataOption = StringUtils.EMPTY;
		this.shouldContainsAddIdp = false;
		this.nameIdFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
	}

	private URI getUri(HttpServletRequest request) {
		StringBuffer builder = request.getRequestURL();
		if (request.getQueryString() != null) {
			builder.append("?");
			builder.append(request.getQueryString());
		}
		return URI.create(builder.toString());
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

	public MoSAMLSettings getSettings() {
		return settings;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
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

	public String[] getX509AllCertificates() {
		return x509AllCertificates;
	}

	public void setX509AllCertificates(String[] x509AllCertificates) {
		this.x509AllCertificates = x509AllCertificates;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	public Boolean getSubmitted() {
		return submitted;
	}

	public void setSubmitted(Boolean submitted) {
		this.submitted = submitted;
	}

	public Boolean getSignedRequest() {
		return signedRequest;
	}

	public void setSignedRequest(Boolean signedRequest) {
		this.signedRequest = signedRequest;
	}
	public String getNameIdFormat() { return nameIdFormat; }

	public void setNameIdFormat(String nameIdFormat) { this.nameIdFormat = nameIdFormat; }


	public ArrayList<String> getMetadataOptions() {
		ArrayList<String> idp = new ArrayList<String>();
		idp.add("ADFS");
		idp.add("Azure AD");
		idp.add("Okta");
		idp.add("OneLogin");
		idp.add("Google G Suite");
		Collections.sort(idp);
		this.metadataOptions = idp;
		return metadataOptions;
	}

	public ArrayList<String> getNameIdFormats(){
		ArrayList<String> formats = new ArrayList<String>();
		formats.add(NameIDType.UNSPECIFIED);
		formats.add(NameIDType.EMAIL);
		formats.add(NameIDType.PERSISTENT);
		formats.add(NameIDType.TRANSIENT);
		return formats;
	}

	public void setMetadataOptions(ArrayList<String> metadataOptions) {
		this.metadataOptions = metadataOptions;
	}

	public String getMetadataOption() {
		return metadataOption;
	}

	public void setMetadataOption(String metadataOption) {
		this.metadataOption = metadataOption;
	}

	public String getInputUrl() {
		return inputUrl;
	}

	public void setInputUrl(String inputUrl) {
		this.inputUrl = inputUrl;
	}

	public void setAuthnContextClass(String authnContextClass){
		this.authnContextClass = authnContextClass;
	}

	public String getAuthnContextClass(){
		return this.authnContextClass;
	}

	public void setOtherAuthnContextClass(String otherAuthnContextClass){
		this.otherAuthnContextClass = otherAuthnContextClass;
	}

	public String getOtherAuthnContextClass(){
		return this.otherAuthnContextClass;
	}

	public void setAuthnContextClasses(List<String> authnContextClasses) {
		this.authnContextClasses = authnContextClasses;
	}

	public Boolean getAddIdpSubmitted() {
		return addIdpSubmitted;
	}

	public void setAddIdpSubmitted(Boolean addIdpSubmitted) {
		this.addIdpSubmitted = addIdpSubmitted;
	}

	public Boolean getMetadataImported() {
		return metadataImported;
	}

	public void setMetadataImported(Boolean metadataImported) {
		this.metadataImported = metadataImported;
	}

	public Boolean getFileSubmitted() {
		return fileSubmitted;
	}

	public void setFileSubmitted(Boolean fileSubmitted) {
		this.fileSubmitted = fileSubmitted;
	}

	public Boolean getUrlSubmitted() {
		return urlSubmitted;
	}

	public void setUrlSubmitted(Boolean urlSubmitted) {
		this.urlSubmitted = urlSubmitted;
	}

	public MoIDPConfig getIdpConfig() {
		return idpConfig;
	}

	public void setIdpConfig(MoIDPConfig idpConfig) {
		this.idpConfig = idpConfig;
	}

	public void setNameIdFormats(List<String> nameIdFormats) {
		this.nameIdFormats = nameIdFormats;
	}

	public Boolean getEnableSsoForIdp() {
		return enableSsoForIdp;
	}

	public void setEnableSsoForIdp(Boolean enableSsoForIdp) {
		this.enableSsoForIdp = enableSsoForIdp;
	}

	public Boolean getRefreshMetadata() {
		return refreshMetadata;
	}

	public void setRefreshMetadata(Boolean refreshMetadata) {
		this.refreshMetadata = refreshMetadata;
	}

	public String getRefreshInterval() {
		return refreshInterval;
	}

	public void setRefreshInterval(String refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	public Integer getCustomRefreshInterval() {
		return customRefreshInterval;
	}

	public void setCustomRefreshInterval(Integer customRefreshInterval) {
		this.customRefreshInterval = customRefreshInterval;
	}

	public String getCustomRefreshIntervalUnit() {
		return customRefreshIntervalUnit;
	}

	public void setCustomRefreshIntervalUnit(String customRefreshIntervalUnit) {
		this.customRefreshIntervalUnit = customRefreshIntervalUnit;
	}


	public String getIdpID() {
		return idpID;
	}

	public void setIdpID(String idpID) {
		this.idpID = idpID;
	}

	public String getIdpName() {
		return idpName;
	}

	public void setIdpName(String idpName) {
		this.idpName = idpName;
	}

	public Boolean getShouldContainsAddIdp() {
		return shouldContainsAddIdp;
	}

	public void setShouldContainsAddIdp(Boolean shouldContainsAddIdp) {
		this.shouldContainsAddIdp = shouldContainsAddIdp;
	}

	public List<String> getIdpList() {
		List<String> idpList = settings.getIdPList();
		this.idpList = idpList;
		return this.idpList;
	}

	public void setIdpList(List<String> idpList) {
		this.idpList = idpList;
	}

	public String getAcsUrl() {
		return acsUrl;
	}

	public void setAcsUrl(String acsUrl) {
		this.acsUrl = acsUrl;
	}

	public Boolean getShouldShowBackButton() {
		return shouldShowBackButton;
	}

	public void setShouldShowBackButton(Boolean shouldShowBackButton) {
		this.shouldShowBackButton = shouldShowBackButton;
	}
}
