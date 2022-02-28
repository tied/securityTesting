package com.miniorange.oauth.confluence.action;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.plugin.PluginException;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;

public class MoLicenseDetailsAction extends ConfluenceActionSupport {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MoLicenseDetailsAction.class);

	private MoOAuthSettings settings;
	private MoOAuthPluginHandler pluginHandler;
	
	public MoLicenseDetailsAction(MoOAuthSettings settings, MoOAuthPluginHandler pluginHandler) {
		super();
		this.settings = settings;
		this.pluginHandler = pluginHandler;
	}
	
	public void validate() {
		LOGGER.debug("License Details Action doValidate");
		super.validate();
	}

	@Override
	public String execute() throws Exception {
		try {
			LOGGER.debug("License Details Action doExecute Submitted: ");
			if(!settings.isLicenseValid()){
				return "invalid";
			}
			return "success";
		} catch (PluginException e) {
			LOGGER.error(e.getMessage());
			addActionError(e.getMessage());
			return "input";
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			addActionError("An error occured while saving your details. Please check logs for more info.");
			return "input";
		}
	}

	protected List getPermissionTypes() {
		List requiredPermissions = super.getPermissionTypes();
		requiredPermissions.add("ADMINISTRATECONFLUENCE");
		return requiredPermissions;
	}

	public MoOAuthSettings getSettings() {
		return this.settings;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

}
