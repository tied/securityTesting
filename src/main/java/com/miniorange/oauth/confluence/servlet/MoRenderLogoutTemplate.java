package com.miniorange.oauth.confluence.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.miniorange.oauth.confluence.MoOAuthSettings;

public class MoRenderLogoutTemplate extends HttpServlet{
	
	private static Log LOGGER = LogFactory.getLog(MoRenderLogoutTemplate.class);
	private MoOAuthSettings settings;
	
	public MoRenderLogoutTemplate(MoOAuthSettings settings) {
		super();
		this.settings = settings;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		redirectToCustomLogoutTemplate(response, request);
	}
	
	public void redirectToCustomLogoutTemplate(ServletResponse response, ServletRequest request) {
		try {
			LOGGER.debug("Logout template enabled.");
			Map context = MacroUtils.defaultVelocityContext();
			context.put("baseUrl", settings.getBaseUrl());
			String result = VelocityUtils.getRenderedContent((CharSequence) settings.getLogoutTemplate(), context);
			LOGGER.debug("result = " + result);
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write(result);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}

	public MoOAuthSettings getSettings() {
		return settings;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}


}
