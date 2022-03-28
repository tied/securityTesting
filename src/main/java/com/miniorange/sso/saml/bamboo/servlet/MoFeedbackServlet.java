package com.miniorange.sso.saml.bamboo.servlet;

import com.atlassian.json.jsonorg.JSONObject;
import com.google.gson.*;
import com.miniorange.sso.saml.bamboo.MoPluginConfigurationsHandler;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoHttpUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


public class MoFeedbackServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoFeedbackServlet.class);

    private String reason;
    private String feedback_content;
    private String feedback_email;
    private Boolean can_contact;
    private Boolean send_configurations;
    private MoSAMLSettings settings;
    private MoPluginConfigurationsHandler pluginConfigurationsHandler;

    public MoFeedbackServlet(MoSAMLSettings settings, MoPluginConfigurationsHandler pluginConfigurationsHandler) {
        this.settings = settings;
        this.pluginConfigurationsHandler = pluginConfigurationsHandler;
    }



    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            LOGGER.debug("Received feedback - reason:" + request.getParameter("reason") + ",feedback_content:" + request.getParameter("feedback_content")
                    + ",feedback_email:" + request.getParameter("feedback_email") + ",can_contact:" + request.getParameter("can_contact"));
            this.reason = request.getParameter("reason");
            this.feedback_content = StringUtils.defaultIfEmpty(request.getParameter("feedback_content"), StringUtils.EMPTY);
            this.feedback_email = StringUtils.defaultIfEmpty(request.getParameter("feedback_email"), StringUtils.EMPTY);
            this.can_contact = BooleanUtils.toBoolean(request.getParameter("can_contact"));
            this.send_configurations = BooleanUtils.toBoolean(request.getParameter("send_configurations"));
            LOGGER.debug("Sending feedback - reason:" + this.reason + " ,feedback_content:"+this.feedback_content+",feedback_email:" + this.feedback_email+ ",send_configurations");
            String content=new String();
            String canContact = this.can_contact?"yes":"no";
            content = content + "Hello,<br><br>Email: " + this.feedback_email + "<br><br>Can contact: " + canContact +
                    "<br><br>Plugin Name: " +MoPluginConstants.PLUGIN_NAME;
            if (!this.reason.equalsIgnoreCase("Select Reason")) {
                content =content + "<br><br>Reason for feedback: " + this.reason;
            } else {
                content =content + "<br><br>Reason for feedback: Not selected. ";
            }
            content = content + "<br><br>Details: "+ this.feedback_content;
            try {
                if (this.send_configurations) {
                    content = content + "<br><br>Plugin configurations : <br><br>";
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    JsonParser jp = new JsonParser();
                    JsonObject je = (JsonObject) jp.parse(pluginConfigurationsHandler.generateConfigurationsJson());
                    je.getAsJsonObject("Sign In Settings").remove("Logout Template");
                    je.getAsJsonObject("Sign In Settings").remove("Error Message Template");
                    je.getAsJsonObject("Sign In Settings").remove("Login Template");
                    content = returnJsonString(je,content);
                } else {
                    content = content + "<br><br>Plugin configurations: Not sent.";
                }
            }catch (Exception e){
                LOGGER.debug("Error :" + e.toString());
            }
            content = content + "<br><br>Thanks<br>Atlassian Admin";

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("customerKey", MoPluginConstants.DEFAULT_CUSTOMER_KEY);
            jsonObject.put("sendEmail", true);
            JSONObject emailObject = new JSONObject();
            emailObject.put("customerKey", MoPluginConstants.DEFAULT_CUSTOMER_KEY);
            emailObject.put("fromEmail", "no-reply@xecurify.com");
            emailObject.put("bccEmail", "no-reply@xecurify.com");
            emailObject.put("fromName", "miniOrange");
            emailObject.put("toEmail", "atlassiansupport@xecurify.com");
            emailObject.put("toName", "atlassiansupport@xecurify.com");
            emailObject.put("bccEmail", "atlassiansupport@xecurify.com");
            emailObject.put("subject", "Feedback for " + MoPluginConstants.PLUGIN_NAME);
            emailObject.put("content", content);
            jsonObject.put("email", emailObject);
            String json = jsonObject.toString();
            String response1 = MoHttpUtils.sendPostRequest(MoPluginConstants.NOTIFY_API, json,
                    MoHttpUtils.CONTENT_TYPE_JSON, MoPluginHandler.getAuthorizationHeaders(Long.valueOf(MoPluginConstants.DEFAULT_CUSTOMER_KEY),
                            MoPluginConstants.DEFAULT_API_KEY));
            LOGGER.debug("Send_feedback response: " + response1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String returnJsonString(JsonObject jsonObject, String content){
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()){
            if (entry.getKey().equalsIgnoreCase("Identity Providers")){
                JsonArray idps = entry.getValue().getAsJsonArray();
                for (int idpCount=0;idpCount<idps.size();idpCount++){
                    JsonObject idpConfigurations = (JsonObject) idps.get(idpCount);
                    for (Map.Entry<String, JsonElement> subEntry : idpConfigurations.entrySet()){
                        content = content + "<b>"+subEntry.getKey() +" : </b>" +subEntry.getValue()+ "<br>";
                    }
                    content = content + "<br>";
                }
            }else{
                content = content + entry + "<br><br>";
            }
        }
        return content;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFeedback_content() {
        return feedback_content;
    }

    public void setFeedback_content(String feedback_content) {
        this.feedback_content = feedback_content;
    }

    public String getFeedback_email() {
        return feedback_email;
    }

    public void setFeedback_email(String feedback_email) {
        this.feedback_email = feedback_email;
    }

    public Boolean getCan_contact() {
        return can_contact;
    }

    public void setCan_contact(Boolean can_contact) {
        this.can_contact = can_contact;
    }
    public Boolean getSend_configurations () {
        return send_configurations;
    }

    public void setSend_configurations (Boolean send_configurations){
        this.send_configurations = send_configurations;
    }
}


