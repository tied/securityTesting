package com.miniorange.oauth.bamboo.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang3.StringUtils;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.bamboo.MoOAuthPluginHandler;
import com.miniorange.oauth.bamboo.MoOAuthSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.apache.struts2.ServletActionContext;

public class MoOAuthAttributeMappingAction extends BambooActionSupport {

    private static final Log LOGGER = LogFactory.getLog(MoOAuthAttributeMappingAction.class);

    private MoOAuthSettings settings;
    
    private PluginLicenseManager pluginLicenseManager;
    private Boolean attributeMappingSubmitted;
    private String usernameAttribute;
	private Boolean regexPatternEnabled;
	private String regexPattern;
    private String emailAttribute;
    private String fullNameAttribute = StringUtils.EMPTY;
    private String firstNameAttribute = StringUtils.EMPTY;
    private String lastNameAttribute = StringUtils.EMPTY;
    private Boolean useSeparateNameAttributes;
    private Boolean keepExistingUserAttributes;
    private String loginUserAttribute;
	private UserManager userManager;
	private LoginUriProvider loginUriProvider;
	private String xsrfToken;
	private ArrayList userLoginOptions;

    
    public MoOAuthAttributeMappingAction(MoOAuthSettings settings, 
    		PluginLicenseManager pluginLicenseManager, UserManager userManager, LoginUriProvider loginUriProvider){
        this.settings = settings;
        this.pluginLicenseManager=pluginLicenseManager;
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
    }
    
	@Override
	public String execute() throws Exception {
		LOGGER.info("AttributeMappingConfigurationAction doExecute called");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		final UserProfile user = userManager.getRemoteUser();

		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				if (!settings.isLicenseValid()) {
					try {
						response.sendRedirect(settings.getManageAddOnURL());
						return null;
					} catch (IOException e) {
						e.printStackTrace();

					}
				}
				LOGGER.debug(" before attributeMappingSubmitted... " + this.attributeMappingSubmitted);
				xsrfToken = XsrfTokenUtils.getXsrfToken(request);
				if (BooleanUtils.toBoolean(attributeMappingSubmitted)) {
					Boolean error = Validation(usernameAttribute,regexPatternEnabled, regexPattern);
					if(error == false) {
						MoOAuthPluginHandler.saveAttributeMapping(usernameAttribute, emailAttribute, regexPattern,
								regexPatternEnabled, fullNameAttribute, useSeparateNameAttributes, firstNameAttribute, lastNameAttribute, keepExistingUserAttributes, loginUserAttribute);
						LOGGER.info("attributeMapping saved");
						addActionMessage(getText("oauth.success.config"));
					}
				}
				initializeOAuthConfig();
				return "success";
			} catch (MoOAuthPluginException e) {
				e.printStackTrace();
				addActionError(e.getMessage());
				return "input";
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.debug("error is : " + e);
				addActionError("An error occurred.");
				return "input";
			}
		}else{
			response.sendRedirect(loginUriProvider.getLoginUri(MoOAuthPluginHandler.getUri(request)).toASCIIString());
			return null;
		}
	}
	
	private void initializeOAuthConfig() {
		LOGGER.info("initializeOAuthConfig called... ");
		this.usernameAttribute = settings.getUsernameAttribute();
		this.emailAttribute = settings.getEmailAttribute();
		this.regexPattern = settings.getRegexPattern();
		this.regexPatternEnabled = settings.getRegexPatternEnabled();
		this.fullNameAttribute = settings.getFullNameAttribute();
		this.useSeparateNameAttributes = settings.getUseSeparateNameAttributes();
		this.firstNameAttribute = settings.getFirstNameAttribute();
		this.lastNameAttribute = settings.getLastNameAttribute();
		this.keepExistingUserAttributes = settings.getKeepExistingUserAttributes();
		this.loginUserAttribute = settings.getLoginUserAttribute();
	}



	private Boolean Validation(String usernameAttribute,Boolean regexPatternEnabled, String regexPattern) {
		LOGGER.info("Attribute Mapping Configuration Action doValidate");
		Boolean error = false;
		if (!BooleanUtils.toBoolean(this.attributeMappingSubmitted)) {
			error = true;
		}
		if (BooleanUtils.toBoolean(regexPatternEnabled)) {
			LOGGER.info("Validating Regular Expression.........");
			if (StringUtils.isBlank(usernameAttribute)) {
				addActionError("Enter Username for Regex Pattern");
				LOGGER.error("Username attribute is empty, please enter the value");
				error = true;
			}
			if (StringUtils.isBlank(regexPattern)) {
				error = true;
				addActionError("Please Enter Regular Expression");
			}
			try {
				Pattern.compile(regexPattern);
			} catch (PatternSyntaxException exception) {
				error = true;
				addActionError("Regular expression is not valid");
			}
		}
		if (BooleanUtils.toBoolean(error)) {
			initializeOAuthConfig();
		}
		return error;
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

	public Boolean getAttributeMappingSubmitted() {
		return attributeMappingSubmitted;
	}

	public void setAttributeMappingSubmitted(Boolean attributeMappingSubmitted) {
		this.attributeMappingSubmitted = attributeMappingSubmitted;
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

	public String getLoginUserAttribute() {
		return loginUserAttribute;
	}

	public void setLoginUserAttribute(String loginUserAttribute) {
		this.loginUserAttribute = loginUserAttribute;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
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
}