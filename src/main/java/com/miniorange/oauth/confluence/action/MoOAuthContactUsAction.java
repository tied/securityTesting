package com.miniorange.oauth.confluence.action;

import java.util.List;

import com.atlassian.xwork.RequireSecurityToken;
import com.atlassian.xwork.SimpleXsrfTokenGenerator;
import com.atlassian.xwork.XsrfTokenGenerator;
import com.opensymphony.webwork.ServletActionContext;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;

import javax.servlet.http.HttpServletRequest;

public class MoOAuthContactUsAction extends ConfluenceActionSupport {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MoOAuthContactUsAction.class);

	private Boolean submitSupportQuery;
	
	private MoOAuthSettings settings;
	private MoOAuthPluginHandler pluginHandler;
	private String xsrfToken;
	
	public MoOAuthContactUsAction(MoOAuthSettings settings, MoOAuthPluginHandler pluginHandler) {
		this.settings = settings;
		this.pluginHandler = pluginHandler;
	}
	
	public void validate() {
		LOGGER.debug("Configure Action doValidate");
		
		if (!BooleanUtils.toBoolean(submitSupportQuery)) {
			return;
		}

		super.validate();
	}

	@Override
	public String execute() throws Exception {
		try {
			LOGGER.debug("Configure Action doExecute Submitted: " + this.submitSupportQuery);

			HttpServletRequest request = ServletActionContext.getRequest();
			XsrfTokenGenerator tokenGenerator = new SimpleXsrfTokenGenerator();
			xsrfToken = tokenGenerator.generateToken(request);

			if(!settings.isLicenseValid()){
				return "invalid";
			}
			
			if (BooleanUtils.toBoolean(this.submitSupportQuery)) {
				String email = getCurrentRequest().getParameter("email");
				String phone = getCurrentRequest().getParameter("phone");
				String query = getCurrentRequest().getParameter("query");
				pluginHandler.submitSupportQuery(email, phone, query);
				addActionMessage(getText("oauth.success.submit.query"), "success", true, null);
			}

			return "success";
		} catch (MoOAuthPluginException e) {
			LOGGER.error(e.getMessage());
			addActionError(e.getMessage());
			return "input";
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			addActionError("An error occurred while saving your details. Please check logs for more info.");
			return "input";
		}
	}

	protected List getPermissionTypes() {
		List requiredPermissions = super.getPermissionTypes();
		requiredPermissions.add("ADMINISTRATECONFLUENCE");
		return requiredPermissions;
	}

	public MoOAuthSettings getSettings() {
		return settings;
	}

	public void setSettings(MoOAuthSettings settings) {
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
