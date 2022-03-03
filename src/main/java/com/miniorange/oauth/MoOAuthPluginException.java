package com.miniorange.oauth;

public class MoOAuthPluginException extends RuntimeException {
	private PluginErrorCode errorCode;
	private String message;

	public MoOAuthPluginException(PluginErrorCode errorCode, String message) {
		this.errorCode = errorCode;
		this.message = message;
	}

	public PluginErrorCode getErrorCode() {
		return this.errorCode;
	}

	public void setErrorCode(PluginErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public enum PluginErrorCode {
		UNKNOWN("An unknown error occurred."),
		SAVING_DETAILS("An error occurred while saving details.");

		private String message;

		private PluginErrorCode(String message) {
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}
	}
}
