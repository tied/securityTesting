package com.miniorange.oauth.confluence.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MoOAuthDownloadAppGuidesServlet extends HttpServlet {

	//public String guidePath = "";
	private static Log LOGGER = LogFactory.getLog(MoOAuthDownloadAppGuidesServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String filename = "";
		LOGGER.debug("request.getAttributes = " + request.getParameter("appName"));
		String appGuidesName = request.getParameter("appName");
		LOGGER.debug("appGuidesName : " + appGuidesName);


		switch (appGuidesName) {
			case "Discord":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-discord";
				break;
			case "Facebook":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-facebook";
				break;
			case "Gitlab":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-gitlab";
				break;
			case "Google":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-google-apps";
				break;
			case "Meetup":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-meetup";
				break;
			case "Slack":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-slack";
				break;
			//case "Microsoft":
			//	filename = "Setup Guide for Microsoft App.pdf";
			//	break;
			case "GitHub":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-github";
				break;
			case "GitHub Enterprise":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-github-enterprise";                break;
			case "Azure AD":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-azure-ad";
				break;
			case "Azure B2C":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-azure-b2c";
				break;
			case "ADFS":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-adfs";
				break;
			case "AWS Cognito":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-aws-cognito";
				break;
			case "Keycloak":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-keycloak";
				break;
			case "Salesforce":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-salesforce";
				break;
			case "OKTA":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-okta";
				break;

			case "Custom App":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-any-oauth-provider";
				break;
				
			case "OpenID":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-any-openid-provider";
				break;

			case "miniOrange":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-into-confluence-using-miniorange";
				break;
			case "Gluu Server":
				filename = "https://plugins.miniorange.com/oauth-openid-single-sign-on-sso-confluence-using-gluu-server";
				break;
		}

		response.sendRedirect(filename);

		return;

	}

}
