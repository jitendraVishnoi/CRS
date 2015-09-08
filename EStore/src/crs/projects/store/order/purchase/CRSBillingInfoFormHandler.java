package crs.projects.store.order.purchase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import atg.commerce.CommerceException;
import atg.commerce.order.HardgoodShippingGroup;
import atg.commerce.order.Order;
import atg.commerce.order.ShippingGroup;
import atg.commerce.order.purchase.ShippingGroupContainerService;
import atg.core.util.StringUtils;
import atg.projects.store.order.purchase.BillingInfoFormHandler;
import atg.repository.RepositoryItem;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import crs.paypal.PaypalManager;

public class CRSBillingInfoFormHandler extends BillingInfoFormHandler{
	
	private String paymentType;
	private PaypalManager paypalManager;
	private String payPalReturnUrl, payPalCancelUrl;
	private ShippingGroupContainerService shippingGroupContainerService;
	
	@Override
	public boolean handleBillingWithNewAddressAndNewCard(
			DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException, CommerceException {
		
		if (StringUtils.isBlank(getPaymentType())) {
			return super.handleBillingWithNewAddressAndNewCard(pRequest, pResponse);
		} else {
			addNewAddress();
			initiatePayment();
			return true;
		}
	}

	
	private void initiatePayment() {
		HashMap<String, String> details = new HashMap<String, String>();
		details.put("returnUrl", getPayPalReturnUrl());
		details.put("cancelUrl", getPayPalCancelUrl());
		Map<String, ShippingGroup> shippingGroups = getShippingGroupContainerService().getShippingGroupMap();
		HardgoodShippingGroup defaultAddress  = null;
		for (Entry<String, ShippingGroup> entry : shippingGroups.entrySet()) {
			if (entry.getKey().equals(getStoredAddressSelection())) {
				defaultAddress = (HardgoodShippingGroup) entry.getValue();
				break;
			}
		}
		details.put("returnUrl", getPayPalReturnUrl());
		details.put("cancelUrl", getPayPalReturnUrl());
		details.put("shipToName", defaultAddress.getShippingAddress().getFirstName()); 
		details.put("shipToStreet", defaultAddress.getShippingAddress().getAddress1());
		details.put("shipToCity",defaultAddress.getShippingAddress().getCity()); 
		details.put("shipToState", defaultAddress.getShippingAddress().getState());
		details.put("shipToCountryCode", defaultAddress.getShippingAddress().getCountry()); 
		details.put("shipToZip", defaultAddress.getShippingAddress().getPostalCode());
		details.put("shipToStreet2", defaultAddress.getShippingAddress().getAddress2());
		//details.put("phoneNum"), defaultAddress.get);
		
		getPaypalManager().callExpressCheckout(getOrder(), details);
	}


	private void addNewAddress() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean handleBillingWithSavedAddressAndNewCard(
			DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException, CommerceException {
		if (StringUtils.isBlank(getPaymentType())) {
			return super.handleBillingWithSavedAddressAndNewCard(pRequest, pResponse);
		} else {
			initiatePayment();
			return true;
		}
		
	}


	public String getPaymentType() {
		return paymentType;
	}


	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}


	public PaypalManager getPaypalManager() {
		return paypalManager;
	}


	public void setPaypalManager(PaypalManager paypalManager) {
		this.paypalManager = paypalManager;
	}


	public String getPayPalReturnUrl() {
		return payPalReturnUrl;
	}


	public void setPayPalReturnUrl(String payPalReturnUrl) {
		this.payPalReturnUrl = payPalReturnUrl;
	}


	public String getPayPalCancelUrl() {
		return payPalCancelUrl;
	}


	public void setPayPalCancelUrl(String payPalCancelUrl) {
		this.payPalCancelUrl = payPalCancelUrl;
	}


	public ShippingGroupContainerService getShippingGroupContainerService() {
		return shippingGroupContainerService;
	}


	public void setShippingGroupContainerService(
			ShippingGroupContainerService shippingGroupContainerService) {
		this.shippingGroupContainerService = shippingGroupContainerService;
	}

}
