package com.miniorange.sso.saml;

public class MoPluginException extends RuntimeException {

	private PluginErrorCode errorCode;

	private String message;

	public MoPluginException(PluginErrorCode errorCode, String message, Throwable t) {
		super(t);
		this.errorCode = errorCode;
		this.message = message;
	}

	public MoPluginException(PluginErrorCode errorCode, String message) {
		this.errorCode = errorCode;
		this.message = message;
	}

	public PluginErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(PluginErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public enum PluginErrorCode {

		UNKNOWN("An unknown error occurred."),

		METADATA_PARSE("An error occurred while parsing IDP Metadata File."),
		
		SAVING_DETAILS("An error occurred while saving details."),

		EMPTY_CONFIGURATIONS("The plugin configurations file has no Identity Provider information"),

		CERT_ERROR("Certificate error.");

		private String message;

		private PluginErrorCode(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}
}
