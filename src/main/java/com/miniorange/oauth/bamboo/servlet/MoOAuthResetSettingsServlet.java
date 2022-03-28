package com.miniorange.oauth.bamboo.servlet;

import org.apache.http.NameValuePair;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.oauth.bamboo.MoOAuthManager;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import com.miniorange.oauth.bamboo.MoOAuthPluginConfigurationsHandler;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.message.BasicNameValuePair;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class MoOAuthResetSettingsServlet extends HttpServlet {

    private static Log LOGGER = LogFactory.getLog(MoOAuthResetSettingsServlet.class);
    private final UserManager userManager;
    private MoOAuthSettings settings;
    private MoOAuthPluginConfigurationsHandler moOAuthPluginConfigurationsHandler;

    public MoOAuthResetSettingsServlet(UserManager userManager, MoOAuthSettings settings, MoOAuthPluginConfigurationsHandler moOAuthPluginConfigurationsHandler) {
        this.userManager = userManager;
        this.settings = settings;
        this.moOAuthPluginConfigurationsHandler = moOAuthPluginConfigurationsHandler;
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("MoOAuthResetSettings servlet called");

        final UserProfile user = userManager.getRemoteUser();
        if (user != null && userManager.isAdmin(user.getUserKey())) {
            //API request to reset the settings
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("action", "resetSettings"));
            MoOAuthHttpUtils.sendPostRequest(settings.getCreateUserUrl(), postParameters,
                "application/x-www-form-urlencoded", null);


            String redirectURL = settings.getBaseUrl()+"/plugins/servlet/bamboo-oauth/configure.action";
            String content = "Please wait!! We are resetting plugin settings.";

            MoOAuthManager.httpRedirectWithText(response,redirectURL,content);
        }
        return;
    }
    public UserManager getUserManager() {
        return userManager;
    }

    public MoOAuthSettings getSettings() {
        return settings;
    }

    public void setSettings(MoOAuthSettings settings) {
        this.settings = settings;
    }
}
