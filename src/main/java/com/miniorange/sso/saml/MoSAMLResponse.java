package com.miniorange.sso.saml;

import java.util.Map;

public class MoSAMLResponse implements java.io.Serializable {

	private Map<String, String[]> attributes;
	private String nameId;
	private String sessionIndex;
	private String relayStateURL;

	public MoSAMLResponse(Map<String, String[]> attributes, String nameId, String sessionIndex, String relayStateURL)
	{
		this.attributes = attributes;
		this.nameId = nameId;
		this.sessionIndex = sessionIndex;
		this.relayStateURL = relayStateURL;
	}
	
	public Map<String, String[]> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String[]> attributes) {
		this.attributes = attributes;
	}

	public String getNameId() {
		return nameId;
	}

	public void setNameId(String nameId) {
		this.nameId = nameId;
	}

	public String getRelayStateURL() {
		return relayStateURL;
	}

	public void setRelayStateURL(String relayStateURL) {
		this.relayStateURL = relayStateURL;
	}

	public String getSessionIndex() {
		return sessionIndex;
	}
	public void setSessionIndex(String sessionIndex) {
		this.sessionIndex = sessionIndex;
	}
	@Override
	public String toString()
	{
		return "SAMLResponse{attributes=" + this.attributes + ", nameId=" + this.nameId + ", sessionIndex='" + this.sessionIndex + ", relayStateURL='" + this.relayStateURL + "}";
	}
}
