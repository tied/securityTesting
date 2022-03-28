package com.miniorange.sso.saml;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MoAPIRequest {
	private String customerKey;
	private String email;
	private String authType;
	private String txId;
	private String token;

	public MoAPIRequest() {

	}

	public MoAPIRequest(String customerKey, String email, String authType, String txId, String token) {
		this.customerKey = customerKey;
		this.email = email;
		this.authType = authType;
		this.txId = txId;
		this.token = token;
	}

	public String getCustomerKey() {
		return customerKey;
	}

	public void setCustomerKey(String customerKey) {
		this.customerKey = customerKey;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
