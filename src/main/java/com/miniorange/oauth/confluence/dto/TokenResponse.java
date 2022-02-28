package com.miniorange.oauth.confluence.dto;

public class TokenResponse {
    private String response;
    private ProtocolType type;

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
        return "TokenResponse{" +
                "response='" + response + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
