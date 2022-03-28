package com.miniorange.sso.saml.bamboo.servlet;

import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.bamboo.schedulers.MoMetadataJobRunnerImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

public class ReplaceOldSettingWithNewServlet extends HttpServlet  {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceOldSettingWithNewServlet.class);
    private MoSAMLSettings settings;
    private MoMetadataJobRunnerImpl metadataJobRunnerImpl;

    public ReplaceOldSettingWithNewServlet(MoSAMLSettings settings, MoMetadataJobRunnerImpl metadataJobRunnerImpl) {
        this.settings = settings;
        this.metadataJobRunnerImpl = metadataJobRunnerImpl;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.debug("ReplaceOldSettingWithNewServlet doPost");
        try {
            String idpID = req.getParameter("idpID");
            String idpName = req.getParameter("idpName");

            if (StringUtils.isNotBlank(settings.getSsoServiceUrl())) {
                MoPluginHandler.saveIdPConfiguration(idpID, idpName, settings.getIdpEntityId(), settings.getSsoBindingType(),
                        settings.getSsoServiceUrl(), settings.getSloBindingType(), settings.getSloServiceUrl(),
                        (List<String>) settings.getAllX509Certificates(), settings.getX509Certificate(), settings.getSignedRequest(),
                        settings.getNameIdFormat(), settings.getEnableSsoForIdp());
                MoPluginHandler.saveAttributeMapping(idpID, settings.getUsernameAttribute(), settings.getEmailAttribute(),
                        settings.getFullNameAttribute(), settings.getUseSeparateNameAttributes(),
                        settings.getFirstNameAttribute(), settings.getLastNameAttribute(),
                        settings.getKeepExistingUserAttributes(), settings.getRegexPattern(),
                        settings.getRegexPatternEnabled(), settings.getLoginUserAttribute());
                MoPluginHandler.saveRoleMapping(idpID, settings.getRoleAttribute(), settings.getCreateUsersIfRoleMapped(),
                        settings.getKeepExistingUserAttributes(), settings.getRoleMapping(), settings.getDefaultGroup(),
                        (List<String>) settings.getDefaultGroups(), settings.getRestrictUserCreation(),
                        settings.getEnableDefaultGroupsFor(), settings.getOnTheFlyGroupMapping(), (List<String>) settings.getOnTheFlyDoNotRemoveGroups(),
                        settings.getOnTheFlyAssignNewGroupsOnly(), settings.getCreateNewGroups(),settings.getGroupRegexPatternEnabled(),settings.getRegexPatternForGroup(), settings.getRegexPattern(),settings.getTestRegex());
                MoPluginHandler.saveImportMetadata(idpID, settings.getInputMetadataUrl(), settings.getIdpMetadataURL(),
                        settings.getRefreshMetadata(), settings.getRefreshInterval(), settings.getCustomRefreshInterval(),
                        settings.getCustomRefreshIntervalUnit());
                settings.setMetadataOption(idpID, settings.getMetadataOption(MoPluginConstants.DEFAULT_IDP_ID));
                MoPluginHandler.saveListIDPConfigurations(idpID, Boolean.TRUE);
                metadataJobRunnerImpl.deleteSchedule(MoPluginConstants.DEFAULT_IDP_ID);
                MoPluginHandler.toggleSchedulerService(idpID);

                settings.clearOldConfiguration();
            }

            String attrsJSON = "success";
            resp.setContentType(MediaType.TEXT_PLAIN);
            resp.getOutputStream().write(attrsJSON.getBytes());
            resp.getOutputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
            String attrsJSON = "error";
            resp.setContentType(MediaType.TEXT_PLAIN);
            resp.getOutputStream().write(attrsJSON.getBytes());
            resp.getOutputStream().close();
        }

    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public MoMetadataJobRunnerImpl getMetadataJobRunnerImpl() {
        return metadataJobRunnerImpl;
    }

    public void setMetadataJobRunnerImpl(MoMetadataJobRunnerImpl metadataJobRunnerImpl) {
        this.metadataJobRunnerImpl = metadataJobRunnerImpl;
    }
}
