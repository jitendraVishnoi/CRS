package crs.commerce.order;

import atg.commerce.CommerceException;
import atg.commerce.order.OrderTools;
import atg.commerce.order.PaymentAddressContainer;
import atg.commerce.order.PaymentGroupImpl;
import atg.commerce.order.RepositoryAddress;
import atg.commerce.order.RepositoryContactInfo;
import atg.core.util.Address;
import atg.nucleus.logging.ApplicationLogging;
import atg.nucleus.logging.ClassLoggingFactory;

public class PayPalPaymentGroup extends PaymentGroupImpl implements
		PaymentAddressContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ApplicationLogging logging = ClassLoggingFactory.getFactory()
			.getLoggerForClass(PayPalPaymentGroup.class);
	private Address billingAddress;

	public PayPalPaymentGroup() {
		billingAddress = new Address();
	}

	@Override
	public Address getBillingAddress() {
		return billingAddress;
	}

	@Override
	public void setBillingAddress(Address address) {
		if (logging.isLoggingDebug()) {
			logging.logDebug("Inside PaypalPaymentGroup.setBillingAddress "
					+ address);
		}
		if (address != null) {
			if ((address instanceof RepositoryContactInfo)
					|| (address instanceof RepositoryAddress)) {
				if (getBillingAddress() != null) {
					getBillingAddress().deleteObservers();
				}
				this.billingAddress = address;
				this.billingAddress.addObserver(this);
			} else {
				try {
					if (getBillingAddress() == null) {
						this.billingAddress = address.getClass().newInstance();
					}
					OrderTools.copyAddress(address, this.billingAddress);
				} catch (InstantiationException | IllegalAccessException
						| CommerceException e) {
					throw new RuntimeException(e.getMessage());
				}
			}
			setSaveAllProperties(true);
		}
		if (logging.isLoggingDebug()) {
			logging.logDebug("Exting from PaypalPaymentGroup.setBillingAddress");
		}

	}

	public void setToken(String token) {
		setPropertyValue("token", token);
	}

	public String getToken() {
		return (String) getPropertyValue("token");
	}

	public void setBillingId(String billingAgreementId) {
		setPropertyValue("billingAgreementId", billingAgreementId);
	}

	public String getBillingId() {
		return (String) getPropertyValue("billingAgreementId");
	}

	public void setCheckoutStatus(String checkoutStatus) {
		setPropertyValue("checkoutStatus", checkoutStatus);
	}

	public String getCheckoutStatus() {
		return (String) getPropertyValue("checkoutStatus");
	}

	public void setPayerId(String payerId) {
		setPropertyValue("payerId", payerId);
	}

	public String getPayerId() {
		return (String) getPropertyValue("payerId");
	}

	public void setPayerStatus(String payerStatus) {
		setPropertyValue("payerStatus", payerStatus);
	}

	public String getPayerStatus() {
		return (String) getPropertyValue("payerStatus");
	}

	public void setTransactionId(String pTransactionId) {
		setPropertyValue("transactionId", pTransactionId);
	}

	public String getTransactionId() {
		return (String) getPropertyValue("transactionId");
	}

}
