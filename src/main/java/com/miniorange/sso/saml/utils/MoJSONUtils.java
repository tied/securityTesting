package com.miniorange.sso.saml.utils;

import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

public class MoJSONUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(MoJSONUtils.class);

    public static Map<String, String> convertJsonToMap(String json, Map<String,String> mapToFill){
        try{
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> keys = jsonObject.keys();
            while(keys.hasNext()){
                String key = keys.next();
                mapToFill.put(key,jsonObject.getString(key));
            }
        } catch (JSONException e) {
            LOGGER.error("An error occurred while converting json to map ",e);
        }
        return mapToFill;
    }

    public static String convertMapToJSON(Map<String,String> map){
        try {
            JSONObject jsonObject = new JSONObject();
            for (String key : map.keySet()) {
                jsonObject.put(key, map.get(key));
            }
            return jsonObject.toString();
        }catch(JSONException e){
            LOGGER.error("An error occurred while converting map to json ",e);
            return null;
        }

    }

    public static String addKeyValue(String json, String ruleName, String ruleExpression) {
        try {
            JSONObject jsonObject = null;
            if(StringUtils.isNotBlank(json))
                jsonObject = new JSONObject(json);
            else
                jsonObject = new JSONObject();

            jsonObject.put(ruleName,ruleExpression);
            json = jsonObject.toString();
        } catch (JSONException e) {
            LOGGER.error("An error occurred while adding Key Value pair to JSON Object ",e);
        }
        return json;
    }
}
