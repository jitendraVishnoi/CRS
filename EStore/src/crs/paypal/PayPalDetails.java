package crs.paypal;

import java.io.Serializable;

import atg.core.util.ContactInfo;

public class PayPalDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String orderid, transactionId, parentTransactionId,
			transactionType, paymentStatus, pendingReason, reasonCode,
			checkoutStatus, payerid, payerStatus, shippingOption;
	private boolean payerStatusCertified = false, addressConfirmed = false;
	private ContactInfo shippingAddress, billingAddress;
	private double shippingAmount;

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getParentTransactionId() {
		return parentTransactionId;
	}

	public void setParentTransactionId(String parentTransactionId) {
		this.parentTransactionId = parentTransactionId;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getPendingReason() {
		return pendingReason;
	}

	public void setPendingReason(String pendingReason) {
		this.pendingReason = pendingReason;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getCheckoutStatus() {
		return checkoutStatus;
	}

	public void setCheckoutStatus(String checkoutStatus) {
		this.checkoutStatus = checkoutStatus;
	}

	public String getPayerid() {
		return payerid;
	}

	public void setPayerid(String payerid) {
		this.payerid = payerid;
	}

	public String getPayerStatus() {
		return payerStatus;
	}

	public void setPayerStatus(String payerStatus) {
		this.payerStatus = payerStatus;
	}

	public String getShippingOption() {
		return shippingOption;
	}

	public void setShippingOption(String shippingOption) {
		this.shippingOption = shippingOption;
	}

	public boolean isPayerStatusCertified() {
		return payerStatusCertified;
	}

	public void setPayerStatusCertified(boolean payerStatusCertified) {
		this.payerStatusCertified = payerStatusCertified;
	}

	public boolean isAddressConfirmed() {
		return addressConfirmed;
	}

	public void setAddressConfirmed(boolean addressConfirmed) {
		this.addressConfirmed = addressConfirmed;
	}

	public ContactInfo getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(ContactInfo shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public ContactInfo getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(ContactInfo billingAddress) {
		this.billingAddress = billingAddress;
	}

	public double getShippingAmount() {
		return shippingAmount;
	}

	public void setShippingAmount(double shippingAmount) {
		this.shippingAmount = shippingAmount;
	}

}
