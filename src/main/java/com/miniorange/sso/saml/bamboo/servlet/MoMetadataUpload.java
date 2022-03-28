package com.miniorange.sso.saml.bamboo.servlet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
import org.apache.struts2.dispatcher.multipart.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSAMLUtils;

public class MoMetadataUpload extends HttpServlet {

	private MoSAMLSettings settings;
	private String xsrfToken;
	private static final Logger LOGGER = LoggerFactory.getLogger(MoMetadataUpload.class);

	public MoMetadataUpload(MoSAMLSettings settings) {
		this.settings = settings;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("MoMetadataUpload doPost() method called");
		String fileContents = null;
		String uploadMetadataStatus = null;
		String idpID = null;
		String setupType = null;
		String idpName = null;
		String metadataOption = null;
		String customIDPName = null;
		String newIdpName = null;
		String finalIdpname;
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);
		try {
			settings.setSpConfigurationStatus(null);

			String urlSubmitted = request.getParameter("urlSubmitted");
			String fileSubmitted = request.getParameter("fileSubmitted");
			setupType = request.getParameter("setupType");

			MoSAMLSettings.isMetadataUpload = Boolean.TRUE;

			if (BooleanUtils.toBoolean(urlSubmitted)) {
				idpID = request.getParameter("idpID").trim();
				idpName = request.getParameter("idpName").trim();
				String metadataUrlString = request.getParameter("inputUrl").trim();
				metadataOption = request.getParameter("metadataOption");
				String effectiveUrl = getIdpMetadataUrl(metadataUrlString, metadataOption);
				Boolean hasError = validateSPConfigurationImportMetadataUrl(request,effectiveUrl);

				if (!hasError) {
					try {
						MoPluginHandler.fetchMetadata(idpID, idpName, metadataUrlString, effectiveUrl, metadataOption);
						request.getSession().setAttribute("success", "success");
					} catch (MoPluginException e) {
						if (e.getErrorCode() == MoPluginException.PluginErrorCode.METADATA_PARSE) {
							request.getSession().setAttribute("error", "An error occurred while parsing metadata.");
						} else {
							request.getSession().setAttribute("error", "Connection to Metadata URL failed. Download the metadata file and upload it using Import From Metadata option.");
						}
					}
				}
				response.sendRedirect("addidp.action?idpid="+idpID);

			} else if (BooleanUtils.toBoolean(fileSubmitted)) {

				MultiPartRequestWrapper wrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();
				UploadedFile[] files = wrapper.getFiles("xmlFile");

				if (files != null) {
					idpID = wrapper.getParameter("idpID").trim();
					idpName = wrapper.getParameter("idpName").trim();
					metadataOption = wrapper.getParameter("metadataOption");
					customIDPName = wrapper.getParameter("customIdpName");
					newIdpName = wrapper.getParameter("newIdpname");

					if (StringUtils.isEmpty(idpID)) {
						idpID = UUID.randomUUID().toString();
					}

					if (StringUtils.equalsIgnoreCase(customIDPName, "yes") && StringUtils.isNotBlank(newIdpName)) {
						finalIdpname = newIdpName;
					} else {
						finalIdpname = idpName;
					}

					for (int var6 = 0; var6 < (files.length); ++var6) {
						UploadedFile file = files[var6];
						if (file.isFile()) {
							try {
								fileContents = FileUtils.readFileToString((File) file.getContent());
								fileContents = fileContents.replaceAll("[^\\x20-\\x7e]", "");
								if(StringUtils.isEmpty(fileContents)){
									request.getSession().setAttribute("error", "Selected metadata file is empty.");
									//settings.setSpConfigurationStatus("Selected metadata file is empty.");
								} else {
									uploadMetadataStatus = setAllFields(idpID, finalIdpname, fileContents, metadataOption);
									if(StringUtils.contains(uploadMetadataStatus, "error")){
										request.getSession().setAttribute("error", "Invalid IDP Metadata provided. Could not parse metadata.");
									} else if (StringUtils.contains(uploadMetadataStatus, "success")) {
										request.getSession().setAttribute("success", "success");
									}
								}
							} catch (Exception var9) {
								request.getSession().setAttribute("error", "Invalid IDP Metadata provided. Could not parse metadata.");

							}

							if(StringUtils.equalsIgnoreCase(setupType, "manualSetup")) {
								LOGGER.debug("manual setup flow... succeed :" + uploadMetadataStatus);
								response.sendRedirect("addidp.action?idpid=" + idpID);

							}else if(StringUtils.equalsIgnoreCase(setupType, "quickSetup")){

								request.getSession().setAttribute("idp", idpName);
								request.getSession().setAttribute("idpid", idpID);
								request.getSession().setAttribute("customIdpname", customIDPName);
								request.getSession().setAttribute("newIdpname", newIdpName);
								request.getSession().setAttribute("metadataOption", metadataOption);
								response.sendRedirect("flowdrivensetup.action?idp=" + idpName);
							}
							return;
						}
					}
				}else{
					LOGGER.debug("no file found...");
					if(StringUtils.equalsIgnoreCase(setupType, "manualSetup")) {
						request.getSession().setAttribute("error", "Selected metadata file is empty.");
						response.sendRedirect("addidp.action?idpid="+idpID);

					}else if(StringUtils.equalsIgnoreCase(setupType, "quickSetup")){
						idpName = wrapper.getParameter("idpName").trim();
						request.getSession().setAttribute("idp", idpName);
						request.getSession().setAttribute("error", "Selected metadata file is empty.");
						response.sendRedirect("flowdrivensetup.action?idp=" + idpName);
					}
				}
			} else {
				request.getSession().setAttribute("error", "Connection to Metadata URL failed. Download the metadata file and upload it using Import From Metadata option.");
				response.sendRedirect("addidp.action?idpid="+idpID);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			request.getSession().setAttribute("error", "Connection to Metadata URL failed. Download the metadata file and upload it using Import From Metadata option.");
			//settings.setSpConfigurationStatus("Connection to Metadata URL failed. Download the metadata file and upload it using Import From Metadata option.");
			if(StringUtils.equals(setupType, "manualSetup")) {
				response.sendRedirect("addidp.action?idpid="+idpID);
			}else if(StringUtils.equals(setupType, "quickSetup")){
				response.sendRedirect("flowdrivensetup.action?idp=" + idpName);
			}
		}
	}

	private String setAllFields(String idpID, String idpName, String fileContents, String metadataOption) {
		HashMap<String, Object> attrs = MoSAMLUtils.parseXml(fileContents);
		if(((String) attrs.get("message")).equals("error") || (attrs.get("idpEntityId") == null) ||
				(attrs.get("singleSignOnUrl") == null) || attrs.get("X509Certificate") == null || attrs.get("allX509Certificates") ==null ||
				(((List<String>) attrs.get("allX509Certificates")).size() ==0)){
			return "error";
		} else {
			String idpEntityId = (String) attrs.get("idpEntityId");
			String ssoBinding = (String) attrs.get("ssoBinding");
			String singleSignOnUrl = (String) attrs.get("singleSignOnUrl");
			String sloBinding = (String) attrs.get("sloBinding");
			String singleLogoutUrl = (String) attrs.get("singleLogoutUrl");
			List<String> allX509Certificates = (List<String>) attrs.get("allX509Certificates");
			String x509Certificate = (String) attrs.get("X509Certificate");
			String nameIdFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
			String authnContextClass = "";
			String otherAuthnContextClass = "";
			Boolean enableSsoForIdp = Boolean.TRUE;
			Boolean isRequestSigned = Boolean.TRUE;
//			if(settings.getIdpEntityId() != null && settings.getSsoBindingType() != null && settings.getSloBindingType() != null
//					&& settings.getSsoServiceUrl() != null && settings.getSloServiceUrl() != null &&
//					settings.getAllX509Certificates() != null && settings.getX509Certificate() != null) {

			settings.setMetadataOption(idpID, StringUtils.defaultIfBlank(metadataOption, StringUtils.EMPTY));
			MoPluginHandler.saveIdPConfiguration(idpID, idpName, idpEntityId, ssoBinding, singleSignOnUrl,
						sloBinding, singleLogoutUrl, allX509Certificates, x509Certificate, isRequestSigned,
					nameIdFormat, enableSsoForIdp);
			//MoPluginHandler.saveImportMetadata(idpID, "", "", false, "hourly", 60, "minutes");
			MoPluginHandler.saveAdvancedOption(idpID, true,false,"", "", false,
					"hourly", 60, "minutes", "", MoPluginConstants.FORCE_REDIRECT,true);
			MoPluginHandler.toggleSchedulerService(idpID);
			return "success";
//			}
		}
//		return null;
	}			
	public MoSAMLSettings getSettings() {
		return settings;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	private String getIdpMetadataUrl(String inputUrl, String metadataOption) {
		String getIdpMetadataUrl = StringUtils.EMPTY;
		switch (metadataOption) {
			case "ADFS":
				getIdpMetadataUrl = "https://"+inputUrl+"/federationmetadata/2007-06/federationmetadata.xml";
				break;
			case "Azure AD":
				getIdpMetadataUrl = "https://login.microsoftonline.com/"+inputUrl+"/FederationMetadata/2007-06/FederationMetadata.xml";
				break;
			default:
				getIdpMetadataUrl = inputUrl;
				break;
		}
		return getIdpMetadataUrl;
	}

	private Boolean validateSPConfigurationImportMetadataUrl(HttpServletRequest request, String importUrl) throws IOException {
		LOGGER.debug("Validating SP Configuration Data.");
		if (StringUtils.isBlank(importUrl)) {
			//settings.setSpConfigurationStatus("Import Metadata URL can not be left blank.");
			request.getSession().setAttribute("error", "Import Metadata URL can not be left blank.");
			return Boolean.TRUE;
		} else {
			try {
				new URL(importUrl);
			} catch (MalformedURLException e) {
				//settings.setSpConfigurationStatus("Invalid Metadata URL.");
				request.getSession().setAttribute("error", "Invalid Metadata URL.");
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
}
