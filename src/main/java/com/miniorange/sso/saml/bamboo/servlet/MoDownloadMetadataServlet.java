package com.miniorange.sso.saml.bamboo.servlet;

import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoHttpUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

public class MoDownloadMetadataServlet extends HttpServlet {
    private static Log LOGGER = LogFactory.getLog(MoDownloadMetadataServlet.class);
    private MoSAMLSettings settings;

    public MoDownloadMetadataServlet(MoSAMLSettings settings) {
        this.settings = settings;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.info("Downloading metadata");
        String metadata_url = settings.getBaseUrl() + "/plugins/servlet/saml/metadata";
        String metadata = MoHttpUtils.sendGetRequest(metadata_url);

        try {
            response.setHeader("Content-Disposition", "attachment; filename=\"sp_metadata.xml\"");
            response.setHeader("Cache-Control", "max-age=0");
            response.setHeader("Pragma", "");
            response.setContentType(MediaType.APPLICATION_XML);
            response.getOutputStream().write(metadata.getBytes());
        } catch (Exception e) {
            LOGGER.error("An error occurred while downloading the metadata." + e.getMessage());
        }
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }
}