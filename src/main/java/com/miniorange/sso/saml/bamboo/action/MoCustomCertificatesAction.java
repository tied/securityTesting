package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.plugin.PluginException;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.sal.api.user.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MoCustomCertificatesAction extends BambooActionSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoCustomCertificatesAction.class);

    private UserManager userManager;
    private String publicSPCertificate;
    private String privateSPCertificate;
    private Boolean certificateSubmitted;

    private MoSAMLSettings settings;
    private String xsrfToken;
    private LoginUriProvider loginUriProvider;
    private String emailAdsress;
    private String companyName;
    private String orgUnit;
    private String locationName;
    private String countryCode;
    private String validityDays;

    public MoCustomCertificatesAction(UserManager userManager, MoSAMLSettings settings, LoginUriProvider loginUriProvider) {
        this.userManager = userManager;
        this.settings = settings;
        this.loginUriProvider = loginUriProvider;
    }

    public void validate() {
        HttpServletRequest request = ServletActionContext.getRequest();
        if (BooleanUtils.toBoolean(certificateSubmitted)) {
            if (!StringUtils.isBlank(this.publicSPCertificate) && !MoSAMLUtils.isValidPublicCertificate(this.publicSPCertificate)) {
                addActionError("Invalid public certificate.");
            }
            if (!StringUtils.isBlank(this.privateSPCertificate) && !MoSAMLUtils.isValidPrivateCertificate(this.privateSPCertificate)) {
                addActionError("Invalid private certificate.");
            }
            if (super.hasActionErrors()) {
                xsrfToken = XsrfTokenUtils.getXsrfToken(request);
                initializeSAMLConfig();
            }
        }
    }

    @Override
    public String execute() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        xsrfToken = XsrfTokenUtils.getXsrfToken(request);

        final UserProfile user = userManager.getRemoteUser();

        if (user != null && userManager.isAdmin(user.getUserKey())) {
            try {
                if (!settings.isValidLicense()) {
                    return "upm";
                }

                String operation = request.getParameter("operation");
                if ("updated".equalsIgnoreCase(operation)) {
                    addActionMessage(getText("Public and Private Certificate updated"));
                }
                if (BooleanUtils.toBoolean(certificateSubmitted)) {
                    if (!validateCertificate(this.publicSPCertificate,this.privateSPCertificate)) {
                        MoPluginHandler.saveSPCertificates(this.publicSPCertificate, this.privateSPCertificate);
                        addActionMessage(getText("samlsso.success.config"));

                    }
                }
                initializeSAMLConfig();
                return "success";
            } catch (PluginException e) {
                e.printStackTrace();
                addActionError(e.getMessage());
                return "input";
            } catch (Exception e) {
                e.printStackTrace();
                addActionError("An error occurred while saving your details. Please check logs for more info.");
                return "input";
            }
        } else {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return null;
        }
    }

    private void initializeSAMLConfig() {
        this.publicSPCertificate = settings.getPublicSPCertificate();
        this.privateSPCertificate = settings.getPrivateSPCertificate();
        this.emailAdsress=StringUtils.EMPTY;
        this.companyName=StringUtils.EMPTY;
        this.countryCode=StringUtils.EMPTY;
        this.orgUnit=StringUtils.EMPTY;
        this.validityDays=StringUtils.EMPTY;
        this.locationName=StringUtils.EMPTY;
    }

    Boolean validateCertificate(String publicSPCertificate, String privateSPCertificate){

        Boolean error = Boolean.FALSE;
        if (StringUtils.isNotBlank(publicSPCertificate) && !MoSAMLUtils.isValidPublicCertificate(publicSPCertificate)) {
            addActionError("Invalid public certificate.");
            error = Boolean.TRUE;
        }
        if (StringUtils.isNotBlank(privateSPCertificate) && !MoSAMLUtils.isValidPrivateCertificate(privateSPCertificate)) {
            addActionError("Invalid private certificate.");
            error = Boolean.TRUE;
        }
        if(StringUtils.isBlank(privateSPCertificate) || StringUtils.isBlank(publicSPCertificate)){
            addActionError("Certificate cannot be blank.");
            error = Boolean.TRUE;
        }
        return error;
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    public HashMap<String, String> getIdpMap() {
        return settings.getIdpMap();
    }

    public ArrayList<String> getIdpList() {
        return settings.getIdPList();
    }

    public String getPublicSPCertificate() {
        return publicSPCertificate;
    }

    public void setPublicSPCertificate(String publicSPCertificate) {
        this.publicSPCertificate = publicSPCertificate;
    }

    public String getPrivateSPCertificate() {
        return privateSPCertificate;
    }

    public void setPrivateSPCertificate(String privateSPCertificate) {
        this.privateSPCertificate = privateSPCertificate;
    }

    public Boolean getCertificateSubmitted() {
        return certificateSubmitted;
    }

    public void setCertificateSubmitted(Boolean certificateSubmitted) {
        this.certificateSubmitted = certificateSubmitted;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
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

    public LoginUriProvider getLoginUriProvider() {
        return loginUriProvider;
    }

    public void setLoginUriProvider(LoginUriProvider loginUriProvider) {
        this.loginUriProvider = loginUriProvider;
    }

    public String getEmailAdsress() {
        return emailAdsress;
    }

    public void setEmailAdsress(String emailAdsress) {
        this.emailAdsress = emailAdsress;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(String orgUnit) {
        this.orgUnit = orgUnit;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(String validityDays) {
        this.validityDays = validityDays;
    }
}
