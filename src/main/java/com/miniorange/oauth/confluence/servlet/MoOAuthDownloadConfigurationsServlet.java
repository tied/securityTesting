package com.miniorange.oauth.confluence.servlet;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.miniorange.oauth.confluence.MoOAuthPluginConfigurationsHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Permission;

public class MoOAuthDownloadConfigurationsServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoOAuthDownloadConfigurationsServlet.class);
    private MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler;
    private PermissionManager permissionManager;

    public MoOAuthDownloadConfigurationsServlet(MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler, PermissionManager permissionManager) {
        this.pluginConfigurationsHandler = pluginConfigurationsHandler;
        this.permissionManager = permissionManager;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("Downloading app's Configuration.");
        try {
            String jsonConfigurations = StringUtils.EMPTY;
            ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
            if (permissionManager.isSystemAdministrator(confluenceUser)) {
                jsonConfigurations = pluginConfigurationsHandler.generateConfigurationsJson();
                response.setHeader("Content-Disposition", "attachment; filename=\"app_configurations.json\"");
            }else{
                JSONObject config = new JSONObject();
                config.putOpt("Status", "ERROR");
                config.putOpt("Message", "You are not autorized to access this content");
                response.setStatus(HttpStatus.SC_FORBIDDEN);
                jsonConfigurations = config.toString();
            }
            response.setHeader("Cache-Control", "max-age=0");
            response.setHeader("Pragma", "");
            response.setContentType(MediaType.APPLICATION_JSON);
            response.getOutputStream().write(jsonConfigurations.getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e) {
            LOGGER.error("An error occurred while downloading the json."+ e.getMessage());
        }
    }

    public MoOAuthPluginConfigurationsHandler getPluginConfigurationsHandler() {
        return this.pluginConfigurationsHandler;
    }

    public void setPluginConfigurationsHandler(MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler) {
        this.pluginConfigurationsHandler = pluginConfigurationsHandler;
    }

}