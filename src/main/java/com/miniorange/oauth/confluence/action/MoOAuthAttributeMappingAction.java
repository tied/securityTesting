package com.miniorange.oauth.confluence.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.atlassian.xwork.SimpleXsrfTokenGenerator;
import com.atlassian.xwork.XsrfTokenGenerator;
import com.opensymphony.webwork.ServletActionContext;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.miniorange.oauth.confluence.action.MoOAuthAttributeMappingAction;

import javax.servlet.http.HttpServletRequest;

public class MoOAuthAttributeMappingAction extends ConfluenceActionSupport {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(MoOAuthAttributeMappingAction.class);
	
	private MoOAuthSettings settings;
	private MoOAuthPluginHandler pluginHandler;
	private PluginLicenseManager pluginLicenseManager;
	
	private Boolean attributeMappingSubmitted;
	private String usernameAttribute;
    private String emailAttribute;
    private Boolean regexPatternEnabled;
	private String regexPattern;
	private String fullNameAttribute;
	private String firstNameAttribute;
	private String lastNameAttribute;
	private Boolean useSeparateNameAttributes;
	private Boolean keepExistingUserAttributes;	
	private String loginUserAttribute;
	private Map<String, String> customAttributeMapping;
	private ArrayList<String> extendedAttributesList = new ArrayList<>();
	private String xsrfToken;

	public MoOAuthAttributeMappingAction(MoOAuthSettings settings, MoOAuthPluginHandler pluginHandler,PluginLicenseManager pluginLicenseManager){
		this.settings = settings;
		this.pluginHandler = pluginHandler;
		this.pluginLicenseManager = pluginLicenseManager;
	}
	
	public Boolean validation() {
		LOGGER.debug("Attribute Mapping Configuration Action doValidate");
		Boolean error = false;
	    if(!BooleanUtils.toBoolean(this.attributeMappingSubmitted)) {
			error = true;
	    }
		if (StringUtils.isEmpty(usernameAttribute) && BooleanUtils.toBoolean(regexPatternEnabled)) {
			error = true;
			addActionError("Username cannot be empty.");
		}
		if (StringUtils.endsWithIgnoreCase(loginUserAttribute, "email")) {
			if (StringUtils.isEmpty(emailAttribute)) {
				error = true;
				addActionError("Email cannot be empty.");
			}
		}

	    if(BooleanUtils.toBoolean(regexPatternEnabled)){
	       	 LOGGER.info("Validating Regular Expression.........");
	       	 if(StringUtils.isBlank(regexPattern)){
	       		error = true;
	       		addActionError("Please add Regular Expresion");
	            }
	       	try{
	       		Pattern.compile(regexPattern);
	       		LOGGER.info("Regular pattern is validated");
	       	}catch (PatternSyntaxException exception) {
	       		error = true;
	       		LOGGER.error("Invalid Regular Expression");
	       		addActionError("Regular expression is not valid");
	           }
	    }

		if (BooleanUtils.toBoolean(error)) {
			initializeOAuthConfig();
		}
		return error;
	}
	
	public String execute() throws Exception {
		LOGGER.debug("AttributeMappingConfigurationAction doExecute");
        try{
			HttpServletRequest request = ServletActionContext.getRequest();
        	XsrfTokenGenerator tokenGenerator = new SimpleXsrfTokenGenerator();
        	xsrfToken = tokenGenerator.generateToken(request);

        	if(!settings.isLicenseValid()){
				return "invalid";
			}
            LOGGER.debug(" before attributeMappingSubmitted... " + this.attributeMappingSubmitted);
            if (BooleanUtils.toBoolean(attributeMappingSubmitted)) {  
            	LOGGER.debug("attributeMappingSubmitted... ");
            	LOGGER.debug("attribute mapping details, fullname : "+ fullNameAttribute);
            	LOGGER.debug("firstname : "+ firstNameAttribute);
            	LOGGER.debug("lastname : "+ lastNameAttribute);

				HashMap<String, String> customAttributeMapping = new HashMap<>();

				int totalNumberOfRoles = Integer.parseInt(request.getParameter("totalNumberOfRoles"));

				for (int i=0;i<totalNumberOfRoles;i++) {
					String key = request.getParameter("userAttrKey_" + i);
					String value = request.getParameter("userAttrValue_" + i);

					if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value) && !StringUtils.equalsIgnoreCase("select-attribute-to-map", key)) {
						if (!BooleanUtils.toBoolean(customAttributeMapping.containsKey(key.trim()))) {
							customAttributeMapping.put(key.trim(), value.trim());
						}
					}
				}



            	Boolean error = validation();
            	if (!error) {
					pluginHandler.saveAttributeMapping(usernameAttribute, emailAttribute, regexPattern, 
							regexPatternEnabled, fullNameAttribute, useSeparateNameAttributes, firstNameAttribute, 
							lastNameAttribute, keepExistingUserAttributes, loginUserAttribute, customAttributeMapping);
					LOGGER.debug("attributeMapping saved");
					addActionMessage(getText("oauth.success.config"), "success", true, null);
				}
            }
            initializeOAuthConfig();
            LOGGER.debug("initializeOAuthConfig called... ");
            return "success";
        } catch (MoOAuthPluginException e) {
            LOGGER.error(e.getMessage());
            addActionError(e.getMessage());
            return "input";
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.debug("error is : " + e);
            addActionError("An error occurred.");
            return "input";
        }
	}
	
	public void initializeOAuthConfig() {

		LOGGER.debug("initialize OAuth Configuration for attribute mapping");
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
		this.extendedAttributesList = settings.getExtendedAttributes();
		this.customAttributeMapping = new TreeMap<>(settings.getCustomAttributeMapping());
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

	public void setExtendedAttributesList(ArrayList<String> extendedAttributesList) {
		this.extendedAttributesList = extendedAttributesList;
	}


	public ArrayList<String> getExtendedAttributesList() {
		return extendedAttributesList;
	}

	/**
	 * @getter setters for custom user profile attribute
	 */
	public Map<String, String> getCustomAttributeMapping() {
		return customAttributeMapping;
	}

	public void setCustomAttributeMapping(Map<String, String> customAttributeMapping) {
		this.customAttributeMapping = customAttributeMapping;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}
}