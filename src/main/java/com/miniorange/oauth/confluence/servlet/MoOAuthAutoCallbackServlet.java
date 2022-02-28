package com.miniorange.oauth.confluence.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.google.gson.JsonParseException;
import com.miniorange.oauth.confluence.dto.TokenResponse;
import com.miniorange.oauth.confluence.factory.IProtocolAction;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.miniorange.oauth.confluence.factory.OAuthOpenIdDecisionHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.miniorange.oauth.confluence.MoOAuthManager;


class MoOAuthAutoCallbackServlet extends HttpServlet {

	private static Log LOGGER = LogFactory.getLog(MoOAuthAutoCallbackServlet.class);

	private MoOAuthSettings settings;

	public MoOAuthAutoCallbackServlet(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String error = request.getParameter("error");
			if(StringUtils.isNotEmpty(error) || error != null ){
//				TimeUnit.SECONDS.sleep(15);
				if (StringUtils.isBlank(getCurrentLoggedInUser())) {
//					MoOAuthManager.httpRedirect(response, "http://localhost:8090/plugins/servlet/oauth/autoAuth");
				}
			}
			else{
					Cookie sessionCookie = new Cookie("IDPSESSIONEXIST", "true");
					sessionCookie.setMaxAge(4000);
					sessionCookie.setPath("/");
					response.addCookie(sessionCookie);
					return;
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
		String refreshToken = req.getParameter("refresh_token");
		if (refreshToken != null)
//			settings.setRefreshToken(refreshToken);

		return;
	}

	private String getCurrentLoggedInUser() {
		ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
		if (confluenceUser == null) {
			return StringUtils.EMPTY;
		}
		return confluenceUser.getName();
	}

	public MoOAuthSettings getSettings() {
		return settings;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}
}
