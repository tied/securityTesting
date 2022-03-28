package com.miniorange.oauth.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.apache.commons.lang3.StringEscapeUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.gson.JsonObject;
import com.miniorange.oauth.bamboo.MoOAuthPluginConstants;

public class MoOAuthUtils {
	
	private static Log LOGGER = LogFactory.getLog(MoOAuthUtils.class);
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);


	public boolean checkIfKeyExist(HashMap<String, String> userInfoMap, String keyName) {
		String value = userInfoMap.get(keyName);
		if (value != null)
			return true;
		else
			return false;
	}

	public String getValue(HashMap<String, String> userInfoMap, String keyName) {
		LOGGER.debug("Taking Value");
		return userInfoMap.get(keyName);
	}
	public Map<String, JsonElement> jsonToMap(JsonObject getGroupUserInfoResponse){
		Set<Map.Entry<String, JsonElement>> keys = getGroupUserInfoResponse.entrySet();
		Map<String, JsonElement> map = new HashMap<>();
		for( Map.Entry<String, JsonElement> entry : keys ) {
			String key = entry.getKey();
			if ( getGroupUserInfoResponse.get(key).isJsonObject() ) {
				jsonToMap(getGroupUserInfoResponse.get(key).getAsJsonObject());
			} else {
				map.put(key, getGroupUserInfoResponse.get(key));
			}
		}
		return map;
	}

	public HashMap<String, Object> toMap(JsonObject object,HashMap<String, Object> map){

		Set<Map.Entry<String, JsonElement>> keysItr = object.entrySet();
		for(Map.Entry<String, JsonElement> entry : keysItr) {
			String key = entry.getKey();
			JsonElement value = object.get(key);
			Object valueToPutInMap;
			if(value.isJsonArray()) {
				valueToPutInMap = toList(value.getAsJsonArray(),map);
			}
			else if(value.isJsonObject()) {
				valueToPutInMap = toMap(value.getAsJsonObject(),map);
			}else{
				if(!value.isJsonNull()) {
					valueToPutInMap = value.getAsString();
					map.put(key, valueToPutInMap);
				}
			}
		}
		return map;
	}

	public Map<String, Object> toMap(JsonObject object,Map<String, Object> map){

		Set<Map.Entry<String, JsonElement>> keysItr = object.entrySet();
		for(Map.Entry<String, JsonElement> entry : keysItr) {
			String key = entry.getKey();
			JsonElement value = object.get(key);
			Object valueToPutInMap;
			if(value.isJsonArray()) {
				valueToPutInMap = toList(value.getAsJsonArray(),map);
			}
			else if(value.isJsonObject()) {
				valueToPutInMap = toMap(value.getAsJsonObject(),map);
			}else{
				if(!value.isJsonNull()) {
					valueToPutInMap = value.getAsString();
					map.put(key, valueToPutInMap);
				}
			}
		}
		return map;
	}

	public List<Object> toList(JsonArray array, HashMap<String, Object> map){
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < array.size(); i++) {
			JsonElement value = array.get(i);
			Object valueToPutInMap = value;
			if(value.isJsonArray()) {
				valueToPutInMap = toList(value.getAsJsonArray(),map);
			}else if(value.isJsonObject()) {
				valueToPutInMap = toMap(value.getAsJsonObject(),map);
			}
			if(!value.isJsonNull()) {
				valueToPutInMap = value.getAsString();
				list.add(valueToPutInMap);
			}
		}

		return list;
	}

	public List<Object> toList(JsonArray array, Map<String, Object> map){
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < array.size(); i++) {
			JsonElement value = array.get(i);
			Object valueToPutInMap = value;
			if(value.isJsonArray()) {
				valueToPutInMap = toList(value.getAsJsonArray(),map);
			}else if(value.isJsonObject()) {
				valueToPutInMap = toMap(value.getAsJsonObject(),map);
			}
			if(!value.isJsonNull()) {
				valueToPutInMap = value.getAsString();
				list.add(valueToPutInMap);
			}
		}
		return list;
	}

	public HashMap<String, String> copyToStringValueMap(
			HashMap<String, Object> input) {
		HashMap<String, String> ret = new HashMap<>();
		String currValue="";
		for (Map.Entry<String, Object> entry : input.entrySet()) {
			if(entry.getValue()!=null) {
				currValue = ((String) entry.getValue());
				ret.put(entry.getKey(), currValue);
			}
		}
		return ret;
	}

	public Map<String, String> copyToStringValueMap(
			Map<String, Object> input) {
		HashMap<String, String> ret = new HashMap<>();
		for (Map.Entry<String, Object> entry : input.entrySet()) {
			ret.put(entry.getKey(), (String) entry.getValue());
		}
		return ret;
	}
	
	public String findKey(JsonObject getUserInfoData, String key, String appName) {
		try {
			Set<Map.Entry<String, JsonElement>> x = getUserInfoData.entrySet();
			String multipleValues = StringUtils.EMPTY;
			LOGGER.debug("Fetching Value for Attribute: " + key);
			for (Map.Entry<String,JsonElement>keySet : x) {
				
				String currentKey = keySet.getKey();
				
				if ((getUserInfoData.get(currentKey).isJsonObject()) && (getUserInfoData != null) && !(StringUtils.equals(key, "id"))) {
					
					LOGGER.debug("JSONObject: " + getUserInfoData.getAsJsonObject(currentKey));
					String value = findKey(getUserInfoData.getAsJsonObject(currentKey), key, appName);
					if ( value != null)
						return value;
				
				} else if ((getUserInfoData.get(currentKey).isJsonArray()) && (getUserInfoData != null) && !(StringUtils.equals(key, "id"))) {
				
					LOGGER.debug("JSONArray found.. " + getUserInfoData.getAsJsonArray(currentKey));
					JsonArray array = getUserInfoData.getAsJsonArray(currentKey);
					LOGGER.debug("Size of this array: "+array.size());
					if (StringUtils.equals(appName, MoOAuthPluginConstants.MEETUP))
						continue;
					for (int index=0; index < array.size(); index++) {
						JsonObject object = (getUserInfoData.getAsJsonArray(currentKey)).get(index).getAsJsonObject();
						LOGGER.debug("JSONObject at index "+index+" : "+object);
						if(index > 0) {
							multipleValues = multipleValues.concat(";");
						}

						if(object.has(key)) {
							LOGGER.debug("Found the key: "+key);
							LOGGER.debug("value: "+object.get(key).getAsString());
							multipleValues = multipleValues.concat(object.get(key).getAsString());
						}
					}
					LOGGER.debug("Multiple Values: " + multipleValues);			
					return multipleValues;
				
				} else if (getUserInfoData.get(currentKey).isJsonPrimitive()) {
					
					String value = getUserInfoData.get(currentKey).getAsString();
					if (StringUtils.equals(currentKey, key)) {
						LOGGER.debug("Key: " + key + " Value: " + value);
						return value;
					} else {
						continue;
					}
			
				}
			}	
		} catch (Exception e) {
			LOGGER.error("An exception occurred while parsing Json",e);
			e.printStackTrace();
		}
		return StringUtils.EMPTY;
	}

	public Set<String> findEmails(HashMap<String, String> userInfoMap){
		Set<String> emailSet = new HashSet<String>();
		HashMap<String, String> map = new HashMap<String, String>(userInfoMap);
		Iterator iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry)iterator.next();
			String value = (String) pair.getValue();
			if(value != null && isEmailId(value)){
				emailSet.add(value);
			}
			iterator.remove(); 
		}
		return emailSet;
	}
	
	public boolean isEmailId(String emailStr) {
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
		return matcher.find();
	}

	public static boolean isOpenIdProtocol(String appName) {
		LOGGER.debug("isOpenId: "+appName);
		if(StringUtils.equals(appName, "Custom OpenID") || StringUtils.equals(appName, "AWS Cognito") || StringUtils.equals(appName, "Keycloak") ||
				StringUtils.equals(appName, "ADFS") || StringUtils.equals(appName, "Okta") || StringUtils.equals(appName, "Azure B2C") )
			return true;
		else return false;
	}

	/**
	 * This function is used to separate different json object and turn their keys
	 * in to a map of <String, String> which is stored with the key in final hash
	 * map Ex:-- {a: {b:1, c:2}} with be turned into hashmap a ={b=1, c=2}
	 */

	public HashMap<String, Object> toMapObjects(JsonObject object, HashMap<String, Object> map) throws JsonParseException {
		Set<Map.Entry<String, JsonElement>> keysItr = object.entrySet();
		for(Map.Entry<String, JsonElement> entry : keysItr) {
			String key = entry.getKey();
			JsonElement value = object.get(key);
			Object valueToPutInMap;
			if(!value.isJsonNull()) {
				if (value.isJsonArray()) {
					valueToPutInMap = toMapList(value.getAsJsonArray(), key);
					map.putAll((HashMap) valueToPutInMap);
				} else if (value.isJsonObject()) {
					JsonObject obj = value.getAsJsonObject();
					if (obj.isJsonNull()) {
						map.put(key, null);
					} else {
						HashMap<String, Object> newMap = new HashMap<String, Object>();
						valueToPutInMap = mapObjectsWithUpdatedKey(obj, newMap, key);
						map.putAll((HashMap) valueToPutInMap);
					}
				} else {
					try {
						map.put(key, value.getAsString());
					} catch (UnsupportedOperationException e) {
						map.put(key, value.toString());
					}
				}
			}else{
				map.put(key, StringUtils.EMPTY);
			}
		}
		return map;
	}

	public HashMap<String, Object> mapObjectsWithUpdatedKey(JsonObject object, HashMap<String, Object> map,
															String keyName) throws JsonParseException {

		Set<Map.Entry<String, JsonElement>> keysItr = object.entrySet();
		for(Map.Entry<String, JsonElement> entry : keysItr) {
			String key = entry.getKey();
			JsonElement value = object.get(key);
			key = keyName + '.' + key;
			Object valueToPutInMap;
			if(value.isJsonArray()) {
				valueToPutInMap = toMapList(value.getAsJsonArray(), key);
				map.putAll((HashMap) valueToPutInMap);
			}
			else if(value.isJsonObject()) {
				JsonObject obj = value.getAsJsonObject();
				if (obj.isJsonNull()) {
					map.put(key, null);
				} else {
					HashMap<String, Object> newMap = new HashMap<String, Object>();
					valueToPutInMap = mapObjectsWithUpdatedKey(obj, newMap, key);
					map.putAll((HashMap) valueToPutInMap);
				}
			} else{
				map.put(key, value.toString());
			}
		}
		return map;
	}

	public HashMap<String, Object> toMapList (JsonArray array, String key) throws JsonParseException {
		HashMap<String, Object> completeMap = new HashMap<String, Object>();
		List<String> list = new ArrayList<String>();
		if (array.size() == 0) {
			completeMap.put(key, null);
		} else {
			for (int i = 0; i < array.size(); i++) {
				JsonElement value = array.get(i);
				Object valueToPutInMap;
				if (value.isJsonArray()) {
					valueToPutInMap = toMapList(value.getAsJsonArray(), key + '[' + i + ']');
					completeMap.putAll((HashMap) valueToPutInMap);
				} else if (value.isJsonObject()) {
					JsonObject obj = value.getAsJsonObject();
					if (obj.isJsonNull()) {
						completeMap.put(key + '[' + i + ']', null);
					} else {
						HashMap<String, Object> newMap = new HashMap<String, Object>();
						valueToPutInMap = mapObjectsWithUpdatedKey(obj, newMap, key + '[' + i + ']');
						completeMap.putAll((HashMap) valueToPutInMap);
					}
				}  else {
					list.add(value.toString());
				}
			}
			if (list.size() > 0) {
				String temp = list.get(0);
				for (int i = 1; i < list.size(); i++)
					temp = temp + ";" + list.get(i);

				completeMap.put(key, temp);
			}
		}
		return completeMap;
	}

	public static String deserializePublicKey(String publicKey) {
		LOGGER.debug("Deserializing Public Key");
		String BEGIN_PUBLIC_KEY = "BEGIN PUBLIC KEY";
		String END_PUBLIC_KEY = "END PUBLIC KEY";
		if (StringUtils.isNotBlank(publicKey)) {
			publicKey = StringUtils.remove(publicKey, "\r");
			publicKey = StringUtils.remove(publicKey, "\n");
			publicKey = StringUtils.remove(publicKey, "-");
			publicKey = StringUtils.remove(publicKey, BEGIN_PUBLIC_KEY);
			publicKey = StringUtils.remove(publicKey, END_PUBLIC_KEY);
			publicKey = StringUtils.remove(publicKey, " ");
		}
		return publicKey;
	}

	public static String sanitizeText(String text) {
		if(StringUtils.isBlank(text)){
			return text;
		}
		//Removing all the HTML Tags
		LOGGER.debug("Text before sanitization: "+text);
		text = Jsoup.parse(text).text();
		LOGGER.debug("Text after sanitization: "+text);
		return text;
	}

	public static String sanitizeRegex(String regex){
		LOGGER.debug("Original input : "+regex);
		String cleanRegex = StringEscapeUtils.escapeEcmaScript(regex);
		LOGGER.debug("Clean input : "+cleanRegex);
		return cleanRegex;
	}

}
