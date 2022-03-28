package com.miniorange.sso.saml.bamboo.action;


import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class MoOverviewAction extends BambooActionSupport {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MoOverviewAction.class);
    private String idpID;
    private String idpName;
    private String idpEntityId;
    private String ssoUrl;
    private String ssoBindingType;
    private String sloUrl;
    private String sloBindingType;
    private String x509Certificate;
    private String signedRequest;

    // Attribute Mapping
    private String username;
    private String email;
    private String useSeparateNameAttribute;
    private String fullName;
    private String firstNameAttribute;
    private String lastNameAttribute;
    private String keepExistingUserAttributes;
    private String regexPattern;
    private String regexPatternEnabled;
    private String loginUserAttribute;
    private Map<String, String> customAttributeMapping;

    // Group Mapping
    private String roleAttribute;
    private String createUsersIfRoleMapped;
    private String keepExistingUserRoles;
    private HashMap<String, String> roleMapping;
    private List<String> defaultGroups;
    private String enableDefaultGroupsFor;
    private String onTheFlyGroupMapping;
    private String createNewGroups;
    private String onTheFlyAssignNewGroupsOnly;
    private String onTheFlyDoNotRemoveGroups;
    private List<String> onTheFlyDoNotRemoveGroupsList;
    private List<String> certificates;
    private int numberOfCertificates;

    private MoIDPConfig idpConfig;
    private MoSAMLSettings settings;
    private MoPluginHandler pluginHandler;
    private UserManager userManager;
    private LoginUriProvider loginUriProvider;


    //Sp metadata pop-up
    private Map<String, String> certificateInfo;

    public MoOverviewAction(MoSAMLSettings settings, MoPluginHandler pluginHandler, MoIDPConfig idpConfig, UserManager userManager,
                            LoginUriProvider loginUriProvider) {
        this.settings = settings;
        this.pluginHandler = pluginHandler;
        this.idpConfig = idpConfig;
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
    }

    public void validate() {

        LOGGER.info("Overview Action doValidate called");
        super.validate();
    }

    @Override
    public String execute() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        final UserProfile user = userManager.getRemoteUser();

        if (user != null && userManager.isAdmin(user.getUserKey())) {
            try {
                if (!settings.isValidLicense()) {
                    LOGGER.error("No valid license found");
                    return "upm";
                }

                if (StringUtils.isBlank(this.idpID)) {
                    if (StringUtils.isNotBlank(request.getParameter("idpid"))) {
                        this.idpID = request.getParameter("idpid");
                    }
                }

                if (StringUtils.isBlank(idpID)) {
                    idpID = MoPluginConstants.DEFAULT_IDP_ID;
                }

                initializeIDPConfig();
                return "success";
            } catch (MoPluginException e) {
                LOGGER.error("An error occurred while saving your details. Please check logs for more info.", e);
                addActionError(e.getMessage());
                return "input";
            }
        } else {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return null;
        }
    }

    private void initializeIDPConfig() {
        idpConfig = pluginHandler.constructIdpConfigObject(this.idpID);
        if (idpConfig==null){
            initializeNewForm();
            return;
        }
        this.idpID = idpConfig.getId();
        this.idpName = idpConfig.getIdpName();
        this.idpEntityId = idpConfig.getIdpEntityId();
        this.ssoUrl = idpConfig.getSsoUrl();
        this.ssoBindingType = replaceString(idpConfig.getSsoBindingType());
        this.sloBindingType = replaceString(idpConfig.getSloBindingType());
        this.x509Certificate = StringUtils.defaultIfBlank(idpConfig.getX509Certificate(), StringUtils.EMPTY);
        this.sloUrl = idpConfig.getSloUrl();
        this.signedRequest = BooleanUtils.toStringYesNo(idpConfig.getSignedRequest());

        this.username = idpConfig.getUsernameAttribute();
        this.email = idpConfig.getEmailAttribute();
        this.useSeparateNameAttribute=BooleanUtils.toStringYesNo(idpConfig.getUseSeparateNameAttributes());
        this.fullName = idpConfig.getFullNameAttribute();
        this.firstNameAttribute = idpConfig.getFirstNameAttribute();
        this.lastNameAttribute = idpConfig.getLastNameAttribute();
        this.keepExistingUserAttributes=BooleanUtils.toStringYesNo(idpConfig.getKeepExistingUserAttributes());
        this.regexPattern = idpConfig.getRegexPattern();
        this.regexPatternEnabled = BooleanUtils.toStringYesNo(idpConfig.getRegexPatternEnabled());
        //this.customAttributeMapping = new TreeMap<>(idpConfig.getCustomAttributeMapping());

        this.loginUserAttribute = idpConfig.getLoginUserAttribute();
        this.roleAttribute = idpConfig.getRoleAttribute();
        this.createUsersIfRoleMapped = BooleanUtils.toStringYesNo(idpConfig.getCreateUsersIfRoleMapped());
        this.keepExistingUserRoles = BooleanUtils.toStringYesNo(idpConfig.getKeepExistingUserRoles());
        this.roleMapping = idpConfig.getRoleMapping();
        this.defaultGroups = idpConfig.getDefaultGroupsList();
        this.enableDefaultGroupsFor = replaceString(idpConfig.getEnableDefaultGroupsFor());
        this.onTheFlyGroupMapping = BooleanUtils.toStringYesNo(idpConfig.getOnTheFlyGroupCreation());
        this.createNewGroups = BooleanUtils.toStringYesNo(idpConfig.getCreateNewGroups());
        this.onTheFlyAssignNewGroupsOnly = BooleanUtils.toStringYesNo(idpConfig.getOnTheFlyAssignNewGroupsOnly());
        this.onTheFlyDoNotRemoveGroupsList = idpConfig.getOnTheFlyDoNotRemoveGroups();
        this.certificates = idpConfig.getCertificates();
        this.numberOfCertificates = idpConfig.getCertificates().size();
    }

    private void initializeNewForm() {
        this.idpName = StringUtils.EMPTY;
        this.idpEntityId = StringUtils.EMPTY;
        this.ssoUrl = StringUtils.EMPTY;
        this.ssoBindingType = "Http-redirect";
        this.sloUrl = StringUtils.EMPTY;
        this.sloBindingType = "Http-redirect";
        this.x509Certificate = StringUtils.EMPTY;
        this.x509Certificate = StringUtils.EMPTY;
        this.signedRequest = BooleanUtils.toStringYesNo(settings.getSignedRequest());

        this.username =settings.getUsernameAttribute();
        this.email = settings.getEmailAttribute();
        this.useSeparateNameAttribute=BooleanUtils.toStringYesNo(settings.getUseSeparateNameAttributes());
        this.fullName = settings.getFullNameAttribute();
        this.firstNameAttribute = StringUtils.EMPTY;
        this.lastNameAttribute = StringUtils.EMPTY;
        this.keepExistingUserAttributes=BooleanUtils.toStringYesNo(settings.getKeepExistingUserAttributes());
        this.regexPattern = StringUtils.EMPTY;
        this.regexPatternEnabled = BooleanUtils.toStringYesNo(settings.getRegexPatternEnabled());
        //this.customAttributeMapping = new TreeMap<>(settings.getCustomAttributeMapping());

        this.loginUserAttribute = settings.getLoginUserAttribute();
        this.roleAttribute = settings.getRoleAttribute();
        this.createUsersIfRoleMapped =  BooleanUtils.toStringYesNo(settings.getCreateUsersIfRoleMapped());
        this.keepExistingUserRoles = BooleanUtils.toStringYesNo(settings.getKeepExistingUserRoles());
        this.roleMapping = settings.getRoleMapping();
        this.defaultGroups = (List<String>) settings.getDefaultGroups();
        this.enableDefaultGroupsFor = replaceString(settings.getEnableDefaultGroupsFor());
        this.onTheFlyGroupMapping = BooleanUtils.toStringYesNo(settings.getOnTheFlyGroupMapping());
        this.createNewGroups = BooleanUtils.toStringYesNo(settings.getCreateNewGroups());
        this.onTheFlyAssignNewGroupsOnly = BooleanUtils.toStringYesNo(settings.getOnTheFlyAssignNewGroupsOnly());
        this.onTheFlyDoNotRemoveGroupsList = (List<String>) settings.getOnTheFlyDoNotRemoveGroups();
        this.certificates = idpConfig.getCertificates();
        this.numberOfCertificates = idpConfig.getCertificates().size();
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    private String replaceString(String replace) {

        String value = StringUtils.EMPTY;

        switch (replace) {
            case "HttpRedirect":
                value = "Http-redirect";
                break;
            case "HttpPost":
                value = "Http-post";
                break;
            case "newUsers":
                value = "New Users";
                break;
            case "allUsers":
                value = "All Users";
                break;
            case "doNotAssignDefaultGroup":
                value = "None";
        }
        return value;
    }


    public static Logger getLOGGER() {
        return LOGGER;
    }

    public String getIdpID() {
        return idpID;
    }

    public String getIdpName() {
        return idpName;
    }

    public String getIdpEntityId() {
        return idpEntityId;
    }

    public String getSsoUrl() {
        return ssoUrl;
    }

    public String getSsoBindingType() {
        return ssoBindingType;
    }

    public String getSloUrl() {
        return sloUrl;
    }

    public String getSloBindingType() {
        return sloBindingType;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getUseSeparateNameAttribute() {
        return useSeparateNameAttribute;
    }

    public String getFullName() { return fullName; }

    public String getFirstNameAttribute() {
        return firstNameAttribute;
    }

    public String getLastNameAttribute() {
        return lastNameAttribute;
    }

    public String getKeepExistingUserAttributes() {
        return keepExistingUserAttributes;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public String getRegexPatternEnabled() {
        return regexPatternEnabled;
    }

    public Map<String, String> getCustomAttributeMapping() {
        return customAttributeMapping;
    }

    public String getLoginUserAttribute() {
        return loginUserAttribute;
    }

    public String getRoleAttribute() {
        return roleAttribute;
    }

    public String getCreateUsersIfRoleMapped() {
        return createUsersIfRoleMapped;
    }

    public String getKeepExistingUserRoles() {
        return keepExistingUserRoles;
    }

    public HashMap<String, String> getRoleMapping() {
        return roleMapping;
    }

    public List<String> getDefaultGroups() {
        return defaultGroups;
    }


    public String getEnableDefaultGroupsFor() {
        return enableDefaultGroupsFor;
    }

    public String getOnTheFlyGroupMapping() {
        return onTheFlyGroupMapping;
    }

    public MoIDPConfig getIdpConfig() {
        return idpConfig;
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public MoPluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public Map<String, String> getCertificateInfo() {
        return MoSAMLUtils.getCertificateInfo(settings.getPublicSPCertificate());
    }

    public String getX509Certificate() {
        return x509Certificate;
    }

    public void setCertificateInfo(Map<String, String> certificateInfo) {
        this.certificateInfo = certificateInfo;
    }

    public String getSignedRequest() {
        return signedRequest;
    }

    public String getCreateNewGroups() {
        return createNewGroups;
    }

    public String getOnTheFlyAssignNewGroupsOnly() {
        return onTheFlyAssignNewGroupsOnly;
    }

    public String getOnTheFlyDoNotRemoveGroups() {
        return onTheFlyDoNotRemoveGroups;
    }

    public List<String> getOnTheFlyDoNotRemoveGroupsList() {
        return onTheFlyDoNotRemoveGroupsList;
    }

    public List<String> getCertificates(){
        return certificates;
    }
    public void setCertificates(List<String> certificates){
        this.certificates=certificates;
    }

    public int getNumberOfCertificates() {
        return numberOfCertificates;
    }
    public void setNumberOfCertificates( int numberOfCertificates){
        this.numberOfCertificates=numberOfCertificates;
    }
}

