package com.miniorange.sso.saml.bamboo.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.miniorange.sso.saml.bamboo.MoPluginConfigurationsHandler;

public class MoDownloadPluginConfiguration extends HttpServlet{
	
	private static Log LOGGER = LogFactory.getLog(MoMetadataServlet.class);
	private MoPluginConfigurationsHandler moPluginConfigurationsHandler;

	public MoDownloadPluginConfiguration(MoPluginConfigurationsHandler moPluginConfigurationsHandler) {
		this.moPluginConfigurationsHandler = moPluginConfigurationsHandler;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("Download app configuration: doGet() called");
		try {
			String jsonConfigurations = moPluginConfigurationsHandler.generateConfigurationsJson();
			LOGGER.debug("Downloading json Configurations file.");
			response.setHeader("Content-Disposition", "attachment; filename=\"app_configurations.json\"");
			response.setHeader("Cache-Control", "max-age=0");
			response.setHeader("Pragma", "");
			response.setContentType(MediaType.APPLICATION_JSON);
			response.getOutputStream().write(jsonConfigurations.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			PrintWriter writer = response.getWriter();
			writer.print("An error occurred while generating the configuration file.");
			writer.close();
		}
		
	}

	public MoPluginConfigurationsHandler getMoPluginConfigurationsHandler() {
		return moPluginConfigurationsHandler;
	}

	public void setMoPluginConfigurationsHandler(MoPluginConfigurationsHandler moPluginConfigurationsHandler) {
		this.moPluginConfigurationsHandler = moPluginConfigurationsHandler;
	}
	
}
