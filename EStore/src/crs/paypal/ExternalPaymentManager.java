package crs.paypal;

import java.util.HashMap;

import atg.nucleus.GenericService;

public class ExternalPaymentManager extends GenericService{

	//static paypalfunctions ppf = new paypalfunctions();
	
	public HashMap callShortcutExpressCheckout(String amount, String returnUrl, String cancelUrl) {
		return new paypalfunctions().CallShortcutExpressCheckout(amount, returnUrl, cancelUrl);
	}
	
	public HashMap callMarkExpressCheckout(String amount, String returnUrl, String cancelUrl) {
		return new paypalfunctions().CallMarkExpressCheckout(amount, returnUrl, cancelUrl, 
				null, null, null, null, null, 
				null, null, null);
	}
}