package com.miniorange.sso.saml.bamboo.servlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniorange.sso.saml.MoConfigAjaxAttributes;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MoConfigurationServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(MoConfigurationServlet.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private MoSAMLSettings settings;

	public MoConfigurationServlet(MoSAMLSettings settings){
		this.settings = settings;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("Configuration action doGet called");

		String referer = request.getHeader("referer");
		LOGGER.debug("Referer Header:" + referer);

		if(settings.getPluginApiAccessRestriction() && !StringUtils.startsWith(referer, this.settings.getSpBaseUrl())) {
			LOGGER.error("Access Denied. API Restriction is enabled and request is not originated from the Bamboo.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You are not allowed to access this page.");
			//response.sendRedirect(settings.getLoginPageUrl());
			return;
		}

		if (!settings.isEvaluationOrSubscriptionLicense()) {
				getConfiguration(request, response, true);
		} else if (settings.isEvaluationOrSubscriptionLicense() && settings.isValidLicense()) {
			getConfiguration(request, response, true);
		} else {
			getConfiguration(request, response, false);
		}
	}

	public void getConfiguration(HttpServletRequest request, HttpServletResponse response, Boolean hasValidLicense) {
		try {
			Boolean isConfigured=false;
			String attrsJSON;
			if (BooleanUtils.toBoolean(hasValidLicense)) {
				if ((settings.getIdpMap().size() > 0 || StringUtils.isNotBlank(settings.getSsoServiceUrl()))
						&& BooleanUtils.toBoolean(settings.getEnableSAMLSSO())) {
					isConfigured = true;
				}
				Boolean canUserSaveDetails = false;
				if (settings.getIdpMap().size() > 0 || StringUtils.isNotBlank(settings.getSsoServiceUrl())) {
					canUserSaveDetails = true;
				}
				//Boolean isConfigured = StringUtils.isNotBlank(settings.getSsoServiceUrl());
				Boolean sloConfigured = isSloConfigured(request);
				Boolean customLogoutUrlConfigured = StringUtils.isNotBlank(settings.getCustomLogoutURL());
				Boolean customLogoutTemplateEnabled = BooleanUtils.toBoolean(settings.getEnableLogoutTemplate());
				Boolean enableAutoRedirectDelay = BooleanUtils.toBoolean(settings.getEnableAutoRedirectDelay());
				String numberOfLoginAttempts = settings.getNumberOfLoginAttempts();

				HashMap<String, String> idpMap = settings.getIdpMap();
				List<String> idpList = settings.getIdPList();
				Boolean useDomainMapping = settings.getUseDomainMapping() && idpList.size() > 1;

				String username = request.getParameter("username");
				String idp = (StringUtils.isNotBlank(username)) ? getIdPFromUsername(username.trim())
						: StringUtils.EMPTY;

				String backdoorKey = settings.getBackdoorKey();
				String backdoorValue = settings.getBackdoorValue();
				Boolean restrictBackdoor = settings.getRestrictBackdoor();
				Boolean enableLoginTemplate = settings.getEnableLoginTemplate();
				String loginTemplate = settings.getLoginTemplate();
				Boolean enablePasswordChange = settings.getEnablePasswordChange();
				Boolean headerAuthentication = BooleanUtils.toBoolean(settings.getHeaderAuthenticationSettings());
				String headerAuthenticationAttribute = settings.getHeaderAuthenticationAttribute();
				String defaultBambooIDP = settings.getDefaultBambooIDP();
				String defaultRedirectUrl = settings.getDefaultRedirectURL();
				Boolean enableSAMLSSO = settings.getEnableSAMLSSO();
				Boolean showLoginButtons = settings.getShowLoginButtons();
				Boolean pluginApiAccessRestriction= settings.getPluginApiAccessRestriction();
				String resetAssertionIDListInterval =settings.getResetAssertionIDListInterval();
				int customResetInterval =settings.getCustomResetInterval();
				List<String> ssoEnabledForIdpList = settings.getSsoEnabledForIdPList();

				MoConfigAjaxAttributes attrs = new MoConfigAjaxAttributes(isConfigured, settings.getDefaultLoginDisabled(),
						settings.getBackdoorEnabled(), enableAutoRedirectDelay,numberOfLoginAttempts, settings.getLoginButtonText(), sloConfigured,
						customLogoutUrlConfigured, backdoorKey, backdoorValue, customLogoutTemplateEnabled, idpMap, idpList,
						useDomainMapping, idp, restrictBackdoor, enableLoginTemplate, loginTemplate, canUserSaveDetails,
						enablePasswordChange, headerAuthentication, headerAuthenticationAttribute,defaultBambooIDP, defaultRedirectUrl,enableSAMLSSO, showLoginButtons,
						pluginApiAccessRestriction, resetAssertionIDListInterval, customResetInterval, ssoEnabledForIdpList);
				attrsJSON = OBJECT_MAPPER.writeValueAsString(attrs);
			} else {
				JSONObject json = new JSONObject();
				try {
					json.put("configured", BooleanUtils.toBoolean(isConfigured));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				attrsJSON = json.toString();
			}
			//LOGGER.debug("attrsJSON = "+attrsJSON);
			response.setContentType(MediaType.APPLICATION_JSON);
			response.getOutputStream().write(attrsJSON.getBytes());
			response.getOutputStream().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getIdPFromUsername(String username) {
		if (username.indexOf("@") > 0) {
			String domain = username.substring(username.indexOf("@") + 1);
			HashMap<String, String> domainMapping = settings.getDomainMapping();
			if (domainMapping.containsKey(domain)) {
				return domainMapping.get(domain);
			}
		}
		return StringUtils.EMPTY;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}

	private Boolean isSloConfigured(HttpServletRequest request){
		LOGGER.debug("isSloConfigured Called");
		try {
			Cookie logoutCookie = getCookie("LOGOUTCOOKIE",request);
			if(logoutCookie!=null){

				String idpName = logoutCookie.getValue();
				LOGGER.debug("[isSloConfigured] idpName: "+idpName);
				MoIDPConfig idpConfig = MoPluginHandler.constructIdpConfigObject(idpName);
				LOGGER.debug("IDP Config "+idpConfig);
				if(idpConfig == null){
					return Boolean.FALSE;
				}
				return StringUtils.isNotBlank(idpConfig.getSloUrl());
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			LOGGER.error("Error Occurred while geting configurations");
		}
		return Boolean.FALSE;
	}

	private Cookie getCookie(String cookieName, HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName)) {
					return cookie;
				}
			}
		}
		return null;
	}
}
