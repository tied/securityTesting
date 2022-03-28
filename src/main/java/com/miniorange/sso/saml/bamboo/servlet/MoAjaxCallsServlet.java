package com.miniorange.sso.saml.bamboo.servlet;

import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.miniorange.sso.saml.bamboo.*;
import static com.miniorange.sso.saml.bamboo.MoPluginHandler.directoryManager;


import com.miniorange.sso.saml.utils.MoHttpUtils;
import com.miniorange.sso.saml.utils.MoJSONUtils;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.sal.api.message.I18nResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MoAjaxCallsServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoAjaxCallsServlet.class);

    private MoSAMLSettings settings;
    private BambooUserManager bambooUserManager;
    private MoPluginHandler pluginHandler;
    private I18nResolver i18nResolver;
    private MoSAMLManager samlManager;
    private MoSAMLUserManager userManager;

    public MoAjaxCallsServlet(MoSAMLSettings samlSettings/*, CrowdService crowdService*/, BambooUserManager bambooUserManager, MoPluginHandler pluginHandler,
                              I18nResolver i18nResolver, MoSAMLManager samlManager, MoSAMLUserManager userManager) {
        super();
        this.settings = samlSettings;
        this.bambooUserManager = bambooUserManager;
        this.pluginHandler = pluginHandler;
        this.i18nResolver = i18nResolver;
        this.samlManager = samlManager;
        this.userManager = userManager;


    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!pluginHandler.checkForAdminSession()){
            LOGGER.error("Access Denied. You will need admin privileges to make this API call");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You will need admin privileges to make this API call.");
            return;
        }
        String action = org.apache.commons.lang3.StringUtils.trimToEmpty(req.getParameter("action"));
        LOGGER.debug("Parameters: ");
        Enumeration<String> parameterNames = req.getParameterNames();
        while(parameterNames.hasMoreElements()){
            LOGGER.debug("Parameter "+parameterNames.nextElement());
        }
        String referer = req.getHeader("referer");
        LOGGER.debug("Referer Header:" + referer);
        if (settings.getPluginApiAccessRestriction()
                && !org.apache.commons.lang3.StringUtils.startsWith(referer, this.settings.getSpBaseUrl())) {
            LOGGER.error(
                    "Access Denied. API Restriction is enabled and request is not originated from the Bamboo.");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You are not allowed to access this page.");
            LOGGER.error("403. Access Denied.You are not allowed to access this page.");
            return;
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(action)) {
            resp.setContentType(MediaType.APPLICATION_JSON);
            LOGGER.error(" 400.The requested parameter [action] not found or is empty");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The requested parameter [action] not found or is empty");
            return;
        }

        switch (action) {
            case "deleteRule":
                deleteRedirectionRule(req, resp);
                break;
            case "clearMigrationValue":
                clearMigrationValue(req, resp);
                break;

            default:
                resp.setContentType(MediaType.APPLICATION_JSON);
                LOGGER.error(" 405.The requested action is not allowed. Choose valid Action");
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "The requested action is not allowed. Choose valid Action");
                return;
        }
    }

    private void clearMigrationValue(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        settings.setisMigrated(false);
        sendSuccessFullResponse(JSONObject.quote("Migration Value Cleared").toString(),resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.debug("Ajax Servlet doGet Called");
        String action = req.getParameter("action");

        String referer = req.getHeader("referer");
        LOGGER.debug("Referer Header:" + referer);

        if(settings.getPluginApiAccessRestriction() && !StringUtils.startsWith(referer, this.settings.getSpBaseUrl())) {
            LOGGER.error("Access Denied. API Restriction is enabled and request is not originated from the Bamboo.  ");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You are not allowed to access this page.");
            //resp.sendRedirect(settings.getLoginPageUrl());
            return;
        }

        if(StringUtils.isBlank(action)){
            resp.setContentType("text/html");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"The requested parameter [action] not found or is empty");
            return;

        }

        switch (action){
            case "checkBackdoorAccess":
                checkBackdoorAccess(req, resp);
                break;
            case "fetchGroups":
                if (settings.getCurrentBuildNumber() > 60604) {
                    fetchGroups(req, resp);
                }
                break;
            case "setLoginAttempts":
                setLoginAttempts(req,resp);
                break;
            case "deleteLoginAttempts":
                deleteLoginAttempts(req,resp);
                break;
            case "getLoginAttempts":
                getLoginAttempts(req,resp);
                break;
            case "setRememberMeCookie":
                handleRememberMeCookie(req,resp);
                break;
            case "getAttributes":
                getAttributes(req, resp);
                break;
            case "getGroups":
                getGroups(req, resp);
                break;
            case "getRule":
                getRule(req,resp);
                break;
            case "getBambooRedirectionRules":
                getBambooRedirectionRules(req, resp);
                break;
            case "processRedirectionRules":
                processRedirectionRules(req, resp);
                break;
            case "fetchDirectory":
                fetchDirectory(req, resp);
                break;
            default:
                resp.setContentType("text/html");
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"The requested action is not allowed. Choose valid Action");
                return;
        }
        return;
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!pluginHandler.checkForAdminSession()){
            LOGGER.error("Access Denied. You will need admin privileges to make this API call");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You will need admin privileges to make this API call.");
            return;
        }

        String action = req.getParameter("action");
        LOGGER.debug("Action "+action);
        String referer = req.getHeader("referer");
        LOGGER.debug("Referer Header:" + referer);

        if (settings.getPluginApiAccessRestriction()
                && !StringUtils.startsWith(referer, this.settings.getSpBaseUrl())) {
            LOGGER.error(
                    "Access Denied. API Restriction is enabled and request is not originated from the Bamboo.");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You are not allowed to access this page.");
            LOGGER.error("403. Access Denied.You are not allowed to access this page.");
            return;
        }

        if (StringUtils.isBlank(action)) {
            resp.setContentType(MediaType.APPLICATION_JSON);
            LOGGER.error(" 400.The requested parameter [action] not found or is empty");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The requested parameter [action] not found or is empty");
            return;
        }


        switch (action) {
            case "manualConfiguration":
                //saveIdpConfigurations(req, resp);
                break;
            case "saveAttributeMapping":
                //saveAttributeMapping(req, resp);
                break;
            case "importFromMetadata":
                //importFromMetadata(req, resp);
                break;
            case "saveSsoConfig":
                saveSSOConfig(req,resp);
                break;
            case "updateUrls":
                updateUrls(req, resp);
                break;
            case "saveDefaultRule":
                saveDefaultRule(req, resp);
                break;
            case "saveDefaultRedirectURL":
                saveDefaultRedirectURL(req,resp);
                break;
            case "saveOrder":
                saveRulesOrder(req,resp);
                break;
            case "setRememberMeCookie":
                handleRememberMeCookie(req,resp);
                break;
            case "setIntroPage":
                setIntroPage(req, resp);
                break;
            case "finishQuickSetup":
                finishQuickSetup(req,resp);
                break;
            default:
                resp.setContentType(MediaType.APPLICATION_JSON);
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        "The requested action is not allowed. Choose valid Action");
                return;
        }
        return;
    }
    private void getLoginAttempts(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        Cookie attemptCookie= getCookie("numberOfAttempts",req);

        JSONObject result = new JSONObject();
        try {
            if(attemptCookie!=null){
                result.put("numberOfAttempts",(String)attemptCookie.getValue());
                LOGGER.debug("VALUE OF BACKDOOR LOGIN ATTEMPT ATTRIBUTE"+(String)attemptCookie.getValue());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.error("An Error Occurred while fetching login Attempts", e);
        }
        sendSuccessFullResponse(result.toString(), resp);
        return;
    }

    private void setLoginAttempts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	String numberOfLoginAttempts = settings.getNumberOfLoginAttempts();
        LOGGER.debug("setLoginAttempts Called");
        Cookie attemptCookie= getCookie("numberOfAttempts",req);         
        if(attemptCookie==null){
            String url=req.getRequestURL().toString();
            attemptCookie = new Cookie("numberOfAttempts","1");
            attemptCookie.setPath("/plugins/servlet/saml");
            attemptCookie.setSecure(StringUtils.isNotBlank(url) && url.length()>7 && url.substring(0,8).equalsIgnoreCase("https://"));
            attemptCookie.setHttpOnly(true);
        }
        else if(Integer.parseInt(attemptCookie.getValue())< Integer.parseInt(numberOfLoginAttempts)+1) {
            int number=Integer.parseInt(attemptCookie.getValue());
                number=number+1;
                attemptCookie.setValue(Integer.toString(number));
                attemptCookie.setHttpOnly(true);
        		if(number== Integer.parseInt(numberOfLoginAttempts)+1){
                LOGGER.debug("LOGIN ATTEMPT LIMIT REACHED");
                attemptCookie.setMaxAge(600);
            } 
        }     
       
       resp.addCookie(attemptCookie);
        JSONObject result = new JSONObject();
        try {
            result.put("numberOfAttempts",attemptCookie.getValue());
        }catch (JSONException e) {
            e.printStackTrace();
            LOGGER.error("An Error Occurred while updating login Attempts", e);
        }
        sendSuccessFullResponse(result.toString(), resp);
        return;
    }

    private void deleteLoginAttempts(HttpServletRequest req,HttpServletResponse resp) throws IOException {
            JSONObject result = new JSONObject();
            clearCookie("numberOfAttempts",req,resp);
            LOGGER.debug("SESSION DELETED SUCCESSFULLY");
            sendSuccessFullResponse(result.toString(), resp);
            return;
    }

    private void setIntroPage(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        LOGGER.debug("\n\nSetting Intro Page value to false...");
        settings.setShowIntroPage(false);
        JSONObject result = new JSONObject();
        sendSuccessFullResponse(result.toString(), resp);
        return;
    }

    private void handleRememberMeCookie(HttpServletRequest req, HttpServletResponse resp){
        Boolean enableRememberMeCookie = org.apache.commons.lang.BooleanUtils.toBoolean(req.getParameter("value"));
        LOGGER.debug("Enable Remember Me : " + enableRememberMeCookie);
        settings.setRememberMeCookieEnabled(enableRememberMeCookie);
        try{
            JSONObject result = new JSONObject();
            result.put("result", "Settings saved");
            sendSuccessFullResponse(result.toString(), resp);
        } catch (JSONException | IOException e) {
            LOGGER.error("An error occurred sending response ", e);
        }
    }

    private void finishQuickSetup (HttpServletRequest req, HttpServletResponse resp){
        String idpID = req.getParameter("idp");
        try {
            JSONObject idpConfigObject = settings.getIdpConfig(idpID);
            idpConfigObject.put("finishQuickSetup", "true");
            settings.setIdpConfig(idpConfigObject,idpID);
            MoHttpUtils.clearCookie(req,resp, MoPluginConstants.QUICKSETUP_IDP);
        }catch (Exception e){
            LOGGER.debug("error saving option finish Quick Setup for idp "+idpID);
        }
    }

    private void deleteRedirectionRule(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ruleId = req.getParameter("key");
        if(org.apache.commons.lang.StringUtils.isBlank(ruleId)){
            sendErrorResponse(JSONObject.quote(i18nResolver.getText("samlsso.error.empty.rule.key")),HttpServletResponse.SC_BAD_REQUEST,resp);
            return;
        }

        settings.deleteRedirectionRule(ruleId);
        sendSuccessFullResponse(JSONObject.quote(i18nResolver.getText("samlsso.success.rule.deleted",ruleId)),resp);

    }

    private void updateUrls(HttpServletRequest req, HttpServletResponse resp) {
        try {
            LOGGER.debug("Update urls called via ajax call.");
            String updatedSpBaseUrl = org.apache.commons.lang.StringUtils.trim(req.getParameter("updatedspbaseurl"));
            String updatedSpEntityID = org.apache.commons.lang.StringUtils.trim(req.getParameter("updatedspentityid"));
            LOGGER.debug("values received : " + updatedSpBaseUrl + ", " + updatedSpEntityID);
            Boolean hasValidURL = Boolean.FALSE;
            if (org.apache.commons.lang3.StringUtils.isNotBlank(updatedSpBaseUrl)) {
                try {
                    new URL(updatedSpBaseUrl);
                    hasValidURL = Boolean.TRUE;
                } catch (MalformedURLException e) {
                    hasValidURL = Boolean.FALSE;
                }
            }
            if (hasValidURL) {
                MoPluginHandler.saveSPConfiguration(StringUtils.trim(updatedSpBaseUrl), StringUtils.trim(updatedSpEntityID));

                JSONObject result = new JSONObject();
                result.put("loginurl",settings.getLoginServletUrl());
                result.put("newentityid",settings.getSpEntityId());
                result.put("metadataurl", settings.getSpBaseUrl() + "/plugins/servlet/saml/metadata");
                sendSuccessFullResponse(result.toString(), resp);
            }
        } catch (JSONException | IOException e) {
            LOGGER.error("An error occurred while fetching groups ", e);
        }
    }

    private void getBambooRedirectionRules(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JSONArray rules = settings.getRedirectionRules();
        ArrayList<String> ssoEnabledIdpList = settings.getSsoEnabledForIdPList();
        Map<String, String> rulesMap = settings.getBambooRedirectionRulesMap();

        JSONObject rule;
        String idpName;
        String ruleName;
        Map<String, String> finalRulesMap = new HashMap<>();

        try {
            for (int i = 0; i < rules.length(); i++) {
                rule = rules.optJSONObject(i);
                ruleName = rule.getString("name");
                idpName = rule.getString("idp");
                if(!org.apache.commons.lang3.StringUtils.equals(idpName, "loginpage")){
                    if(ssoEnabledIdpList.contains(idpName)){
                        finalRulesMap.put(ruleName, rulesMap.get(ruleName));
                    }
                }else{
                    finalRulesMap.put(ruleName, rulesMap.get(ruleName));
                }
            }
            LOGGER.debug("finalRulesMap json: "+ MoJSONUtils.convertMapToJSON(finalRulesMap));
            sendSuccessFullResponse(MoJSONUtils.convertMapToJSON(finalRulesMap), resp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void getGroups(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = request.getParameter("type");
        switch (type) {
            case "backdoor":
                sendBackdoorGroups(request, response);
                break;
            default:
                response.setContentType("text/html");
                LOGGER.error("Incorrect Type selected for fetching groups " + type);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect type selected for fetching groups. Choose valid type");
        }
    }

    private void getAttributes(HttpServletRequest req, HttpServletResponse resp) {
        try {
            JSONObject idpConfig = settings.getIdpConfig(req.getParameter("idpname"));
            String searchParam = org.apache.commons.lang.StringUtils.trim(req.getParameter("search"));
            if (idpConfig.has("testConfig")) {
                Map<String, List<String>> testConfig = MoSAMLUtils.toMap((JSONObject) settings.getIdpConfig(req.getParameter("idpname")).get("testConfig"));
                ArrayList<String> attributesMap = MoPluginHandler.dynamicSearchAttributes(MoSAMLUtils.parseIdpTestConfigObject(testConfig),searchParam);
                JSONObject data = new JSONObject();
                JSONArray resultArray = new JSONArray();
                for (String attribute : attributesMap) {
                    Map<String, Object> attributesResultMap = new HashMap<>();
                    attributesResultMap.put("id", attribute);
                    attributesResultMap.put("text", attribute);
                    resultArray.put(attributesResultMap);
                }
                data.put("results", resultArray);
                sendSuccessFullResponse(data.toString(), resp);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            LOGGER.error("An error occurred while fetching groups ", e);
        }
    }
    private void sendBackdoorGroups(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<String> groups = settings.getBackdoorGroups();
        try {
            JSONObject data = new JSONObject();
            JSONArray resultArray = new JSONArray();

            for (String groupName : groups) {
                Map<String, Object> groupResultsMap = new HashMap<>();
                groupResultsMap.put("id", groupName);
                groupResultsMap.put("text", groupName);
                resultArray.put(groupResultsMap);
            }
            data.put("results", resultArray);
            sendSuccessFullResponse(data.toString(), response);
        } catch (JSONException e) {
            LOGGER.error("An error occurred while fetching groups ", e);
        }
    }

    private void processRedirectionRules(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = org.apache.commons.lang.StringUtils.trim(req.getParameter("mo_username"));
        if (org.apache.commons.lang.StringUtils.isBlank(username)) {
            resp.setContentType(MediaType.APPLICATION_JSON);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The username is empty");
            return;
        }
        String idp = samlManager.processRedirectionRulesAndReturnIDP(username);
        if (org.apache.commons.lang.StringUtils.isBlank(idp)) {
            resp.setContentType(MediaType.APPLICATION_JSON);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Some Error Occurred While Processing you username. Please contact your administrator");
            return;
        }
        try {
            JSONObject idpResponse = new JSONObject();
            idpResponse.put("idp", idp);
            if (StringUtils.equalsIgnoreCase(idp,"redirectUrl")){
                idpResponse.put("defaultRedirectUrl",settings.getDefaultRedirectURL());}
            sendSuccessFullResponse(idpResponse.toString(), resp);
        } catch (JSONException e) {
            LOGGER.error("An Error Occurred while creating response ", e);
        }
    }

    private void getRule(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("key");
        JSONObject rule = settings.getRedirectionRule(id);
        if(rule == null) {
            sendErrorResponse("Rule not found", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp);
            return;
        }
        sendSuccessFullResponse(rule.toString(), resp);
    }

    private void sendErrorResponse(String errorMessages,int errorCode, HttpServletResponse resp) throws IOException {
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setStatus(errorCode);
        resp.getOutputStream().write(errorMessages.getBytes(StandardCharsets.UTF_8));
        resp.getOutputStream().close();
        return;
    }

    private void saveRulesOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Map<String, String> redirectionRulesMap = settings.getBambooRedirectionRulesMap();
        Map<String, String> newRedirectionRulesMap = new LinkedHashMap<>();
        String[] ruleNames = req.getParameterValues("ruleKey");

        for(String ruleName:ruleNames){
            newRedirectionRulesMap.put(ruleName, redirectionRulesMap.get(ruleName));
        }

        pluginHandler.saveBambooRedirectionRules(newRedirectionRulesMap);

        sendSuccessFullResponse(null,resp);
    }

    private void saveDefaultRule(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idpId = req.getParameter("defaultIDP");
        settings.setDefaultBambooIDP(idpId);
        sendSuccessFullResponse(JSONObject.quote(i18nResolver.getText("samlsso.defaultrule.config")), resp);
    }

    private void saveDefaultRedirectURL(HttpServletRequest req, HttpServletResponse resp)throws IOException{
        String defaultRedirectURL = org.apache.commons.lang3.StringUtils.trim(req.getParameter("defaultRedirectURL"));
        if (org.apache.commons.lang.StringUtils.isBlank(defaultRedirectURL)){
            sendErrorResponse(i18nResolver.getText("samlsso.error.empty.redirect.url.platform"),HttpServletResponse.SC_BAD_REQUEST,resp);
            return;
        }else {
            try {
                new URL(defaultRedirectURL);
            } catch (MalformedURLException e) {
                LOGGER.error("Invalid redirect URL", e);
                sendErrorResponse(i18nResolver.getText("samlsso.error.wrong.redirect.url.platform"), HttpServletResponse.SC_BAD_REQUEST, resp);
                return;
            }
        }
        LOGGER.debug("default redirect URL for Bamboo submitted : " + defaultRedirectURL);
        settings.setDefaultRedirectURL(defaultRedirectURL);
        settings.setDefaultBambooIDP("redirectUrl");
        sendSuccessFullResponse(JSONObject.quote(i18nResolver.getText("samlsso.defaultrule.config")), resp);
    }

    private void checkBackdoorAccess(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = StringUtils.trim(req.getParameter("mo_username"));
        if (StringUtils.isBlank(username)) {
            resp.setContentType(MediaType.APPLICATION_JSON);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The username is empty");
            return;
        }
        BambooUser bambooUser = bambooUserManager.getBambooUser(username);
        boolean isUserPresentInGroups;
        JSONObject result = new JSONObject();

        if (bambooUser != null) {
            List<String> groupList = settings.getBackdoorGroups();
            /*if (groupList.size() <= 0) {
                groupList.add("bamboo-admin");
            }*/
            isUserPresentInGroups = isUserPresentInGroups(username, groupList);
            LOGGER.debug("isUserPresentInGroups " + isUserPresentInGroups);
            try {
                result.put("isUserAllowedBackdoorAccess", isUserPresentInGroups);
            } catch (JSONException e) {
                e.printStackTrace();
                LOGGER.error("An Error Occurred while checking Backdoor Access for User ", e);
            }
        }else{
            LOGGER.debug("User {" + username + "} not found.");
            result.put("isUserAllowedBackdoorAccess", false);
        }
        sendSuccessFullResponse(result.toString(), resp);

    }

    private void saveSSOConfig(HttpServletRequest request, HttpServletResponse response){
        JSONObject result = new JSONObject();
        String idpId = request.getParameter("idpid");
        String isChecked = request.getParameter("isChecked");
        try {
            MoPluginHandler.saveListIDPConfigurations(idpId, BooleanUtils.toBoolean(isChecked));
            result.put("result", "Settings saved");
            sendSuccessFullResponse(result.toString(), response);
        }catch (Exception e) {
            LOGGER.error("An error occurred while saving enable SSO details",e);
        }
    }

    private Boolean isUserPresentInGroups(String username, List<String> groups) {
        LOGGER.debug("Testing for user " + username);
        try {
            BambooUser bambooUser = bambooUserManager.getBambooUser(username);
            List<String> existingGroupsOfUser = new ArrayList<>();
            existingGroupsOfUser = bambooUserManager.getGroupNamesAsList(bambooUser);
            if (bambooUser != null) {
                for (String group : groups) {
                    if (existingGroupsOfUser.contains(group)) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    private void fetchGroups(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.debug("Ajax Servlet fetchGroup Called");
        try {
            String search = StringUtils.trim(req.getParameter("search"));
            if (StringUtils.equalsIgnoreCase(search, "*")) {
                search = StringUtils.EMPTY;
            }

            com.atlassian.crowd.embedded.api.CrowdService crowdService = ComponentLocator.getComponent(com.atlassian.crowd.embedded.api.CrowdService.class);

            EntityQuery<com.atlassian.crowd.embedded.api.Group> query = QueryBuilder.queryFor(com.atlassian.crowd.embedded.api.Group.class, EntityDescriptor.group())
                    .with(Restriction.on(GroupTermKeys.NAME).startingWith(search))
                    .returningAtMost(10);

            LOGGER.debug("query =  " + query);

            Iterable<com.atlassian.crowd.embedded.api.Group> groups = crowdService.search(query);

            JSONObject data = new JSONObject();
            JSONArray resultArray = new JSONArray();

            for (com.atlassian.crowd.embedded.api.Group groupName : groups) {
                Map<String, Object> groupResultsMap = new HashMap<>();
                groupResultsMap.put("id", groupName.getName());
                groupResultsMap.put("text", groupName.getName());
                resultArray.put(groupResultsMap);
            }
            LOGGER.debug("resultArray :" +resultArray.toString());

            data.put("results", resultArray);
            sendSuccessFullResponse(data.toString(), resp);
        }catch (JSONException | IOException e){
            e.printStackTrace();
            LOGGER.error("An error occurred while fetching groups ",e);
        }
    }

    private void fetchDirectory(HttpServletRequest req, HttpServletResponse resp){
        try{
            JSONArray resultArray = new JSONArray();
            JSONObject data = new JSONObject();
            List<Directory> directoryList = directoryManager.findAllDirectories();
            LOGGER.debug("Directory list size {}"+ directoryList.size());
            for (Directory directoryName : directoryList) {
                Map<String, Object> directoryResultsMap = new HashMap<>();
                directoryResultsMap.put("id", directoryName.getName());
                directoryResultsMap.put("text", directoryName.getName());
                resultArray.put(directoryResultsMap);
            }

            data.put("results", resultArray);
            sendSuccessFullResponse(data.toString(), resp);

        }catch (Exception e){
            LOGGER.error("An error occurred while fetching Directory ", e);
        }

    }


    private void sendSuccessFullResponse(String result, HttpServletResponse resp) throws IOException {
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setStatus(HttpServletResponse.SC_OK);
        if (result != null) {
            resp.getOutputStream().write(result.getBytes(StandardCharsets.UTF_8));
            resp.getOutputStream().close();
        }
        return;
    }

    private Cookie getCookie(String cookieName, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public void clearCookie(String cookieName, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Clearing cookie : "+cookieName);
        Cookie cookie = getCookie(cookieName, request);
        if (cookie != null) {
            cookie.setPath("/plugins/servlet/saml");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }


    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public BambooUserManager getBambooUserManager() {
        return bambooUserManager;
    }

    public void setBambooUserManager(BambooUserManager bambooUserManager) {
        this.bambooUserManager = bambooUserManager;
    }

    public MoPluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public void setPluginHandler(MoPluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;
    }

    public I18nResolver getI18nResolver() {
        return i18nResolver;
    }

    public void setI18nResolver(I18nResolver i18nResolver) {
        this.i18nResolver = i18nResolver;
    }
}
