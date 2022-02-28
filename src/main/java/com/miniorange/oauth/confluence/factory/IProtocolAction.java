package com.miniorange.oauth.confluence.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.miniorange.oauth.confluence.dto.ProtocolType;
import com.miniorange.oauth.confluence.dto.TokenResponse;
import com.miniorange.oauth.utils.MoOAuthUtils;

import javax.servlet.http.HttpServletRequest;

public interface IProtocolAction {
    public TokenResponse sendTokenRequest(MoOAuthSettings settings,String code);
    public TokenResponse sendUserInfoRequest(String accessTokenResponse,MoOAuthSettings settings) throws JsonParseException;
    public TokenResponse sendGroupInfoRequest(JsonObject userInfodata, HttpServletRequest request, String email,
                                              String id, MoOAuthSettings settings,
                                              MoOAuthUtils oAuthUtils) throws JsonParseException;
    public TokenResponse endpointCallToGetRefreshToken(String code,MoOAuthSettings settings) throws JsonParseException;
    public ProtocolType getProtocolType();
}
