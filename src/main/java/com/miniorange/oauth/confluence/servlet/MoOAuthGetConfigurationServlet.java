/*
 * Called during the login flow
 * Returns the response with the App name to be displayed in the Login button
 * URL : base URL + /plugins/servlet/oauth/getconfig
 */

package com.miniorange.oauth.confluence.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.Cookie;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import com.google.gson.*;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthPluginConstants;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import org.apache.commons.lang3.StringUtils;

import com.atlassian.elasticsearch.shaded.netty.util.internal.StringUtil;
import com.miniorange.oauth.MoOAuthConfigAjaxAttributes;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MoOAuthGetConfigurationServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(MoOAuthGetConfigurationServlet.class);


	private MoOAuthPluginHandler pluginHandler;
	private MoOAuthSettings settings;

	public MoOAuthGetConfigurationServlet(MoOAuthSettings settings, MoOAuthPluginHandler pluginHandler) {
		this.settings = settings;
		this.pluginHandler = pluginHandler;
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		LOGGER.debug("Get Configuration servlet invoked..");
		String referer = request.getHeader("referer");
		if (settings.getPluginApiAccessRestriction()
				&& !org.apache.commons.lang.StringUtils.startsWith(referer, this.settings.getBaseUrl())) {
			LOGGER.error(
					"Access Denied. API Restriction is enabled and request is not originated from the Confluence.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You are not allowed to access this page.");
			return;
		}

		if ((!settings.isEvaluationOrSubscriptionLicense()) ||
				(settings.isEvaluationOrSubscriptionLicense() && settings.isLicenseValid())) {
			getConfiguration(request, response);
		}
	}
	
	public void getConfiguration(HttpServletRequest request, HttpServletResponse response) {
		try {
			
			Boolean isConfigured = StringUtils.isNotBlank(settings.getClientID());
			Boolean enableOAuthSSO = settings.getEnableOAuthSSO();
			Boolean defaultLoginDisabled=settings.getDefaultLoginDisabled();
			Boolean enableBackdoor=settings.getBackdoorEnabled();
			Boolean enableAutoRedirectDelay=settings.getEnableAutoRedirectDelay();
			String autoRedirectDelayInterval=settings.getAutoRedirectDelayInterval();
			String ssoButtonLocation = settings.getSsoButtonLocation();
			Boolean restrictBackdoor = settings.getRestrictBackdoor();
			String backdoorKey = settings.getBackdoorKey();
			String backdoorValue = settings.getBackdoorValue();
			HashMap<String, String> userSessionDetails =  settings.getUserSessionTimeout();
			Boolean disableAnonymousAccess=settings.getDisableAnonymousAccess();
			Boolean isGuestCookieExpired=isGuestCookieExpired(request);
			Boolean checkIDPSession = settings.getCheckIDPSession();
			String autoLoginURL = settings.getAutoLoginURL();
			Boolean enableLoginTemplate = settings.getEnableLoginTemplate();
			String loginTemplate = settings.getLoginTemplate();
						
			MoOAuthConfigAjaxAttributes attrs = new MoOAuthConfigAjaxAttributes(isConfigured,enableOAuthSSO,settings.getLoginButtonText(),
					defaultLoginDisabled,disableAnonymousAccess, isGuestCookieExpired, enableBackdoor, enableAutoRedirectDelay,
					autoRedirectDelayInterval, ssoButtonLocation, restrictBackdoor, backdoorKey, backdoorValue, userSessionDetails, enableLoginTemplate, loginTemplate
					, checkIDPSession, autoLoginURL);

			String attrsJSON = attrs.getJSON();
			response.setContentType(MediaType.APPLICATION_JSON);
			response.getOutputStream().write(attrsJSON.getBytes(StandardCharsets.UTF_8));
			response.getOutputStream().close();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

	private Boolean isGuestCookieExpired(HttpServletRequest request) {
		Cookie guestCookie = MoOAuthHttpUtils.getCookie(MoOAuthPluginConstants.GUEST_COOKIE,request);
		return guestCookie==null;
	}
}

