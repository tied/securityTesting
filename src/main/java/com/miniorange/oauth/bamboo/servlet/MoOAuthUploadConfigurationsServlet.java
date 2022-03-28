package com.miniorange.oauth.bamboo.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.atlassian.json.jsonorg.JSONException;
import com.miniorange.oauth.MoOAuthPluginException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.miniorange.oauth.bamboo.MoOAuthPluginConfigurationsHandler;
import com.miniorange.oauth.bamboo.MoOAuthPluginHandler;
import com.miniorange.oauth.bamboo.MoOAuthSettings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
import org.apache.struts2.dispatcher.multipart.UploadedFile;



public class MoOAuthUploadConfigurationsServlet extends HttpServlet{
    private static final Log LOGGER = LogFactory.getLog(MoOAuthUploadConfigurationsServlet.class);
    private MoOAuthSettings settings;
    private MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler;
    private String xsrfToken;
    private UserManager userManager;

    public MoOAuthUploadConfigurationsServlet(MoOAuthSettings settings, 
                                            MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler) {
        this.settings = settings;
        this.pluginConfigurationsHandler = pluginConfigurationsHandler;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String referer = request.getHeader("referer");
        if(settings.getPluginApiAccessRestriction()
                && !StringUtils.startsWith(referer, this.settings.getBaseUrl())){
            LOGGER.error(
                    "Access Denied. API Restriction is enabled and request is not originated from the Bamboo.");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You are not allowed to access this page.");
            return;
        }
        xsrfToken = XsrfTokenUtils.getXsrfToken(request);
        LOGGER.info("MoOAuthUploadConfigurationServlet: doPost  called");
        MoOAuthSettings.isPluginConfigurationFileUploaded = Boolean.TRUE;
        try {
            handleUploadConfigurations(request);
            response.sendRedirect(settings.getImportExportPageUrl());
        }catch (Exception e) {
            e.printStackTrace();
            settings.setConfigurationStatus("An error occurred while saving your details.Please check logs for more info.");
            response.sendRedirect(settings.getImportExportPageUrl());
        }
    }

    private void handleUploadConfigurations(HttpServletRequest request) throws IOException {
        LOGGER.info("handleUploadConfigurations called");

        String fileContents = StringUtils.EMPTY;
        MultiPartRequestWrapper wrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();
        UploadedFile[] files = wrapper.getFiles("configFile");

        if (files != null) {
            for (int i = 0; i < (files.length); ++i) {
                UploadedFile file = files[i];
                if (file.isFile()) {
                    try {
                        fileContents = FileUtils.readFileToString((File) file.getContent());
                        if(StringUtils.isEmpty(fileContents)){
                            settings.setConfigurationStatus("Empty file uploaded");
                            LOGGER.error("Empty file uploaded");
                            return;
                        } else {
                            pluginConfigurationsHandler.importConfigurations((File) file.getContent());
                            settings.setConfigurationStatus("success");
                            return ;
                        }
                    } catch(JSONException e) {
                        e.printStackTrace();
                        LOGGER.debug("JSONException message = "+e.getMessage());
                        settings.setConfigurationStatus("Invalid file is  provided. Could not parse app configuration file.");
                    } catch(MoOAuthPluginException e) {
                        e.printStackTrace();
                        LOGGER.debug("Exception message = "+e.getMessage());
                        settings.setConfigurationStatus(e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOGGER.debug("exception message = "+e.getMessage());
                        LOGGER.error("Invalid file is provided. Could not parse app configuration file.");
                        settings.setConfigurationStatus("Invalid file is  provided. Could not parse app configuration file.");
                        return ;
                    }
                }
            }
        }
        return ;
    }

    public MoOAuthSettings getSettings() {
        return settings;
    }

    public void setSettings(MoOAuthSettings settings) {
        this.settings = settings;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    public MoOAuthPluginConfigurationsHandler getPluginConfigurationsHandler() {
        return pluginConfigurationsHandler;
    }

    public void setPluginConfigurationsHandler(MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler) {
        this.pluginConfigurationsHandler = pluginConfigurationsHandler;
    }

}
