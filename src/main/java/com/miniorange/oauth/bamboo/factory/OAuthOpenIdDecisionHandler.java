package com.miniorange.oauth.bamboo.factory;

import com.miniorange.oauth.bamboo.action.MoOAuthAction;
import com.miniorange.oauth.bamboo.action.MoOpenIdAction;
import com.miniorange.oauth.utils.MoOAuthUtils;

public class OAuthOpenIdDecisionHandler {
    public static IProtocolAction getProtocolHandler(String appName) {
        return MoOAuthUtils.isOpenIdProtocol(appName) ? new MoOpenIdAction() : new MoOAuthAction();
    }
}
