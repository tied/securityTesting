package com.miniorange.sso.saml.bamboo.action;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;

public class MoAccountDetailsAction extends BambooActionSupport{
	
	private String xsrfToken;
	private UserManager userManager;
	private LoginUriProvider loginUriProvider;
	private MoSAMLSettings settings;
	private static final Logger LOGGER = LoggerFactory.getLogger(MoAccountDetailsAction.class);

	public MoAccountDetailsAction(UserManager userManager, LoginUriProvider loginUriProvider,MoSAMLSettings settings) {
		super();
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.settings = settings;
	}

	public void validate() {
		LOGGER.debug("Account Details Action: validate() called");
	}
	
	@Override
	public String execute() throws Exception {
		LOGGER.debug("Account Details Action: execute() called");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);
		
		final UserProfile user = userManager.getRemoteUser();
		
		if (user != null && userManager.isAdmin(user.getUserKey())) {
			if (!settings.isValidLicense()) {
				LOGGER.debug("Invalide License");
				return "upm";
			}
			return "success";
		}else {
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

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
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

	
}
