package com.miniorange.oauth.confluence.factory;

import com.miniorange.oauth.confluence.action.MoOAuthAction;
import com.miniorange.oauth.confluence.action.MoOpenIdAction;
import com.miniorange.oauth.utils.MoOAuthUtils;

import java.util.ArrayList;

public class OAuthOpenIdDecisionHandler {
    /*Change made in original function : Added parameters - current appname and set of apps supporting OIDC*/
    public static IProtocolAction getProtocolHandler(String scopeToken, String appName, ArrayList<String> listOfOpenIdApps) {
        return MoOAuthUtils.isOpenIdProtocol(scopeToken, appName, listOfOpenIdApps) ? new MoOpenIdAction() : new MoOAuthAction();
    }
}

