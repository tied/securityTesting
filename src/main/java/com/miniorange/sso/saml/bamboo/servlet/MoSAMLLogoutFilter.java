package com.miniorange.sso.saml.bamboo.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import com.miniorange.sso.saml.utils.MoHttpUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.user.BambooAuthenticationContext;
import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.user.User;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;

public class MoSAMLLogoutFilter implements Filter{
	private static final Logger LOGGER = LoggerFactory.getLogger(MoSAMLLogoutFilter.class);
	private MoSAMLSettings settings;
	private BambooAuthenticationContext bambooAuthenticationContext;
	private MoIDPConfig idpConfig;

	public MoSAMLLogoutFilter(MoSAMLSettings settings, MoIDPConfig idpConfig,
					   BambooAuthenticationContext bambooAuthenticationContext){
		this.settings = settings;
		this.idpConfig = idpConfig;
		this.bambooAuthenticationContext = bambooAuthenticationContext;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Do Nothing
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		LOGGER.debug("MoSAMLLogoutFilter doFilter Called");
		try {
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse res = (HttpServletResponse) response;

			Cookie performLogoutCookie = MoHttpUtils.createCookie(req.getRequestURL().toString(), "PERFORM_LOGOUT",
					"PerformLogout", false);
			res.addCookie(performLogoutCookie);
			String url = settings.getLogoutServletUrl();
			res.sendRedirect(url);

//			User bambooUser = bambooAuthenticationContext.getUser();
//			if (bambooUser != null) {
//				chain.doFilter(request, response);
//			}

//			Cookie logoutCookie = getCookie("LOGOUTCOOKIE", req);
//			Cookie nameIDCookie = getCookie("NAMEIDCOOKIE", req);
//			Cookie sessionIndexCookie = getCookie("SESSIONINDEXCOOKIE", req);

//			String idp = StringUtils.EMPTY;
//			if (logoutCookie != null) {
//				idp = logoutCookie.getValue();
//			}

//			if (StringUtils.isNotBlank(idp)) {
//				idpConfig = pluginHandler.constructIdpConfigObject(idp);
//			}

//			clearCookie("LOGOUTCOOKIE", req, res);
//			clearCookie("NAMEIDCOOKIE", req, res);
//			clearCookie("SESSIONINDEXCOOKIE", req, res);

//			Boolean isConfigured = StringUtils.isNotBlank(idpConfig.getSloUrl());
//			Boolean isCustomLogoutURL = StringUtils.isNoneBlank(settings.getCustomLogoutURL());
//			Boolean enableLogoutTemplate = BooleanUtils.toBoolean(settings.getEnableLogoutTemplate());
//			if(isConfigured || isCustomLogoutURL || enableLogoutTemplate){
//				try {
//					HttpSession session=req.getSession();
//					session.setAttribute("SAMLLOGOUT", idp);
//					session.setAttribute("SAMLNAMEID", nameIDCookie.getValue());
//					session.setAttribute("SAMLSESSIONINDEX", sessionIndexCookie.getValue());
//					return;
//				} catch (Exception e) {
//					e.printStackTrace();
//					return;
//				}
//			} else {
//				chain.doFilter(request, response);
//			}
			chain.doFilter(request, response);

		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		
	}
	
	@Override
	public void destroy() {
		// Do Nothing
	}

	public MoSAMLSettings getSettings() {
		return settings;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}

	public BambooAuthenticationContext getBambooAuthenticationContext() {
		return bambooAuthenticationContext;
	}

	public void setBambooAuthenticationContext(BambooAuthenticationContext bambooAuthenticationContext) {
		this.bambooAuthenticationContext = bambooAuthenticationContext;
	}

	public MoIDPConfig getIdpConfig() {
		return idpConfig;
	}

	public void setIdpConfig(MoIDPConfig idpConfig) {
		this.idpConfig = idpConfig;
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
		if (cookie != null) {
			cookie.setPath("/");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}
}
