package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.plugin.PluginException;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.dto.MoIDPConfig;
//import com.sun.org.slf4j.internal.LoggerFactory;
//import com.sun.tools.javac.util.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MoAttributeMappingAction extends BambooActionSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(MoAttributeMappingAction.class);

	private UserManager userManager;
	private LoginUriProvider loginUriProvider;
	private MoSAMLSettings settings;

	private String usernameAttribute;
	private String emailAttribute;
	private String fullNameAttribute;
	private Boolean useSeparateNameAttributes;
	private Boolean keepExistingUserAttributes;
	private String firstNameAttribute;
	private String lastNameAttribute;
	private Boolean regexPatternEnabled;
	private String regexPattern;
	private Boolean attributeMappingSubmitted;
	private String loginUserAttribute;
	private ArrayList userLoginOptions;

	private String xsrfToken;
	private Boolean amIdpChanged;

	private String idpID;
	private String idpName;
	private HashMap<String,String> idpMap;

	private Boolean enableButtons;
	private Boolean testConfigPerformed;

	public MoAttributeMappingAction(UserManager userManager, LoginUriProvider loginUriProvider, MoSAMLSettings settings) {
		super();
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.settings = settings;
	}

	public void validate() {
		LOGGER.debug("AttributeMapping Action validate() called");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		if (!BooleanUtils.toBoolean(attributeMappingSubmitted)) {
			return;
		}
		if (BooleanUtils.toBoolean(regexPatternEnabled)) {

			if (StringUtils.isBlank(regexPattern)) {
				addActionError("Regex pattern can not be left blank.");
			}
			try	{
				Pattern.compile(regexPattern);
			} catch (PatternSyntaxException exception) {
				addActionError("Regular expression is not valid");
			}
		}
		if (super.hasActionErrors()) {
			xsrfToken = XsrfTokenUtils.getXsrfToken(request);
			initializeSAMLConfig();
		}
	}

	@Override
	public String execute() throws Exception {
		LOGGER.debug("AttributeMapping Action execute() called");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);

		final UserProfile user = userManager.getRemoteUser();

		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				LOGGER.debug("AttributeMapping Action execute Submitted: " + this.attributeMappingSubmitted);
				if (!settings.isValidLicense()) {
					LOGGER.debug("Invalide License");
					return "upm";
				}
				LOGGER.debug("User Login  option = "+loginUserAttribute);

				if (StringUtils.isNotBlank(request.getParameter("idpid"))) {
					this.idpID = request.getParameter("idpid");
				}

				if (BooleanUtils.toBoolean(this.attributeMappingSubmitted)) {
					if(StringUtils.isBlank(idpID)){
						idpID = MoPluginConstants.DEFAULT_IDP_ID;
					}

					String userNameAttr = StringUtils.split(this.usernameAttribute," ")[0];
					String userEmailAttr = StringUtils.split(this.emailAttribute," ")[0];
					String userFullNameAttr = StringUtils.EMPTY;
					if (StringUtils.isNotEmpty(this.fullNameAttribute)){
						userFullNameAttr =  StringUtils.split(this.fullNameAttribute," ")[0];
					}
					String userFirstNameAttr = StringUtils.EMPTY;
					if (StringUtils.isNotEmpty(this.firstNameAttribute)){
						userFirstNameAttr =  StringUtils.split(this.firstNameAttribute," ")[0];
					}
					String userLastNameAttr = StringUtils.EMPTY;
					if (StringUtils.isNotEmpty(this.lastNameAttribute)){
						userLastNameAttr =  StringUtils.split(this.lastNameAttribute," ")[0];
					}

					MoPluginHandler.saveAttributeMapping(idpID, setDefaultToNameID(userNameAttr), setDefaultToNameID(userEmailAttr), setDefaultToEmpty(userFullNameAttr),
							useSeparateNameAttributes,setDefaultToEmpty( userFirstNameAttr),setDefaultToEmpty( userLastNameAttr),keepExistingUserAttributes,
							regexPattern, regexPatternEnabled,loginUserAttribute);
					addActionMessage(getText("samlsso.success.config"));
				}

				if(StringUtils.isBlank(this.idpID)){
					if(!(settings.getIdpMap().isEmpty())){
						this.idpID = settings.getIdPList().get(0);
					}
					else{
						if(StringUtils.isNotBlank(settings.getSsoServiceUrl())){
							this.idpID = UUID.randomUUID().toString();
							this.idpName = "IDP";
							MoPluginHandler.replaceOldSettingsWithNew(this.idpID,this.idpName);
						} else {
							initializeNewForm();
							return "success";
						}
					}
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

	private void initializeNewForm() {
		this.idpName = "";
		this.usernameAttribute = "NameID";
		this.emailAttribute = "NameID";
		this.fullNameAttribute = "";
		this.useSeparateNameAttributes = false;
		this.firstNameAttribute = "";
		this.lastNameAttribute = "";
		this.keepExistingUserAttributes = false;
		this.regexPattern = "";
		this.regexPatternEnabled = false;
		this.loginUserAttribute = "username";
		this.enableButtons = false;
		this.testConfigPerformed = settings.checkIfTestConfig(this.idpID);
	}

	private void initializeSAMLConfig() {
		MoIDPConfig idpConfig = MoPluginHandler.constructIdpConfigObject(this.idpID);

		if(idpConfig == null){
			initializeNewForm();
			return;
		}
		this.idpName = idpConfig.getIdpName();
		this.usernameAttribute = idpConfig.getUsernameAttribute();
		this.emailAttribute = idpConfig.getEmailAttribute();
		this.fullNameAttribute = idpConfig.getFullNameAttribute();
		this.useSeparateNameAttributes = idpConfig.getUseSeparateNameAttributes();
		this.firstNameAttribute = idpConfig.getFirstNameAttribute();
		this.lastNameAttribute = idpConfig.getLastNameAttribute();
		this.keepExistingUserAttributes = idpConfig.getKeepExistingUserAttributes();
		this.regexPattern = idpConfig.getRegexPattern();
		this.regexPatternEnabled = idpConfig.getRegexPatternEnabled();
		this.loginUserAttribute = idpConfig.getLoginUserAttribute();
		this.enableButtons = true;
		this.testConfigPerformed = settings.checkIfTestConfig(this.idpID);
	}

	private String setDefaultToNameID(String attributeName) {

		if(StringUtils.equalsIgnoreCase(attributeName,"--Select--") ){
			return "NameID";
		}
		return attributeName;
	}

	private String setDefaultToEmpty(String attributeName) {
		if(StringUtils.equalsIgnoreCase(attributeName,"--Select--") ){
			return StringUtils.EMPTY;
		}
		return attributeName;
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

	public Boolean getKeepExistingUserAttributes() {
		return keepExistingUserAttributes;
	}

	public void setKeepExistingUserAttributes(Boolean keepExistingUserAttributes) {
		this.keepExistingUserAttributes = keepExistingUserAttributes;
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

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	public Boolean getAttributeMappingSubmitted() {
		return attributeMappingSubmitted;
	}

	public void setAttributeMappingSubmitted(Boolean attributeMappingSubmitted) {
		this.attributeMappingSubmitted = attributeMappingSubmitted;
	}

	public Boolean getRegexPatternEnabled() {
		return regexPatternEnabled;
	}

	public void setRegexPatternEnabled(Boolean regexPatternEnabled) {
		this.regexPatternEnabled = regexPatternEnabled;
	}

	public String getRegexPattern() {
		return regexPattern;
	}

	public void setRegexPattern(String regexPattern) {
		this.regexPattern = regexPattern;
	}

	public String getLoginUserAttribute() {
		return loginUserAttribute;
	}

	public void setLoginUserAttribute(String loginUserAttribute) {
		this.loginUserAttribute = loginUserAttribute;
	}

	public ArrayList getUserLoginOptions() {
		this.userLoginOptions = new ArrayList<String>();
		this.userLoginOptions.add("username");
		this.userLoginOptions.add("email");
		return this.userLoginOptions;
	}

	public void setUserLoginOptions(ArrayList userLoginOptions) {
		this.userLoginOptions = userLoginOptions;
	}

	public Boolean getAmIdpChanged() {
		return amIdpChanged;
	}

	public void setAmIdpChanged(Boolean amIdpChanged) {
		this.amIdpChanged = amIdpChanged;
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

	public HashMap<String, String> getIdpMap() {
		idpMap = settings.getIdpMap();
		return idpMap;
	}

	public void setIdpMap(HashMap<String, String> idpMap) {
		this.idpMap = idpMap;
	}

	public Boolean getEnableButtons() {
		return enableButtons;
	}

	public void setEnableButtons(Boolean enableButtons) {
		this.enableButtons = enableButtons;
	}

	public Boolean getTestConfigPerformed() {
		return testConfigPerformed;
	}

	public void setTestConfigPerformed(Boolean testConfigPerformed) {
		this.testConfigPerformed = testConfigPerformed;
	}
}
