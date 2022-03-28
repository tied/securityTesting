package com.miniorange.sso.saml.utils;

import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.json.jsonorg.JSONObject;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import org.apache.commons.lang3.StringUtils;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class MoSendAlert {

    private BambooPermissionManager bambooPermissionManager;
    private BambooUserManager bambooUserManager;
    private MoSAMLSettings settings;
    private UserManager userManager;
    private static Log LOGGER = LogFactory.getLog(MoSendAlert.class);

    public MoSendAlert(BambooPermissionManager bambooPermissionManager,BambooUserManager bambooUserManager,MoSAMLSettings settings,UserManager userManager){
        this.bambooPermissionManager=bambooPermissionManager;
        this.bambooUserManager=bambooUserManager;
        this.settings=settings;
        this.userManager=userManager;
    }

    public Boolean sendMail() {
        LOGGER.info("Sending Alert email to administrator user");
        String adminEmail=getAdminEmail();
        try {
            String content=new String();
            content = content + "Hello,<br><br>Email: " + adminEmail + "<br><br>Plugin Name: " + MoPluginConstants.PLUGIN_NAME;
            content = content + "<br><br>Details: miniOrange SAML SSO certificates are due to expire in " + settings.getSPCertExpireOn() + " days. Kindly visit plugin settings and update certificates.";
            content = content + "<br><br>Thanks<br>Atlassian Admin";
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("customerKey", MoPluginConstants.DEFAULT_CUSTOMER_KEY);
            jsonObject.put("sendEmail", true);
            JSONObject emailObject=new JSONObject();
            emailObject.put("customerKey", MoPluginConstants.DEFAULT_CUSTOMER_KEY);
            emailObject.put("fromEmail", "no-reply@xecurify.com");
            emailObject.put("bccEmail", "no-reply@xecurify.com");
            emailObject.put("fromName", "miniOrange");
            emailObject.put("toEmail", adminEmail);
            emailObject.put("toName", adminEmail);
            emailObject.put("bccEmail", adminEmail);
            emailObject.put("subject", "Alert Email for  "+MoPluginConstants.PLUGIN_NAME);
            emailObject.put("content", content);
            jsonObject.put("email", emailObject);
            String json= jsonObject.toString();
            LOGGER.debug("Sending Email... ");
            StringBuilder url = new StringBuilder(MoPluginConstants.AUTH_BASE_URL);
            url.append(MoPluginConstants.NOTIFY_API);
            String response1 = MoHttpUtils.sendPostRequest(url.toString(), json, MoHttpUtils.CONTENT_TYPE_JSON, MoPluginHandler.getAuthorizationHeaders(Long.valueOf(
                    MoPluginConstants.DEFAULT_CUSTOMER_KEY), MoPluginConstants.DEFAULT_API_KEY));
            LOGGER.debug("Send_feedback response: " + response1);
        } catch (Exception e){
            LOGGER.error("An error occurred while sending the email alert.",e);
            return false;
        }
        return true;
    }

    private Collection<String> getAdminUsers() {
        Set<String> adminUsers = new HashSet<>();
        adminUsers.addAll(bambooPermissionManager.getAdminUsers());
        adminUsers.addAll(bambooPermissionManager.getRestrictedAdminUsers());
        for (String groupName : bambooPermissionManager.getAdminGroups()) {
            List<String> users = bambooUserManager.getMemberNamesAsList(bambooUserManager.getGroup(groupName));
            adminUsers.addAll(users);
        }
        return adminUsers;
    }

    private String getAdminEmail(){
        String email="";
       /* Iterator<String> iterator=getAdminUsers().iterator();*/
        for (String adminUser: getAdminUsers()){
           email= userManager.getUserProfile(adminUser).getEmail();
        }
        return email;
    }

    public BambooPermissionManager getBambooPermissionManager() {
        return bambooPermissionManager;
    }

    public void setBambooPermissionManager(BambooPermissionManager bambooPermissionManager) {
        this.bambooPermissionManager = bambooPermissionManager;
    }

    public BambooUserManager getBambooUserManager() {
        return bambooUserManager;
    }

    public void setBambooUserManager(BambooUserManager bambooUserManager) {
        this.bambooUserManager = bambooUserManager;
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }
}
