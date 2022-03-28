package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.plugin.PluginException;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import com.miniorange.sso.saml.dto.MoIDPMetadata;
import com.miniorange.sso.saml.utils.MoHttpUtils;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.apache.struts2.RequestUtils.getUri;

public class MoFlowDrivenSetupAction extends BambooActionSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoFlowDrivenSetupAction.class);

    /**
     * variables common to all forms
     **/
    private MoPluginHandler moPluginHandler;
    private MoSAMLSettings settings;
    private LoginUriProvider loginUriProvider;
    private String pageNumber;
    private String setupGuideLink;
    private Boolean error = Boolean.FALSE;
    private String idpGuide;
    private UserManager userManager;
    private String xsrfToken;
    private Boolean resetPageNumber;

    /**
     * variables for sp metadata form
     **/
    private Boolean spMetaDataSubmitted;
    private Boolean isJCEInstalled;
    private Map<String, String> certificateInfo;
    private Boolean updatedUrls;
    private String updatedSpBaseUrl;
    private String updatedSpEntityId;

    /**
     * variables for configure idp form
     **/
    private Boolean configureIdpSubmitted;
    private Boolean foundIdpConfiguration = Boolean.FALSE;
    private Boolean testConfigPerformed = Boolean.FALSE;
    private Boolean configureIdpNextSubmitted;
    private String metadataOption;
    private String customIdpName;
    private String inputUrl;
    private String idpName;
    private String newIdpname;
    private String idpID;
    private String x509Certificate;
    private String[] x509AllCertificates;
    private List<String> certificates;
    private String idpEntityId;
    private String ssoUrl;
    private String ssoBindingType;
    private Boolean signedRequest;
    private String spBaseUrl;

    /**
     * variables for user profile form
     **/
    private Boolean userProfileSubmitted;
    private String userNameAttribute;
    private String userEmailAttribute;
    private String fullNameAttribute;
    private String firstNameAttribute;
    private String lastNameAttribute;
    private String loginAttribute;
    private String nameOption;
    private Boolean useSeparateNameAttributes;


    /**
     * variables for user groups form
     **/
    private Boolean userGroupsSubmitted;
    private String[] defaultGroups;
    private String defaultGroup;
    private List<String> defaultGroupsList;
    private String enableDefaultGroupsFor;

    /**
     * variables for test config form
     **/
    private Boolean testConfigSubmitted;
    private Map<String, List<String>> attributesMap;
    private String displaySamlResponse;
    private String displaySamlRequest;

    public MoFlowDrivenSetupAction(MoPluginHandler moPluginHandler, MoSAMLSettings settings, LoginUriProvider loginUriProvider, UserManager userManager) {
        this.moPluginHandler = moPluginHandler;
        this.settings = settings;
        this.loginUriProvider = loginUriProvider;
        this.userManager = userManager;
    }

    private void doValidate(){
        this.error = false;

        if (BooleanUtils.toBoolean(this.spMetaDataSubmitted)) {
            LOGGER.debug("Sp metadata validation");
        } else if (BooleanUtils.toBoolean(this.configureIdpSubmitted)) {
            LOGGER.debug("Configure IDP validation");
            if (StringUtils.equalsIgnoreCase(this.customIdpName,"yes") && StringUtils.isBlank(this.newIdpname)){
                error = Boolean.TRUE;
                addActionError(getText("samlsso.error.config.metadata.custom.name.empty"));
            }
            if (StringUtils.isEmpty(this.metadataOption)) {
                error = Boolean.TRUE;
                addActionError(getText("samlsso.error.config.metadata.option.empty"));
            }
            if (StringUtils.isNotEmpty(this.metadataOption) && StringUtils.equalsIgnoreCase(this.metadataOption, "manual")) {
                if (StringUtils.isBlank(this.ssoUrl)) {
                    addActionError(getText("samlsso.error.config.ssourl.empty"));
                    error = true;
                } else {
                    try {
                        new URL(this.ssoUrl);
                    } catch (MalformedURLException e) {
                        LOGGER.error("Invalid URL", e);
                        addActionError(getText("samlsso.error.config.ssourl.malformed"));
                        error = true;
                    }
                }
                if (StringUtils.isBlank(this.idpEntityId)) {
                    addActionError(getText("samlsso.error.config.idpentityid.empty"));
                    error = true;
                }
                //Empty certificates array shouldn't be validated. Hence removing all the empty Strings
                if (x509AllCertificates != null) {
                    certificates = new ArrayList<String>(Arrays.asList(x509AllCertificates));
                    certificates.removeAll(Arrays.asList(""));
                    x509AllCertificates = certificates.toArray(new String[0]);
                }
                if (x509AllCertificates != null) {
                    certificates = new ArrayList<String>(Arrays.asList(x509AllCertificates));
                    certificates.removeAll(Arrays.asList(""));
                    x509AllCertificates = certificates.toArray(new String[0]);
                }
                if (x509AllCertificates != null && x509AllCertificates.length > 0) {
                    if (x509AllCertificates.length == 1) {
                        if (org.apache.commons.lang.StringUtils.isBlank(x509AllCertificates[0])) {
                            addActionError(getText("samlsso.error.config.certificate.empty"));
                            error = true;
                        } else if (!MoSAMLUtils.isValidPublicCertificate(x509AllCertificates[0]).booleanValue()) {
                            addActionError(getText("samlsso.error.config.certificate.invalid"));
                            error = true;
                        }
                    } else {
                        for (String certificate : x509AllCertificates) {
                            if (!MoSAMLUtils.isValidPublicCertificate(certificate).booleanValue()
                                    && !org.apache.commons.lang.StringUtils.isBlank(certificate)) {
                                addActionError("Invalid Signing Certificate "
                                        + ((Arrays.asList(x509AllCertificates).indexOf(certificate) + 1)));
                                error = true;
                            }
                        }
                    }
                } else {
                    if (org.apache.commons.lang.StringUtils.isBlank(this.x509Certificate)) {
                        addActionError(getText("samlsso.error.config.certificate.empty"));
                        error = true;
                    } else if (!MoSAMLUtils.isValidPublicCertificate(this.x509Certificate).booleanValue()) {
                        addActionError(getText("samlsso.error.config.certificate.invalid"));
                        error = true;
                    }
                }
            }
        } else if (BooleanUtils.toBoolean(this.userProfileSubmitted)) {
            LOGGER.debug("User Profile validation");
        } else if (BooleanUtils.toBoolean(this.userGroupsSubmitted)) {
            LOGGER.debug("User Groups validation");
            if (this.defaultGroups != null && this.defaultGroups.length <= 0) {
                LOGGER.error("Default group is blank. Please select atleast one default group.");
                addActionError(getText("samlsso.error.config.defaultgroup.empty"));
                error = true;
            }
        } else if (BooleanUtils.toBoolean(this.testConfigSubmitted)) {
            LOGGER.debug("Test Configuration validation");
        } else {

        }
        if (BooleanUtils.toBoolean(error)) {
            initialiseSAMLConfig(ServletActionContext.getRequest());
        }
    }

    public String execute() throws Exception {
        this.resetPageNumber = false;
        LOGGER.debug("Add IDP Action execute() called");

        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        xsrfToken = XsrfTokenUtils.getXsrfToken(request);
        LOGGER.debug("REFERER called"+ request.getHeader("referer"));
        if(StringUtils.containsIgnoreCase(request.getHeader("referer"),"supportedidps.action")){
            //this is to identify whether request is made for adding new IDP or for continuing the setup
            this.resetPageNumber = true;
        }


        final UserProfile user = userManager.getRemoteUser();

        if (user != null && userManager.isAdmin(user.getUserKey())) {
            try {
                LOGGER.info("FlowDrivenSetup Action doExecute called");
                if (!settings.isValidLicense()) {
                    LOGGER.error("No valid license found");
                    return "upm";
                }

                if(StringUtils.isNotBlank(request.getParameter("idp"))) {
                    this.idpName = request.getParameter("idp");
                }

                doValidate();
                if (StringUtils.isNotEmpty(request.getParameter("idpid"))) {
                    this.idpID = request.getParameter("idpid");
                    String lastPageSubmitted = getIdpQuickSetupOptions(this.idpID, "pageNumber");
                    int lastPageNumber = 2;
                    if(StringUtils.isNotBlank(lastPageSubmitted)){
                        lastPageNumber = Integer.parseInt(lastPageSubmitted);
                    }else if( StringUtils.isNotBlank(request.getParameter("pageNumber"))){
                        lastPageNumber = Integer.parseInt(request.getParameter("pageNumber"));
                    }

                    if (lastPageNumber == 2 && !settings.checkIfTestConfig(this.idpID)) {
                        /**
                         * This is the case where user has added the IDP in step 2 but has not performed test config and has gone back to
                         * common settings. When he clicks on Continue Quick Setup, he should be taken to step 2 so that he has a chance to
                         * perform test config. Typical behaviour is Continue Quick Setup will take user to the next form, not the one he was
                         * previously on.
                         */
                        settings.setPageNumber(Integer.toString(lastPageNumber));
                    } else if (lastPageNumber == 2 && settings.checkIfTestConfig(this.idpID) &&
                            StringUtils.isNotEmpty(getIdpQuickSetupOptions(this.idpID, "attributeMappingSubmitted"))) {
                        /**
                         * This is the case where the user adds IDP using Quick Setup, but configures User Attributes using Manual Setup.
                         * Here the Continue option will still be visible, but it should skip step 3(User Profile) and go directly to
                         * (User Groups)
                         */
                        LOGGER.debug("case 2");
                        settings.setPageNumber(Integer.toString(lastPageNumber + 2));
                    } else {
                        /**
                         * In all other cases the user will be taken to the form that comes after the one he was previously on.
                         */
                        LOGGER.debug("case 3");
                        settings.setPageNumber(Integer.toString(lastPageNumber + 1));
                    }
                }

                if (BooleanUtils.toBoolean(this.spMetaDataSubmitted) && !error) {
                    settings.setPageNumber("2");
                    LOGGER.debug("Sp metadata submitted");

                } else if (MoSAMLSettings.isMetadataUpload) {

                    MoSAMLSettings.isMetadataUpload = Boolean.FALSE;
                    if(request.getSession().getAttribute("idpid") != null){
                        idpID = (String)request.getSession().getAttribute("idpid");
                        request.getSession().removeAttribute("idpid");
                    }
                    if(request.getSession().getAttribute("customIdpname") != null){
                        customIdpName = (String)request.getSession().getAttribute("customIdpname");
                        request.getSession().removeAttribute("customIdpname");
                    }
                    if(request.getSession().getAttribute("newIdpname") != null){
                        newIdpname = (String)request.getSession().getAttribute("newIdpname");
                        request.getSession().removeAttribute("newIdpname");
                    }
                    if(request.getSession().getAttribute("metadataOption") != null){
                        metadataOption = (String)request.getSession().getAttribute("metadataOption");
                        request.getSession().removeAttribute("metadataOption");
                    }
                    if(request.getSession().getAttribute("idp") != null){
                        idpName = (String)request.getSession().getAttribute("idp");
                        request.getSession().removeAttribute("idp");
                    }

                    LOGGER.debug("information retrived... :idpID :" + idpID + ", idpName :" + idpName + ", custom IDPname :" + customIdpName +
                            ", newIDP name :" + newIdpname + ", metadata Option :" + metadataOption);


                    if (request.getSession().getAttribute("success") != null || request.getSession().getAttribute("error") != null) {

                        if (request.getSession().getAttribute("success") != null) {
                            addActionMessage(getText("samlsso.success.config"));
                            request.getSession().removeAttribute("success");
                        } else if (request.getSession().getAttribute("error") != null) {
                            addActionError(request.getSession().getAttribute("error").toString());
                            request.getSession().removeAttribute("error");
                        }
                    }
                    request.getSession().removeAttribute("error");
                    request.getSession().removeAttribute("success");

                    saveIdpQuickSetupOptions(this.idpID, "idpGuide", moPluginHandler.getIdpGuides().get(idpName));
                    saveIdpQuickSetupOptions(this.idpID, "actualIdpName", idpName);
                    saveIdpQuickSetupOptions(this.idpID, "customIdpName", this.customIdpName);
                    saveIdpQuickSetupOptions(this.idpID, "metadataOption", this.metadataOption);
                }
                /**
                 * The Configure IDP form is submitted twice, as such there are 2 hidden variables that are submitted one after the other.
                 * The explanation fro each case can be found below under their respective conditional statements
                 */
                else if (BooleanUtils.toBoolean(this.configureIdpSubmitted) && !error) {
                    /**
                     * This is the first time the Configure IDP form is submitted.
                     * Here the IDP metadata is taken as input.
                     * Once this form is submitted successfully the user gets the option to perform test configuration.
                     */
                    LOGGER.debug("Configure IDP submitted : Save");
                    this.foundIdpConfiguration = Boolean.TRUE;
                    this.idpName = request.getParameter("idp");
                    String actualIDPName = this.idpName;
                    if (StringUtils.isEmpty(request.getParameter("idpId"))) {
                        this.idpID = UUID.randomUUID().toString();
                        LOGGER.debug("New IDP ID generated : " + this.idpID);
                    }
                    if (!BooleanUtils.toBoolean(error)) {
                        String finalIdpname = new String();
                        if (StringUtils.equalsIgnoreCase(this.customIdpName, "yes")) {
                            finalIdpname = this.newIdpname;
                        } else {
                            finalIdpname = this.idpName;
                        }
                        LOGGER.debug("final IDP name : " + finalIdpname);
                        LOGGER.debug("metadataOption : " + this.metadataOption);
                        if (StringUtils.equalsIgnoreCase(this.metadataOption, "fromUrl")) {
                            LOGGER.debug("frmo URL importing metadata...");
                            switch (this.idpName) {
                                case "ADFS":
                                    MoPluginHandler.saveMetadataOption(this.idpID, "ADFS");
                                    handleFetchMetadata("ADFS", finalIdpname);
                                    break;
                                case "Azure AD":
                                    MoPluginHandler.saveMetadataOption(this.idpID, "Azure AD");
                                    handleFetchMetadata("Azure AD", finalIdpname);
                                    break;
                                default:
                                    MoPluginHandler.saveMetadataOption(this.idpID, this.metadataOption);
                                    handleFetchMetadata(this.metadataOption, finalIdpname);
                                    break;
                            }
                            LOGGER.debug("out of switch case method...");
                        } else if (StringUtils.equalsIgnoreCase(this.metadataOption, "fromFile")) {
                            MoPluginHandler.saveMetadataOption(this.idpID, this.metadataOption);
                            handleUploadMetadata(finalIdpname);
                        } else if (StringUtils.equalsIgnoreCase(this.metadataOption, "manual")) {

                            List<String> x509allcertificates = new ArrayList<String>();
                            if (x509Certificate != null) {
                                x509allcertificates.add(StringUtils.trim(x509Certificate));
                            }
                            if (x509AllCertificates != null) {
                                x509allcertificates = new ArrayList<String>(Arrays.asList(x509AllCertificates));
                                x509allcertificates.removeAll(Arrays.asList(null, ""));
                                if (StringUtils.isBlank(x509Certificate)) {
                                    x509Certificate = x509allcertificates.get(0);
                                }
                            }
                            MoPluginHandler.saveIdPConfiguration(this.idpID, StringUtils.trim(finalIdpname),
                                    StringUtils.trim(this.idpEntityId), this.ssoBindingType, StringUtils.trim(this.ssoUrl),
                                    StringUtils.EMPTY, StringUtils.EMPTY, x509allcertificates, StringUtils.trim(this.x509Certificate),
                                    this.signedRequest, StringUtils.EMPTY, Boolean.TRUE);
                            addActionMessage(getText("samlsso.success.config"));
                        }
                        /**
                         * Saving idpGuide to handle for cases where custom IDP name is configured. Idp Guides are fetched by name, which
                         * will not be possible with the custom name.
                         */
                        saveIdpQuickSetupOptions(this.idpID, "idpGuide", moPluginHandler.getIdpGuides().get(actualIDPName));
                        saveIdpQuickSetupOptions(this.idpID, "actualIdpName", actualIDPName);
                        saveIdpQuickSetupOptions(this.idpID, "customIdpName", this.customIdpName);
                        saveIdpQuickSetupOptions(this.idpID, "metadataOption", this.metadataOption);
                    }
                    saveIdpQuickSetupOptions(this.idpID, "pageNumber", "2");
                } else if (BooleanUtils.toBoolean(this.configureIdpNextSubmitted) && !error) {
                    /**
                     * This is the second time the configure IDP form is submitted.
                     * This submission saves no IDP data, but signifies that the IDP configuration is done and the process can move to the
                     * next step in the flow.
                     */
                    settings.setPageNumber("3");
                    LOGGER.debug("Configure IDP submitted : Next");
                    if (StringUtils.isBlank(this.idpID)) {
                        if (!(settings.getIdpMap().isEmpty())) {
                            this.idpID = settings.getIdPList().get(0);
                            LOGGER.debug("configure IDP final IDP ID : " + this.idpID);
                        } else {
                            this.idpID = UUID.randomUUID().toString();
                        }
                    }
                    this.testConfigPerformed = settings.checkIfTestConfig(this.idpID);
                    saveIdpQuickSetupOptions(this.idpID, "pageNumber", "2");
                } else if (BooleanUtils.toBoolean(this.userProfileSubmitted) && !error) {
                    settings.setPageNumber("4");
                    LOGGER.debug("User Profile submitted");
                    if (StringUtils.isBlank(this.idpID)) {
                        if (!(settings.getIdpMap().isEmpty())) {
                            this.idpID = settings.getIdPList().get(0);
                            LOGGER.debug("user profile IdpID : " + this.idpID);
                        } else {
                            this.idpID = UUID.randomUUID().toString();
                        }
                    }
                    String userNameAttr = StringUtils.split(this.userNameAttribute, " ")[0];
                    String userEmailAttr = StringUtils.split(this.userEmailAttribute, " ")[0];
                    String userFullNameAttr = StringUtils.EMPTY;
                    if (StringUtils.isNotEmpty(this.fullNameAttribute)) {
                        userFullNameAttr = StringUtils.split(this.fullNameAttribute, " ")[0];
                    }
                    String userFirstNameAttr = StringUtils.EMPTY;
                    if (StringUtils.isNotEmpty(this.firstNameAttribute)) {
                        userFirstNameAttr = StringUtils.split(this.firstNameAttribute, " ")[0];
                    }
                    String userLastNameAttr = StringUtils.EMPTY;
                    if (StringUtils.isNotEmpty(this.lastNameAttribute)) {
                        userLastNameAttr = StringUtils.split(this.lastNameAttribute, " ")[0];
                    }
                    MoPluginHandler.saveAttributeMapping(this.idpID, setDefaulttoNameID(userNameAttr), setDefaulttoNameID(userEmailAttr),
                            setDefaulttoEmpty(userFullNameAttr), this.useSeparateNameAttributes, setDefaulttoEmpty(userFirstNameAttr), setDefaulttoEmpty(userLastNameAttr),
                            false, StringUtils.EMPTY, false, this.loginAttribute);
                    saveIdpQuickSetupOptions(this.idpID, "pageNumber", "3");
                    this.testConfigPerformed = settings.checkIfTestConfig(this.idpID);
                } else if (BooleanUtils.toBoolean(this.userGroupsSubmitted) && !error) {
                    settings.setPageNumber("5");
                    LOGGER.debug("User Groups submitted");
                    if (StringUtils.isBlank(this.idpID)) {
                        if (!(settings.getIdpMap().isEmpty())) {
                            this.idpID = settings.getIdPList().get(0);
                            LOGGER.debug("group mapping IdpID : " + this.idpID);
                        } else {
                            this.idpID = UUID.randomUUID().toString();
                        }
                    }
                    List<String> defaultGroupList = new ArrayList<>();
                    if (defaultGroup != null) {
                        LOGGER.debug("single default group is -" + this.defaultGroup);
                        defaultGroupList.add(defaultGroup);
                    }

                    LOGGER.debug("Default Groups : " + defaultGroups);

                    if (defaultGroups != null) {
                        LOGGER.debug("multiple default groups -" + defaultGroups.toString());
                        defaultGroupList = Arrays.asList(StringUtils.split(defaultGroups[0], ","));
                        defaultGroupList.removeAll(Arrays.asList(null, ""));

                    }
                    moPluginHandler.saveRoleMapping(this.idpID, StringUtils.EMPTY, false, false,
                            new HashMap<String, String>(), this.defaultGroup, defaultGroupList, true,this.enableDefaultGroupsFor,
                            false, new ArrayList<String>(), true, true,false,StringUtils.EMPTY,StringUtils.EMPTY,StringUtils.EMPTY);

                    initialiseTestConfig();
                    saveIdpQuickSetupOptions(this.idpID, "pageNumber", "4");
                } else if (BooleanUtils.toBoolean(this.testConfigSubmitted) && !error) {
                    LOGGER.debug("Test Configuration submitted");

                }
                initialiseSAMLConfig(request);
                return "input";
            } catch (MoPluginException e) {
                LOGGER.error("MoPluginException = ", e);
                addActionError(e.getMessage());
                initialiseSAMLConfig(request);
                return INPUT;
            } catch (PluginException e) {
                LOGGER.error("PluginException = ", e);
                addActionError(e.getMessage());
                initialiseSAMLConfig(request);
                return INPUT;
            } catch (Exception e) {
                LOGGER.error("Exception  ", e);
                addActionError("An error occurred while saving your details. Please check logs for more info.");
                initialiseSAMLConfig(request);
                return INPUT;
            }
        } else {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return null;
        }
    }
    private void initialiseTestConfig() {
        MoIDPConfig idpConfig = moPluginHandler.constructIdpConfigObject(this.idpID);
        this.testConfigPerformed = settings.checkIfTestConfig(this.idpID);
        try {
            if (this.testConfigPerformed && idpConfig != null) {
                LOGGER.debug("3");
                this.attributesMap = MoSAMLUtils.toMap((JSONObject) settings.getIdpConfig(this.idpID).get("testConfig"));
                this.displaySamlRequest = (String) settings.getIdpConfig(this.idpID).get("samlRequest");
                this.displaySamlResponse = (String) settings.getIdpConfig(this.idpID).get("samlResponse");
            }
        } catch (Exception e) {
            LOGGER.debug("error getting the test config details from settings.getIdpConfig(this.idpID)");
        }
    }


    private String getIdpQuickSetupOptions(String idpID, String optionName) {
        String optionValue = new String();
        try {
            JSONObject idpConfigObject = settings.getIdpConfig(idpID);
            if (idpConfigObject.has(optionName))
                optionValue = (String) idpConfigObject.get(optionName);
        } catch (Exception e) {
            LOGGER.debug("Error fetching IDP quick setup options");
        }
        return optionValue;
    }
    private void handleFetchMetadata(String metadataOrIDPOption, String idpName) {
        LOGGER.debug("handleFetchMetadata called");
        try {
            if (StringUtils.isBlank(inputUrl)) {
                LOGGER.error("Input Metadata URL is empty");
                this.foundIdpConfiguration = false;
                addActionError(getText("samlsso.error.url.empty"));
            }else {
                String idpMetadataUrl = getIdpMetadataUrl();
                MoPluginHandler.fetchMetadata(this.idpID, idpName, inputUrl, idpMetadataUrl, metadataOrIDPOption);
                addActionMessage(getText("samlsso.success.config"));
            }
        } catch (MoPluginException e) {
            LOGGER.error(e.getMessage(), e);
            this.foundIdpConfiguration = Boolean.FALSE;
            if (e.getErrorCode() == MoPluginException.PluginErrorCode.METADATA_PARSE) {
                throw new MoPluginException(MoPluginException.PluginErrorCode.METADATA_PARSE,
                        getText("samlsso.error.invalid.metadata"), e);
            } else {
                throw new MoPluginException(MoPluginException.PluginErrorCode.METADATA_PARSE,
                        getText("samlsso.error.url.connect"), e);
            }
        }catch (Exception e){
            this.foundIdpConfiguration = Boolean.FALSE;
            addActionError(getText("Invalid Metadata"));
        }
    }
    private void saveIdpQuickSetupOptions(String idpID, String optionName, String optionValue) {
        try {
            JSONObject idpConfigObject = settings.getIdpConfig(idpID);
            idpConfigObject.put(optionName, optionValue);
            settings.setIdpConfig(idpConfigObject, idpID);
        } catch (Exception e) {
            LOGGER.debug("error saving option " + optionName + " for idp " + idpID);
        }
    }
    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
    private String getIdpMetadataUrl() {
        LOGGER.debug("Getting IdP Metadata URL for IdP");
        String getIdpMetadataUrl = StringUtils.EMPTY;
        if (this.inputUrl.contains("https://") || this.inputUrl.contains("http://")) {
            metadataOption = "fromUrl";
        }
        switch (this.idpName) {
            case "ADFS":
                getIdpMetadataUrl = "https://" + this.inputUrl + "/federationmetadata/2007-06/federationmetadata.xml";
                break;
            case "Azure AD":
                getIdpMetadataUrl = "https://login.microsoftonline.com/" + this.inputUrl + "/FederationMetadata/2007-06/FederationMetadata.xml";
                break;
            default:
                getIdpMetadataUrl = this.inputUrl;
                break;
        }
        return getIdpMetadataUrl;
    }
    private String setDefaulttoNameID(String attributeName) {

        if(StringUtils.equalsIgnoreCase(attributeName,"--Select--") ){
            return "NameID";
        }
        return attributeName;
    }

    private String setDefaulttoEmpty(String attributeName) {
        if(StringUtils.equalsIgnoreCase(attributeName,"--Select--") ){
            return StringUtils.EMPTY;
        }
        return attributeName;
    }

    public void initialiseSAMLConfig(HttpServletRequest request) {
        LOGGER.debug("initialiseSAMLConfig for flow driven called");
        this.pageNumber = settings.getPageNumber(resetPageNumber);
        this.isJCEInstalled = isJCEEnable();
        if (MoHttpUtils.getCookie(request, MoPluginConstants.QUICKSETUP_IDP)!=null){
            LOGGER.debug("Getting IDP from cookie");
            this.idpID = MoHttpUtils.getCookie(request, MoPluginConstants.QUICKSETUP_IDP).getValue();
        }
        MoIDPConfig idpConfig = MoPluginHandler.constructIdpConfigObject(this.idpID);
        if (idpConfig != null) {
            LOGGER.debug("IDconfig object found");
            this.idpGuide = getIdpQuickSetupOptions(this.idpID,"idpGuide");
            LOGGER.debug("IDPGuide link : " + this.idpGuide);
            this.ssoUrl = StringUtils.defaultIfBlank(idpConfig.getSsoUrl(), StringUtils.EMPTY);
            this.newIdpname = StringUtils.defaultIfBlank(idpConfig.getIdpName(), StringUtils.EMPTY);
            this.inputUrl = StringUtils.defaultIfBlank(idpConfig.getInputUrl(), StringUtils.EMPTY);
            this.idpEntityId = StringUtils.defaultIfBlank(idpConfig.getIdpEntityId(), StringUtils.EMPTY);
            this.certificates = idpConfig.getCertificates();
            this.x509Certificate = StringUtils.defaultIfBlank(idpConfig.getX509Certificate(), StringUtils.EMPTY);
            this.inputUrl = StringUtils.defaultIfBlank(idpConfig.getInputUrl(), StringUtils.EMPTY);
            this.userNameAttribute = StringUtils.defaultIfBlank(idpConfig.getUsernameAttribute(), "NameID");
            this.userEmailAttribute = StringUtils.defaultIfBlank(idpConfig.getEmailAttribute(), "NameID");
            this.loginAttribute = StringUtils.defaultIfBlank(idpConfig.getLoginUserAttribute(), "username");
            this.fullNameAttribute = StringUtils.defaultIfBlank(idpConfig.getFullNameAttribute(), StringUtils.EMPTY);
            this.firstNameAttribute = StringUtils.defaultIfBlank(idpConfig.getFirstNameAttribute(), StringUtils.EMPTY);
            this.lastNameAttribute = StringUtils.defaultIfBlank(idpConfig.getLastNameAttribute(), StringUtils.EMPTY);
            this.useSeparateNameAttributes = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getUseSeparateNameAttributes(), Boolean.FALSE);
            this.metadataOption = StringUtils.defaultIfBlank(getIdpQuickSetupOptions(this.idpID, "metadataOption"), "fromUrl");
            this.customIdpName = StringUtils.defaultIfBlank(getIdpQuickSetupOptions(this.idpID, "customIdpName"), "yes");
            this.defaultGroup = idpConfig.getDefaultGroup();
            this.defaultGroupsList = idpConfig.getDefaultGroupsList();
            this.enableDefaultGroupsFor = idpConfig.getEnableDefaultGroupsFor();
            this.foundIdpConfiguration = Boolean.TRUE;
            this.ssoBindingType = StringUtils.defaultIfBlank(idpConfig.getSsoBindingType(), "HttpRedirect");
            this.signedRequest = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getSignedRequest(), Boolean.TRUE);
            this.spBaseUrl = settings.getSpBaseUrl();
        } else {
            LOGGER.debug("New Idp");
            this.idpID = "";
            this.idpName = request.getParameter("idp");
            LOGGER.debug("idpname in initialize method :" + idpName);
            this.newIdpname = StringUtils.EMPTY;
            if (StringUtils.equalsIgnoreCase(this.idpName, "G_Suite")) {
                this.metadataOption = "fromFile";
            } else {
                this.metadataOption = "fromUrl";
            }
            this.idpGuide = moPluginHandler.getIdpGuides().get(this.idpName);
            LOGGER.debug("IDP guide new IDP: " + this.idpGuide);
            this.customIdpName = "yes";
            this.inputUrl = settings.getIdpMetadataURL();
            this.ssoUrl = settings.getSsoServiceUrl();
            this.idpEntityId = settings.getIdpEntityId();
            this.x509Certificate = settings.getX509Certificate();
            this.userNameAttribute = settings.getUsernameAttribute();
            this.userEmailAttribute = settings.getEmailAttribute();
            this.loginAttribute = settings.getLoginUserAttribute();
            this.fullNameAttribute = settings.getFullNameAttribute();
            this.firstNameAttribute = settings.getFirstNameAttribute();
            this.lastNameAttribute = settings.getLastNameAttribute();
            this.useSeparateNameAttributes = settings.getUseSeparateNameAttributes();
            this.defaultGroup = settings.getDefaultGroup();
            this.defaultGroupsList = (List<String>) settings.getDefaultGroups();
            this.enableDefaultGroupsFor = settings.getEnableDefaultGroupsFor();
            this.ssoBindingType = "HttpRedirect";
            this.signedRequest = settings.getSignedRequest();
            this.spBaseUrl = settings.getSpBaseUrl();
            LOGGER.debug("spBaseUrl "+ spBaseUrl);
            LOGGER.debug("idp "+ idpID);
        }
    }

    public boolean isJCEEnable() {
        try {
            if (Cipher.getMaxAllowedKeyLength("SHA256") != Integer.MAX_VALUE) {
                return false;
            } else
                return true;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("No Such Algorithm Exception" + e);
            return false;
        }

    }

    private void handleUploadMetadata(String idpName) {
        LOGGER.debug("handleUploadMetadata called");
        try {
            MultiPartRequestWrapper wrapper = (MultiPartRequestWrapper) com.opensymphony.webwork.ServletActionContext.getRequest();
            if (wrapper.getFiles("xmlFile") == null) {
                LOGGER.error("Metadata file is empty");
                addActionError(getText("samlsso.error.file.empty"));
            } else {
                File metadataFile = wrapper.getFiles("xmlFile")[0];
                MoPluginHandler.uploadMetadata(this.idpID, idpName, metadataFile, this.metadataOption);
                addActionMessage(getText("samlsso.success.config"));
            }
        } catch (MoPluginException e) {
            LOGGER.error(e.getMessage(), e);
            if ((MoIDPMetadata.entityId == null || org.apache.commons.lang.StringUtils.isBlank(MoIDPMetadata.entityId)) &&
                    (MoIDPMetadata.signingCertificates == null || MoIDPMetadata.signingCertificates.isEmpty())) {
                this.foundIdpConfiguration = Boolean.FALSE;
                LOGGER.error("Invalid Metadata File");
                addActionError(getText("samlsso.error.invalid.metadata"));
            } else if (org.apache.commons.lang.StringUtils.isNotEmpty(MoIDPMetadata.certificate) && MoIDPMetadata.signingCertificates.isEmpty()) {
                this.foundIdpConfiguration = Boolean.FALSE;
                LOGGER.error("Invalid X.509 Certificate in Metadata File");
                addActionError(getText("samlsso.metadata.error.config.certificate.invalid"));
            } else if (org.apache.commons.lang.StringUtils.isEmpty(MoIDPMetadata.certificate)) {
                this.foundIdpConfiguration = Boolean.FALSE;
                LOGGER.error("Unable to find X.509Certificate in Metadata File");
                addActionError(getText("samlsso.metadata.error.config.certificate.absent"));
            } else if (org.apache.commons.lang.StringUtils.isBlank(MoIDPMetadata.entityId)) {
                this.foundIdpConfiguration = Boolean.FALSE;
                LOGGER.error("Unable to find EntityId in Metadata File");
                addActionError(getText("samlsso.metadata.error.config.idpentityid.absent"));
            }
        }
    }

    public MoPluginHandler getMoPluginHandler() {
        return moPluginHandler;
    }

    public void setMoPluginHandler(MoPluginHandler moPluginHandler) {
        this.moPluginHandler = moPluginHandler;
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public LoginUriProvider getLoginUriProvider() {
        return loginUriProvider;
    }

    public void setLoginUriProvider(LoginUriProvider loginUriProvider) {
        this.loginUriProvider = loginUriProvider;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getSetupGuideLink() {
        return setupGuideLink;
    }

    public void setSetupGuideLink(String setupGuideLink) {
        this.setupGuideLink = setupGuideLink;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getIdpGuide() {
        return idpGuide;
    }

    public void setIdpGuide(String idpGuide) {
        this.idpGuide = idpGuide;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public Boolean getSpMetaDataSubmitted() {
        return spMetaDataSubmitted;
    }

    public void setSpMetaDataSubmitted(Boolean spMetaDataSubmitted) {
        this.spMetaDataSubmitted = spMetaDataSubmitted;
    }

    public Boolean getJCEInstalled() {
        return isJCEInstalled;
    }

    public void setJCEInstalled(Boolean JCEInstalled) {
        isJCEInstalled = JCEInstalled;
    }

    public Map<String, String> getCertificateInfo() {
        return certificateInfo;
    }

    public void setCertificateInfo(Map<String, String> certificateInfo) {
        this.certificateInfo = certificateInfo;
    }

    public Boolean getUpdatedUrls() {
        return updatedUrls;
    }

    public void setUpdatedUrls(Boolean updatedUrls) {
        this.updatedUrls = updatedUrls;
    }

    public String getUpdatedSpBaseUrl() {
        return updatedSpBaseUrl;
    }

    public void setUpdatedSpBaseUrl(String updatedSpBaseUrl) {
        this.updatedSpBaseUrl = updatedSpBaseUrl;
    }

    public String getUpdatedSpEntityId() {
        return updatedSpEntityId;
    }

    public void setUpdatedSpEntityId(String updatedSpEntityId) {
        this.updatedSpEntityId = updatedSpEntityId;
    }

    public Boolean getConfigureIdpSubmitted() {
        return configureIdpSubmitted;
    }

    public void setConfigureIdpSubmitted(Boolean configureIdpSubmitted) {
        this.configureIdpSubmitted = configureIdpSubmitted;
    }

    public Boolean getFoundIdpConfiguration() {
        return foundIdpConfiguration;
    }

    public void setFoundIdpConfiguration(Boolean foundIdpConfiguration) {
        this.foundIdpConfiguration = foundIdpConfiguration;
    }

    public Boolean getTestConfigPerformed() {
        return testConfigPerformed;
    }

    public void setTestConfigPerformed(Boolean testConfigPerformed) {
        this.testConfigPerformed = testConfigPerformed;
    }

    public Boolean getConfigureIdpNextSubmitted() {
        return configureIdpNextSubmitted;
    }

    public void setConfigureIdpNextSubmitted(Boolean configureIdpNextSubmitted) {
        this.configureIdpNextSubmitted = configureIdpNextSubmitted;
    }

    public String getMetadataOption() {
        return metadataOption;
    }

    public void setMetadataOption(String metadataOption) {
        this.metadataOption = metadataOption;
    }

    public String getCustomIdpName() {
        return customIdpName;
    }

    public void setCustomIdpName(String customIdpName) {
        this.customIdpName = customIdpName;
    }

    public String getInputUrl() {
        return inputUrl;
    }

    public void setInputUrl(String inputUrl) {
        this.inputUrl = inputUrl;
    }

    public String getIdpName() {
        return idpName;
    }

    public void setIdpName(String idpName) {
        this.idpName = idpName;
    }

    public String getNewIdpname() {
        return newIdpname;
    }

    public void setNewIdpname(String newIdpname) {
        this.newIdpname = newIdpname;
    }

    public String getIdpID() {
        return idpID;
    }

    public void setIdpID(String idpID) {
        this.idpID = idpID;
    }

    public String getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(String x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    public String[] getX509AllCertificates() {
        return x509AllCertificates;
    }

    public void setX509AllCertificates(String[] x509AllCertificates) {
        this.x509AllCertificates = x509AllCertificates;
    }

    public List<String> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<String> certificates) {
        this.certificates = certificates;
    }

    public String getIdpEntityId() {
        return idpEntityId;
    }

    public void setIdpEntityId(String idpEntityId) {
        this.idpEntityId = idpEntityId;
    }

    public String getSsoUrl() {
        return ssoUrl;
    }

    public void setSsoUrl(String ssoUrl) {
        this.ssoUrl = ssoUrl;
    }

    public String getSsoBindingType() {
        return ssoBindingType;
    }

    public void setSsoBindingType(String ssoBindingType) {
        this.ssoBindingType = ssoBindingType;
    }

    public Boolean getSignedRequest() {
        return signedRequest;
    }

    public void setSignedRequest(Boolean signedRequest) {
        this.signedRequest = signedRequest;
    }

    public Boolean getUserProfileSubmitted() {
        return userProfileSubmitted;
    }

    public void setUserProfileSubmitted(Boolean userProfileSubmitted) {
        this.userProfileSubmitted = userProfileSubmitted;
    }

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }

    public String getUserEmailAttribute() {
        return userEmailAttribute;
    }

    public void setUserEmailAttribute(String userEmailAttribute) {
        this.userEmailAttribute = userEmailAttribute;
    }

    public String getFullNameAttribute() {
        return fullNameAttribute;
    }

    public void setFullNameAttribute(String fullNameAttribute) {
        this.fullNameAttribute = fullNameAttribute;
    }

    public String getFirstNameAttribute() {
        return firstNameAttribute;
    }

    public void setFirstNameAttribute(String firstNameAttribute) {
        this.firstNameAttribute = firstNameAttribute;
    }

    public String getLastNameAttribute() {
        return lastNameAttribute;
    }

    public void setLastNameAttribute(String lastNameAttribute) {
        this.lastNameAttribute = lastNameAttribute;
    }

    public String getLoginAttribute() {
        return loginAttribute;
    }

    public void setLoginAttribute(String loginAttribute) {
        this.loginAttribute = loginAttribute;
    }

    public String getNameOption() {
        return nameOption;
    }

    public void setNameOption(String nameOption) {
        this.nameOption = nameOption;
    }

    public Boolean getUseSeparateNameAttributes() {
        return useSeparateNameAttributes;
    }

    public void setUseSeparateNameAttributes(Boolean useSeparateNameAttributes) {
        this.useSeparateNameAttributes = useSeparateNameAttributes;
    }

    public Boolean getUserGroupsSubmitted() {
        return userGroupsSubmitted;
    }

    public void setUserGroupsSubmitted(Boolean userGroupsSubmitted) {
        this.userGroupsSubmitted = userGroupsSubmitted;
    }

    public String[] getDefaultGroups() {
        return defaultGroups;
    }

    public void setDefaultGroups(String[] defaultGroups) {
        this.defaultGroups = defaultGroups;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public List<String> getDefaultGroupsList() {
        return defaultGroupsList;
    }

    public void setDefaultGroupsList(List<String> defaultGroupsList) {
        this.defaultGroupsList = defaultGroupsList;
    }

    public String getEnableDefaultGroupsFor() {
        return enableDefaultGroupsFor;
    }

    public void setEnableDefaultGroupsFor(String enableDefaultGroupsFor) {
        this.enableDefaultGroupsFor = enableDefaultGroupsFor;
    }

    public Boolean getTestConfigSubmitted() {
        return testConfigSubmitted;
    }

    public void setTestConfigSubmitted(Boolean testConfigSubmitted) {
        this.testConfigSubmitted = testConfigSubmitted;
    }

    public Map<String, List<String>> getAttributesMap() {
        return attributesMap;
    }

    public void setAttributesMap(Map<String, List<String>> attributesMap) {
        this.attributesMap = attributesMap;
    }

    public String getDisplaySamlResponse() {
        return displaySamlResponse;
    }

    public void setDisplaySamlResponse(String displaySamlResponse) {
        this.displaySamlResponse = displaySamlResponse;
    }

    public String getDisplaySamlRequest() {
        return displaySamlRequest;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    public void setDisplaySamlRequest(String displaySamlRequest) {
        this.displaySamlRequest = displaySamlRequest;
    }

    public String getLogSettingsUrl(){
        return settings.getSpBaseUrl() + "/admin/configLog4j.action";
    }

    public String getTroubleshootingUrl(){
        return settings.getSpBaseUrl() + "/plugins/servlet/troubleshooting/view/";
    }

    public String getSpBaseUrl() {
        return spBaseUrl;
    }

    public void setSpBaseUrl(String spBaseUrl) {
        this.spBaseUrl = spBaseUrl;
    }
}
