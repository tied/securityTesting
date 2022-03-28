package com.miniorange.sso.saml.bamboo.servlet;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.scheduler.config.JobId;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.bamboo.schedulers.MoMetadataJobRunnerImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MoDeleteIdpServlet extends HttpServlet {

    private MoSAMLSettings settings;
    MoMetadataJobRunnerImpl metadataJobRunnerImpl;
    private String xsrfToken;

    private ArrayList<String> idpList;
    private HashMap<String,String> idpMap;
    private Boolean useDomainMapping;
    private List<String> domainsList;

    private static Log LOGGER = LogFactory.getLog(MoDeleteIdpServlet.class);

    public MoDeleteIdpServlet(MoSAMLSettings settings, MoMetadataJobRunnerImpl metadataJobRunnerImpl){
        this.settings = settings;
        this.metadataJobRunnerImpl = metadataJobRunnerImpl;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.debug("MoDeleteIdpServlet : execute called");

        xsrfToken = XsrfTokenUtils.getXsrfToken(request);
        try {
            String idpID = request.getParameter("idpID");
            String status = StringUtils.EMPTY;
            if(StringUtils.isNotBlank(idpID)) {
                HashMap<String,String> idpMap = settings.getIdpMap();
                if(idpMap == null || !idpMap.containsKey(idpID)) {
                    status="missing";
                } else {
                    settings.removeIdp(idpID);
                    settings.removeRedirectionRulesForIdp(idpID);
                    if(StringUtils.equals(settings.getDefaultBambooIDP(),idpID))
                        settings.setDefaultBambooIDP("loginPage");
                    metadataJobRunnerImpl.deleteSchedule(idpID);
                    status = "success";
                }
            } else {
                status = "empty";
            }

            if (settings.getIdPList().size() == 1) {
                settings.setUseDomainMapping(Boolean.FALSE);
            }

            LOGGER.debug("MoDeleteIdpServlet : Before Redirecting to listidp");
            response.sendRedirect("listidp.action?operation=delete"+"&status="+status);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("error", "There is some problem for deleting IDP. Please contact to administrator.");
            response.sendRedirect("listidp.action?operation=delete"+"&status=error");
            return;
        }
    }

    private static JobId toJobId(String jobID) {
        return JobId.of("Job ID =" + jobID);
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public MoMetadataJobRunnerImpl getMetadataJobRunnerImpl() {
        return metadataJobRunnerImpl;
    }

    public void setMetadataJobRunnerImpl(MoMetadataJobRunnerImpl metadataJobRunnerImpl) {
        this.metadataJobRunnerImpl = metadataJobRunnerImpl;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    public ArrayList<String> getIdpList() {
        idpList = settings.getIdPList();

        return this.idpList;
    }

    public void setIdpList(ArrayList<String> idpList) {
        this.idpList = idpList;
    }

    public HashMap<String, String> getIdpMap() {
        this.idpMap = settings.getIdpMap();
        return idpMap;
    }

    public void setIdpMap(HashMap<String, String> idpMap) {
        this.idpMap = idpMap;
    }

    public Boolean getUseDomainMapping() {
        this.useDomainMapping = settings.getUseDomainMapping();
        return useDomainMapping;
    }

    public void setUseDomainMapping(Boolean useDomainMapping) {
        this.useDomainMapping = useDomainMapping;
    }

    public List<String> getDomainsList() {
        this.domainsList = settings.getDomains();
        return domainsList;
    }

    public void setDomainsList(List<String> domainsList) {
        this.domainsList = domainsList;
    }
}
