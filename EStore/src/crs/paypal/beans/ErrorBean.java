package crs.paypal.beans;

public class ErrorBean {

	private String shortMsg;
	private String longMsg;
	private String errorCode;
	private String severitycode;

	public String getShortMsg() {
		return shortMsg;
	}

	public void setShortMsg(String shortMsg) {
		this.shortMsg = shortMsg;
	}

	public String getLongMsg() {
		return longMsg;
	}

	public void setLongMsg(String longMsg) {
		this.longMsg = longMsg;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getSeveritycode() {
		return severitycode;
	}

	public void setSeveritycode(String severitycode) {
		this.severitycode = severitycode;
	}

}
