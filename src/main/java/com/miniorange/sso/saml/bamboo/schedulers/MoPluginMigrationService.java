package com.miniorange.sso.saml.bamboo.schedulers;

import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MoPluginMigrationService implements LifecycleAware {
    private static Log LOGGER = LogFactory.getLog(MoPluginMigrationService.class);
    public MoSAMLSettings settings;
    public MoPluginHandler pluginHandler;

    public MoPluginMigrationService(MoSAMLSettings settings, MoPluginHandler pluginHandler) {
        this.settings = settings;
        this.pluginHandler = pluginHandler;
    }


    @Override
    public void onStart() {
        LOGGER.debug("MoPluginMigrationService afterPropertiesSet Called");
        if(!(settings.getisMigrated() || settings.getIdPList().isEmpty())) {
            migrateSettings();

        }
        settings.setisMigrated(true);
    }

    private void migrateSettings() {

        addIssuerMap();

        //add other tab settings
        if(settings.getUseDomainMapping())
            migrateDomainMappingSettings();

        migrateAutoRedirectToIDP();

        migrateAdanceSsoSettings();

        migrateMappingAndAdvanceSsoSettings();

        settings.setSsoEnabledForIdPList(settings.getIdPList());

        if (settings.getIdPList().isEmpty()) {
            settings.setPrivateSPCertificate(settings.getNewPrivateSPCertificate());
            settings.setPublicSPCertificate(settings.getNewPublicSPCertificate());
        }
    }

    private void addIssuerMap() {
        LOGGER.info("Adding Issuer Map");
       /* if(!settings.getIssuerMap().isEmpty())
            return;*/
        List<String> idpList = settings.getIdPList();
        HashMap<String,String> issuerMap = new HashMap<>();
        for(String idpId : idpList) {
            MoIDPConfig idpConfig = MoPluginHandler.constructIdpConfigObject(idpId);
            issuerMap.put(idpConfig.getId(), idpConfig.getIdpEntityId());
        }
        settings.setIssuerMap(issuerMap);
    }

    private void migrateAutoRedirectToIDP() {
        settings.setBackdoorEnabled(true);
        if(settings.getDefaultLoginDisabled() && settings.getIdPList().size()==1) {
            List<String> idpList = settings.getIdPList();
            settings.setDefaultBambooIDP(idpList.get(0));
            return;
        }
        settings.setDefaultBambooIDP("loginPage");


    }

    public void migrateAdanceSsoSettings(){
        if(settings.getAllowUserCreation()==true)
            settings.setAllowUserCreation(false);
        else
            settings.setAllowUserCreation(true);
        //String keyValue= (String)settings.getMoConstantsKey("miniornage.saml.PLUGIN_API_ACCESS_RESTRICTION");
        //settings.setPluginApiAccessRestriction(BooleanUtils.toBoolean(keyValue));
    }


    private void migrateDomainMappingSettings() {
        LOGGER.debug("Migrating Domain Mapping Settings");
        String ruleName="domainMapping_";
        String decisionFactor="domain";
        String conditionOperation="equals";
        Map<String,String> domainMapping = settings.getDomainMapping();
        int i=0;
        try {
            for (Map.Entry<String, String> domain : domainMapping.entrySet()) {
                i++;
                pluginHandler.saveRedirectionRules(ruleName + i, decisionFactor, conditionOperation, domain.getKey(), domain.getValue());
            }
        }
        catch (JSONException e) {
            LOGGER.error("An error occurred while migrating domain mapping",e);
        }
    }

    public void migrateMappingAndAdvanceSsoSettings(){

        ArrayList<String> idpList = settings.getIdPList();
        String relayStateUrl = settings.getRelayState();
        String relayStateRedirectionType = settings.getRelayStateRedirectionType();
        settings.setSsoEnabledForIdPList(idpList);

        for (String idpId : idpList) {

            //Getting IDP configuration Object
            MoIDPConfig idpConfig = MoPluginHandler.constructIdpConfigObject(idpId);

            //Getting refresh metadata settings
            JSONObject importMetadataConfigObj = settings.getImportMetadataConfig(idpId);
            if (importMetadataConfigObj != null) {
                idpConfig = insertImportMetadataConfig(idpConfig, importMetadataConfigObj);
            }

            Boolean allowUserCreation = Boolean.TRUE;
            if(BooleanUtils.toBoolean(idpConfig.getRestrictUserCreation()))
                allowUserCreation = false;
            Boolean enablePassiveSso = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getEnablePassiveSso(),false);

             Boolean forceAuthentication = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getForceAuthentication(), false);

            String timeDelay = StringUtils.defaultIfBlank(idpConfig.getTimeDelay(), settings.getTimeDelay());
            Boolean refreshMetadata = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getRefreshMetadata(), false);
            String inputUrl = StringUtils.defaultIfBlank(idpConfig.getInputUrl(), "");
            String refreshInterval = StringUtils.defaultIfBlank(idpConfig.getRefreshInterval(), "hourly");
            Integer customRefreshInterval = idpConfig.getCustomRefreshInterval() != null ? idpConfig.getCustomRefreshInterval() : 60;
            String customRefreshIntervalUnit = StringUtils.defaultIfBlank(idpConfig.getCustomRefreshIntervalUnit(), "minutes");

            MoPluginHandler.saveAdvancedOption(idpId, allowUserCreation,forceAuthentication, inputUrl, timeDelay, refreshMetadata,
                    refreshInterval, customRefreshInterval, customRefreshIntervalUnit, relayStateUrl, relayStateRedirectionType,enablePassiveSso);

            //Attribute mapping migration
            idpConfig.setKeepExistingUserAttributes(!idpConfig.getKeepExistingUserAttributes());

            MoPluginHandler.saveAttributeMapping(idpId, idpConfig.getUsernameAttribute(), idpConfig.getEmailAttribute(), idpConfig.getFullNameAttribute(),
                    idpConfig.getUseSeparateNameAttributes(), idpConfig.getFirstNameAttribute(), idpConfig.getLastNameAttribute(),
                    idpConfig.getKeepExistingUserAttributes(), idpConfig.getRegexPattern(), idpConfig.getRegexPatternEnabled(),
                    idpConfig.getLoginUserAttribute());

            settings.setKeepExistingUserAttributes(!settings.getKeepExistingUserAttributes());

            //Group mapping migration
            idpConfig.setKeepExistingUserRoles(!idpConfig.getKeepExistingUserRoles());

            MoPluginHandler.saveRoleMapping(idpId, idpConfig.getRoleAttribute(), idpConfig.getCreateUsersIfRoleMapped(),
                    idpConfig.getKeepExistingUserRoles(), idpConfig.getRoleMapping(), idpConfig.getDefaultGroup(), idpConfig.getDefaultGroupsList(),
                    idpConfig.getRestrictUserCreation(), idpConfig.getEnableDefaultGroupsFor(), idpConfig.getOnTheFlyGroupCreation(),
                    idpConfig.getOnTheFlyDoNotRemoveGroups(), idpConfig.getOnTheFlyAssignNewGroupsOnly(), idpConfig.getCreateNewGroups(),idpConfig.getGroupRegexPatternEnabled(),
                    idpConfig.getRegexPatternForGroup(),idpConfig.getRegexGroups(),idpConfig.getTestRegex());

            settings.setKeepExistingUserRoles(!settings.getKeepExistingUserRoles());

        }

        settings.setAllowUserCreation(!settings.getRestrictUserCreation());

        //String keyValue= (String)settings.getMoConstantsKey("miniornage.saml.PLUGIN_API_ACCESS_RESTRICTION");
        //settings.setPluginApiAccessRestriction(BooleanUtils.toBoolean(keyValue));
    }

    private static MoIDPConfig insertImportMetadataConfig(MoIDPConfig idpConfig, JSONObject importMetadataConfigObj) {
        if (importMetadataConfigObj != null) {
            idpConfig.setRefreshMetadata(importMetadataConfigObj.optBoolean(MoPluginConstants.REFRESH_METADATA, false));
            idpConfig.setRefreshInterval(importMetadataConfigObj.optString(MoPluginConstants.METADATA_REFRESH_INTERVAL,
                    "hourly"));
            idpConfig.setCustomRefreshInterval(importMetadataConfigObj.optInt(MoPluginConstants.CUSTOM_REFRESH_INTERVAL,
                    60));
            idpConfig.setCustomRefreshIntervalUnit(importMetadataConfigObj.optString(MoPluginConstants.CUSTOM_REFRESH_INTERVAL_UNIT,
                    "minutes"));
            idpConfig.setInputUrl(importMetadataConfigObj.optString(MoPluginConstants.INPUT_METADATA_URL, ""));
            idpConfig.setIdpMetadataUrl(importMetadataConfigObj.optString(MoPluginConstants.IDP_METADATA_URL, ""));
        }

        return idpConfig;
    }

}

