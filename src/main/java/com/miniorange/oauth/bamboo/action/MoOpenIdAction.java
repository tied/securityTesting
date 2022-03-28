package com.miniorange.oauth.bamboo.action;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.*;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.miniorange.oauth.bamboo.MoOAuthPluginConstants;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import com.miniorange.oauth.bamboo.dto.JwtIDToken;
import com.miniorange.oauth.bamboo.dto.ProtocolType;
import com.miniorange.oauth.bamboo.dto.TokenResponse;
import com.miniorange.oauth.bamboo.factory.IProtocolAction;

import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import com.miniorange.oauth.utils.MoOAuthUtils;

public class MoOpenIdAction implements IProtocolAction {

	private static Log LOGGER = LogFactory.getLog(MoOpenIdAction.class);

	private static ProtocolType TYPE = ProtocolType.OPENID;
	private JsonParser parser = new JsonParser();

	private static String algKey = "alg";

	private static String audience = "aud";

	@Override
	public TokenResponse sendTokenRequest(MoOAuthSettings settings, String code) {

		LOGGER.info("Create and send OpenID ID Token Request");
		LOGGER.debug("Within OPEN ID SEND TOKEN REQUEST");

		String redirectURI = settings.getBaseUrl().concat("/plugins/servlet/oauth/callback") + settings.getCustomizableCallbackURL();
		String accessTokenURI = settings.getAccessTokenEndpoint();

		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("redirect_uri", redirectURI));
		postParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
		postParameters.add(new BasicNameValuePair("client_id", settings.getClientID()));
		postParameters.add(new BasicNameValuePair("client_secret", settings.getClientSecret()));
		postParameters.add(new BasicNameValuePair("code", code));


		HashMap<String, String> header = new HashMap<String, String>();
		header.put("accept", "application/json");

		// TODO : Hardcoding the content-type for now . SHould make it app specific
		String response = MoOAuthHttpUtils.sendPostRequest(accessTokenURI, postParameters,
				"application/x-www-form-urlencoded", header);
		return new TokenResponse(response, TYPE);
	}

	/**
	 * The basic OpenID call doesn't need a user info request to be made as ID Token
	 * has all the relevant information in the Access Token call's response. This
	 * function exists in-case there's an app which might need to make further API
	 * calls to fetch extra user information.
	 *
	 * For now this function simply returns the ID token in the accessTokenResponse.
	 *
	 * TODO : com.atlassian.confluence.json.parser.JSONObject is deprecated. Need to
	 * update to another class.
	 */
	@Override
	public TokenResponse sendUserInfoRequest(String accessTokenResponse, MoOAuthSettings settings)
			throws JsonParseException {

		//LOGGER.debug("INSIDE OPENID SEND USER INFO "+accessTokenResponse);
		if(StringUtils.equals(accessTokenResponse, "error")){
			return new TokenResponse("invalid_request", TYPE);
		}

		JsonObject endpointResponse = parser.parse(accessTokenResponse).getAsJsonObject();
		String idToken = endpointResponse.get("id_token").getAsString();

		LOGGER.debug("ID Token = " + idToken);
		JwtIDToken jwtIDToken = new JwtIDToken(idToken);
		return isValidJWT(jwtIDToken, settings) ? new TokenResponse(jwtIDToken.getPayload(), TYPE)
				: new TokenResponse("invalid_token", TYPE);
	}

	@Override
	public TokenResponse sendGroupInfoRequest(JsonObject userInfodata, HttpServletRequest request, String email,
			String id, MoOAuthSettings settings, MoOAuthUtils oAuthUtils) throws JsonParseException {
		if (StringUtils.isNotEmpty(settings.getRoleAttribute())) {

			HashMap<String, String> userInfoMap = new HashMap<String, String>();
			userInfoMap = oAuthUtils
					.copyToStringValueMap(oAuthUtils.toMap(userInfodata, new HashMap<String, Object>()));
			String role = StringUtils.defaultIfBlank(userInfoMap.get(settings.getRoleAttribute()), "");
			return new TokenResponse(role.toString(), TYPE);
		}
		return new TokenResponse(StringUtils.EMPTY, TYPE);
	}

	@Override
	public TokenResponse endpointCallToGetRefreshToken(String code, MoOAuthSettings settings) throws JsonParseException {
		return null;
	}

	@Override
	public ProtocolType getProtocolType() {
		return this.TYPE;
	}

	private Boolean isValidJWT(JwtIDToken jwtIDToken, MoOAuthSettings settings) throws JsonParseException{
		//LOGGER.debug("OPENID is valid  jwt");
		String encodedHeader = jwtIDToken.getEncodedHeader();
		String encodedPayload = jwtIDToken.getEncodedPayload();
		String signature = jwtIDToken.getSignature();
		String algo = getAlgoFromToken(jwtIDToken.getHeader());
		String jwtWithoutSignature = encodedHeader + "." + encodedPayload;
		Boolean validIssuer = isValidIssuer(jwtIDToken.getPayload(), settings);
		Boolean validSig = isValidSig(algo, signature, settings, jwtWithoutSignature);
		return (validIssuer && validSig);
	}

	private String getAlgoFromToken(String header) throws JsonParseException {
		JsonObject obj = parser.parse(header).getAsJsonObject();
		return obj.get(algKey).toString();
	}

	// TODO

	private Boolean isValidIssuer(String payload, MoOAuthSettings settings) throws JsonParseException {
		LOGGER.debug("OPENID IS valid Issuer");
		String audVal;
		if (BooleanUtils.toBoolean(settings.getEnableCheckIssuerFor())) {
			JsonObject obj = parser.parse(payload).getAsJsonObject();
			try {
				JsonArray arr = obj.get(audience).getAsJsonArray();
				audVal = arr.get(0).getAsString();
			}
			catch(Exception e){
				audVal = obj.get(audience).getAsString();
			}

			LOGGER.debug("Audience : " + audVal);
			if (StringUtils.equalsIgnoreCase(settings.getCheckIssuerFor(), "Default")) {
				return audVal.equals(settings.getClientID());
			} else {
				if (StringUtils.isNotBlank(settings.getCustomIssuerValue())) {
					return audVal.equals(settings.getCustomIssuerValue());
				}
				return Boolean.FALSE;
			}
		} else {
			return  Boolean.TRUE;
		}
	}

	private Boolean isValidSig(String algo, String signature, MoOAuthSettings settings, String obj) {
		// TODO
		return Boolean.TRUE;
	}
}
