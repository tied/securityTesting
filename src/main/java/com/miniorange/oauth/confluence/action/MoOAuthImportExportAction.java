package com.miniorange.oauth.confluence.action;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.xwork.SimpleXsrfTokenGenerator;
import com.atlassian.xwork.XsrfTokenGenerator;
import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.confluence.MoOAuthPluginConfigurationsHandler;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MoOAuthImportExportAction extends ConfluenceActionSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoOAuthImportExportAction.class);

    private MoOAuthSettings settings;
    private MoOAuthPluginHandler pluginHandler;
    private MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler;
    private Boolean applicationConfigured;

    public ArrayList errorMessage;
    public String message = StringUtils.EMPTY;

    private Boolean importFileSubmitted;

    private String xsrfToken;

    public MoOAuthImportExportAction(MoOAuthSettings settings,MoOAuthPluginHandler pluginHandler, MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler)
    {
        this.settings = settings;
        this.pluginHandler=pluginHandler;
        this.pluginConfigurationsHandler=pluginConfigurationsHandler;
    }

    public void validate() {
        LOGGER.debug("Configure Action doValidate");


        if (!BooleanUtils.toBoolean(importFileSubmitted)) {
            return;
        }

        super.validate();
    }

    @Override
    public String execute() throws Exception {
        LOGGER.info("MoImportExportAction: doExecute called.");
        try {
            if (!settings.isLicenseValid()) {
                LOGGER.error("No valid license found");
                return "invalid";
            }

            HttpServletRequest request = ServletActionContext.getRequest();
            XsrfTokenGenerator tokenGenerator = new SimpleXsrfTokenGenerator();
            xsrfToken = tokenGenerator.generateToken(request);

            if(StringUtils.isNotBlank(settings.getClientID())){
                LOGGER.debug("Application has been configured.");
                this.applicationConfigured = true;
            } else{
                this.applicationConfigured = false;
            }

            if (this.importFileSubmitted == null) {
                try {
                    MultiPartRequestWrapper wrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();
                    if (wrapper != null) {
                        this.importFileSubmitted = BooleanUtils.toBoolean(wrapper.getParameter("importFileSubmitted"));
                    }
                } catch (Exception e) { }
            }

            if (BooleanUtils.toBoolean(this.importFileSubmitted)) {
                handleUploadConfigurations();
            }
            LOGGER.debug("applicationConfigured : "+applicationConfigured);
            return "success";
        } catch (MoOAuthPluginException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getMessage());
            addActionError(e.getMessage());
            return "input";
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getMessage());
            addActionError("An error occurred while saving plugin configurations");
            return "input";
        }

    }

    private void handleUploadConfigurations() throws IOException{
        LOGGER.debug("handleUploadConfigurations called");

        settings.getBaseUrl().concat("/plugins/servlet/oauth/downloadconfigurations");

        LOGGER.info("Validating uploaded File.");
        MultiPartRequestWrapper wrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();
        LOGGER.debug("Metadata File = " + wrapper.getFiles("configFile"));
        if (wrapper.getFiles("configFile") == null || wrapper.getFiles("configFile").length==0) {
            LOGGER.error("uploaded file is empty or file not selected");
            addActionError("Selected file is empty.");
        }else {
            File metadataFile = wrapper.getFiles("configFile")[0];
            pluginConfigurationsHandler.importConfigurations(metadataFile);
            addActionMessage(getText("oauth.success.config"), "success", true, null);
        }
    }

    public MoOAuthSettings getSettings() {
        return settings;
    }

    public void setSettings(MoOAuthSettings settings) {
        this.settings = settings;
    }

    public MoOAuthPluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public void setErrorMessage(ArrayList errorMessage) {
        this.errorMessage = errorMessage;
    }
    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

}
