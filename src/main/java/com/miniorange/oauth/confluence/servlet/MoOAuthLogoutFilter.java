package com.miniorange.oauth.confluence.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.miniorange.oauth.confluence.MoOAuthManager;
import com.miniorange.oauth.confluence.MoOAuthSettings;

public class MoOAuthLogoutFilter implements Filter {
	private MoOAuthSettings settings;
	private MoOAuthManager oauthManager;
	private Object renderer;
	private static Log LOGGER = LogFactory.getLog(MoOAuthLogoutFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}
	
	public MoOAuthLogoutFilter(MoOAuthSettings settings, MoOAuthManager oauthManager) {
		super();
		this.settings = settings;
		this.oauthManager = oauthManager;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String path = ((HttpServletRequest) request).getRequestURI();
		if (path.contains("/autocallback") || path.contains("/autoAuth")) {
			chain.doFilter(request, response);
			return;
		}
		// TODO Auto-generated method stub
		LOGGER.debug("request.getParameter(\"logout\") = "+request.getParameter("logout"));
		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session=req.getSession();
		
		if(StringUtils.equalsIgnoreCase(request.getParameter("logout"), "true") && getCookie("mo.confluence-oauth.LOGOUTCOOKIE",req)!=null) {
			clearCookie("mo.confluence-oauth.LOGOUTCOOKIE", req, (HttpServletResponse) response);
			clearCookie("SESSIONCOOKIE", req, (HttpServletResponse) response);
			clearCookie("VALIDATESESSIONCOOKIE", req, (HttpServletResponse) response);

			if (StringUtils.isNotEmpty(settings.getSingleLogoutURL())) {
				LOGGER.debug("Redirect to single logout url: " + settings.getSingleLogoutURL());
				session.invalidate();
				oauthManager.httpRedirect((HttpServletResponse) response,  settings.getSingleLogoutURL());
				return;
			} else if (BooleanUtils.toBoolean(settings.getEnablelogoutTemplate())
					&& StringUtils.isNotBlank(settings.getLogoutTemplate())) {
				LOGGER.debug("Redirect to logout template");
				session.invalidate();
				//redirectToCustomLogoutTemplate(response, request);
				oauthManager.httpRedirect((HttpServletResponse) response, settings.getCustomLogoutTemplateUrl());
				return;
			} else if(StringUtils.isNotEmpty(settings.getCustomLogoutURL())) {
				LOGGER.debug("Redirect to custom logout url: " + settings.getCustomLogoutURL());
				session.invalidate();
				oauthManager.httpRedirect((HttpServletResponse) response, settings.getCustomLogoutURL());
				return;
			} else {
				LOGGER.debug("Redirect to login page");
				session.invalidate();
				((HttpServletResponse) response).sendRedirect(settings.getLoginPageUrl());
				return ;
			}
			
		}
		
		chain.doFilter(request, response);
		return;	
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
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
	
	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public static void clearCookie(String cookieName, HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = getCookie(cookieName, request);
		if (cookie != null) {
			cookie.setPath("/");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}

	private static Cookie getCookie(String cookieName, HttpServletRequest request) {
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
