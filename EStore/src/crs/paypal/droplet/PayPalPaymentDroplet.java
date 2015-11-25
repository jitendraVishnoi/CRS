package crs.paypal.droplet;

import java.io.IOException;

import javax.servlet.ServletException;

import atg.repository.RepositoryItem;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.DynamoServlet;
import atg.userprofiling.Profile;

public class PayPalPaymentDroplet extends DynamoServlet {

	private static final String PARAM_PROFILE = "profile";
	private static final String OPARAM_BILLING_AGREEMENT = "billing";
	private static final String OPARAM_EXPRESS_CHECKOUT = "express";
	private static final String OPARAM_ERROR = "error";

	public void service(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {
		vlogDebug("Inside PayPalPaymentDroplet.service");

		Profile profile = (Profile) pRequest.getObjectParameter(PARAM_PROFILE);
		if (profile == null) {
			pRequest.serviceParameter(OPARAM_ERROR, pRequest, pResponse);
		} else {
			/*RepositoryItem billingAgreement = (RepositoryItem) profile.getPropertyValue("defaultPaypalBillingAgreement");

			vlogDebug("billingAgreement:{0}", billingAgreement);
			if (billingAgreement != null) {
				pRequest.serviceParameter(OPARAM_BILLING_AGREEMENT, pRequest, pResponse);
			}
			else {*/
			pRequest.serviceParameter(OPARAM_EXPRESS_CHECKOUT, pRequest, pResponse);
		}
		vlogDebug("Exting PayPalPaymentDroplet.service");
	}

}
