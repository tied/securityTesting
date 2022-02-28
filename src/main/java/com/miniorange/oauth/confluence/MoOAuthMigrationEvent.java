package com.miniorange.oauth.confluence;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;

public class MoOAuthMigrationEvent implements InitializingBean, DisposableBean {
    private static Log LOGGER = LogFactory.getLog(MoOAuthMigrationEvent.class);
    private MoOAuthSettings settings;

    public MoOAuthMigrationEvent(MoOAuthSettings settings) {
        this.settings = settings;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.debug("MoOAuthMigrationEvent afterPropertiesSet Called");

        if(!settings.getRequestParamsMigrated() && StringUtils.isNotEmpty(settings.getClientID())) {
            //only for the existing users (who are migrating) //Not for new users
            LOGGER.debug("Migrating Hardcoded request parameters settings");
            migrateRequestParams();
            settings.setRequestParamsMigrated(true);
        }
    }

    //migrating hardcoded request parameter to custom parameters (access_type=offline and include_granted_scope=true)
    private void migrateRequestParams(){
        HashMap<String, String> oauth_request_parameters = settings.getOauth_request_parameters();
        if (!BooleanUtils.toBoolean(oauth_request_parameters.containsKey("access_type"))) {
            oauth_request_parameters.put("access_type", "offline");
        }
        if (!BooleanUtils.toBoolean(oauth_request_parameters.containsKey("include_granted_scopes"))) {
            oauth_request_parameters.put("include_granted_scopes", "true");
        }
        settings.setOauth_request_parameters(oauth_request_parameters);
    }

    @Override
    public void destroy() throws Exception {
        LOGGER.debug("destroy method called");
    }
}
