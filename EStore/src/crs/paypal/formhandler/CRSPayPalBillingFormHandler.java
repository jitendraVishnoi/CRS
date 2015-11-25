package crs.paypal.formhandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.transaction.Transaction;

import crs.paypal.PayPalProcessor;
import crs.paypal.beans.SetExpressCheckoutRequest;

import atg.commerce.CommerceException;
import atg.commerce.order.HardgoodShippingGroup;
import atg.core.util.StringUtils;
import atg.droplet.DropletFormException;
import atg.projects.store.order.purchase.BillingInfoFormHandler;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;

public class CRSPayPalBillingFormHandler extends BillingInfoFormHandler{

	private PayPalProcessor payPalProcessor;

	public boolean handleCheckoutWithPayPal(DynamoHttpServletRequest pRequest, 
			DynamoHttpServletResponse pResponse) throws IOException, ServletException {

		vlogDebug("Entering handleCheckoutWithPayPal...");

		Transaction tr = null;

		tr = ensureTransaction();
		try {

			final SetExpressCheckoutRequest setExpressCheckoutRequest = new SetExpressCheckoutRequest();
			setExpressCheckoutRequest.setOrder(getOrder());
			setExpressCheckoutRequest.setProfile(getProfile());
			setExpressCheckoutRequest.setReturnURL(getPayPalProcessor().getSuccessDestURL());
			setExpressCheckoutRequest.setCancelURL(getPayPalProcessor().getCancelDestURL());
			String currencyCode = null;
			if (getOrder().getPriceInfo() != null) {
				currencyCode = getOrder().getPriceInfo().getCurrencyCode();
			}
			if (StringUtils.isBlank(currencyCode)) {
				currencyCode = getPayPalProcessor().getDefaultCurrencyCode();
			}
			setExpressCheckoutRequest.setCurrencyCode(currencyCode);
			setExpressCheckoutRequest.setCallback(false);

			setExpressCheckoutRequest
			.setShippingAddress(((HardgoodShippingGroup) getOrder().getShippingGroups().get(0))
					.getShippingAddress());
			vlogDebug("setExpressCheckoutRequest:{0}", setExpressCheckoutRequest);
			// Make SetExpressCheckout API
			final String token = getPayPalProcessor().callSetExpressCheckout(setExpressCheckoutRequest);

			if (StringUtils.isNotBlank(token)) {
				// Handle Response
				try {
					getPayPalProcessor().addTokenToPayPalPG(getOrder(), token);
				} catch (final CommerceException ce) {
					vlogError("CheckoutWithPayPalFormHandler.handleCheckoutWithPayPal:"
							+ "CommerceException while updating the PayPal payment group with the token. {0}", ce);

					addFormException(new DropletFormException("Error adding pay pal token to payment group",ce.getMessage()));
					return true;
				}

				vlogDebug("Customer sent to PayPal");
				pResponse.sendRedirect(getPayPalProcessor().getHandoffURL() + "&token=" + token);
				vlogDebug("Customer Back from PayPal");
			} else {
				vlogError("Something bad happended, recieved token is null");
			}
		} finally {
			commitTransaction(tr);
		}
		// Return false to indicate we should terminate the Servlet chain.
		return false;
	}

	public PayPalProcessor getPayPalProcessor() {
		return payPalProcessor;
	}

	public void setPayPalProcessor(PayPalProcessor payPalProcessor) {
		this.payPalProcessor = payPalProcessor;
	}


}
