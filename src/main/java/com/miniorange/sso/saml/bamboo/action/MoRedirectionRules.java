package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MoRedirectionRules extends BambooActionSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoRedirectionRules.class);

    private MoSAMLSettings settings;
    private MoPluginHandler pluginHandler;
    private UserManager userManager;
    private LoginUriProvider loginUriProvider;

    private DirectoryManager directoryManager;

    private String ruleName;
    private String decisionFactor;
    private String conditionOperation;
    private String conditionValue;
    private String idp;
    private Boolean addRedirectionRuleSubmitted;
    private Boolean editRedirectionRuleSubmitted;
    private Boolean bambooRulesOrderSubmitted;
    private Map<String,String> bambooRedirectionRulesMap;
    private Map<String,String> idpMap;
    private ArrayList<String> ssoEnabledIdpList;
    private String defaultBambooIDP;
    private String defaultRedirectUrl;
    private Boolean enableAutoRedirectDelay;
    private Boolean enableAutoRedirect;
    private Boolean enableBackdoor;
    private String backdoorKey;
    private String backdoorValue;
    private Boolean restrictBackdoor;
    private List<String> backdoorGroupsList;
    private String backdoorGroups;
    private Boolean backdoorSubmitted;
    private Boolean advancedSettingsSubmitted;
    private String numberOfLoginAttempts;
    private String xsrfToken;
    private List<String> noSsoUrls;


    public MoRedirectionRules(MoSAMLSettings settings, MoPluginHandler pluginHandler, DirectoryManager directoryManager,
                              UserManager userManager, LoginUriProvider loginUriProvider) {
        this.settings = settings;
        this.pluginHandler = pluginHandler;

        this.directoryManager = directoryManager;
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
    }

    public Boolean doValidate() {
        LOGGER.info("Validating Redirection Rules");

        boolean error = false;
        if(BooleanUtils.toBoolean(addRedirectionRuleSubmitted) || BooleanUtils.toBoolean(editRedirectionRuleSubmitted)) {
            //Check if rule name is empty
            if(StringUtils.isBlank(ruleName) ){
                addActionError(getText("samlsso.error.empty.rule.name"));
                error = true;
            }
            //Check if the same rule name exists
            Map<String,String> ruleMap = settings.getBambooRedirectionRulesMap();
            if(ruleMap.containsKey(ruleName) && !BooleanUtils.toBoolean(editRedirectionRuleSubmitted)){
                addActionError(getText("samlsso.error.duplicate.rule.name"));
                error=true;
            }
            if(StringUtils.isBlank(decisionFactor)){
                addActionError(getText("samlsso.error.empty.decisionFactor"));
                error = true;
                return error;
            }
            if(StringUtils.isBlank(conditionOperation)){
                addActionError(getText("samlsso.error.empty.conditionOperation"));
                error = true;
                return error;
            }
            //Check if the Domain, Directory or Group is empty

            if(StringUtils.isBlank(conditionValue)){
                error=true;
                switch (decisionFactor){
                    case "domain":
                        addActionError(getText("samlsso.error.empty.domain"));
                        break;
                    case "directory":
                        addActionError(getText("samlsso.error.empty.directory"));
                        break;
                    case "group":
                        addActionError(getText("samlsso.error.empty.group"));
                        break;
                    default:
                        addActionError(getText("samlsso.error.unsuppported.decisionfactor"));
                        break;
                }
            }

            //Check if IDP is empty
            if(StringUtils.isBlank(idp)){
                addActionError(getText("samlsso.error.empty.idp"));
                error=true;
            }
        }
        if(BooleanUtils.toBoolean(advancedSettingsSubmitted)){
            if (BooleanUtils.toBoolean(restrictBackdoor) && settings.convertSelect2StringToList(backdoorGroups).size()==0){
                LOGGER.debug("backdoor restriction enabled but no backdoor groups have been added");
                addActionError(getText("samlsso.error.empty.backdoor.groups"));
                error = true;
            }
        }
        if(error){
            initializeRedirectionRules();
        }
        return error;
    }

    private Boolean hasError(String backdoorKey, String backdoorValue) {
        if (StringUtils.isBlank(backdoorKey.trim())) {
            addActionError("Backdoor query parameter key is invalid. Please make sure it's not empty and doesn't contain spaces.");
            return true;
        }
        if (StringUtils.isBlank(backdoorValue.trim())) {
            addActionError("Backdoor query parameter value is invalid. Please make sure it's not empty and doesn't contain spaces.");
            return true;
        }
        if (StringUtils.isBlank(this.backdoorKey) || this.backdoorKey.split("\\s+").length > 1) {
            addActionError(getText("samlsso.error.config.backdoorkey.invalid"));
            return true;
        }
        if (StringUtils.isBlank(this.backdoorValue) || this.backdoorValue.split("\\s+").length > 1) {
            addActionError(getText("samlsso.error.config.backdoorvalue.invalid"));
            return true;
        }
        return false;
    }

    @Override
    public String execute() throws Exception {
        LOGGER.debug("MoRedirection Rules execute called. ");
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        xsrfToken = XsrfTokenUtils.getXsrfToken(request);



        final UserProfile user = userManager.getRemoteUser();
        if (user != null && userManager.isAdmin(user.getUserKey())) {
            try {
                if (!settings.isValidLicense()) {
                    LOGGER.error("No valid license found");
                    return "upm";
                }

                boolean error = doValidate();
                if (error)
                    return INPUT;


                if (BooleanUtils.toBoolean(addRedirectionRuleSubmitted) || BooleanUtils.toBoolean(editRedirectionRuleSubmitted)) {
                    LOGGER.debug("addRedirectionRuleSubmitted or editRedirectionRuleSubmitted.. ");
                    pluginHandler.saveRedirectionRules(ruleName, decisionFactor, conditionOperation, conditionValue, idp);
                    addActionMessage(getText("samlsso.success.config"));

                } else if (BooleanUtils.toBoolean(bambooRulesOrderSubmitted)) {

                    bambooRedirectionRulesMap = settings.getBambooRedirectionRulesMap();
                    int size = bambooRedirectionRulesMap.size();
                    bambooRedirectionRulesMap = new LinkedHashMap<>();
                    for (int i = 0; i < size; i++) {
                        ruleName = request.getParameter("ruleKey_" + i);
                        String ruleExpression = request.getParameter("ruleExpression_" + i);
                        bambooRedirectionRulesMap.put(ruleName, ruleExpression);
                        //pluginHandler.saveBambooRedirectionRules(bambooRedirectionRulesMap);
                        addActionMessage(getText("samlsso.success.config"));
                    }
                } else if (BooleanUtils.toBoolean(advancedSettingsSubmitted)) {
                    LOGGER.debug("advancedSettingsSubmitted .. ");
                    LOGGER.debug("Saving sign in settings...");
                    if (settings.getIdPList().size() == 1) {
                        if (BooleanUtils.toBoolean(enableAutoRedirect)) {
                            LOGGER.debug("autoRedirect enabled for : " + settings.getIdpMap().get(settings.getIdPList().get(0)));
                            settings.setDefaultBambooIDP(settings.getIdPList().get(0));
                        } else {
                            LOGGER.debug("autoRedirect disabled for : " + settings.getIdpMap().get(settings.getIdPList().get(0)));
                            settings.setDefaultBambooIDP("loginPage");

                        }
                    }
                    int totalNumberOfRoles = Integer.parseInt(request.getParameter("totalNumberOfRoles"));
                    LOGGER.debug("totalNumberOfRoles = " + totalNumberOfRoles);
                    noSsoUrls = new ArrayList<>();
                    for (int i=0;i<totalNumberOfRoles;i++){
                        String value = request.getParameter("userAttrValue[" + i + "]");
                        if (StringUtils.isNotEmpty(value)){
                            noSsoUrls.add(value);
                        }
                    }
                    backdoorGroupsList = settings.convertSelect2StringToList(backdoorGroups);
                    LOGGER.debug("backdoorGroupsList {}", backdoorGroupsList);
                    LOGGER.debug("enblebackdoor" + enableBackdoor);

                    pluginHandler.saveAdvancedRedirectionSettings(enableAutoRedirectDelay, enableBackdoor, restrictBackdoor,
                            backdoorGroupsList, numberOfLoginAttempts,noSsoUrls);
                    addActionMessage(getText("samlsso.success.config"));
                } else if (BooleanUtils.toBoolean(backdoorSubmitted)) {
                    LOGGER.debug("backdoorKey: " + this.backdoorKey);
                    LOGGER.debug("backdoorValue: " + this.backdoorValue);
                    this.backdoorKey = MoSAMLUtils.sanitizeText(backdoorKey);
                    this.backdoorValue = MoSAMLUtils.sanitizeText(backdoorValue);
                    error = hasError(this.backdoorKey, this.backdoorValue);
                    if (!error) {
                        MoPluginHandler.saveBackdoorValues(this.backdoorKey, this.backdoorValue);
                        addActionMessage(getText("samlsso.backdoor.success.config"));
                    }
                }
                initializeRedirectionRules();
                if (error) {
                    response.setContentType(MediaType.APPLICATION_JSON);
                    LOGGER.error(" 400.The Entered parameters are  not found or is empty");
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The requested parameter entered are not found or is empty");
                    return ERROR;
                }

                return SUCCESS;
            } catch (JSONException e) {
                LOGGER.error("An error occurred while creating the rule ", e);
                addActionError("An Error Occurred while creating the rule " + e.getMessage());
                return ERROR;
            } catch (Exception e) {
                LOGGER.error("An error occurred while saving the rule", e);
                addActionError("An Error Occurred while creating the rule. " + e.getMessage());
                return ERROR;
            }
        }else {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return null;
        }


    }


    private void initializeRedirectionRules() {
        bambooRedirectionRulesMap = settings.getBambooRedirectionRulesMap();
        LOGGER.debug("bambooRedirectionRulesMap {}", bambooRedirectionRulesMap);

        idpMap = settings.getIdpMap();
        ssoEnabledIdpList = settings.getSsoEnabledForIdPList();
        defaultBambooIDP = settings.getDefaultBambooIDP();
        defaultRedirectUrl = settings.getDefaultRedirectURL();
        noSsoUrls=settings.getNoSSOUrls();

        if (settings.getSsoEnabledForIdPList().size() == 1) {
            if (!StringUtils.equalsIgnoreCase(settings.getDefaultBambooIDP(), settings.getSsoEnabledForIdPList().get(0))) {
                LOGGER.debug("initializing autoredirect to FALSE");
                enableAutoRedirect = false;
            } else {
                LOGGER.debug("initializing autoRedirect to TRUE");
                enableAutoRedirect = true;
            }
        }


        enableAutoRedirectDelay = settings.getEnableAutoRedirectDelay();


        restrictBackdoor=settings.getRestrictBackdoor();
        enableBackdoor=settings.getBackdoorEnabled();
        backdoorGroupsList=settings.getBackdoorGroups();
        numberOfLoginAttempts = settings.getNumberOfLoginAttempts();
        backdoorKey = settings.getBackdoorKey();
        backdoorValue = settings.getBackdoorValue();
        backdoorSubmitted = BooleanUtils.toBoolean(backdoorSubmitted);
        advancedSettingsSubmitted= BooleanUtils.toBoolean(advancedSettingsSubmitted);
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public MoPluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public void setPluginHandler(MoPluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;
    }

    public DirectoryManager getDirectoryManager() {
        return directoryManager;
    }

    public void setDirectoryManager(DirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDecisionFactor() {
        return decisionFactor;
    }

    public void setDecisionFactor(String decisionFactor) {
        this.decisionFactor = decisionFactor;
    }

    public String getConditionOperation() {
        return conditionOperation;
    }

    public void setConditionOperation(String conditionOperation) {
        this.conditionOperation = conditionOperation;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public String getIdp() {
        return idp;
    }

    public void setIdp(String idp) {
        this.idp = idp;
    }

    public Boolean getAddRedirectionRuleSubmitted() {
        return addRedirectionRuleSubmitted;
    }

    public void setAddRedirectionRuleSubmitted(Boolean addRedirectionRuleSubmitted) {
        this.addRedirectionRuleSubmitted = addRedirectionRuleSubmitted;
    }

    public Boolean getEditRedirectionRuleSubmitted() {
        return editRedirectionRuleSubmitted;
    }

    public void setEditRedirectionRuleSubmitted(Boolean editRedirectionRuleSubmitted) {
        this.editRedirectionRuleSubmitted = editRedirectionRuleSubmitted;
    }

    public Boolean getBambooRulesOrderSubmitted() {
        return bambooRulesOrderSubmitted;
    }

    public void setBambooRulesOrderSubmitted(Boolean bambooRulesOrderSubmitted) {
        this.bambooRulesOrderSubmitted = bambooRulesOrderSubmitted;
    }

    public Map<String, String> getBambooRedirectionRulesMap() {
        return bambooRedirectionRulesMap;
    }

    public void setBambooRedirectionRulesMap(Map<String, String> bambooRedirectionRulesMap) {
        this.bambooRedirectionRulesMap = bambooRedirectionRulesMap;
    }

    public Map<String, String> getIdpMap() {
        return idpMap;
    }

    public void setIdpMap(Map<String, String> idpMap) {
        this.idpMap = idpMap;
    }

    public ArrayList<String> getSsoEnabledIdpList() {
        return ssoEnabledIdpList;
    }

    public void setSsoEnabledIdpList(ArrayList<String> ssoEnabledIdpList) {
        this.ssoEnabledIdpList = ssoEnabledIdpList;
    }

    public String getDefaultBambooIDP() {
        return defaultBambooIDP;
    }

    public void setDefaultBambooIDP(String defaultBambooIDP) {
        this.defaultBambooIDP = defaultBambooIDP;
    }

    public String getDefaultRedirectUrl() {
        return defaultRedirectUrl;
    }

    public void setDefaultRedirectUrl(String defaultRedirectUrl) {
        this.defaultRedirectUrl = defaultRedirectUrl;
    }

    public Boolean getEnableAutoRedirectDelay() {
        return enableAutoRedirectDelay;
    }

    public void setEnableAutoRedirectDelay(Boolean enableAutoRedirectDelay) {
        this.enableAutoRedirectDelay = enableAutoRedirectDelay;
    }

    public Boolean getEnableAutoRedirect() {
        return enableAutoRedirect;
    }

    public void setEnableAutoRedirect(Boolean enableAutoRedirect) {
        this.enableAutoRedirect = enableAutoRedirect;
    }

    public Boolean getEnableBackdoor() {
        return enableBackdoor;
    }

    public void setEnableBackdoor(Boolean enableBackdoor) {
        this.enableBackdoor = enableBackdoor;
    }

    public String getBackdoorKey() {
        return backdoorKey;
    }

    public void setBackdoorKey(String backdoorKey) {
        this.backdoorKey = backdoorKey;
    }

    public String getBackdoorValue() {
        return backdoorValue;
    }

    public void setBackdoorValue(String backdoorValue) {
        this.backdoorValue = backdoorValue;
    }

    public Boolean getRestrictBackdoor() {
        return restrictBackdoor;
    }

    public void setRestrictBackdoor(Boolean restrictBackdoor) {
        this.restrictBackdoor = restrictBackdoor;
    }

    public List<String> getBackdoorGroupsList() {
        return backdoorGroupsList;
    }

    public void setBackdoorGroupsList(List<String> backdoorGroupsList) {
        this.backdoorGroupsList = backdoorGroupsList;
    }

    public String getNumberOfLoginAttempts() {
        return numberOfLoginAttempts;
    }

    public void setNumberOfLoginAttempts(String numberOfLoginAttempts) {
        this.numberOfLoginAttempts = numberOfLoginAttempts;
    }

    public String getBackdoorGroups() {
        return backdoorGroups;
    }

    public void setBackdoorGroups(String backdoorGroups) {
        this.backdoorGroups = backdoorGroups;
    }

    public Boolean getBackdoorSubmitted() {
        return backdoorSubmitted;
    }

    public void setBackdoorSubmitted(Boolean backdoorSubmitted) {
        this.backdoorSubmitted = backdoorSubmitted;
    }

    public Boolean getAdvancedSettingsSubmitted() {
        return advancedSettingsSubmitted;
    }

    public void setAdvancedSettingsSubmitted(Boolean advancedSettingsSubmitted) {
        this.advancedSettingsSubmitted = advancedSettingsSubmitted;
    }

    public List<Directory> getDirectoryList() {
        List<Directory> directoryList = directoryManager.findAllDirectories();
        LOGGER.debug("Directory list size {}",directoryList.size());
        return directoryList;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    public List<String> getIdpList() {
        return settings.getIdPList();
    }

    public List<String> getNoSsoUrls() {
        return noSsoUrls;
    }

    public void setNoSsoUrls(List<String> noSsoUrls) {
        this.noSsoUrls = noSsoUrls;
    }
}
