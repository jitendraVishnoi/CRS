package crs.projects.store.order.purchase;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;

import crs.paypal.ExternalPaymentManager;

import atg.commerce.CommerceException;
import atg.commerce.order.OrderHolder;
import atg.core.util.StringUtils;
import atg.droplet.DropletException;
import atg.projects.store.order.purchase.BillingInfoFormHandler;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;


public class CRSBillingInfoFormHandler extends BillingInfoFormHandler{
	
	private String paymentType;
	private ExternalPaymentManager paymentManager;
	private OrderHolder shoppingCart;
	private String returnUrl, cancelUrl;
	
	@Override
	public boolean handleBillingWithNewAddressAndNewCard(
			DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException, CommerceException {

		if (StringUtils.isBlank(getPaymentType())) {
			return super.handleBillingWithNewAddressAndNewCard(pRequest, pResponse);
		} else {
			updateBillingAddress();
			return initiatePayment(pRequest, pResponse);
		}
	}

	private boolean initiatePayment(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {
		
		
		HashMap nvp = getPaymentManager().callMarkExpressCheckout(
				String.valueOf(getShoppingCart().getCurrent().getPriceInfo().getAmount()), 
				getReturnUrl(), getCancelUrl());
		String strAck = nvp.get("ACK").toString();
		if(strAck !=null && (strAck.equalsIgnoreCase("Success") || strAck.equalsIgnoreCase("SuccessWithWarning")))
		{
			//session.setAttribute("token", nvp.get("TOKEN").toString());
			//' Redirect to paypal.com
			String payPalURL = "https://www.sandbox.paypal.com/webscr?cmd=_express-checkout&token=" + nvp.get("TOKEN");

		    //response.sendRedirect( payPalURL );
			pResponse.setStatus(302);
			pResponse.setHeader( "Location", payPalURL );
			pResponse.setHeader( "Connection", "close" );

			return true;
		}
		else
		{
			// Display a user friendly Error on the page using any of the following error information returned by PayPal

			String ErrorCode = nvp.get("L_ERRORCODE0").toString();
			String ErrorShortMsg = nvp.get("L_SHORTMESSAGE0").toString();
			String ErrorLongMsg = nvp.get("L_LONGMESSAGE0").toString();
			String ErrorSeverityCode = nvp.get("L_SEVERITYCODE0").toString();
			vlogError("error while payment with paypal {0}", ErrorLongMsg);
			addFormException(new DropletException(ErrorLongMsg));
			return false;
		}
			
	}
	
	private void updateBillingAddress() {
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
			return initiatePayment(pRequest, pResponse);
		}
	}
	
	

	public ExternalPaymentManager getPaymentManager() {
		return paymentManager;
	}

	public void setPaymentManager(ExternalPaymentManager paymentManager) {
		this.paymentManager = paymentManager;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public OrderHolder getShoppingCart() {
		return shoppingCart;
	}

	public void setShoppingCart(OrderHolder shoppingCart) {
		this.shoppingCart = shoppingCart;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getCancelUrl() {
		return cancelUrl;
	}

	public void setCancelUrl(String cancelUrl) {
		this.cancelUrl = cancelUrl;
	}
	
}
