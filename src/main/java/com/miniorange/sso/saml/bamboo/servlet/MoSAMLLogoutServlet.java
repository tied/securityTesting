package com.miniorange.sso.saml.bamboo.servlet;

import com.atlassian.seraph.auth.Authenticator;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import com.miniorange.sso.saml.utils.MoEncryptionUtils;
import com.miniorange.sso.saml.bamboo.MoSAMLManager;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoHttpUtils;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MoSAMLLogoutServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(MoSAMLLogoutServlet.class);
	private MoSAMLSettings settings;
	private MoSAMLManager samlManager;
	private final TemplateRenderer renderer;

	public MoSAMLLogoutServlet(MoSAMLSettings settings, MoSAMLManager samlManager, TemplateRenderer renderer) {
		this.settings = settings;
		this.samlManager = samlManager;
		this.renderer = renderer;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("MoSAMLLogoutServlet doGet called");
		try {
			clearCookie("numberOfAttempts",request,response);
			Cookie logoutCookie = getCookie("LOGOUTCOOKIE", request);
			if (logoutCookie != null) {
				String idpId = logoutCookie.getValue();
				LOGGER.debug("idpId : "+idpId);
				clearCookie("PERFORM_LOGOUT", request, response);
				clearCookie("seraph.bamboo",request,response);
				if (StringUtils.isNotBlank(idpId) || StringUtils.isNotEmpty(idpId)) {
					handleSAMLLogout(request, response, false, idpId);
				} else {
					clearCookie("LOGOUTCOOKIE", request, response);
					clearCookie("seraph.bamboo",request,response);
					samlManager.httpRedirect(response, settings.getBaseUrl());
				}
			} else {
				clearCookie("LOGOUTCOOKIE", request, response);
				clearCookie("seraph.bamboo",request,response);
				samlManager.httpRedirect(response, settings.getBaseUrl());
			}
		} catch (Exception e) {
			e.printStackTrace();
			clearCookie("LOGOUTCOOKIE", request, response);
			clearCookie("seraph.bamboo",request,response);
			samlManager.httpRedirect(response, settings.getBaseUrl());
		}
		return;
		
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("MoSAMLLogoutServlet doPost called");
		try {
			Cookie logoutCookie = getCookie("LOGOUTCOOKIE", request);
			if (logoutCookie != null) {
				String idpId = logoutCookie.getValue();
				clearCookie("PERFORM_LOGOUT", request, response);
				clearCookie("seraph.bamboo",request,response);
				if (StringUtils.isNotBlank(idpId) || StringUtils.isNotEmpty(idpId)) {
					handleSAMLLogout(request, response, true, idpId);
				}
			}else{
				clearCookie("LOGOUTCOOKIE", request, response);
				clearCookie("seraph.bamboo",request,response);
				samlManager.httpRedirect(response, settings.getBaseUrl());
			}
		} catch (Exception e) {
			e.printStackTrace();
			clearCookie("LOGOUTCOOKIE", request, response);
			clearCookie("seraph.bamboo",request,response);
			samlManager.httpRedirect(response, settings.getBaseUrl());
		}
		return;
	}

	private void handleSAMLLogout(HttpServletRequest request, HttpServletResponse response, Boolean isPostRequest, String idpId)
			throws IOException {
		HttpSession session = request.getSession();

		MoIDPConfig idpConfig = MoPluginHandler.constructIdpConfigObject(idpId);

		if (idpConfig != null && StringUtils.isNotBlank(idpConfig.getSloUrl())) {
			if (StringUtils.isNotBlank(request.getParameter("SAMLResponse"))) {
				if (StringUtils.isNotBlank(settings.getCustomLogoutURL())) {
					session.invalidate();
					LOGGER.debug("Here After custom logout URL");
					clearCookie("LOGOUTCOOKIE", request, response);
					samlManager.httpRedirect(response, settings.getCustomLogoutURL());
					return;
				} else if (settings.getEnableLogoutTemplate()) {
					session.invalidate();
					LOGGER.debug("Here After custom logout template");
					redirectToLogoutTemplate(response);
					return;
				} else {
					session.invalidate();
					clearCookie("LOGOUTCOOKIE", request, response);
					samlManager.httpRedirect(response, settings.getLoginPageUrl());
					return;
				}
			} else if (StringUtils.isNotBlank(request.getParameter("SAMLRequest"))) {
				try {
					Authenticator authenticator = SecurityConfigFactory.getInstance().getAuthenticator();
					authenticator.logout(request, response);
					if (idpConfig.getSignedRequest()) {
						samlManager.createLogoutResponseAndRedirect(request, response, isPostRequest, idpConfig);
					} else {
						samlManager.createUnSignedLogoutResponseAndRedirect(request, response, isPostRequest, idpConfig);
					}
					return;
				} catch (AuthenticatorException e) {
					e.printStackTrace();
				}
			} else {
				String nameId = getCookie("NAMEIDCOOKIE", request).getValue();
				String sessionIndex = getCookie("SESSIONINDEXCOOKIE", request).getValue();
				clearCookie("NAMEIDCOOKIE", request, response);
				clearCookie("SESSIONINDEXCOOKIE", request, response);
				if (idpConfig.getSignedRequest()) {
					samlManager.createLogoutRequestAndRedirect(request, response, nameId, sessionIndex, idpConfig);
				} else {
					samlManager.createUnSignedLogoutRequestAndRedirect(request, response, nameId, sessionIndex, idpConfig);
				}
				return;
			}
		}

		if (StringUtils.isNotBlank(settings.getCustomLogoutURL())) {
			session.invalidate();
			clearCookie("LOGOUTCOOKIE", request, response);
			samlManager.httpRedirect(response, settings.getCustomLogoutURL());
		} else if (settings.getEnableLogoutTemplate()) {
			session.invalidate();
			clearCookie("LOGOUTCOOKIE", request, response);
			redirectToLogoutTemplate(response);
		} else {
			session.invalidate();
			clearCookie("LOGOUTCOOKIE", request, response);
			samlManager.httpRedirect(response, settings.getLoginPageUrl());
		}
		return;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}

	private void redirectToLogoutTemplate(HttpServletResponse response) throws IOException {
		try {
			Map<String, Object> context = new HashMap();
			context.put("baseUrl", settings.getLoginPageUrl());
			String result = renderer.renderFragment(settings.getLogoutTemplate(), context);
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write(result);
		} catch (Exception e) {
			e.printStackTrace();
			samlManager.httpRedirect(response, settings.getLoginPageUrl());
		}
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

	public void clearCookie(String cookieName, HttpServletRequest request, HttpServletResponse response) {
		LOGGER.debug("Clearing cookie : "+cookieName);
		Cookie cookie = getCookie(cookieName, request);
		if (cookie != null){ 
			if(cookieName.equals("numberOfAttempts")){
				cookie.setPath("/plugins/servlet/saml");
			}else{
			cookie.setPath("/");
			}
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}
}
