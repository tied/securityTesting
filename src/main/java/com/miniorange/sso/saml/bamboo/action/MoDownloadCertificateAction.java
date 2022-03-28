package com.miniorange.sso.saml.bamboo.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;

public class MoDownloadCertificateAction extends BambooActionSupport{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MoDownloadCertificateAction.class);
	private MoSAMLSettings settings;

	public MoDownloadCertificateAction(MoSAMLSettings settings){
		this.settings = settings;
	}

	@Override
	public String execute() throws Exception {
		LOGGER.debug("Download Certificate action execute() called");
		try {
			HttpServletRequest request = ServletActionContext.getRequest();
			HttpServletResponse response = ServletActionContext.getResponse();
			
			if(settings.isValidLicense()) {
				String certificate = settings.getPublicSPCertificate();
				response.setHeader("Content-Disposition", "attachment; filename=\"sp-certificate.crt\"");
				response.setHeader("Cache-Control", "max-age=0");
				response.setHeader("Pragma", "");
				response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				response.getOutputStream().write(certificate.getBytes());
				return null;
			}
		} catch (Exception e) {
			LOGGER.error("An error occurred while downloading the certificate.", e);
		}
		return "input";
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}
}
