package com.miniorange.oauth.confluence.action;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.atlassian.xwork.SimpleXsrfTokenGenerator;
import com.atlassian.xwork.XsrfTokenGenerator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.confluence.MoOAuthPluginConfigurationsHandler;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;

import javax.servlet.http.HttpServletRequest;

public class MoOAuthSessionManagementAction extends ConfluenceActionSupport {
	private static final Logger LOGGER = LoggerFactory.getLogger(MoOAuthSessionManagementAction.class);

    private MoOAuthSettings settings;
    private MoOAuthPluginHandler pluginHandler;
    private MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler;
    
    private HashMap<String, String> userSessionTimeout;
	private String []userAttrKey;
	private String []userAttrValue;
	private String loginCookie;

    public ArrayList errorMessage;
    public String message = StringUtils.EMPTY;

    private Boolean sessionManagementSubmitted;
	private String xsrfToken;

    public MoOAuthSessionManagementAction(MoOAuthSettings settings,MoOAuthPluginHandler pluginHandler, MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler)
    {
        this.settings = settings;
        this.pluginHandler=pluginHandler;
        this.pluginConfigurationsHandler=pluginConfigurationsHandler;
    }
    
    public Boolean validation() {
		LOGGER.debug("Configure Action Validate");
		Boolean error = false;
		if (!BooleanUtils.toBoolean(this.sessionManagementSubmitted)) {
			error = true;
		}
		
		if (BooleanUtils.toBoolean(error)) {
			initializeOAuthConfig();
		}
		
		return error;
	}
    
    @Override
    public String execute() throws Exception {
        LOGGER.info("MoOAuthSessionManagementAction: doExecute called.");
        try {
            if (!settings.isLicenseValid()) {
                LOGGER.error("No valid license found");
                return "invalid";
            }

			HttpServletRequest request = ServletActionContext.getRequest();
			XsrfTokenGenerator tokenGenerator = new SimpleXsrfTokenGenerator();
			xsrfToken = tokenGenerator.generateToken(request);
            
            if (BooleanUtils.toBoolean(this.sessionManagementSubmitted)) {
				Boolean error;
				error = validation();
                if(!error) {
                	HashMap<String, String> userSessionTimeout = new HashMap<>();
        			
    				if (userAttrKey != null && userAttrValue != null) {
    					LOGGER.debug("userAttrKey.length : "+ userAttrKey.length);
    				
    					for (int count=0; count <userAttrKey.length ; count++) {
    						LOGGER.debug("userAttrKey : "+ userAttrKey[count]);
    						LOGGER.debug("userAttrValue : "+ userAttrValue[count]);
    						if(!StringUtils.isEmpty(userAttrKey[count]) && !StringUtils.isEmpty(userAttrValue[count])) {
    							userSessionTimeout.put(userAttrKey[count].trim(), userAttrValue[count].trim());
    						}
    						LOGGER.debug("userSessionTimeout : "+ userSessionTimeout);
    					}			
    				}
    				
    				pluginHandler.saveSessionManagement(userSessionTimeout, this.loginCookie);
    				addActionMessage(getText("oauth.success.config"), "success", true, null);
				}
			}
            initializeOAuthConfig();
            return "success";
        } catch (MoOAuthPluginException e) {
			e.printStackTrace();
			addActionMessage(e.getMessage());
			return "input";
		} catch (Exception e) {
			e.printStackTrace();
			addActionMessage("An error occurred while saving your details. Please check logs for more info.");
			return "input";
		}

    }
   
    private void initializeOAuthConfig() {
		LOGGER.debug("initialize OAuth Configurations for Session Management tab...");
		this.userSessionTimeout = new HashMap<>(settings.getUserSessionTimeout());
		this.loginCookie = settings.getLoginCookie();
	}
    
    public HashMap<String, String> getUserSessionTimeout() {
		return userSessionTimeout;
	}

	public void setUserSessionTimeout(HashMap<String, String> userSessionTimeout) {
		this.userSessionTimeout = userSessionTimeout;
	}

	public String[] getUserAttrKey() {
		return userAttrKey;
	}

	public void setUserAttrKey(String[] userAttrKey) {
		this.userAttrKey = userAttrKey;
	}

	public String[] getUserAttrValue() {
		return userAttrValue;
	}

	public void setUserAttrValue(String[] userAttrValue) {
		this.userAttrValue = userAttrValue;
	}

	public String getLoginCookie() {
		return loginCookie;
	}

	public void setLoginCookie(String loginCookie) {
		this.loginCookie = loginCookie;
	}
	
    public Boolean getSessionManagementSubmitted() {
		return sessionManagementSubmitted;
	}

	public void setSessionManagementSubmitted(Boolean sessionManagementSubmitted) {
		this.sessionManagementSubmitted = sessionManagementSubmitted;
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

    public void setErrorMessage(ArrayList errorMessage) {
        this.errorMessage = errorMessage;
    }
	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}
}
