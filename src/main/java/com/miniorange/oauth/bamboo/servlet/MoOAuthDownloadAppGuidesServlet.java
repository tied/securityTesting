package com.miniorange.oauth.bamboo.servlet;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MoOAuthDownloadAppGuidesServlet extends HttpServlet{

	private static Log LOGGER = LogFactory.getLog(MoOAuthDownloadAppGuidesServlet.class);

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String guidePath="";
		LOGGER.debug("request.getAttributes = "+ request.getParameter("appName"));
		String appGuidesName = request.getParameter("appName");
		LOGGER.debug("appGuidesName : "+ appGuidesName);

		switch (appGuidesName) {
			case "Discord":
				guidePath= "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-discord";
				break;
			case "Facebook":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-facebook";
				break;
			case "GitLab":
				guidePath= "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-gitlab";
				break;
			case "Google":
				guidePath= "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-google-apps";
				break;
			case "Meetup":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-meetup";
				break;
			case "Slack":
				guidePath= "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-slack";
				break;
			case "GitHub":
				guidePath = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-github";
				break;
			case "GitHub Enterprise":
				guidePath = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-github-enterprise";
				break;
			case "Azure AD":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-azure-ad";
				break;
			case "Azure B2C":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-azure-b2c";
				break;
			case "Keycloak":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-keycloak";
				break;
			case "AWS Cognito":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-aws-cognito";
				break;
			case "ADFS":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-adfs";
				break;
			case "Okta":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-okta";
				break;
			case "Salesforce":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-salesforce";
				break;
			case "Gluu Server":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-gluu-server";
				break;
			case "Custom OpenID":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-any-openid-provider";
				break;
			case "Custom OAuth":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-bamboo-using-any-oauth-provider";
				break;
			case "miniOrange":
				guidePath="https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-into-bamboo-using-miniorange";
				break;
		}
		response.sendRedirect(guidePath);
	}

}
