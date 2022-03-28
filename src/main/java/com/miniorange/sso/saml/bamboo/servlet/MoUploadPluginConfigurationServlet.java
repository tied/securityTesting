package com.miniorange.sso.saml.bamboo.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
import org.apache.struts2.dispatcher.multipart.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.sal.api.user.UserManager;
import com.miniorange.sso.saml.bamboo.MoPluginConfigurationsHandler;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;

public class MoUploadPluginConfigurationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private MoSAMLSettings settings;
	private String xsrfToken;
	private UserManager userManager;
	private static final Logger LOGGER = LoggerFactory.getLogger(MoUploadPluginConfigurationServlet.class);

	public MoUploadPluginConfigurationServlet(MoSAMLSettings settings, UserManager userManager) {
		this.settings = settings;
		this.userManager = userManager;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);
		LOGGER.debug("MoUploadPluginConfigurationServlet: doPost  called");
		MoSAMLSettings.isPluginConfigurationFileUploaded = Boolean.TRUE;
		try {
			handleUploadConfigurations(response);
			response.sendRedirect(settings.getImportExportPageUrl());
		}catch (Exception e) {
			e.printStackTrace();
			settings.setSpConfigurationStatus("An error occurred while saving your details.Please check logs for more info.");
			response.sendRedirect(settings.getImportExportPageUrl());
		}
	}
	
	
	private void handleUploadConfigurations(HttpServletResponse response) throws IOException {
		LOGGER.debug("handleUploadConfigurations called");
		String fileContents = org.apache.commons.lang3.StringUtils.EMPTY;
		MultiPartRequestWrapper wrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();
		UploadedFile[] files = wrapper.getFiles("configFile");

		if (files != null) {
			for (int i = 0; i < (files.length); ++i) {
				UploadedFile file = files[i];
				if (file.isFile()) {
					try {
						fileContents = FileUtils.readFileToString((File) file.getContent());
						if(StringUtils.isEmpty(fileContents)){
							settings.setSpConfigurationStatus("Uploaded app configuration file is empty.");
							return;
						} else {
							MoPluginHandler.importConfigurations(fileContents);
							settings.setSpConfigurationStatus("success");
							return ;
						}
					} catch (Exception e) {
						e.printStackTrace();
						LOGGER.debug("exception message = "+e.getMessage());
						LOGGER.debug("Invalid file is  provided. Could not parse app configuration file.");
						settings.setSpConfigurationStatus("Invalid file is  provided. Could not parse app configuration file.");
						return ;
					}
				}
			}
		}
		LOGGER.debug("An error occurred while uploading the app configuring file.");
		settings.setSpConfigurationStatus("An error occurred while uploading the app configuring file.");
		return ;
	}

	public MoSAMLSettings getSettings() {
		return settings;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
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
	
}
