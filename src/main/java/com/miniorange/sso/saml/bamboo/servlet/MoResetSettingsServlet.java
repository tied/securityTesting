package com.miniorange.sso.saml.bamboo.servlet;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MoResetSettingsServlet extends HttpServlet {

	private static Log LOGGER = LogFactory.getLog(MoResetSettingsServlet.class);
	private MoSAMLSettings settings;
	private String xsrfToken;


	public MoResetSettingsServlet(MoSAMLSettings settings) {
		super();
		this.settings = settings;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("plugin reset doGet called...");
		try {
			String htmlStart = "<html><head><script type=\"text/javascript\">window.onload = function(){" +
					"document.forms['plugin-reset-form'].submit(); }</script></head>" +
					"<body>Resetting plugin configuration...!" +
					"<form action=\"" +settings.getSpBaseUrl() + "/plugins/servlet/saml/moconfreset\" " +
					"method=\"post\" id=\"plugin-reset-form\">" +
					"<input type=\"hidden\" name=\"atl_token\" value=\"" + xsrfToken + "/></form>" +
					"</body></html>";

			response.setContentType("text/html");
			response.setCharacterEncoding("iso-8859-1");
			response.getOutputStream().write(htmlStart.getBytes(StandardCharsets.UTF_8));
		}catch (Exception e){
			LOGGER.error(e);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("plugin reset doPost called...");
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);
		try {
				LOGGER.debug("clearing plugin settings");
				MoPluginHandler.stopAllSchedulers();
				settings.clearPluginSettings();
				response.sendRedirect(settings.getBaseUrl().concat("/plugins/servlet/bamboo-sso/configure.action?reset=success"));
				LOGGER.debug("Plugin setting is clear. Redirecting to the Configured IDPs tab");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public MoSAMLSettings getSettings() {
		return settings;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}

}
