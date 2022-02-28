package com.miniorange.oauth.confluence.servlet;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.exception.AccountNotFoundException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.spring.container.ContainerManager;
import com.miniorange.oauth.confluence.MoOAuthPluginConfigurationsHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Base64;
import java.util.Enumeration;

public class MoConfigurePlugin extends HttpServlet {
    private static Log LOGGER = LogFactory.getLog(MoConfigurePlugin.class);
    private MoOAuthSettings settings;
    private CrowdService crowdService;
    private UserAccessor userAccessor;
    private PermissionManager permissionManager;
    private MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler;

    public MoConfigurePlugin(MoOAuthSettings settings, MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler, CrowdService crowdService, UserAccessor userAccessor, PermissionManager permissionManager) {
        this.settings = settings;
        this.pluginConfigurationsHandler = pluginConfigurationsHandler;
        this.crowdService = crowdService;
        this.userAccessor = userAccessor;
        this.permissionManager = permissionManager;

    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Configure Plugin servlet called from doGet");
        Enumeration headerNames = request.getHeaderNames();
        String key = "";
        String value = "";
        String username = "";
        String pwd = "";
        Boolean isAdmin = Boolean.FALSE;
        while(headerNames.hasMoreElements()) {
            try {
                key = (String)headerNames.nextElement();
                if (StringUtils.equalsIgnoreCase("Authorization", key)) {
                    value = request.getHeader(key);
                    value = value.split(" ")[1];
                    byte[] valueDecoded = Base64.getDecoder().decode(value);
                    value = new String(valueDecoded);
                    String[] arr = value.split(":");
                    username = arr[0];
                    pwd = arr[1];
                }
            } catch (ArrayIndexOutOfBoundsException var17) {
                LOGGER.error("Something went wrong while fetching username and password.", var17);
            }
        }

        if (StringUtils.isNotBlank(username)) {
            try {
                UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
                this.crowdService.authenticate(username, pwd);
                ConfluenceUser confluenceUser = userAccessor.getUserByName(username);
                isAdmin = this.permissionManager.isSystemAdministrator(confluenceUser);
            } catch (AccountNotFoundException e) {
                LOGGER.error("The user does not exist", e);
                this.showErrorMessage(response, "Error Occurred", "Something went wrong. Please try again.");
            } catch (InactiveAccountException e) {
                LOGGER.error("The user does not exist", e);
                this.showErrorMessage(response, "Error Occurred", "Something went wrong. Please try again.");
            } catch (ExpiredCredentialException e) {
                LOGGER.error("The user's credentials have expired", e);
                this.showErrorMessage(response, "Error Occurred", "Something went wrong. Please try again.");
            } catch (FailedAuthenticationException e) {
                LOGGER.error("The user could not be authenticated.", e);
                this.showErrorMessage(response, "Error Occurred", "Something went wrong. Please try again.");
            } catch (OperationFailedException e) {
                LOGGER.error("The user could not be authenticated.", e);
                this.showErrorMessage(response, "Basic Authentication Failed", " Something went wrong. Please try again.");
            }
        }

        if (BooleanUtils.toBoolean(isAdmin)) {
            try {
                if (this.settings.isLicenseValid()) {
                    String jsonConfigurations = this.pluginConfigurationsHandler.generateConfigurationsJson();
                    response.setHeader("Cache-Control", "max-age=0");
                    response.setHeader("Pragma", "");
                    response.setContentType("application/json");
                    response.getOutputStream().write(jsonConfigurations.getBytes());
                } else {
                    this.showErrorMessage(response, "Your License has expired", "Please renew the license to use the API Call.");
                }
            } catch (Exception var11) {
                LOGGER.error("An error occurred while fetching the json.", var11);
            }
        } else {
            this.showErrorMessage(response, "Error Occurred", "Something went wrong. Please try again.");
        }

    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Configure Plugin servlet called from doPost");
        Enumeration headerNames = request.getHeaderNames();
        String contentType = request.getContentType();
        String key = "";
        String value = "";
        String username = "";
        String pwd = "";
        Boolean isAdmin = Boolean.FALSE;

        while(headerNames.hasMoreElements()) {
            try {
                key = (String)headerNames.nextElement();
                if (StringUtils.equalsIgnoreCase("Authorization", key)) {
                    value = request.getHeader(key);
                    value = value.split(" ")[1];
                    byte[] valueDecoded = Base64.getDecoder().decode(value);
                    value = new String(valueDecoded);
                    String[] arr = value.split(":");
                    username = arr[0];
                    pwd = arr[1];
                }
            } catch (ArrayIndexOutOfBoundsException var33) {
                LOGGER.error("Something went wrong while fetching username and password.", var33);
            }
        }

        if (StringUtils.isNotBlank(username)) {
            try {
                UserAccessor userAccessor = (UserAccessor)ContainerManager.getComponent("userAccessor");
                this.crowdService.authenticate(username, pwd);
                ConfluenceUser confluenceUser = userAccessor.getUserByName(username);
                isAdmin = this.permissionManager.isSystemAdministrator(confluenceUser);
            } catch (AccountNotFoundException var28) {
                LOGGER.error("The user does not exist", var28);
                this.showErrorMessage(response, "Account Not Found.", "User with the given name could not be found in Confluence.");
            } catch (InactiveAccountException var29) {
                LOGGER.error("The user does not exist", var29);
                this.showErrorMessage(response, "Inactive Account.", "The user account is inactive.");
            } catch (ExpiredCredentialException var30) {
                LOGGER.error("The user's credentials have expired", var30);
                this.showErrorMessage(response, "Expired Credential.", "The user's credentials have expired. The user must change their credentials in order to successfully authenticate.");
            } catch (FailedAuthenticationException var31) {
                LOGGER.error("The user could not be authenticated.", var31);
                this.showErrorMessage(response, "Basic Authentication Failed", "Authentication with the provided credentials failed. It may indicate that the user does not exist or the user's account is inactive or the credentials are incorrect");
            } catch (OperationFailedException var32) {
                LOGGER.error("The user could not be authenticated.", var32);
                this.showErrorMessage(response, "Basic Authentication Failed", "An unknown exception occured while authenticating the user.");
            }
        }

        if (BooleanUtils.toBoolean(isAdmin)) {
            if (contentType.equals("application/json")) {
                StringBuffer sb = new StringBuffer();
                BufferedReader bufferedReader = null;

                try {
                    bufferedReader = request.getReader();
                    char[] charBuffer = new char[128];

                    int bytesRead;
                    while((bytesRead = bufferedReader.read(charBuffer)) != -1) {
                        sb.append(charBuffer, 0, bytesRead);
                    }
                } catch (Exception var34) {
                    LOGGER.error("An error occurred while reading  the configuration", var34);
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException var27) {
                            throw var27;
                        }
                    }

                }

                if (this.settings.isLicenseValid()) {
                    this.settings.clearPluginSettings();
                    this.pluginConfigurationsHandler.importPluginConfigurations(sb.toString());
                    this.showSuccessMessage(response, "Oauth settings updated", "Oauth App Configured successfully.");
                } else {
                    this.showErrorMessage(response, "Your License has expired", "Please renew the license to use the API Call.");
                }
            } else {
                this.showErrorMessage(response, "Operation Failed", "Please check the Content-Type make sure it's applicable/json");
            }
        } else {
            this.showErrorMessage(response, "Error Occurred", "Something went wrong. Please try again.");
        }

    }
    private void showErrorMessage(HttpServletResponse response, String message, String description) {
        JSONObject json = new JSONObject();

        try {
            json.put("Status", "ERROR");
            json.put("Message", message);
            json.put("Description", description);
            response.setStatus(401);
            response.setContentType("application/json");
            response.getOutputStream().write(json.toString().getBytes());
            response.getOutputStream().close();
        } catch (Exception var6) {
            LOGGER.error("An error occurred while showing error message response", var6);
        }

    }

    private void showSuccessMessage(HttpServletResponse response, String message, String description) {
        JSONObject json = new JSONObject();

        try {
            json.put("Status", "SUCCESS");
            json.put("Message", message);
            json.put("Description", description);
            response.setContentType("application/json");
            response.getOutputStream().write(json.toString().getBytes());
            response.getOutputStream().close();
        } catch (Exception var6) {
            LOGGER.error("An error occurred while showing success message", var6);
        }

    }
}
