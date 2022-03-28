package com.miniorange.sso.saml.bamboo.action;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.plugin.PluginException;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;

public class MoContactUsAction extends BambooActionSupport{

	private static final Logger LOGGER = LoggerFactory.getLogger(MoContactUsAction.class);

	private UserManager userManager;
	private LoginUriProvider loginUriProvider;
	private MoSAMLSettings settings;
	
	private Boolean submitSupportQuery;
	private String xsrfToken;
	
	public MoContactUsAction(UserManager userManager, LoginUriProvider loginUriProvider, MoSAMLSettings settings) {
		super();
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.settings = settings;
	}

	public void validate() {
		LOGGER.debug("ContactUs Action validate called");
		if (!BooleanUtils.toBoolean(submitSupportQuery)) {
			return;
		}
	}
	
	@Override
	public String execute() throws Exception {
		LOGGER.debug("ContactUs Action execute called");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);
		
		final UserProfile user = userManager.getRemoteUser();

		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				LOGGER.debug("ContactUs Action doExecute Submitted: " + this.submitSupportQuery);
				
				if (!settings.isValidLicense()) {
					return "upm";
				}
				
				if (BooleanUtils.toBoolean(this.submitSupportQuery)) {
					String email = request.getParameter("email");
					String phone = request.getParameter("phone");
					String query = request.getParameter("query"); 
					MoPluginHandler.submitSupportQuery(email, phone, query);
					addActionMessage(getText("samlsso.success.submit.query"));
				}
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

	public Boolean getSubmitSupportQuery() {
		return submitSupportQuery;
	}

	public void setSubmitSupportQuery(Boolean submitSupportQuery) {
		this.submitSupportQuery = submitSupportQuery;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

}
