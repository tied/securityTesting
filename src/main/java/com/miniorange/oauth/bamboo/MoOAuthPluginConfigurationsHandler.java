package com.miniorange.oauth.bamboo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import com.miniorange.oauth.MoOAuthPluginException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;

public class MoOAuthPluginConfigurationsHandler {
    private static final Log LOGGER = LogFactory.getLog(MoOAuthPluginConfigurationsHandler.class);
    private MoOAuthSettings settings;

    public MoOAuthPluginConfigurationsHandler(MoOAuthSettings settings) {
        this.settings = settings;
    }

    // Export Configuration
    public String generateConfigurationsJson() throws JSONException {
        LOGGER.info("Generating configurations JSON string ");

        JSONObject config = new JSONObject();

        config.putOpt("SERVER_VERSION", settings.getServerVersion());
        config.putOpt("PLUGIN_NAME", settings.getPluginName());
        config.putOpt("PLUGIN_VERSION", settings.getPluginVersion());

        exportOAuthConfig(config);
        exportAttributeMapping(config);
        exportGroupMapping(config);
        exportSignOnSettings(config);

        return config.toString(2);
    }
    private void exportOAuthConfig(JSONObject config) {
        LOGGER.info("Inserting OAuth configuration.");
        JSONObject oauthConfig = new JSONObject();
        try {
            for(MoOAuthPluginConstants.OAuthConfiguration constant : MoOAuthPluginConstants.OAuthConfiguration.values()){
                exportConfig(oauthConfig, String.valueOf(constant), constant.getKey());
            }
            config.putOpt("OAuth Configuration", oauthConfig);
        } catch (JSONException e) {
            LOGGER.error("Error occurred while inserting OAuth configuration in JSON " + e.getMessage() + " Cause: " + e.getCause());
            e.printStackTrace();
        }
    }
    private void exportAttributeMapping(JSONObject config) {
        LOGGER.info("Inserting User Profile configuration in JSON Object.");
        JSONObject attributeMapping = new JSONObject();
        try {
            for(MoOAuthPluginConstants.AttributeMapping constant : MoOAuthPluginConstants.AttributeMapping.values()) {
                exportConfig(attributeMapping, String.valueOf(constant), constant.getKey());
            }
            config.putOpt("Attribute Mapping", attributeMapping);
        } catch (JSONException e) {
            LOGGER.error("Error occurred while inserting Attribute Mapping configuration in JSON " + e.getMessage() + " Cause: " + e.getCause());
            e.printStackTrace();
        }
    }
    private void exportGroupMapping(JSONObject config) {
        LOGGER.info("Inserting User Groups configuration in JSON Object.");
        JSONObject groupMapping = new JSONObject();
        try {
            for(MoOAuthPluginConstants.GroupMapping constant : MoOAuthPluginConstants.GroupMapping.values()) {
                exportConfig(groupMapping, String.valueOf(constant), constant.getKey());
            }
            config.putOpt("Group Mapping", groupMapping);
        } catch (JSONException e) {
            LOGGER.error("Error occurred while inserting Group Mapping configuration in JSON " + e.getMessage() + " Cause: " + e.getCause());
            e.printStackTrace();
        }
    }
    private void exportSignOnSettings(JSONObject config) {
        LOGGER.info("Inserting Sign in settings configuration.");
        JSONObject signInSettings = new JSONObject();
        try {
            for(MoOAuthPluginConstants.SignInSettings constant : MoOAuthPluginConstants.SignInSettings.values()) {
                exportConfig(signInSettings, String.valueOf(constant), constant.getKey());
            }
            config.putOpt("Sign In Settings", signInSettings);
        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.error("Error occurred while inserting Sign in settings configuration in JSON " + e.getMessage() + " Cause: " + e.getCause());
        }
    }
    public void exportConfig(JSONObject jsonObject, String constantName, String constantKey){
        if(settings.getPluginSettings().get(constantKey ) instanceof Boolean){
            //boolean
            jsonObject.putOpt(constantName, BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) settings.getPluginSettings().get(constantKey)), false));
        }
        else if(settings.getPluginSettings().get(constantKey) instanceof List){
            //List
            List<String> list = null;
            if(constantName == "DEFAULT_GROUPS"){
                //DEFAULT_GROUPS .... In Higher Builds only
                if(settings.isLowerBuild())
                    return;
                list = settings.getDefaultGroupsList();
            }else{
                list = (List<String>) settings.getPluginSettings().get(constantKey);
            }

            if(list.isEmpty()){
                jsonObject.append(constantName, "");
            } else{
                for (String listItem : list) {
                    jsonObject.append(constantName, listItem);
                }
            }
        }
        else if(settings.getPluginSettings().get(constantKey) instanceof HashMap){
            //HashMap
            JSONObject jsonObjectForHasMap = new JSONObject();
            if (settings.getRoleMapping() != null) {
                for (String key : settings.getRoleMapping().keySet()) {
                    jsonObjectForHasMap.putOpt(key, settings.getRoleMapping().get(key));
                }
            }
            jsonObject.putOpt(constantName, jsonObjectForHasMap);

            JSONObject jsonObjectForHasMapAuthorizationAttr = new JSONObject();
            if (StringUtils.equalsIgnoreCase(constantName,"OAUTH_REQUEST_PARAMETERS")  && settings.getOauth_request_parameters() != null) {
                for (String key : settings.getOauth_request_parameters().keySet()) {
                    jsonObjectForHasMapAuthorizationAttr.putOpt(key, settings.getOauth_request_parameters().get(key));
                }
                jsonObject.putOpt(constantName, jsonObjectForHasMapAuthorizationAttr);
            }
        }
        else {
            //string
            if(constantName == "DEFAULT_GROUP") {
                //DEFAULT_GROUP .... In Lower Builds only
                if (!settings.isLowerBuild())
                    return;
            }
            jsonObject.putOpt(constantName, StringUtils.defaultIfBlank((String) settings.getPluginSettings().get(constantKey), StringUtils.EMPTY));
        }
    }


    // Import Configuration
    public void importConfigurations(File configFile) throws MoOAuthPluginException, JSONException, IOException {
        LOGGER.info("Importing app's configuration from config file");

        //Get configuration from file
        String pluginConfigurations = IOUtils.toString(new FileInputStream(configFile));
        importPluginConfigurations(pluginConfigurations);
    }
    public void importPluginConfigurations(String pluginConfigurationsJson) throws JSONException, MoOAuthPluginException{
        LOGGER.info("Importing app Configurations in JSON Object ");

        JSONObject pluginConfigObj = new JSONObject(pluginConfigurationsJson);

        JSONObject oauthConfig = pluginConfigObj.optJSONObject("OAuth Configuration");
        if ((oauthConfig != null) && (oauthConfig.length() != 0)) {
            //Check if any application is configured
            String clientId = oauthConfig.optString("CLIENT_ID");
            String clientSecret = oauthConfig.optString("CLIENT_SECRET");
            if(clientId.isEmpty() || clientSecret.isEmpty()){
                //No application found
                String error = "An error occurred while importing configurations. Please make sure you have selected valid configurations file";
                throw new MoOAuthPluginException(MoOAuthPluginException.PluginErrorCode.SAVING_DETAILS, error);
            }
            //clear previous settings
            settings.setResetSettings(Boolean.TRUE);
            clearPluginSettings();
            //import configurations
            importOAuthConfig(oauthConfig);
        } else{
            throw new MoOAuthPluginException(MoOAuthPluginException.PluginErrorCode.SAVING_DETAILS,"No Application Found. Please upload correct file");
        }

        JSONObject attributeMapping = pluginConfigObj.optJSONObject("Attribute Mapping");
        if ((attributeMapping != null) && (attributeMapping.length() != 0)) {
            importAttributeMapping(attributeMapping);
        }

        JSONObject groupMapping = pluginConfigObj.optJSONObject("Group Mapping");
        if ((groupMapping != null) && (groupMapping.length() != 0)) {
            importGroupMapping(groupMapping);
        }

        JSONObject signInSettings = pluginConfigObj.optJSONObject("Sign In Settings");
        if ((signInSettings != null ) && (signInSettings.length() != 0)) {
            importSignInSettings(signInSettings);
        }
    }
    private void importOAuthConfig(JSONObject oAuthConfigObj) throws JSONException {
        for(MoOAuthPluginConstants.OAuthConfiguration constant : MoOAuthPluginConstants.OAuthConfiguration.values()){
            LOGGER.info("importOAuthConfig called ");
            importConfig(oAuthConfigObj, String.valueOf(constant), constant.getKey());
        }
    }
    private void importAttributeMapping(JSONObject attributeMappingObj) throws JSONException {
        LOGGER.info("importAttributeMapping called ");
        for(MoOAuthPluginConstants.AttributeMapping constant : MoOAuthPluginConstants.AttributeMapping.values()){
            importConfig(attributeMappingObj, String.valueOf(constant), constant.getKey());
        }
    }
    private void importGroupMapping(JSONObject groupMappingObj) throws JSONException {
        LOGGER.info("importGroupMapping called ");
        for(MoOAuthPluginConstants.GroupMapping constant : MoOAuthPluginConstants.GroupMapping.values()) {
            importConfig(groupMappingObj, String.valueOf(constant), constant.getKey());
        }
    }
    private void importSignInSettings(JSONObject signInSettingsObj) throws JSONException {
        LOGGER.info("importSignInSettings called ");
        for(MoOAuthPluginConstants.SignInSettings constant : MoOAuthPluginConstants.SignInSettings.values()){
            importConfig(signInSettingsObj, String.valueOf(constant), constant.getKey());
        }
    }
    private void importConfig(JSONObject jsonObject, String constantName, String constantKey){
        if(settings.getPluginSettings().get(constantKey) instanceof Boolean){
            //boolean
            settings.getPluginSettings().put(constantKey, BooleanUtils.toString(jsonObject.optBoolean(constantName), "true", "false", "false"));
        }
        else if (settings.getPluginSettings().get(constantKey) instanceof List) {
            //List
            List<String> list = new ArrayList<String>();
            JSONArray jsonArray = jsonObject.optJSONArray(constantName);
            if(jsonArray != null){
                for(int i = 0; i < jsonArray.length(); i++){
                    list.add(jsonArray.getString(i));
                }
            }
            settings.getPluginSettings().put(constantKey, list);
        }
        else if (settings.getPluginSettings().get(constantKey) instanceof HashMap) {
            //HashMap
            HashMap<String, String> hashMap = new HashMap<>();
            JSONObject jsonObjectForHashMap = jsonObject.optJSONObject(constantName);
            if (jsonObjectForHashMap != null) {
                Iterator<String> iterator = jsonObjectForHashMap.keys();;
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    hashMap.put(key, jsonObjectForHashMap.getString(key));
                }
            }
            settings.getPluginSettings().put(constantKey, hashMap);
        }
        else {
            //string
            LOGGER.debug("String");
            settings.getPluginSettings().put(constantKey, StringUtils.trimToEmpty(StringUtils.defaultIfBlank(jsonObject.optString(constantName), StringUtils.EMPTY)));
        }
    }


    //Reset Configuration
    public void clearPluginSettings() {
        LOGGER.info("clearPluginSettings called");
        resetOAuthConfig();
        resetAttributeMapping();
        resetGroupMapping();
        resetSignOnSettings();

        //Get default configuration
        LOGGER.info("Getting default configuration");
        MoOAuthPluginHandler.saveDefaultConfiguration();

    }
    public void resetOAuthConfig() {
        LOGGER.info("resetOAuthConfig called ");
        for(MoOAuthPluginConstants.OAuthConfiguration constant : MoOAuthPluginConstants.OAuthConfiguration.values()) {
            resetConfig(constant.getKey());
        }
    }
    public void resetAttributeMapping() {
        LOGGER.info("resetAttributeMapping called ");
        for(MoOAuthPluginConstants.AttributeMapping constant : MoOAuthPluginConstants.AttributeMapping.values()) {
            resetConfig(constant.getKey());
        }
    }
    public void resetGroupMapping() {
        LOGGER.info("resetGroupMapping called ");
        for(MoOAuthPluginConstants.GroupMapping constant : MoOAuthPluginConstants.GroupMapping.values()) {
            resetConfig(constant.getKey());
        }
    }
    public void resetSignOnSettings() {
        LOGGER.info("resetSignOnSettings called ");
        for(MoOAuthPluginConstants.SignInSettings constant : MoOAuthPluginConstants.SignInSettings.values()) {
            resetConfig(constant.getKey());
        }
    }
    public void resetConfig(String constantKey){
//        LOGGER.debug("resetConfig called for" + constantKey);
        settings.deleteKey(constantKey);
    }
}