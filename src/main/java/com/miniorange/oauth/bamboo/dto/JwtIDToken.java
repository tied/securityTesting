package com.miniorange.oauth.bamboo.dto;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;

public class JwtIDToken {

    private String header;

    private String payload;

    private String signature;

    private String encodedHeader;

    private String encodedPayload;

    public JwtIDToken(String header, String payload, String signature) {
        this.header = header;
        this.payload = payload;
        this.signature = signature;
    }

    public JwtIDToken(String token) throws JsonParseException {
        String[] arr= StringUtils.split(token,".");
        this.encodedHeader = arr[0];
        this.payload = arr[1];
        this.signature = arr[2];

        Base64.Decoder base64Decoder = Base64.getDecoder();
        this.header = new String(base64Decoder.decode(arr[0]));
        this.payload = new String(base64Decoder.decode(arr[1]));
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getEncodedHeader() {
        return encodedHeader;
    }

    public void setEncodedHeader(String encodedHeader) {
        this.encodedHeader = encodedHeader;
    }

    public String getEncodedPayload() {
        return encodedPayload;
    }

    public void setEncodedPayload(String encodedPayload) {
        this.encodedPayload = encodedPayload;
    }

    @Override
    public String toString() {
        return "JwtIDToken{" +
                "header='" + header + '\'' +
                ", payload='" + payload + '\'' +
                ", signature='" + signature + '\'' +
                ", encodedHeader='" + encodedHeader + '\'' +
                ", encodedPayload='" + encodedPayload + '\'' +
                '}';
    }
}
