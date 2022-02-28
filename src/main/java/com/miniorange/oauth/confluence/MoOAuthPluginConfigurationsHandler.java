package com.miniorange.oauth.confluence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.MoOAuthPluginException.PluginErrorCode;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;

public class MoOAuthPluginConfigurationsHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoOAuthPluginConfigurationsHandler.class);
    private MoOAuthSettings settings;
    private MoOAuthPluginHandler moOAuthPluginHandler;

    public MoOAuthPluginConfigurationsHandler(MoOAuthSettings settings, MoOAuthPluginHandler moOAuthPluginHandler) {
        this.settings = settings;
        this.moOAuthPluginHandler = moOAuthPluginHandler;
    }

    // Export Configuration
    public String generateConfigurationsJson() throws JSONException {
        LOGGER.debug("Generating configurations JSON string ");

        JSONObject config = new JSONObject();

        config.putOpt("PLUGIN_NAME", settings.getPluginName());
        config.putOpt("PLUGIN_VERSION", settings.getPluginVersion());

        insertOAuthConfig(config);
        insertAttributeMapping(config);
        insertGroupMapping(config);
        insertSignOnSettings(config);
        moOAuthPluginHandler.saveDefaultConfiguration();
        
        return config.toString(2);
        
    }
    private void insertOAuthConfig(JSONObject config) {
        LOGGER.debug("Inserting OAuth configuration.");
        JSONObject oauthConfig = new JSONObject();
        try {
            for(MoOAuthPluginConstants.OAuthConfiguration constant : MoOAuthPluginConstants.OAuthConfiguration.values()){
                insertConfig(oauthConfig, String.valueOf(constant), constant.getKey());
            }
            config.putOpt("OAuth Configuration", oauthConfig);
        } catch (JSONException e) {
            LOGGER.error("Error occurred while inserting OAuth configuration in JSON " + e.getMessage() + " Cause: " + e.getCause());
            LOGGER.error(e.getMessage());
        }
    }
    private void insertAttributeMapping(JSONObject config) {
        LOGGER.debug("Inserting User Profile configuration in JSON Object.");
        JSONObject attributeMapping = new JSONObject();
        try {
            for(MoOAuthPluginConstants.AttributeMapping constant : MoOAuthPluginConstants.AttributeMapping.values()) {
                insertConfig(attributeMapping, String.valueOf(constant), constant.getKey());
            }
            config.putOpt("Attribute Mapping", attributeMapping);
        } catch (JSONException e) {
            LOGGER.error("Error occurred while inserting Attribute Mapping configuration in JSON " + e.getMessage() + " Cause: " + e.getCause());
            LOGGER.error(e.getMessage());
        }
    }
    private void insertGroupMapping(JSONObject config) {
        LOGGER.debug("Inserting User Groups configuration in JSON Object.");
        JSONObject groupMapping = new JSONObject();
        try {
            for(MoOAuthPluginConstants.GroupMapping constant : MoOAuthPluginConstants.GroupMapping.values()) {
                insertConfig(groupMapping, String.valueOf(constant), constant.getKey());
            }
            config.putOpt("Group Mapping", groupMapping);
        } catch (JSONException e) {
            LOGGER.error("Error occurred while inserting Group Mapping configuration in JSON " + e.getMessage() + " Cause: " + e.getCause());
            LOGGER.error(e.getMessage());
        }
    }
    private void insertSignOnSettings(JSONObject config) {
        LOGGER.debug("Inserting Sign in settings configuration.");
        JSONObject signInSettings = new JSONObject();
        try {
            for(MoOAuthPluginConstants.SignInSettings constant : MoOAuthPluginConstants.SignInSettings.values()) {
                insertConfig(signInSettings, String.valueOf(constant), constant.getKey());
            }
            config.putOpt("Sign In Settings", signInSettings);
        } catch (JSONException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("Error occurred while inserting Sign in settings configuration in JSON " + e.getMessage() + " Cause: " + e.getCause());
        }
    }
    public void insertConfig(JSONObject jsonObject, String constantName, String constantKey){
        if(settings.getPluginSettings().get(constantKey ) instanceof Boolean){
            //boolean
            jsonObject.putOpt(constantName, BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean((String) settings.getPluginSettings().get(constantKey)), false));
        }
        else if(settings.getPluginSettings().get(constantKey) instanceof List){
            //List
            List<String> list = null;
            if(constantName == "DEFAULT_GROUPS"){
                //DEFAULT_GROUPS
                list = (List<String>) settings.getDefaultGroups();
            }else{
                list = (List<String>) settings.getPluginSettings().get(constantKey);
            }
            for (String listItem : list) {
                jsonObject.append(constantName, listItem);
            }
        }
        else if(settings.getPluginSettings().get(constantKey) instanceof HashMap){
            //HashMap
            JSONObject jsonObjectForHasMapRoles = new JSONObject();
            if (settings.getRoleMapping() != null) {
                for (String key : settings.getRoleMapping().keySet()) {
                	jsonObjectForHasMapRoles.putOpt(key, settings.getRoleMapping().get(key));
                }
                jsonObject.putOpt(constantName, jsonObjectForHasMapRoles);
            }
    
            JSONObject jsonObjectForHasMapCustomAttr = new JSONObject();
            if (StringUtils.equalsIgnoreCase(constantName,"CUSTOM_ATTRIBUTE_MAPPING")  && settings.getCustomAttributeMapping() != null) {
                for (String key : settings.getCustomAttributeMapping().keySet()) {
                	jsonObjectForHasMapCustomAttr.putOpt(key, settings.getCustomAttributeMapping().get(key));
                }
                jsonObject.putOpt(constantName, jsonObjectForHasMapCustomAttr);
            }

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
            jsonObject.putOpt(constantName, StringUtils.defaultIfBlank((String) settings.getPluginSettings().get(constantKey), StringUtils.EMPTY));
        }
    }


    // Import Configuration
    public void importConfigurations(File configFile) throws IOException {
        LOGGER.debug("Importing app's configuration from config file");

        try {
            String pluginConfigurations = IOUtils.toString(new FileInputStream(configFile));
            importPluginConfigurations(pluginConfigurations);
        } catch (JSONException e) {
            LOGGER.error(e.getMessage());
            //throw new MoOAuthPluginException(MoOAuthPluginException.PluginErrorCode.SAVING_DETAILS,"Invalid file uploaded--from importConfigurations");
        }
    }
    public void importPluginConfigurations(String pluginConfigurationsJson) throws JSONException{
        LOGGER.debug("Importing app Configurations in JSON Object ");

        try {
            JSONObject pluginConfigObj = new JSONObject(pluginConfigurationsJson);

            JSONObject oauthConfig = pluginConfigObj.optJSONObject("OAuth Configuration");
            if ((oauthConfig != null) && (oauthConfig.length() != 0)) {
                importOAuthConfig(oauthConfig);
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
        } catch (JSONException e) {
            LOGGER.error("An error occurred while importing app Configurations " + e.getMessage() + " with Cause: "+ e.getCause());
        }
    }
    private void importOAuthConfig(JSONObject oAuthConfigObj) throws JSONException {
        LOGGER.debug("importOAuthConfig called ");
        for(MoOAuthPluginConstants.OAuthConfiguration constant : MoOAuthPluginConstants.OAuthConfiguration.values()){
            saveConfig(oAuthConfigObj, String.valueOf(constant), constant.getKey());
        }
    }
    private void importAttributeMapping(JSONObject attributeMappingObj) throws JSONException {
        LOGGER.debug("importAttributeMapping called ");
        for(MoOAuthPluginConstants.AttributeMapping constant : MoOAuthPluginConstants.AttributeMapping.values()){
            saveConfig(attributeMappingObj, String.valueOf(constant), constant.getKey());
        }
    }
    private void importGroupMapping(JSONObject groupMappingObj) throws JSONException {
        LOGGER.debug("importGroupMapping called ");
        for(MoOAuthPluginConstants.GroupMapping constant : MoOAuthPluginConstants.GroupMapping.values()) {
            saveConfig(groupMappingObj, String.valueOf(constant), constant.getKey());
        }
    }
    private void importSignInSettings(JSONObject signInSettingsObj) throws JSONException {
        LOGGER.debug("importSignInSettings called ");
        for(MoOAuthPluginConstants.SignInSettings constant : MoOAuthPluginConstants.SignInSettings.values()){
            saveConfig(signInSettingsObj, String.valueOf(constant), constant.getKey());
        }
    }
    private void saveConfig(JSONObject jsonObject, String constantName, String constantKey){
        LOGGER.debug("saveConfig called");
        if(settings.getPluginSettings().get(constantKey) instanceof Boolean){
            //boolean
            settings.getPluginSettings().put(constantKey, BooleanUtils.toString(jsonObject.optBoolean(constantName), "true", "false", "false"));
        }
        else if (settings.getPluginSettings().get(constantKey) instanceof List
                || constantName.equalsIgnoreCase("DEFAULT_GROUPS")
                || constantName.equalsIgnoreCase("BACKDOOR_GROUPS")
                || constantName.equalsIgnoreCase("ON_THE_FLY_DO_NOT_REMOVE_GROUPS")) {
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
        else if (settings.getPluginSettings().get(constantKey) instanceof HashMap || constantName.equalsIgnoreCase("ROLE_MAPPING")
                || constantName.equalsIgnoreCase("CUSTOM_ATTRIBUTE_MAPPING")
                || constantName.equalsIgnoreCase("OAUTH_REQUEST_PARAMETERS")) {
            LOGGER.debug("here for role mapping");
            //HashMap
                HashMap<String, String> hashMap = new HashMap<>();
                JSONObject jsonObjectForHashMap = jsonObject.optJSONObject(constantName);

                if (jsonObjectForHashMap != null){
                    Iterator<String> iterator = jsonObjectForHashMap.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        hashMap.put(key, jsonObjectForHashMap.getString(key));
                    }
                    settings.getPluginSettings().put(constantKey, hashMap);
                }
        }
        else {
            //string
            settings.getPluginSettings().put(constantKey, StringUtils.trimToEmpty(StringUtils.defaultIfBlank(jsonObject.optString(constantName), StringUtils.EMPTY)));
        }
    }


    //Reset Configuration
    public void clearPluginSettings() {
        LOGGER.debug("clearPluginSettings called");
        resetOAuthConfig();
        resetAttributeMapping();
        resetGroupMapping();
        resetSignOnSettings();
    }
    public void resetOAuthConfig() {
        LOGGER.debug("resetOAuthConfig called ");
        for(MoOAuthPluginConstants.OAuthConfiguration constant : MoOAuthPluginConstants.OAuthConfiguration.values()) {
            resetConfig(constant.getKey());
        }
    }
    public void resetAttributeMapping() {
        LOGGER.debug("resetAttributeMapping called ");
        for(MoOAuthPluginConstants.AttributeMapping constant : MoOAuthPluginConstants.AttributeMapping.values()) {
            resetConfig(constant.getKey());
        }
    }
    public void resetGroupMapping() {
        LOGGER.debug("resetGroupMapping called ");
        for(MoOAuthPluginConstants.GroupMapping constant : MoOAuthPluginConstants.GroupMapping.values()) {
            resetConfig(constant.getKey());
        }
    }
    public void resetSignOnSettings() {
        LOGGER.debug("resetSignOnSettings called ");

        for(MoOAuthPluginConstants.SignInSettings constant : MoOAuthPluginConstants.SignInSettings.values()) {
            resetConfig(constant.getKey());
        }
    }
    public void resetConfig(String constantKey){
        LOGGER.debug("resetConfig called");
        settings.deleteKey(constantKey);
    }
}