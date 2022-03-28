package com.miniorange.sso.saml.bamboo.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class MoDownloadIdpGuidesServlet extends HttpServlet {
	public String guidePath=""; 
	private static Log LOGGER = LogFactory.getLog(MoDownloadIdpGuidesServlet.class);
	MoSAMLSettings settings;

	public MoDownloadIdpGuidesServlet (MoSAMLSettings settings){
		this.settings = settings;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.debug("Download IdP guides: doGet called.");
		String idpGuidesURL="";
		String idpGuidesName = request.getParameter("idpGuides");

		LOGGER.debug("Requested IdP Guide = "+idpGuidesName);

		if (StringUtils.equalsIgnoreCase(idpGuidesName, "ADFS")) {
			idpGuidesURL= "https://plugins.miniorange.com/saml-single-sign-on-for-bamboo-using-adfs";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "Azure AD")) {
			idpGuidesURL="http://plugins.miniorange.com/saml-single-sign-into-bamboo-using-azure-ad";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "Bitium")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-bitium";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "Centrify")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-centrify";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "GoogleApps")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-google-apps";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "JBoss Keycloak")) {
			idpGuidesURL = "https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-jboss-keyclock";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "miniOrange")) {
			idpGuidesURL = "https://plugins.miniorange.com/saml-single-sign-into-bamboo-using-miniorange";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "Okta")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-okta";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "OneLogin")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-onelogin";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "OpenAM")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-openam";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "Oracle(OEM)")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-oracle-enterprise-manager";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "PingFederate")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-ping-federate";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "Ping One")) {
			idpGuidesURL="http://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-ping-one";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "Salesforce")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-salesforce";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "Simplesaml")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-simplesaml";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "RSA SecurID")) {
			idpGuidesURL="http://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-rsa-securid";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "Shibboleth 2")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-into-bamboo-using-shibboleth-2";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "Shibboleth 3")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-shibboleth-3";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "WSO2")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-wso2";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "AuthAnvil")) {
			idpGuidesURL="http://plugins.miniorange.com/saml-single-sign-on-sso-for-bamboo-using-authanvil";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "Auth0")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-sso-into-bamboo-using-auth0";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "CA Identity")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-into-bamboo-using-ca-identity";
		}
		if (StringUtils.equalsIgnoreCase(idpGuidesName, "AWS")) {
			idpGuidesURL="https://plugins.miniorange.com/saml-single-sign-on-sso-into-bamboo-using-aws-idp";
		}

		response.sendRedirect(idpGuidesURL);

		return;
	}
}