package crs.commerce.order.purchase;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.transaction.Transaction;

import atg.commerce.CommerceException;
import atg.core.util.StringUtils;
import atg.projects.store.mobile.order.purchase.MobileStoreCartFormHandler;
import atg.service.pipeline.RunProcessException;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import crs.paypal.PayPalProcessor;
import crs.paypal.beans.SetExpressCheckoutRequest;

public class CRSCartModifierFormHandler extends MobileStoreCartFormHandler {

	private PayPalProcessor payPalProcessor;
	private boolean callback = false;
	
	public boolean handleCheckoutWithPayPal(DynamoHttpServletRequest req,
			DynamoHttpServletResponse res) throws ServletException, IOException {
		vlogDebug("Inside CRSCartModifierFormHandler.handleCheckoutWithPayPal");

		Transaction tr = null;
		try {

			try {
				modifyOrderByCommerceId(req, res);
			} catch (CommerceException | RunProcessException e) {
				vlogError(e,
						"CRSCartModifierFormHandler.handleCheckoutWithPayPal {0}");
			}

			SetExpressCheckoutRequest expressCheckoutRequest = new SetExpressCheckoutRequest();
			expressCheckoutRequest.setOrder(getOrder());
			expressCheckoutRequest.setProfile(getProfile());
			expressCheckoutRequest.setReturnURL(getPayPalProcessor().getEcSuccessUrl());
			expressCheckoutRequest.setCancelURL(getPayPalProcessor().getEcCancelUrl());
			
			String currencyCode = null;
			currencyCode = getOrder().getPriceInfo().getCurrencyCode();
			if (StringUtils.isBlank(currencyCode)) {
				currencyCode = getPayPalProcessor().getDefaultCurrencyCode();
			}
			expressCheckoutRequest.setCurrencyCode(currencyCode);
			expressCheckoutRequest.setCallback(isCallback());

		} finally {
			if (tr != null) {
				commitTransaction(tr);
			}
		}

		return false;

	}

	public PayPalProcessor getPayPalProcessor() {
		return payPalProcessor;
	}

	public void setPayPalProcessor(PayPalProcessor payPalProcessor) {
		this.payPalProcessor = payPalProcessor;
	}

	public boolean isCallback() {
		return callback;
	}

	public void setCallback(boolean callback) {
		this.callback = callback;
	}
	
	
}
