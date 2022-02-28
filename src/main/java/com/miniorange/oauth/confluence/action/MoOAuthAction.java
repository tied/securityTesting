package com.miniorange.oauth.confluence.action;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.miniorange.oauth.confluence.MoOAuthPluginConstants;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.miniorange.oauth.confluence.dto.ProtocolType;
import com.miniorange.oauth.confluence.dto.TokenResponse;
import com.miniorange.oauth.confluence.factory.IProtocolAction;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import com.miniorange.oauth.utils.MoOAuthUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

public class MoOAuthAction implements IProtocolAction {

    private static Log LOGGER = LogFactory.getLog(MoOAuthAction.class);

    private static ProtocolType TYPE = ProtocolType.OAUTH;

    private String accessToken=StringUtils.EMPTY;
    private String refreshToken=StringUtils.EMPTY;
    JsonParser parser = new JsonParser();

    @Override
    public TokenResponse sendTokenRequest(MoOAuthSettings settings,String code) {

        LOGGER.info("Create and send OAuth Access Token Request");

        String redirectURI = settings.getCallBackUrl() + settings.getCustomCallbackParameter();      
        LOGGER.debug("check callback URL in OAuth flow : "+ redirectURI);
        
        String accessTokenURI = settings.getAccessTokenEndpoint();

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("redirect_uri", redirectURI));
        postParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        /*postParameters.add(new BasicNameValuePair("client_id", settings.getClientID()));
        postParameters.add(new BasicNameValuePair("client_secret", settings.getClientSecret()));*/
        postParameters.add(new BasicNameValuePair("code", code));

        HashMap<String,String> header = new HashMap<String,String>();
        header.put("accept", "application/json");

     /*   if (StringUtils.equalsIgnoreCase(settings.getSendTokenAuthParameterIn(), "HttpBody")) {
            LOGGER.debug("Sending Authorization parameters in HTTP Body of the access token request ");
            postParameters.add(new BasicNameValuePair("client_id", settings.getClientID()));
            postParameters.add(new BasicNameValuePair("client_secret", settings.getClientSecret()));
        }else {
            LOGGER.debug("Sending Authorization parameters in HTTP Header of the access token request ");
            String authString = settings.getClientID() + ":" + settings.getClientSecret();
            byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes(StandardCharsets.UTF_8));
            String authStringEnc = new String(authEncBytes);
            String authorizationValue = "Basic "+authStringEnc;
            header.put("Authorization", authorizationValue);
        }*/
        if((StringUtils.equalsIgnoreCase(settings.getAppName(), "Custom App")
                || StringUtils.equalsIgnoreCase(settings.getAppName(), "miniOrange")
                || StringUtils.equalsIgnoreCase(settings.getAppName(), "OpenID"))
                && StringUtils.equalsIgnoreCase(settings.getSendTokenAuthParameterIn(), "HttpHeader")){
            LOGGER.debug("Sending Authorization parameters in HTTP Header of the access token request ");
            String authString = settings.getClientID() + ":" + settings.getClientSecret();
            byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes(StandardCharsets.UTF_8));
            String authStringEnc = new String(authEncBytes);
            String authorizationValue = "Basic "+authStringEnc;
            header.put("Authorization", authorizationValue);
        }else{
            LOGGER.debug("Sending Authorization parameters in HTTP Body of the access token request ");
            postParameters.add(new BasicNameValuePair("client_id", settings.getClientID()));
            postParameters.add(new BasicNameValuePair("client_secret", settings.getClientSecret()));
        }

        //TODO : Hardcoding the content-type for now . SHould make it app specific
        String response = MoOAuthHttpUtils.sendPostRequest(accessTokenURI, postParameters,
                "application/x-www-form-urlencoded", header);
        return new TokenResponse(response,TYPE);
    }

    @Override
    public TokenResponse sendUserInfoRequest(String accessTokenResponse,MoOAuthSettings settings) throws JsonParseException {

       // JsonObject endpointResponse = (JsonObject)parser.parse(accessTokenResponse);

        JsonElement endpointResponseElement = parser.parse(accessTokenResponse);
        JsonObject endpointResponse = endpointResponseElement.getAsJsonObject();

        String value="";
        Set<Map.Entry<String, JsonElement>> x = endpointResponse.entrySet();
        for (Map.Entry<String, JsonElement> entry : x) {
            value = endpointResponse.get(entry.getKey()).toString();
            LOGGER.debug("EndPointResponse Value : "+ value);
        }

        if(endpointResponse.has("error")){
            if(StringUtils.equals(value, "invalid_client"))
                LOGGER.debug("unauthorized invalid_client");
            return new TokenResponse(value,TYPE);
        }


        accessToken = endpointResponse.get("access_token").getAsString();
        return sendGetRequest(accessToken,settings);
    }

    @Override
    public TokenResponse sendGroupInfoRequest(JsonObject userInfodata, HttpServletRequest request, String email,
                                              String id, MoOAuthSettings settings,
                                              MoOAuthUtils oAuthUtils) throws JsonParseException{
        String groupsResponse = StringUtils.EMPTY;
        if(settings.getAppName().equals(MoOAuthPluginConstants.GOOGLE)) {
            groupsResponse = fetchGroupInfoForGoogle(request, id, settings);
        } else if(!(StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.DISCORD))
                && StringUtils.isNotEmpty(settings.getFetchGroupsEndpoint())) {
            groupsResponse = fetchGroupInfo(request, id,settings);
        }
        return new TokenResponse(groupsResponse,TYPE);
    }

    @Override
    public ProtocolType getProtocolType() {
        return this.TYPE;
    }

    private String fetchGroupInfo(HttpServletRequest request,String id,MoOAuthSettings settings) {

        String authHeader = "Bearer " + this.accessToken;
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", authHeader);

        String groupResponse = StringUtils.EMPTY;
        if(StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.FACEBOOK))
            groupResponse = MoOAuthHttpUtils.sendGetRequestWithHeaders(settings.getFetchGroupsEndpoint() + id + "/groups", headers);
        else {
            groupResponse = MoOAuthHttpUtils.sendGetRequestWithHeaders(settings.getFetchGroupsEndpoint(), headers);
            LOGGER.debug("Groups Response is: " + groupResponse);
        }
        return groupResponse;
    }

    /* This is the function for fetching Group Information from Group Info Endpoint w.r.t Google App only.
     * It fetches Refresh Token and Access Token from the Admin to get groups access for Users.*/
    /* This is the function for fetching Group Information from Group Info Endpoint w.r.t Google App only.
     * It fetches Refresh Token and Access Token from the Admin to get groups access for Users.*/
    private String fetchGroupInfoForGoogle(HttpServletRequest request, String email,
                                           MoOAuthSettings settings) throws JsonParseException {
        accessToken = StringUtils.EMPTY;

        String accessTokenResponse = endpointCallToGetAccessTokenForGoogle(settings);
        //JsonObject accessTokenEndpointData = (JsonObject)parser.parse(accessTokenResponse);
         JsonElement endpointResponseElement = parser.parse(accessTokenResponse);
         JsonObject accessTokenEndpointData = endpointResponseElement.getAsJsonObject();
        LOGGER.debug("accessTokenEndpointData " + accessTokenEndpointData);


        if(accessTokenEndpointData.has("access_token")){
            String accessTokenStr = accessTokenEndpointData.get("access_token").getAsString();
            if(StringUtils.isNotEmpty(accessTokenStr)){
                accessToken = accessTokenStr;
            }
        }

        String authHeader = "Bearer " + accessToken;
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", authHeader);
        String groupInfoURL = settings.getFetchGroupsEndpoint()+"?userKey="+email;
        String groupResponse = MoOAuthHttpUtils.sendGetRequestWithHeaders(groupInfoURL, headers);
        return groupResponse;
    }

    /* This is the function to Get Refresh Token from Access token Endpoint (Only for Google Admin)*/
    @Override
    public TokenResponse endpointCallToGetRefreshToken(String code,MoOAuthSettings settings){

        String ACCESSTOKEN_ENDPOINT = settings.getAccessTokenEndpoint();
        
        String REDIRECT_URI = settings.getCallBackUrl() + settings.getCustomCallbackParameter();
        LOGGER.debug("check callback URL in OAuth flow - endpointCallToGetRefreshToken : "+ REDIRECT_URI);

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();

        postParameters.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));
        postParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        postParameters.add(new BasicNameValuePair("client_id", settings.getClientID()));
        postParameters.add(new BasicNameValuePair("client_secret", settings.getClientSecret()));
        postParameters.add(new BasicNameValuePair("code", code));

        String Response = MoOAuthHttpUtils.sendPostRequest(ACCESSTOKEN_ENDPOINT, postParameters, "application/x-www-form-urlencoded", null);
        LOGGER.debug("response " + Response);

        return new TokenResponse(Response,TYPE);
    }

    /* This is the function to get Access token (Only for Google)*/
    private String endpointCallToGetAccessTokenForGoogle(MoOAuthSettings settings){

        ArrayList<NameValuePair> postParams1 = new ArrayList<NameValuePair>();

        postParams1.add(new BasicNameValuePair("grant_type", "refresh_token"));
        postParams1.add(new BasicNameValuePair("client_id", settings.getClientID()));
        postParams1.add(new BasicNameValuePair("client_secret", settings.getClientSecret()));
        postParams1.add(new BasicNameValuePair("refresh_token", settings.getGoogleRefreshToken()));

        String refreshTokenEndpointResponse = MoOAuthHttpUtils.sendPostRequest(settings.getAccessTokenEndpoint(), postParams1, "application/x-www-form-urlencoded", null);
        LOGGER.debug("response" + refreshTokenEndpointResponse);
        return refreshTokenEndpointResponse;
    }

    private TokenResponse sendGetRequest(String accessToken,MoOAuthSettings settings) {
        String userInfoEndpoint = settings.getUserInfoEndpoint();

        /* Header for User Info Endpoint Call*/
        String authHeader = "Bearer " + accessToken;
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", authHeader);
        if (StringUtils.contains(settings.getUserInfoEndpoint(), "=")&& settings.getUserInfoEndpoint().endsWith("=")) {
            userInfoEndpoint += accessToken;
        }
        String userDetailedInfoResponse = MoOAuthHttpUtils.sendGetRequestWithHeaders(userInfoEndpoint, headers);
        LOGGER.debug("userDetailedInfoResponse " + userDetailedInfoResponse);

        return new TokenResponse(userDetailedInfoResponse,TYPE);
    }
    
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
