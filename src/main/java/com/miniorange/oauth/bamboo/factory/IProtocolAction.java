package com.miniorange.oauth.bamboo.factory;

import com.miniorange.oauth.bamboo.MoOAuthSettings;
import com.miniorange.oauth.bamboo.dto.ProtocolType;
import com.miniorange.oauth.bamboo.dto.TokenResponse;
import com.miniorange.oauth.utils.MoOAuthUtils;
import com.google.gson.JsonParseException;
import com.google.gson.JsonObject;


import javax.servlet.http.HttpServletRequest;

public interface IProtocolAction {
    TokenResponse
    sendTokenRequest(MoOAuthSettings settings, String code);
    TokenResponse sendUserInfoRequest(String accessTokenResponse, MoOAuthSettings settings) throws JsonParseException;
    TokenResponse sendGroupInfoRequest(JsonObject userInfodata, HttpServletRequest request, String email,
                                              String id, MoOAuthSettings settings,
                                              MoOAuthUtils oAuthUtils) throws JsonParseException;
    TokenResponse endpointCallToGetRefreshToken(String code, MoOAuthSettings settings) throws JsonParseException;
    ProtocolType getProtocolType();
}
