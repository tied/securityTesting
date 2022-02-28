package com.miniorange.oauth;

public class MoOAuthPluginException extends RuntimeException {
	private PluginErrorCode errorCode;
	private String message;

	public MoOAuthPluginException(PluginErrorCode errorCode, String message, Throwable t) {
		super(t);
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
		
		UNKNOWN("An unknown error occurred.");

		private String message;

		private PluginErrorCode(String message) {
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}
	}

}
