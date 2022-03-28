package com.miniorange.sso.saml.bamboo.servlet;

import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MoCustomMetadata extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoCustomMetadata.class);

    private String customOrganizationName;
    private String customOrganizationDisplayName;
    private String customOrganizationUrl;
    private String technicalContactName;
    private String technicalContactEmail;
    private String supportContactName;
    private String supportContactEmail;
    private Boolean signing;
    private Boolean encryption;

    private MoSAMLSettings settings;

    public MoCustomMetadata(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException {
        try{
            LOGGER.debug("Inside MoCustomMetadata servlet");
            LOGGER.debug("Request : " + request.getParameterMap().toString());
            LOGGER.debug("Data from form : " + request.getParameter("customOrganizationName") +" "+ request.getParameter("customOrganizationDisplayName")+" "+
                    request.getParameter("customOrganizationUrl")+" "+request.getParameter("technicalContactName")+" "+request.getParameter("technicalContactEmail")+" "+
                    request.getParameter("supportContactName")+" "+request.getParameter("supportContactEmail")+" "+BooleanUtils.toBooleanObject(request.getParameter("signing"))+" "+
                    BooleanUtils.toBooleanObject(request.getParameter("encryption")));
            this.customOrganizationName = request.getParameter("customOrganizationName");
            this.customOrganizationDisplayName = request.getParameter("customOrganizationDisplayName");
            this.customOrganizationUrl = request.getParameter("customOrganizationUrl");
            this.technicalContactName = request.getParameter("technicalContactName");
            this.technicalContactEmail = request.getParameter("technicalContactEmail");
            this.supportContactName = request.getParameter("supportContactName");
            this.supportContactEmail = request.getParameter("supportContactEmail");
            this.signing = BooleanUtils.toBooleanObject(request.getParameter("signing"));
            this.encryption= BooleanUtils.toBooleanObject(request.getParameter("encryption"));
            MoPluginHandler.saveSPCertificates(this.signing, this.encryption);
            MoPluginHandler.saveCustomMetadata(this.customOrganizationName,this.customOrganizationDisplayName,this.customOrganizationUrl,this.technicalContactName,this.technicalContactEmail,this.supportContactName,this.supportContactEmail);

        }catch (Exception e){
            e.printStackTrace();
            LOGGER.debug("error occurred while saving custom metadata");
        }
    }

}
