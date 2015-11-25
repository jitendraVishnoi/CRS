package crs.paypal.beans;

import java.util.ArrayList;

public class ResponseBean {

	private boolean success;
	private String token;
	private ArrayList<ErrorBean> errorBeans = new ArrayList<>();

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public ArrayList<ErrorBean> getErrorBeans() {
		return errorBeans;
	}

	public void setErrorBeans(ArrayList<ErrorBean> errorBeans) {
		this.errorBeans = errorBeans;
	}

}
