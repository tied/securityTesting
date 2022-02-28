package com.miniorange.oauth.confluence.action;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.miniorange.oauth.confluence.dto.JwtIDToken;
import com.miniorange.oauth.confluence.dto.ProtocolType;
import com.miniorange.oauth.confluence.dto.TokenResponse;
import com.miniorange.oauth.confluence.factory.IProtocolAction;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import com.miniorange.oauth.utils.MoOAuthUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class MoOpenIdAction implements IProtocolAction {

    private static Log LOGGER = LogFactory.getLog(MoOpenIdAction.class);

    private static ProtocolType TYPE = ProtocolType.OPENID;

    private static String algKey  = "alg";

    private static String audience = "aud";

    private JsonParser parser = new JsonParser();

    @Override
    public TokenResponse sendTokenRequest(MoOAuthSettings settings,String code) {

        LOGGER.info("Create and send OpenID ID Token Request");

        String redirectURI = settings.getCallBackUrl() + settings.getCustomCallbackParameter();
        LOGGER.debug("check callback URL in OpenID flow : "+ redirectURI);
        
        String accessTokenURI = settings.getAccessTokenEndpoint();

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("redirect_uri", redirectURI));
        postParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        //postParameters.add(new BasicNameValuePair("client_id", settings.getClientID()));
        //postParameters.add(new BasicNameValuePair("client_secret", settings.getClientSecret()));
        postParameters.add(new BasicNameValuePair("code", code));

        HashMap<String,String> header = new HashMap<String,String>();
        header.put("accept", "application/json");

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


    /**
     * The basic OpenID call doesn't need a user info request to be made as ID Token has all the relevant
     * information in the Access Token call's response. This function exists in-case there's an app which
     * might need to make further API calls to fetch extra user information.
     *
     * For now this function simply returns the ID token in the accessTokenResponse.
     *
     * TODO : com.atlassian.confluence.json.parser.JSONObject is deprecated. Need to update to another class.
     */
    @Override
    public TokenResponse sendUserInfoRequest(String accessTokenResponse, MoOAuthSettings settings) throws JsonParseException {
        LOGGER.info("Inside Send User Info.....................");
        JsonElement endpointResponseElement = parser.parse(accessTokenResponse);
        JsonObject endpointResponse = endpointResponseElement.getAsJsonObject();

        LOGGER.debug("ENDPOINTRESPONSE.........." + endpointResponse);

       String idToken = endpointResponse.get("id_token").getAsString();

        if (StringUtils.isEmpty(idToken)) {
            idToken = endpointResponse.get("access_token").getAsString();
        }
        LOGGER.debug("ID Token = " + idToken);
      //  JwtIDToken jwtIDToken = null;
        JwtIDToken jwtIDToken = new JwtIDToken(idToken);

            jwtIDToken = new JwtIDToken(idToken);

            return isValidJWT(jwtIDToken,settings) ? new TokenResponse(jwtIDToken.getPayload(),TYPE) :
                    new TokenResponse("invalid_token",TYPE);

    }

    @Override
    public TokenResponse sendGroupInfoRequest(JsonObject userInfodata, HttpServletRequest request, String email,
                                              String id, MoOAuthSettings settings,
                                              MoOAuthUtils oAuthUtils) throws JsonParseException {
        if (StringUtils.isNotBlank(settings.getRoleAttribute())) {
            HashMap<String, String> userInfoMap = new HashMap<String,String>();
            userInfoMap = oAuthUtils.copyToStringValueMap(oAuthUtils.toMap(userInfodata,new HashMap<String,Object>()));
            String role = StringUtils.defaultIfBlank(userInfoMap.get(settings.getRoleAttribute()), "");
            return new TokenResponse(role, TYPE);
        }
        return new TokenResponse(StringUtils.EMPTY,TYPE);
    }
    @Override
    public TokenResponse endpointCallToGetRefreshToken(String code, MoOAuthSettings settings) throws JsonParseException {
        return null;
    }

    @Override
    public ProtocolType getProtocolType() {
        return this.TYPE;
    }


    private Boolean isValidJWT(JwtIDToken jwtIDToken,MoOAuthSettings settings) throws JsonParseException{
        String encodedHeader = jwtIDToken.getEncodedHeader();
        String encodedPayload = jwtIDToken.getEncodedPayload();
        String signature = jwtIDToken.getSignature();
        String algo = getAlgoFromToken(jwtIDToken.getHeader());
        String jwtWithoutSignature = encodedHeader + "." + encodedPayload;
        Boolean validIssuer = isValidIssuer(jwtIDToken.getPayload(),settings);
        Boolean validSig = isValidSig(algo,signature,settings,jwtWithoutSignature);
        return (validIssuer && validSig);
    }

    private String getAlgoFromToken(String header) throws JsonParseException{
        JsonObject obj = parser.parse(header).getAsJsonObject();
        return obj.get(algKey).toString();
    }

    private Boolean isValidIssuer(String payload,MoOAuthSettings settings) throws JsonParseException{
		if (BooleanUtils.toBoolean(settings.getEnableCheckIssuerFor())) {
            JsonObject obj =  (JsonObject)parser.parse(payload);
			LOGGER.debug("Audience : " + obj.get(audience).getAsString());
			if (StringUtils.equalsIgnoreCase(settings.getCheckIssuerFor(), "Default")) {
				return obj.get(audience).getAsString().equals(settings.getClientID());
			} else {
				if (StringUtils.isNotBlank(settings.getCustomIssuerValue())) {
					return StringUtils.equalsIgnoreCase(obj.get(audience).getAsString(), settings.getCustomIssuerValue());
				}
				return Boolean.FALSE;
			}
		} else {
			return  Boolean.TRUE;
		}
    }

    private Boolean isValidSig(String algo,String signature,MoOAuthSettings settings,String obj) {
        //TODO
        return Boolean.TRUE;
    }
}
