package com.miniorange.sso.saml.bamboo.action;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;

public class MoImportExportAction extends BambooActionSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(MoAddIDPConfigurationAction.class);

	private UserManager userManager;
	private LoginUriProvider loginUriProvider;
	private MoSAMLSettings settings;

	private Boolean importFileSubmitted;
	private String xsrfToken;

	public MoImportExportAction(UserManager userManager, LoginUriProvider loginUriProvider, MoSAMLSettings settings) {
		super();
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.settings = settings;
	}

	public Boolean doValidate() {
		LOGGER.debug("MoImportExportAction: doValidate() called");
		return true;
	}

	@Override
	public String execute() throws Exception {
		LOGGER.debug("MoImportExportAction execute() called");

		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);

		final UserProfile user = userManager.getRemoteUser();

		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				String message = StringUtils.EMPTY;
				LOGGER.debug("MoImportExportAction execute submitted: " + this.importFileSubmitted);
				if (!settings.isValidLicense()) {
					LOGGER.error("Invalide License");
					return "upm";
				}
				
				if (MoSAMLSettings.isPluginConfigurationFileUploaded) {
					MoSAMLSettings.isPluginConfigurationFileUploaded = Boolean.FALSE;
					if (StringUtils.isNotBlank(settings.getSpConfigurationStatus())) {
						if (StringUtils.equalsIgnoreCase(settings.getSpConfigurationStatus(), "success")) {
							addActionMessage(getText("samlsso.success.config"));
						} else {
							addActionError(settings.getSpConfigurationStatus());
						}
					}	
				}
				return "success";
				
			} catch (Exception e) {
				e.printStackTrace();
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

	public Boolean getImportFileSubmitted() {
		return importFileSubmitted;
	}

	public void setImportFileSubmitted(Boolean importFileSubmitted) {
		this.importFileSubmitted = importFileSubmitted;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}
}
