package com.miniorange.oauth.confluence.servlet;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MoOAuthFetchMetadataServlet extends HttpServlet {
    private final Log LOGGER = LogFactory.getLog(MoOAuthGetConfigurationServlet.class);
    private PermissionManager permissionManager;

    public MoOAuthFetchMetadataServlet(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.info("Inside FetchMetadataServlet");
        String metadataURL = request.getParameter("metadata_url");
        metadataURL=metadataURL.trim().replace(" ","%20");
        ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
        LOGGER.debug("Confluence user : "+confluenceUser );

        if (confluenceUser!=null && permissionManager.isSystemAdministrator(confluenceUser)){

            if(isValidURL(metadataURL)){
                String jsonData = MoOAuthHttpUtils.sendGetRequest(metadataURL);
                response.setContentType(MediaType.APPLICATION_JSON);
                response.getOutputStream().write(jsonData.getBytes(StandardCharsets.UTF_8));
                response.getOutputStream().close();
            } else{
                LOGGER.error("Metadata URL is not valid");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "URL not valid");
            }
        }else {
            LOGGER.error("An Authorised users");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "user is not authorized");
        }
    }

    public static boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}