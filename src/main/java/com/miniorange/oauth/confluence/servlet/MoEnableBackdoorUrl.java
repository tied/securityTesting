package com.miniorange.oauth.confluence.servlet;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.login.LoginManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.AccountNotFoundException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.spring.container.ContainerManager;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Enumeration;
import java.nio.charset.StandardCharsets;

public class MoEnableBackdoorUrl extends HttpServlet {

    private static Log LOGGER = LogFactory.getLog(MoEnableBackdoorUrl.class);
    private MoOAuthSettings settings;
    private LoginManager loginManager;
    private PermissionManager permissionManager;
    private CrowdService crowdService;

    public MoEnableBackdoorUrl(MoOAuthSettings settings, LoginManager loginManager, PermissionManager permissionManager, CrowdService crowdService) {
        this.settings = settings;
        this.loginManager = loginManager;
        this.permissionManager = permissionManager;
        this.crowdService =crowdService;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Enable Backdoor servlet called from doGet");
        JSONObject responseJson = new JSONObject();
        responseJson.put("Status", "ERROR");
        responseJson.put("Message", "GET method is not allowed");
        responseJson.put("Description", "The method received in the request-line is known by the origin server but not supported by the target resource. Use POST instead of GET");
        response.setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
        response.setContentType(MediaType.APPLICATION_JSON);
        response.getOutputStream().write(responseJson.toString().getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().close();
        return;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Enable Backdoor servlet called from doPost");

        String key= StringUtils.EMPTY, value=StringUtils.EMPTY, username=StringUtils.EMPTY, password=StringUtils.EMPTY;

        Boolean isAdmin = Boolean.FALSE;
        Boolean isAuthenticated = Boolean.FALSE;

        JSONObject responseJson = new JSONObject();

        Enumeration requestHeader = request.getHeaderNames();

        LOGGER.debug("Request Header : "+requestHeader.toString());

        while (requestHeader.hasMoreElements()) {
            LOGGER.error("Extracting request header informations");
            key = (String) requestHeader.nextElement();
            if (StringUtils.equalsIgnoreCase("Authorization", key)) {
                LOGGER.debug("Extracting Authorization keys");
                value = request.getHeader(key);
                value = value.split(" ")[1];
                byte[] valueDecoded = Base64.getUrlDecoder().decode(value);
                value = new String(valueDecoded);
                String[] arr = value.split(":");
                if(arr.length>=1)
                    username = arr[0];
                if(arr.length>=2)
                    password = arr[1];

                break;
            }
        }

        if(StringUtils.isEmpty(username)&& StringUtils.isEmpty(password)){
            LOGGER.error("username and password not found in the request");
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            responseJson.put("Status", "ERROR");
            responseJson.put("Message", "username and password not found");
            responseJson.put("Description", "API is protected with HTTP basic authentication. Pass the authentication information to the server in an Authorization header.");
        }else if(StringUtils.isEmpty(username)){
            LOGGER.error("username not found in the request");
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            responseJson.put("Status", "ERROR");
            responseJson.put("Message", "username not found");
            responseJson.put("Description", "API is protected with HTTP basic authentication. Pass the authentication information to the server in an Authorization header.");
        } else if (StringUtils.isEmpty(password)){
            LOGGER.error("password not found in the request");
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            responseJson.put("Status", "ERROR");
            responseJson.put("Message", "password not found");
            responseJson.put("Description", "API is protected with HTTP basic authentication. Pass the authentication information to the server in an Authorization header.");
        }else{
            try{
                LOGGER.debug("Authenticating user : "+username);
                UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
                User user = crowdService.authenticate(username, password);
                LOGGER.debug("The user is authenticated successfully");
                isAuthenticated = Boolean.TRUE;

            }catch (AccountNotFoundException e){
                LOGGER.error("The user does not exist");
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                responseJson.put("Status", "ERROR");
                responseJson.put("Message", "Account Not Found.");
                responseJson.put("Description", "User with the given name could not be found in Confluence");
                LOGGER.error(e.getMessage());
            }catch (InactiveAccountException e){
                LOGGER.error("The user does not exist");
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                responseJson.put("Status", "ERROR");
                responseJson.put("Message", "Inactive Account.");
                responseJson.put("Description", "The user account is inactive");
                LOGGER.error(e.getMessage());
            }catch (ExpiredCredentialException e){
                LOGGER.error("The user's credentials have expired");
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                responseJson.put("Status", "ERROR");
                responseJson.put("Message", "Expired Credential.");
                responseJson.put("Description", "The user's credentials have expired. The user must change their credentials in order to successfully authenticate.");
                LOGGER.error(e.getMessage());
            }catch (FailedAuthenticationException e){
                LOGGER.error("The user could not be authenticated.");
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                responseJson.put("Status", "ERROR");
                responseJson.put("Message", "Basic Authentication Failed");
                responseJson.put("Description", "Authentication with the provided credentials failed. It may indicate that the user does not exist or the user's account is inactive or the credentials are incorrect");
                LOGGER.error(e.getMessage());
            }catch (OperationFailedException e){
                LOGGER.error("The user could not be authenticated.");
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                responseJson.put("Status", "ERROR");
                responseJson.put("Message", "Basic Authentication Failed");
                responseJson.put("Description", "An unknown exception occured while authenticating the user");
                LOGGER.error(e.getMessage());
            }

            if(isAuthenticated){
                UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
                ConfluenceUser user = userAccessor.getUserByName(username);
                isAdmin = permissionManager.isSystemAdministrator(user);
                LOGGER.debug("The user has a system admin permission ? "+isAdmin);
            }
        }


        if(isAdmin && isAuthenticated){
            LOGGER.debug("User is authorised. Enabling the backdoor url");
            settings.setBackdoorEnabled(Boolean.TRUE);
            String backdoorURL = settings.getLoginPageUrl();
            backdoorURL+="?"+settings.getBackdoorKey()+"="+settings.getBackdoorValue();
            response.setStatus(HttpStatus.SC_OK);
            responseJson.put("Status", "SUCCESS");
            responseJson.put("Message ", " Backdoor is enabled successfully");
            responseJson.put("Backdoor URL ", backdoorURL);
        }else if(isAuthenticated && !isAdmin){
            LOGGER.error("The user does not have System Admin permission");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            responseJson.put("Status", "ERROR");
            responseJson.put("Message", "Access Denied");
            responseJson.put("Description", "The user does not have System Admin permission");
        }

        LOGGER.debug("Forming response");

        response.setContentType(MediaType.APPLICATION_JSON);
        response.getOutputStream().write(responseJson.toString().getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().close();
        return;

    }

    public MoOAuthSettings getSettings() {
        return settings;
    }

    public void setSettings(MoOAuthSettings settings) {
        this.settings = settings;
    }

    public LoginManager getLoginManager() {
        return loginManager;
    }

    public void setLoginManager(LoginManager loginManager) {
        this.loginManager = loginManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }
}
