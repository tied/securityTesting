package com.miniorange.oauth.bamboo.dto;

import com.miniorange.oauth.bamboo.action.MoOAuthAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TokenResponse {
    private String response;
    private ProtocolType type;
    private static Log LOGGER = LogFactory.getLog(MoOAuthAction.class);

    public TokenResponse(String response, ProtocolType type) {
        this.response = response;
        this.type = type;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public ProtocolType getType() {
        return type;
    }

    public void setType(ProtocolType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        //LOGGER.debug("INSIDE TOKEN Response");
        return "TokenResponse{" +
                "response='" + response + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
