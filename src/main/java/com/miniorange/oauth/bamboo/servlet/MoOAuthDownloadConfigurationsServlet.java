package com.miniorange.oauth.bamboo.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.miniorange.oauth.bamboo.MoOAuthSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.miniorange.oauth.bamboo.MoOAuthPluginConfigurationsHandler;

public class MoOAuthDownloadConfigurationsServlet extends HttpServlet{

    private static Log LOGGER = LogFactory.getLog(MoOAuthDownloadConfigurationsServlet.class);
    private MoOAuthPluginConfigurationsHandler MoOAuthPluginConfigurationsHandler;
    private MoOAuthSettings settings;

    public MoOAuthDownloadConfigurationsServlet(MoOAuthPluginConfigurationsHandler MoOAuthPluginConfigurationsHandler, MoOAuthSettings settings) {
        this.MoOAuthPluginConfigurationsHandler = MoOAuthPluginConfigurationsHandler;
        this.settings = settings;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.info("Download app configuration: doGet() called");
        String referer = request.getHeader("referer");
        if(settings.getPluginApiAccessRestriction()
                && !StringUtils.startsWith(referer, this.settings.getBaseUrl())){
            LOGGER.error(
                    "Access Denied. API Restriction is enabled and request is not originated from the Bamboo.");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You are not allowed to access this page.");
            return;
        }
        try {
            String jsonConfigurations = MoOAuthPluginConfigurationsHandler.generateConfigurationsJson();
            LOGGER.info("Downloading json Configurations file.");
            response.setHeader("Content-Disposition", "attachment; filename=\"app_configurations.json\"");
            response.setHeader("Cache-Control", "max-age=0");
            response.setHeader("Pragma", "");
            response.setContentType(MediaType.APPLICATION_JSON);
            response.getOutputStream().write(jsonConfigurations.getBytes());
        } catch (Exception e) {
            LOGGER.debug(e);
            e.printStackTrace();
            PrintWriter writer = response.getWriter();
            writer.print("An error occurred while generating the configuration file.");
            writer.close();
        }

    }

    public MoOAuthPluginConfigurationsHandler getMoOAuthPluginConfigurationsHandler() {
        return MoOAuthPluginConfigurationsHandler;
    }

    public void setMoOAuthPluginConfigurationsHandler(MoOAuthPluginConfigurationsHandler MoOAuthPluginConfigurationsHandler) {
        this.MoOAuthPluginConfigurationsHandler = MoOAuthPluginConfigurationsHandler;
    }

}
