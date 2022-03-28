package com.miniorange.oauth;

public class MoOAuthException extends RuntimeException {

	private OAuthErrorCode errorCode;

	private String message;

	private String resolution;

	public MoOAuthException(OAuthErrorCode errorCode) {
		this.errorCode = errorCode;
		this.message = errorCode.getMessage();
		this.resolution = errorCode.getResolution();
	}

	public MoOAuthException(String message, String resolution, OAuthErrorCode errorCode) {
		this.errorCode = errorCode;
		this.message = message;
		this.resolution = resolution;
	}

	public MoOAuthException(Throwable cause, OAuthErrorCode errorCode) {
		super(cause);
		this.errorCode = errorCode;
		this.message = cause.getMessage();
		this.resolution = errorCode.getResolution();
	}

	@Override
	public String getMessage() {
		return message;
	}

	public String getResolution() { return resolution; }

	public OAuthErrorCode getErrorCode() {
		return errorCode;
	}

	public enum OAuthErrorCode {
		
		UNKNOWN("An unknown error occurred.", "Please check logs for the exact error and contact support for help.");

		private String message;

		private String resolution;

		private OAuthErrorCode(String message, String resolution) {
			this.message = message;
			this.resolution = resolution;
		}

		public String getMessage() {
			return message;
		}

		public String getResolution() { return resolution; }
	}
}