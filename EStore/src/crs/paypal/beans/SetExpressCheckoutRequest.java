package crs.paypal.beans;

import atg.commerce.order.Order;
import atg.core.util.Address;
import atg.repository.RepositoryItem;

public class SetExpressCheckoutRequest {

	private String email;
	private RepositoryItem profile;
	private Order order;
	private String returnURL;
	private String cancelURL;
	private String currencyCode;
	private Address shippingAddress;
	private boolean callback;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public RepositoryItem getProfile() {
		return profile;
	}

	public void setProfile(RepositoryItem profile) {
		this.profile = profile;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public String getReturnURL() {
		return returnURL;
	}

	public void setReturnURL(String returnURL) {
		this.returnURL = returnURL;
	}

	public String getCancelURL() {
		return cancelURL;
	}

	public void setCancelURL(String cancelURL) {
		this.cancelURL = cancelURL;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public Address getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(Address shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public boolean isCallback() {
		return callback;
	}

	public void setCallback(boolean callback) {
		this.callback = callback;
	}

	@Override
	public String toString() {
		return "SetExpressCheckoutRequest [email=" + email + ", profile="
				+ profile + ", order=" + order + ", returnURL=" + returnURL
				+ ", cancelURL=" + cancelURL + ", currencyCode=" + currencyCode
				+ ", shippingAddress=" + shippingAddress + ", callback="
				+ callback + "]";
	}
	
}
