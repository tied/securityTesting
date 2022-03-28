/*
 * Called during the login flow
 * Returns the response with the App name to be displayed in the Login button
 * URL : base URL + /plugins/servlet/oauth/getconfig
 */

package com.miniorange.oauth.bamboo.servlet;


import com.google.gson.Gson;
import com.miniorange.oauth.MoOAuthConfigAjaxAttributes;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;



public class MoOAuthGetConfigurationServlet extends HttpServlet {

	private static final Gson gson = new Gson();
	private static final Log LOGGER = LogFactory.getLog(MoOAuthGetConfigurationServlet.class);
	private MoOAuthSettings settings;
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {

			String referer = request.getHeader("referer");
			if(settings.getPluginApiAccessRestriction()
			&& !StringUtils.startsWith(referer, this.settings.getBaseUrl())){
				LOGGER.error(
						"Access Denied. API Restriction is enabled and request is not originated from the Bamboo.");
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						"Access Denied. You are not allowed to access this page.");
				return;
			}

			if(!settings.isEvaluationOrSubscriptionLicense()) {
				Boolean isConfigured = StringUtils.isNotBlank(settings.getClientID());
				Boolean backdoorEnabled = settings.getBackdoorEnabled();
				Boolean disableDefaultLogin = settings.getDefaultLoginDisabled();
				Boolean enableAutoRedirectDelay = settings.getEnableAutoRedirectDelay();
				String autoRedirectDelayInterval=settings.getAutoRedirectDelayInterval();
				String ssoButtonLocation =settings.getSsoButtonLocation();
				Boolean restrictBackdoor = settings.getRestrictBackdoor();
				Boolean enableLoginTemplate = settings.getEnableLoginTemplate();
				String loginTemplate = settings.getLoginTemplate();
				String backdoorKey = settings.getBackdoorKey();
				String backdoorValue = settings.getBackdoorValue();
				MoOAuthConfigAjaxAttributes attrs = new MoOAuthConfigAjaxAttributes(settings.getLoginButtonText(),
						isConfigured, backdoorEnabled, disableDefaultLogin, enableAutoRedirectDelay,autoRedirectDelayInterval,ssoButtonLocation, restrictBackdoor,
						enableLoginTemplate,loginTemplate,backdoorKey, backdoorValue);
				String attrsJSON = gson.toJson(attrs);
				response.setContentType(MediaType.APPLICATION_JSON);
				response.getOutputStream().write(attrsJSON.getBytes());
				response.getOutputStream().close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}
}
