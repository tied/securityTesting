package com.miniorange.sso.saml.bamboo.servlet;

import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.json.jsonorg.JSONObject;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.opensaml.xml.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Enumeration;

public class MoEnableBackdoorUrl extends HttpServlet {
    private MoSAMLSettings settings;
    private BambooUserManager bambooUserManager;
    private BambooPermissionManager bambooPermissionManager;

    private static Log LOGGER = LogFactory.getLog(MoEnableBackdoorUrl.class);

    public MoEnableBackdoorUrl(MoSAMLSettings settings, BambooUserManager bambooUserManager, BambooPermissionManager bambooPermissionManager) {
        this.settings = settings;
        this.bambooUserManager = bambooUserManager;
        this.bambooPermissionManager = bambooPermissionManager;
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
        response.getOutputStream().write(responseJson.toString().getBytes());
        response.getOutputStream().close();
        return;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Enable Backdoor servlet called from doPost");
        Enumeration headerNames = request.getHeaderNames();
        String key= "", value="";
        Boolean isAdmin = Boolean.FALSE;
        while (headerNames.hasMoreElements()) {
            key = (String) headerNames.nextElement();
            if (StringUtils.equalsIgnoreCase("Authorization", key)) {
                value = request.getHeader(key);
                value = value.split(" ")[1];
                byte[] valueDecoded = Base64.decode(value);
                value = new String(valueDecoded);
                String[] arr = value.split(":");
                String username = arr[0];
                String pwd = arr[1];
                BambooUser bambooUser = null;
                if (StringUtils.isNoneBlank(username) || StringUtils.isNotEmpty(username)) {
                    bambooUser = bambooUserManager.getBambooUser(username);
                }
                if (bambooUser != null) {
                    LOGGER.debug("User exist with username : "+username);
                    if (BooleanUtils.toBoolean(bambooUserManager.authenticate(bambooUser.getUser().getName(), pwd))) {
                        isAdmin = bambooPermissionManager.isAdmin(username) || bambooPermissionManager.isSystemAdmin(username);
                    } else {
                        LOGGER.debug("Invalid Username & Password");
                        isAdmin = Boolean.FALSE;
                    }
                }
                break;
            }
        }

        if (BooleanUtils.toBoolean(isAdmin)) {
            LOGGER.debug("User has admin permissions");
                settings.setBackdoorEnabled(true);
                String backdoorURL = settings.getLoginPageUrl();
                backdoorURL+="?"+settings.getBackdoorKey()+"="+settings.getBackdoorValue();
                LOGGER.debug("Backdoor is enabled");
                JSONObject json = new JSONObject();
                try {
                    response.setStatus(HttpStatus.SC_OK);
                    json.put("Status", "SUCCESS");
                    json.put("Message ", " Backdoor is enabled successfully");
                    json.put("Backdoor URL ", backdoorURL);
                    response.setContentType(MediaType.APPLICATION_JSON);
                    response.getOutputStream().write(json.toString().getBytes());
                    response.getOutputStream().close();
                } catch (Exception e) {
                    showErrorMessage(response, "An error occurred while enabling backdoor url.");
                    LOGGER.error("An error occurred while enabling backdoor url.");
                    e.printStackTrace();
                }

        } else {
            LOGGER.error("Not a valid Admin user");
            showErrorMessage(response, "User is not a valid admin user");
        }
    }

    private void showErrorMessage(HttpServletResponse response, String message) {
        JSONObject json = new JSONObject();
        try {
            json.put("error", message);
            response.setContentType(MediaType.APPLICATION_JSON);
            response.getOutputStream().write(json.toString().getBytes());
            response.getOutputStream().close();
        } catch (Exception e) {
            LOGGER.error("An error occurred while enabling backdoor url.");
            e.printStackTrace();
        }
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public BambooUserManager getBambooUserManager() {
        return bambooUserManager;
    }

    public void setBambooUserManager(BambooUserManager bambooUserManager) {
        this.bambooUserManager = bambooUserManager;
    }

    public BambooPermissionManager getBambooPermissionManager() {
        return bambooPermissionManager;
    }

    public void setBambooPermissionManager(BambooPermissionManager bambooPermissionManager) {
        this.bambooPermissionManager = bambooPermissionManager;
    }
}
