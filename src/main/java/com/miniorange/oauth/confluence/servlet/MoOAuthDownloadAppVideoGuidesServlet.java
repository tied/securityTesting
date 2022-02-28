package com.miniorange.oauth.confluence.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MoOAuthDownloadAppVideoGuidesServlet extends HttpServlet {

	//public String guidePath = "";
	private static Log LOGGER = LogFactory.getLog(MoOAuthDownloadAppVideoGuidesServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String filename = "";
		LOGGER.debug("request.getAttributes = " + request.getParameter("appName"));
		String appGuidesName = request.getParameter("appName");
		LOGGER.debug("appGuidesName : " + appGuidesName);


		switch (appGuidesName) {
			case "Gitlab":
				filename = "https://www.youtube.com/watch?v=foo516xwRAI";
				break;
			case "Google":
				filename = "https://www.youtube.com/watch?v=4xoF8idhCU8";
				break;
			//case "Microsoft":
			//	filename = "Setup Guide for Microsoft App.pdf";
			//	break;
			case "GitHub":
				filename = "https://www.youtube.com/watch?v=31bE4_uMH3k";
				break;
			case "Azure AD":
				filename = "https://www.youtube.com/watch?v=fY_qNEB5PQI";
				break;
			case "Azure B2C":
				filename = "https://www.youtube.com/watch?v=y3aWXKkIo2g";
				break;
			case "ADFS":
				filename = "https://www.youtube.com/watch?v=tZ0DelGkU7k";
				break;
			case "AWS Cognito":
				filename = "https://www.youtube.com/watch?v=zUkq5sDF1AA";
				break;
			case "Keycloak":
				filename = "https://www.youtube.com/watch?v=LHdcalvmvOQ";
				break;
			case "Salesforce":
				filename = "https://www.youtube.com/watch?v=RKes_3xPDJ8";
				break;
			case "OKTA":
				filename = "https://www.youtube.com/watch?v=wgdHIiwvgrQ";
				break;

			case "Custom App":
				filename = "https://www.youtube.com/watch?v=qJl0LpF9W0g";
				break;
				
			case "OpenID":
				filename = "https://www.youtube.com/watch?v=86SR6AiOoRg";
				break;

			case "miniOrange":
				filename = "https://www.youtube.com/watch?v=DLMM10q6wtw";
				break;
		}

		response.sendRedirect(filename);

		return;

	}

}
