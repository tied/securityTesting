package com.miniorange.oauth.bamboo.servlet;

import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.miniorange.oauth.bamboo.MoOAuthPluginConfigurationsHandler;
import com.miniorange.oauth.bamboo.MoOAuthPluginHandler;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import org.apache.commons.lang3.BooleanUtils;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;

public class MoConfigurePlugin extends HttpServlet {
    private static Log LOGGER = LogFactory.getLog(MoConfigurePlugin.class);
    private MoOAuthSettings settings;
    private MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler;
    private BambooUserManager bambooUserManager;
    private BambooPermissionManager bambooPermissionManager;

    public MoConfigurePlugin(MoOAuthSettings settings, MoOAuthPluginConfigurationsHandler pluginConfigurationsHandler,
                             BambooUserManager bambooUserManager, BambooPermissionManager bambooPermissionManager) {
        this.settings = settings;
        this.pluginConfigurationsHandler = pluginConfigurationsHandler;
        this.bambooUserManager = bambooUserManager;
        this.bambooPermissionManager = bambooPermissionManager;
    }

    public Boolean decodeAndCheckAdmin(HttpServletRequest request,HttpServletResponse response){
        Enumeration headerNames = request.getHeaderNames();
        String key= "", value="";String username =StringUtils.EMPTY;String pwd=StringUtils.EMPTY;
        Boolean isAdmin = Boolean.FALSE;
        while (headerNames.hasMoreElements()) {
            try {
                key = (String) headerNames.nextElement();
                if (StringUtils.equalsIgnoreCase("Authorization", key)) {
                    value = request.getHeader(key);
                    value = value.split(" ")[1];
                    byte[] valueDecoded = Base64.getDecoder().decode(value);
                    value = new String(valueDecoded);
                    String[] arr = value.split(":");
                    username = arr[0];
                    pwd = arr[1];
                }
            } catch (ArrayIndexOutOfBoundsException e){
                LOGGER.error("Something went wrong while fetching username and password.");
                e.printStackTrace();
            }
        }
        BambooUser bambooUser = null;
        if (StringUtils.isNoneBlank(username) || StringUtils.isNotEmpty(username)) {
            bambooUser = bambooUserManager.getBambooUser(username);
        }
        if (bambooUser != null) {
            LOGGER.debug("User exist with username : "+username);
            if (BooleanUtils.toBoolean(bambooUserManager.authenticate(bambooUser.getUser().getName(), pwd))) {
                isAdmin = bambooPermissionManager.isAdmin(username) || bambooPermissionManager.isSystemAdmin(username);
            } else {
                showErrorMessage(response,"Basic Authentication Failed", "Authentication with the provided credentials failed. It may indicate that the user does not exist or the user's account is inactive or the credentials are incorrect");
                isAdmin = Boolean.FALSE;
            }
        }
        return isAdmin;

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Configure Plugin servlet called from doGet");
        Boolean isAdmin=decodeAndCheckAdmin(request,response);
        if(BooleanUtils.toBoolean(isAdmin)){
            try {
                if (settings.isLicenseValid()) {
                    String jsonConfigurations = pluginConfigurationsHandler.generateConfigurationsJson();
                    response.setHeader("Cache-Control", "max-age=0");
                    response.setHeader("Pragma", "");
                    response.setContentType(MediaType.APPLICATION_JSON);
                    response.getOutputStream().write(jsonConfigurations.getBytes());
                } else {
                    showErrorMessage(response,"Your License has expired","Please renew the license to use the API Call.");
                }
            }catch (JSONException e) {
                // TODO: handle exception
                LOGGER.error("An error occurred while fetching the json."+ e.getMessage());
            }
        } else {
            showErrorMessage(response,"Access Denied","App Configurations can be accessed only by administrators.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Configure Plugin servlet called from doPost");
        Boolean isAdmin=decodeAndCheckAdmin(request,response);
        if(BooleanUtils.toBoolean(isAdmin)){
            StringBuffer sb = new StringBuffer();
            BufferedReader bufferedReader = null;
            try {
                bufferedReader =  request.getReader() ;
                char[] charBuffer = new char[128];
                int bytesRead;
                while ( (bytesRead = bufferedReader.read(charBuffer)) != -1 ) {
                    sb.append(charBuffer, 0, bytesRead);
                }
            }catch (Exception e){
                LOGGER.error("An error occurred while reading the configuration"+ e.getMessage());
            }finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ex) {
                        throw ex;
                    }
                }
            }
            try {
                if (settings.isLicenseValid()) {
                    settings.clearPluginSettings();
                    pluginConfigurationsHandler.importPluginConfigurations(sb.toString());
                    showSuccessMessage(response, "OAuth settings updated","OAuth App Configured successfully.");
                } else {
                    showErrorMessage(response,"Your License has expired"," Please renew the license to use the API Call.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showErrorMessage(response,"Access Denied","App Configurations can be accessed only by administrators.");
        }

    }

    private void showErrorMessage(HttpServletResponse response, String message,String description) {
        JSONObject json = new JSONObject();
        try {
            json.put("Status", "ERROR");
            json.put("Message",message);
            json.put("Description",description);
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON);
            response.getOutputStream().write(json.toString().getBytes());
            response.getOutputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSuccessMessage(HttpServletResponse response,String message,String description){
        JSONObject json = new JSONObject();
        try {
            json.put("Status","SUCCESS");
            json.put("Message", message);
            json.put("Description",description);
            response.setContentType(MediaType.APPLICATION_JSON);
            response.getOutputStream().write(json.toString().getBytes());
            response.getOutputStream().close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
