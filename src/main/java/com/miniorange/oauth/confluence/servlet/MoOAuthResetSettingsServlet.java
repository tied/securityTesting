package com.miniorange.oauth.confluence.servlet;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.miniorange.oauth.confluence.MoOAuthPluginConfigurationsHandler;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class MoOAuthResetSettingsServlet extends HttpServlet {

	private static Log LOGGER = LogFactory.getLog(MoOAuthResetSettingsServlet.class);
	private MoOAuthSettings settings;
	private UserAccessor userAccessor;
	private MoOAuthPluginHandler pluginHandler;
	private MoOAuthPluginConfigurationsHandler moOAuthPluginConfigurationsHandler;

	public MoOAuthResetSettingsServlet(MoOAuthSettings settings, UserAccessor userAccessor, MoOAuthPluginHandler pluginHandler) {
		super();
		this.settings = settings;
		this.userAccessor = userAccessor;
		this.pluginHandler = pluginHandler;
	}


	public MoOAuthPluginConfigurationsHandler getMoOAuthPluginConfigurationsHandler() {
		return moOAuthPluginConfigurationsHandler;
	}

	public void setMoOAuthPluginConfigurationsHandler(MoOAuthPluginConfigurationsHandler moOAuthPluginConfigurationsHandler) {
		this.moOAuthPluginConfigurationsHandler = moOAuthPluginConfigurationsHandler;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		try {
			LOGGER.debug("Reset plugin settings servlet called");
			ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
			Boolean isAdmin = Boolean.FALSE;
			LOGGER.debug("MoResetSettingsServlet logged in Confleunce user= " + confluenceUser.getName());
			if (confluenceUser != null)
				isAdmin = userAccessor.hasMembership("confluence-administrators", confluenceUser.getName());

			LOGGER.debug("MoResetSettingsServlet confluence user has admin permission  = " + isAdmin);

			if (confluenceUser != null && isAdmin) {
				LOGGER.debug("clearing plugin settings");
				settings.clearPluginSettings();
				settings.setResetSettings(Boolean.TRUE);
				StringBuffer htmlStart = new StringBuffer();
				StringBuffer buffer = new StringBuffer();
				buffer.append(settings.getConfigureActionUrl());
				htmlStart.append("<html><head><script>window.onload = " + "function() {window.location.href=\""
						+ buffer.toString()
						+ "\"};</script></head><body><br><br><div class=\"aui-message aui-message-info\"><span>Please wait!!"
						+ "We are resetting plugin settings.</span></body></html>");
				PrintWriter writer = response.getWriter();
				writer.write(htmlStart.toString());
				writer.close();
				LOGGER.debug("Plugin setting is clear. Redirecting to the Configure SP tab");
			}
		} catch (NullPointerException e) {
			LOGGER.error(e.getMessage());
		}
		return;
	}

	public MoOAuthSettings getSettings() {
		return settings;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public UserAccessor getUserAccessor() {
		return userAccessor;
	}

	public void setUserAccessor(UserAccessor userAccessor) {
		this.userAccessor = userAccessor;
	}

}
